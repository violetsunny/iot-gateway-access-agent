package cn.enncloud.iot.gateway;

import cn.enncloud.iot.gateway.config.connectors.ModbusServerConfig;
import cn.enncloud.iot.gateway.context.DeviceContext;
import cn.enncloud.iot.gateway.entity.Device;
import cn.enncloud.iot.gateway.entity.Product;
import cn.enncloud.iot.gateway.entity.cloud.ModbusPointMapping;
import cn.enncloud.iot.gateway.entity.tsl.ProductTsl;
import cn.enncloud.iot.gateway.message.Message;
import cn.enncloud.iot.gateway.message.Metric;
import cn.enncloud.iot.gateway.modbus.ModbusDeviceProtocol;
import cn.enncloud.iot.gateway.modbus.ModbusDeviceSessionManager;
import cn.enncloud.iot.gateway.modbus.UpModbusHandler;
import cn.enncloud.iot.gateway.modbus.constant.ByteOrderEnum;
import cn.enncloud.iot.gateway.modbus.constant.DataTypeEnum;
import cn.enncloud.iot.gateway.modbus.core.analyse.ByteAnalyse;
import cn.enncloud.iot.gateway.modbus.core.payloads.BaseModbusPayLoad;
import cn.enncloud.iot.gateway.modbus.core.payloads.ReadCoilStatusPayLoad;
import cn.enncloud.iot.gateway.modbus.core.payloads.ReadHoldingRegisterPayLoad;
import cn.enncloud.iot.gateway.modbus.core.payloads.ReadInputRegisterPayLoad;
import cn.enncloud.iot.gateway.modbus.core.requests.ModBusTcpRequest;
import cn.enncloud.iot.gateway.modbus.core.requests.ModbusRequest;
import cn.enncloud.iot.gateway.modbus.core.typed.ModbusFCode;
import cn.enncloud.iot.gateway.modbus.dto.DevicePointMapDTO;
import cn.enncloud.iot.gateway.modbus.dto.ModbusPointDTO;
import cn.enncloud.iot.gateway.modbus.poll.ModbusDataHandler;
import cn.enncloud.iot.gateway.modbus.poll.ModbusTcpClient;
import cn.enncloud.iot.gateway.modbus.poll.ModbusTcpClientConfig;
import cn.enncloud.iot.gateway.modbus.poll.tcp.ModBusTcpPoll;
import cn.enncloud.iot.gateway.modbus.poll.tcp.ModbusTcpConfig;
import cn.enncloud.iot.gateway.modbus.utils.IotByteUtils;
import cn.hutool.json.JSONUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import static cn.enncloud.iot.gateway.modbus.ModbusConnectorStater.buildPointMap;

@Slf4j
public class Main {
    static ModBusTcpPoll poll;

    public static void main(String[] args) {

//
//        buildModbusConfig();
//        run();

        ModbusServerConfig server =new ModbusServerConfig();
        ModbusTcpClientConfig modbusTcpClientConfig = new ModbusTcpClientConfig();
        modbusTcpClientConfig.setConfigId("test");
        modbusTcpClientConfig.setHost("127.0.0.1");
        modbusTcpClientConfig.setPort(9999);
        modbusTcpClientConfig.setReadDiff(Optional.ofNullable(server.getReadDiff()).orElse(5));
        modbusTcpClientConfig.setMaxRetries(Optional.ofNullable(server.getMaxRetries()).orElse(100));
        modbusTcpClientConfig.setRetryDelay(Optional.ofNullable(server.getRetryDelay()).orElse(60));
        modbusTcpClientConfig.setConnectTimeout(Optional.ofNullable(server.getConnectTimeout()).orElse(5000));

        // 点表路径
//        String pointDTOSPath = server.getPointDTOSPath();

            List<ModbusPointMapping> modbusPoints = new ArrayList<>();
        modbusPoints.add(new ModbusPointMapping(1L, 101, ModbusFCode.READ_HOLDING_REGISTER, 100, DataTypeEnum.getInstance("int32").getValue(), ByteOrderEnum.getInstance("1234").getValue()));
        modbusPoints.add(new ModbusPointMapping(2L, 1, ModbusFCode.READ_INPUT_REGISTER, 103, DataTypeEnum.getInstance("int32").getValue(), ByteOrderEnum.getInstance("1234").getValue()));
        modbusPoints.add(new ModbusPointMapping(3L, 1, ModbusFCode.READ_COIL_STATUS, 114, DataTypeEnum.getInstance("bit0").getValue(), ByteOrderEnum.getInstance("1234").getValue()));

        modbusTcpClientConfig.setPointDTOS(modbusPoints);

        // 点位设备映射信息路径
        String pointMapDTOSPath = server.getPointMapDTOSPath();
//            modbusTcpClientConfig.setPointMapDTOS();

            DevicePointMapDTO devicePointMapDTO = new DevicePointMapDTO();
            devicePointMapDTO.setDeviceId("11233");
            devicePointMapDTO.setMetric("power");
            devicePointMapDTO.setPointId(1L);
            devicePointMapDTO.setProductCode("12333");
            devicePointMapDTO.setRw("r");
            List<DevicePointMapDTO> devicePointMapDTOS = Collections.singletonList(devicePointMapDTO);


        HashMap<Long, DevicePointMapDTO> pointMap = buildPointMap(devicePointMapDTOS);

        ModbusDeviceProtocol protocol = new ModbusDeviceProtocol();
        protocol.setParams(pointMap);
        protocol.setDeviceContext(new DeviceContext() {
            @Override
            public ProductTsl getTslByDeviceId(String deviceId) {
                return null;
            }

            @Override
            public ProductTsl getTslByProductId(String productId) {
                return null;
            }

            @Override
            public ProductTsl getTslByCode(String tslCode) {
                return null;
            }

            @Override
            public String getDeviceIdBySn(String projectId, String sn) {
                return null;
            }

            @Override
            public boolean authDevice(String deviceId, String password) {
                return false;
            }

            @Override
            public Metric getLastDevcieMetric(String deviceId, String metric) {
                return null;
            }

            @Override
            public void storeMessage(Message message) {

            }

            @Override
            public List<Device> getSnByProductId(String productId) {
                return null;
            }

            @Override
            public List<String> getImeiByProductId(String productId) {
                return null;
            }

            @Override
            public List<String> getDeviceIdByProductId(String productId) {
                return null;
            }

            @Override
            public Product getProductByDeviceId(String deviceId) {
                return null;
            }

            @Override
            public String getSnByDeviceId(String deviceId) {
                return null;
            }

            @Override
            public String getDeviceIdByImei(String imei) {
                return null;
            }

            @Override
            public void updateImei(List<Device> sns) {

            }

            @Override
            public String registerDevice(Device sn) {
                return null;
            }

            @Override
            public String getProductProtocolBySn(String sn) {
                return null;
            }

            @Override
            public Map<String, String> modelRef(String pCode, String productId, Object modelRefObj) {
                return null;
            }

            @Override
            public String modelRefMetric(String orgMetric, Map<String, String> modelRef) {
                return null;
            }

            @Override
            public void putDeviceProtocol(String device, String protocol) {

            }

            @Override
            public boolean validSnBelongProduct(String sn, String productId) {
                return false;
            }

            @Override
            public List<ModbusPointMapping> getModbusPoint(String gatewayCode) {
                return Collections.emptyList();
            }
        });

//        UpModbusHandler upModbusHandler = new UpModbusHandler(protocol, server, new ModbusDeviceSessionManager());
        ModbusTcpClient modbusTcpClient = new ModbusTcpClient(new ModbusDataHandler(), modbusTcpClientConfig, protocol, server);
        modbusTcpClient.run();
    }

    static void send(ModbusRequest request) throws ExecutionException, InterruptedException {
        poll.send(request);
    }

    static ConcurrentHashMap<Integer, String> REQUEST_CACHE = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, ModbusPointDTO> POINT_CACHE = new ConcurrentHashMap<>();

    @SneakyThrows
    public static void run() {
        ModbusTcpConfig config = new ModbusTcpConfig.Builder("127.0.0.1", 9997).build();
        poll = new ModBusTcpPoll(config, (resp) -> {

            int flag = resp.flag();
            String pointKey = REQUEST_CACHE.remove(flag);
            ModbusPointDTO modbusPointDTO = POINT_CACHE.get(pointKey);


            byte[] bts = (byte[]) resp.data();
            if (resp.code() == 1) {
                System.out.println("1:" + Arrays.toString(ByteAnalyse.coil_analyse(bts)));
                Number booleanValue = ModbusDataHandler.getBooleanValue(modbusPointDTO, bts);
                System.out.println(booleanValue);


            } else if (resp.code() == 2) {
                System.out.println("2:" + Arrays.toString(ByteAnalyse.input_analyse(bts)));
            } else if (resp.code() == 3) {
                ByteBuf buffer = Unpooled.buffer();
                buffer.writeBytes(bts);

                ByteOrderEnum byteOrder = ByteOrderEnum.getInstance(modbusPointDTO.getByteOrder());
                DataTypeEnum dataType = DataTypeEnum.getInstance(modbusPointDTO.getDataType());
                Number value = IotByteUtils.getNumberFromRegisterRegion(buffer, 0,
                        byteOrder, dataType);

                System.out.println("3-value:" + value);
                System.out.println("3:" + Arrays.toString(ByteAnalyse.holding_analyse(bts)));
            } else if (resp.code() == 4) {
                ByteBuf buffer = Unpooled.buffer();
                buffer.writeBytes(bts);
                ByteOrderEnum byteOrder = ByteOrderEnum.getInstance(modbusPointDTO.getByteOrder());
                DataTypeEnum dataType = DataTypeEnum.getInstance(modbusPointDTO.getDataType());
                Number value = IotByteUtils.getNumberFromRegisterRegion(buffer, 0,
                        byteOrder, dataType);

                System.out.println("4-value:" + value);
                System.out.println("4:" + Arrays.toString(ByteAnalyse.input_register_analyse(bts)));
            } else {
                System.out.println(resp);
            }
        }, (ch) -> {
            System.out.println("已断开连接");
        }, (channelFuture) -> {
        });


        poll.connect();


//       try{
//           // 写入单个线圈 三种方式
//           //send(new ModBusTcpRequest(new WriteSingleCoilPayLoad(0,true)));
//           //send(new ModBusTcpRequest(new WriteSingleCoilPayLoad(0,1)));
//           //send(new ModBusTcpRequest(new WriteSingleCoilPayLoad(0,(short) 1)));
//           //=============================================================================
//           // 写入多个线圈
//           //send(new ModBusTcpRequest(new WriteMultipleCoilPayLoad(0,new BinaryValue(1,1,1,1,0,0,1,1,1,1,1))));
//           //send(new ModBusTcpRequest(new WriteMultipleCoilPayLoad(0,new BinaryValue((short) 1,(short)1,(short)1,(short)1,(short)0,(short)0,(short)0,(short)1))));
//           //=============================================================================
//           // 写入单个寄存器
//           //send(new ModBusTcpRequest(new WriteSingleRegisterPayLoad(1,(short) 199)));
//           //=============================================================================
//           // 写入多个寄存器
//           //send(new ModBusTcpRequest(new WriteMultipleRegisterPayLoad(0,new MultipleValue((short) 199, (short) 299, (short) 399, (short) 499, (short) 599, (short) 699, (short) 799, (short) 899))));
//
//       }catch (InterruptedException | ExecutionException exception){
//           exception.printStackTrace();
//       }
        Thread.sleep(5000);

        List<ModbusPointDTO> pointDTOS = new ArrayList<>();
        pointDTOS.add(new ModbusPointDTO(1L, 101, ModbusFCode.READ_HOLDING_REGISTER, 100, DataTypeEnum.getInstance("int32").getValue(), ByteOrderEnum.getInstance("1234").getValue()));
        pointDTOS.add(new ModbusPointDTO(2L, 1, ModbusFCode.READ_INPUT_REGISTER, 103, DataTypeEnum.getInstance("int32").getValue(), ByteOrderEnum.getInstance("1234").getValue()));
        pointDTOS.add(new ModbusPointDTO(3L, 1, ModbusFCode.READ_COIL_STATUS, 114, DataTypeEnum.getInstance("bit0").getValue(), ByteOrderEnum.getInstance("1234").getValue()));

        pointDTOS.forEach(pointDTO -> POINT_CACHE.put(pointDTO.getPointId().toString(), pointDTO));

        new Thread(() -> {
            while (true) {
                try {
//                   poll.send(new ModBusTcpRequest((short) 1,new ReadCoilStatusPayLoad(100,10)));
//                   poll.send(new ModBusTcpRequest((short) 1,new ReadInputStatusPayLoad(0,10)));
//                    poll.send(new ModBusTcpRequest((short) 1, new ReadHoldingRegisterPayLoad(100, 2)));
//                    poll.send(new ModBusTcpRequest((short) 1, new ReadInputRegisterPayLoad(103, 2)));

                    POINT_CACHE.entrySet().forEach(point -> {

                        try {
                            Integer send = poll.send(buildReq(point.getValue()));
                            if (Objects.nonNull(send)) {
                                REQUEST_CACHE.put(send, point.getKey());
                            }
                        } catch (ExecutionException e) {
                            throw new RuntimeException(e);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    Thread.sleep(1000);
//                } catch (ExecutionException e) {
//                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();


    }

    private static ModbusRequest buildReq(ModbusPointDTO pointDTOS) {

        BaseModbusPayLoad baseModbusPayLoad = null;
        if (pointDTOS.getFunctionCode().equals(ModbusFCode.READ_HOLDING_REGISTER)) {
            baseModbusPayLoad = new ReadHoldingRegisterPayLoad(pointDTOS.getRegisterAddress(), transAmount(pointDTOS.getDataType()));
        } else if (pointDTOS.getFunctionCode().equals(ModbusFCode.READ_INPUT_REGISTER)) {
            baseModbusPayLoad = new ReadInputRegisterPayLoad(pointDTOS.getRegisterAddress(), transAmount(pointDTOS.getDataType()));
        }else if(pointDTOS.getFunctionCode().equals(ModbusFCode.READ_COIL_STATUS)){
            baseModbusPayLoad =  new ReadCoilStatusPayLoad(pointDTOS.getRegisterAddress(), 1);
        }
        ModBusTcpRequest modBusTcpRequest = new ModBusTcpRequest(pointDTOS.getSlaveAddr().shortValue(), baseModbusPayLoad);
        return modBusTcpRequest;
    }

    private static int transAmount(String dataType) {

        return 2;
    }

    public static void buildModbusConfig() {
//        String pointDTOSPath = server.getPointDTOSPath();

//        String url = "http://10.39.68.191:9002/protocol-test/protocol-energy-mqtt.js?Content-Disposition=attachment%3B%20filename%3D%22protocol-energy-mqtt.js%22&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20240407%2F%2Fs3%2Faws4_request&X-Amz-Date=20240407T060311Z&X-Amz-Expires=432000&X-Amz-SignedHeaders=host&X-Amz-Signature=6e3c0651a98a24cd14e7f6dab6839000d2e993fb120660f0244dc0f193f3c508";

//        String s = HttpUtil.get(url);
//        System.out.println(s);

        List<ModbusPointDTO> pointDTOS = new ArrayList<>();
        pointDTOS.add(new ModbusPointDTO(1L, 1, ModbusFCode.READ_HOLDING_REGISTER, 100, DataTypeEnum.getInstance("int32").getValue(), ByteOrderEnum.getInstance("1234").getValue()));
        pointDTOS.add(new ModbusPointDTO(1L, 1, ModbusFCode.READ_INPUT_REGISTER, 103, DataTypeEnum.getInstance("int32").getValue(), ByteOrderEnum.getInstance("1234").getValue()));
        pointDTOS.add(new ModbusPointDTO(2L, 1, ModbusFCode.READ_COIL_STATUS, 114, DataTypeEnum.getInstance("bit1").getValue(), ByteOrderEnum.getInstance("1234").getValue()));


        // 点位设备映射信息路径
//        String pointMapDTOSPath = server.getPointMapDTOSPath();
        DevicePointMapDTO devicePointMapDTO = new DevicePointMapDTO();
        devicePointMapDTO.setDeviceId("11233");
        devicePointMapDTO.setMetric("power");
        devicePointMapDTO.setPointId(1L);
        devicePointMapDTO.setProductCode("12333");
        devicePointMapDTO.setRw("r");
        List<DevicePointMapDTO> devicePointMapDTOS = Collections.singletonList(devicePointMapDTO);

        System.out.println(JSONUtil.toJsonPrettyStr(pointDTOS));
        System.out.println(JSONUtil.toJsonStr(devicePointMapDTOS));
    }
}