package cn.enncloud.iot.gateway.bacnetip;

import cn.enncloud.iot.gateway.config.connectors.BacnetServerConfig;
import cn.enncloud.iot.gateway.exception.DecodeMessageException;
import cn.enncloud.iot.gateway.message.Message;
import cn.enncloud.iot.gateway.protocol.Protocol;
import cn.enncloud.iot.gateway.service.RedisService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkBuilder;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.PropertyReferences;
import com.serotonin.bacnet4j.util.PropertyValues;
import com.serotonin.bacnet4j.util.ReadListener;
import com.serotonin.bacnet4j.util.RequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import javax.annotation.PreDestroy;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BacnetIpTimerTask extends TimerTask {

    private final Protocol protocol;
    private final BacnetServerConfig bacnetServerConfig;
    private final RedisService redisService;
    private final IpNetwork ipNetwork;
    private final LocalDevice localDevice;
    private final Map<String, String> dataMap;

    //excel regtype mapping object_type
    private final Map<String, String> objectTypeMap = new HashMap<String, String>() {{
        put("BI(Binary Input)", "binary-input");
        put("BO(Binary Output)", "binary-output");
        put("BV(Binary Value)", "binary-value");
        put("AI(Analog Input)", "analog-input");
        put("AO(Analog Output)", "analog-output");
        put("AV(Analog Value)", "analog-value");
        put("MI(Multi-state Input)", "multi-state-input");
    }};

    public BacnetIpTimerTask(Protocol protocol, BacnetServerConfig bacnetServerConfig, RedisService redisService) throws Exception {
        this.protocol = protocol;
        this.bacnetServerConfig = bacnetServerConfig;
        this.redisService = redisService;
        this.ipNetwork = createIpNetwork();
        this.localDevice = createLocalDevice();
        this.dataMap = initDataMap(bacnetServerConfig.getTablePath());
    }

    private IpNetwork createIpNetwork() {
        return new IpNetworkBuilder()
                .withLocalBindAddress(bacnetServerConfig.getLocalBindAddress())
                .withSubnet(bacnetServerConfig.getSubnetAddress(), bacnetServerConfig.getNetworkPrefixLength())
                .withPort(bacnetServerConfig.getPort()).withReuseAddress(true).build();
    }

    private LocalDevice createLocalDevice() throws Exception {
        Transport transport = new DefaultTransport(ipNetwork);
        transport.setTimeout(bacnetServerConfig.getTimeout());
        return new LocalDevice(bacnetServerConfig.getDeviceNumber(), transport);
    }

    private void createBBMD() throws BACnetException {
        ipNetwork.enableBBMD();
        InetSocketAddress remoteAddress = new InetSocketAddress(bacnetServerConfig.getRemoteAddress(), bacnetServerConfig.getRemotePort());
        ipNetwork.registerAsForeignDevice(remoteAddress, 10000000);
    }

    @Override
    public void run() {
        String lockKey = "distributed:lock:" + bacnetServerConfig.getLocalBindAddress() + ":" + bacnetServerConfig.getPort()+":"+bacnetServerConfig.getBaId();
        long expire = 60;
        if (redisService.lock(lockKey, lockKey, expire)) {
            try {
                start();
            } finally {
                redisService.deleteKey(lockKey);
            }
        } else {
            log.warn("BacnetIpTimerTask {}:{}:{} 没有拿到锁", bacnetServerConfig.getLocalBindAddress(), bacnetServerConfig.getPort(),bacnetServerConfig.getBaId());
        }

    }

    private void start() {
        log.info("BacnetIpTimerTask 定时任务开始 running device:{} local={}:{} remote:{}",bacnetServerConfig.getBaId(), bacnetServerConfig.getLocalBindAddress(), bacnetServerConfig.getPort(),bacnetServerConfig.getRemoteAddress());
        try {
            // 初始化本地设备
            localDevice.initialize();
            // BBMD
            this.createBBMD();
            // 搜寻网段内远程设备
            localDevice.startRemoteDeviceDiscovery();

            // 等待一段时间以确保发现所有远程设备
            TimeUnit.SECONDS.sleep(10);

            // 获取所有发现的远程设备
            RemoteDevice remoteDevice = localDevice.getRemoteDeviceBlocking(bacnetServerConfig.getBaId());

            if(remoteDevice == null){
                log.warn("BacnetIpTimerTask remoteDevice is null device:{} local={}:{} remote:{}",bacnetServerConfig.getBaId(), bacnetServerConfig.getLocalBindAddress(), bacnetServerConfig.getPort(),bacnetServerConfig.getRemoteAddress());
                return;
            }

            searchAndGetData(remoteDevice);

            log.info("BacnetIpTimerTask 定时任务完成 device:{} local={}:{} remote:{}",bacnetServerConfig.getBaId(), bacnetServerConfig.getLocalBindAddress(), bacnetServerConfig.getPort(),bacnetServerConfig.getRemoteAddress());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 恢复中断状态
            log.error("BacnetIpTimerTask device:{} {}:{} interrupted: ",bacnetServerConfig.getBaId(), bacnetServerConfig.getLocalBindAddress(), bacnetServerConfig.getPort(), e);
        } catch (Exception e) {
            log.error("BacnetIpTimerTask device:{} local={}:{} remote:{} error: ",bacnetServerConfig.getBaId(), bacnetServerConfig.getLocalBindAddress(), bacnetServerConfig.getPort(),bacnetServerConfig.getRemoteAddress(), e);
        } finally {
            // 停止本地设备
            localDevice.terminate();
        }

    }

    public Object getStatus() {
        return localDevice.isInitialized();
    }

    /**
     * 搜索远程设备并获取数据
     */
    private void searchAndGetData(RemoteDevice remoteDevice) {
        try {
            getData(remoteDevice);
        } catch (Exception e) {
            log.error("BacnetIpTimerTask device:{} local={}:{} remote:{} error: ",bacnetServerConfig.getBaId(), bacnetServerConfig.getLocalBindAddress(), bacnetServerConfig.getPort(),bacnetServerConfig.getRemoteAddress(), e);
        }
    }

    private void getData(RemoteDevice remoteDevice) throws BACnetException, DecodeMessageException {
        //获取远程设备的标识符对象
        List<ObjectIdentifier> objectList = RequestUtils.getObjectList(localDevice, remoteDevice).getValues();
        List<ObjectIdentifier> aiList = new ArrayList<>();
        log.info("<===================deviceId:{} 对象标识符的对象类型，实例数(下标)===================>", remoteDevice.getInstanceNumber());
        //Object所有标识符
        for (ObjectIdentifier oi : objectList) {
            String key = oi.getObjectType() + " " + oi.getInstanceNumber();
            if (dataMap.containsKey(key)) {
                log.info("BacnetIpTimerTask deviceId:{} 过滤的标识符:{},{}", remoteDevice.getInstanceNumber(), oi.getObjectType().toString(), oi.getInstanceNumber());
                aiList.add(new ObjectIdentifier(oi.getObjectType(), oi.getInstanceNumber()));
            }
        }
        if (CollectionUtils.isEmpty(aiList)) {
            log.warn("BacnetIpTimerTask deviceId:{} 没有取值数据", remoteDevice.getInstanceNumber());
            return;
        }

        //根据对象属性标识符的类型进行取值操作 [测试工具模拟的设备点位的属性有objectName、description、present-value等等]
        //analog-input
        PropertyValues pvAiObjectName = readValueByPropertyIdentifier(localDevice, remoteDevice, aiList, null, PropertyIdentifier.objectName);
        PropertyValues pvAiPresentValue = readValueByPropertyIdentifier(localDevice, remoteDevice, aiList, null, PropertyIdentifier.presentValue);
        PropertyValues pvAiDescription = readValueByPropertyIdentifier(localDevice, remoteDevice, aiList, null, PropertyIdentifier.description);
        PropertyValues pvAiIdentifier = readValueByPropertyIdentifier(localDevice, remoteDevice, aiList, null, PropertyIdentifier.objectIdentifier);
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (ObjectIdentifier oi : aiList) {
            Map<String, Object> valueMap = new HashMap<>();
            valueMap.put("deviceId", remoteDevice.getInstanceNumber());
            valueMap.put("name", pvAiObjectName.getString(oi, PropertyIdentifier.objectName));
            valueMap.put("value", pvAiPresentValue.getString(oi, PropertyIdentifier.presentValue));
            valueMap.put("description", pvAiDescription.getString(oi, PropertyIdentifier.description));
            valueMap.put("object_identifier", pvAiIdentifier.getString(oi, PropertyIdentifier.objectIdentifier));
            valueMap.put("object_type", oi.getObjectType().toString());
            valueMap.put("object_address", oi.getInstanceNumber());
            dataList.add(valueMap);
        }
        log.info("BacnetIpTimerTask deviceId:{} 取值数据：{}", remoteDevice.getInstanceNumber(), JSONObject.toJSONString(dataList));

        List<? extends Message> messages = protocol.decodeMulti(JSON.toJSONBytes(dataList), dataMap);
        log.info("BacnetIpTimerTask deviceId:{} 解析后数据：{}", remoteDevice.getInstanceNumber(), JSON.toJSONString(messages));
        if (CollectionUtils.isNotEmpty(messages)) {
            messages.forEach(message -> protocol.getDeviceContext().storeMessage(message));
            log.info("BacnetIpTimerTask deviceId:{} 上报成功！！！", remoteDevice.getInstanceNumber());
        }
    }

    /**
     * 读取远程设备的属性值 根据属性标识符
     */
    public PropertyValues readValueByPropertyIdentifier(final LocalDevice localDevice, final RemoteDevice d, final List<ObjectIdentifier> ois, final ReadListener callback, PropertyIdentifier propertyIdentifier) throws BACnetException {
        if (ois.isEmpty()) {
            return new PropertyValues();
        }

        final PropertyReferences refs = new PropertyReferences();
        for (final ObjectIdentifier oid : ois) {
            refs.add(oid, propertyIdentifier);
        }

        return RequestUtils.readProperties(localDevice, d, refs, false, callback);
    }

    private synchronized Map<String, String> initDataMap(String excelFilePath) {
        Map<String, String> date = new HashMap<>();
        try (FileInputStream fis = getFileInputStream(excelFilePath);
             Workbook workbook = new HSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip the header row
            if (rowIterator.hasNext()) {
                rowIterator.next();
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Cell regTypeCell = row.getCell(2); // RegType
                Cell regAddressCell = row.getCell(3); // RegAddress
                Cell deviceIDCell = row.getCell(9); // DeviceID
                Cell measureCell = row.getCell(10); // measure

                if (regTypeCell != null && regAddressCell != null && deviceIDCell != null) {
                    String key = objectTypeMap.get(regTypeCell.toString()) + " " + regAddressCell.toString().replaceAll("\\.0$", "");
                    String value = deviceIDCell.toString().replaceAll("\\.0$", "") + "|" + measureCell.toString();
                    date.put(key, value);
                }
            }

            log.info("BacnetIpTimerTask deviceId:{} 点表数据：{}",bacnetServerConfig.getBaId(), JSONObject.toJSONString(date));
        } catch (IOException e) {
            log.error("BacnetIpTimerTask deviceId:{} error:",bacnetServerConfig.getBaId(), e);
            throw new RuntimeException("加载点表失败");
        }
        return date;
    }

    private FileInputStream getFileInputStream(String filePath) throws IOException {
        if (filePath.contains("://")) {
            return downloadFile(filePath);
        } else {
            return new FileInputStream(filePath);
        }
    }

    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 5000;

    private FileInputStream downloadFile(String fileUrl) throws IOException {
        HttpURLConnection urlConnection = null;
        File tempFile = null;
        try {
            URL url = new URL(fileUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
            urlConnection.setReadTimeout(READ_TIMEOUT);
            urlConnection.setRequestMethod("GET");

            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                tempFile = File.createTempFile("downloaded-", ".xlsx");
                try (InputStream is = urlConnection.getInputStream();
                     OutputStream os = Files.newOutputStream(tempFile.toPath())) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
                return new FileInputStream(tempFile);
            } else {
                throw new IOException("Failed to download file. HTTP response code: " + responseCode+",url:"+fileUrl);
            }
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (tempFile != null && tempFile.exists()) {
                tempFile.deleteOnExit();
            }
        }
    }

    @PreDestroy
    public void close() {
        if (localDevice != null) {
            localDevice.terminate();
        }
    }

}