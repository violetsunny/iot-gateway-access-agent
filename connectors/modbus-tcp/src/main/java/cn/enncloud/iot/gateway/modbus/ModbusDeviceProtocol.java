package cn.enncloud.iot.gateway.modbus;

import cn.enncloud.iot.gateway.entity.cloud.ModbusPointMapping;
import cn.enncloud.iot.gateway.exception.AuthException;
import cn.enncloud.iot.gateway.exception.DecodeMessageException;
import cn.enncloud.iot.gateway.exception.EncodeMessageException;
import cn.enncloud.iot.gateway.message.*;
import cn.enncloud.iot.gateway.modbus.constant.ByteOrderEnum;
import cn.enncloud.iot.gateway.modbus.constant.DataTypeEnum;
import cn.enncloud.iot.gateway.modbus.core.payloads.*;
import cn.enncloud.iot.gateway.modbus.core.requests.ModBusTcpRequest;
import cn.enncloud.iot.gateway.modbus.core.requests.ModbusRequest;
import cn.enncloud.iot.gateway.modbus.core.responses.ModbusResponse;
import cn.enncloud.iot.gateway.modbus.core.typed.ModbusFCode;
import cn.enncloud.iot.gateway.modbus.core.value.BinaryValue;
import cn.enncloud.iot.gateway.modbus.core.value.MultipleValue;
import cn.enncloud.iot.gateway.modbus.dto.DevicePointMapDTO;
import cn.enncloud.iot.gateway.modbus.dto.ModbusPointDTO;
import cn.enncloud.iot.gateway.modbus.dto.ModbusPointWriteDTO;
import cn.enncloud.iot.gateway.modbus.utils.IotByteUtils;
import cn.enncloud.iot.gateway.protocol.AbstractProtocol;
import cn.enncloud.iot.gateway.utils.JsonUtil;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ModbusDeviceProtocol extends AbstractProtocol {


    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getName() {
        return "modbus-protocol-map";
    }

    private HashMap<String, Integer> devicePointToModbusMap;


    public void setDevicePointToModbusMap(HashMap<String, Integer> devicePointToModbusMap) {
        this.devicePointToModbusMap = devicePointToModbusMap;
    }

    @Override
    public List<? extends Message> decodeMulti(byte[] messageBytes, Object... params) throws DecodeMessageException {
        ModbusResponse value = JSONObject.parseObject(messageBytes, ModbusResponse.class);
        ModbusPointMapping modbusPointDTO = (ModbusPointMapping) params[0];
        // 获取参数值
        Number number = respHandler(value, modbusPointDTO);

        long timeMillis = System.currentTimeMillis();
        MetricReportRequest metricReportRequest = new MetricReportRequest();
        metricReportRequest.setTimeStamp(timeMillis);
        metricReportRequest.setDeviceId(modbusPointDTO.getDeviceId());

        String dataType = modbusPointDTO.getDataType();

        Metric metric = new Metric(timeMillis, modbusPointDTO.getMetric(), number);
        metricReportRequest.setMetrics(Collections.singletonList(metric));

        return Collections.singletonList(metricReportRequest);
    }


    @Override
    public byte[] encode(Message message, Object... params) throws EncodeMessageException {

        return new byte[0];
    }

    @Override
    public byte[] encodeMulti(List<? extends Message> messages, Object... params) throws EncodeMessageException {
        //TODO: 根据测点映射转化modbus写指令
        ConcurrentHashMap<String, ModbusPointDTO> POINT_CACHE = (ConcurrentHashMap<String, ModbusPointDTO>) params[0];
        if (Objects.isNull(devicePointToModbusMap)) {
            initModbusMappingInfo();
        }

        List<ModbusRequest> cmds = new ArrayList<>();
        messages.forEach(message -> {

            OperationRequest cmd = (OperationRequest) message;
            String deviceId = cmd.getDeviceId();
            Map<String, Object> param = cmd.getParam();
            param.entrySet().forEach(metric -> {

                String key = metric.getKey();
                Integer pointId = devicePointToModbusMap.get(deviceId + key);
                Object value = metric.getValue();
                ModbusPointWriteDTO modbusPointWriteDTO = new ModbusPointWriteDTO(pointId, value);
                ModbusPointDTO modbusPointDTO = POINT_CACHE.get(modbusPointWriteDTO.getPointId());
                ModbusRequest modbusRequest = buildWriteReq(modbusPointWriteDTO, modbusPointDTO);
                cmds.add(modbusRequest);
            });
        });


        return JsonUtil.object2JsonBytes(cmds);
    }

//    private synchronized void initMappingInfo() {
//        Map params1 = getParams();
//        setDevicePointMap((HashMap<Integer, DevicePointMapDTO>) params1);
//    }

    private synchronized void initModbusMappingInfo() {
        HashMap<Integer, DevicePointMapDTO> params1 = (HashMap<Integer, DevicePointMapDTO>) getParams();

        HashMap<String, Integer> modbusPointMap = new HashMap<>();
        params1.forEach((pointId, value) -> {

            String key = value.getDeviceId() + value.getMetric();

            if (StringUtils.equals("w", value.getRw())) {
                modbusPointMap.put(key, pointId);
            }
        });

        setDevicePointToModbusMap(modbusPointMap);
    }

    @Override
    public boolean login(LoginRequest loginRequest) throws AuthException {
        return false;
    }

    public Number respHandler(ModbusResponse resp, ModbusPointMapping modbusPointDTO) {
        byte[] bts = (byte[]) resp.data();
        Number value = null;
        log.info("功能码：{}，接收数据：{}", modbusPointDTO.getFunctionCode(), resp);
        if (resp.code() == 1) {
            value = getBooleanValue(modbusPointDTO, bts);
        } else if (resp.code() == 2) {
            value = getBooleanValue(modbusPointDTO, bts);
        } else if (resp.code() == 3) {
            value = getValue(modbusPointDTO, bts);

        } else if (resp.code() == 4) {
            value = getValue(modbusPointDTO, bts);
        } else {
            log.warn("功能码暂不支持，code：{}，info：{}", resp.code(), resp.toString());
        }

        if (Objects.nonNull(value)) {
            log.info("功能码：{}，解析数据：{}", modbusPointDTO.getFunctionCode(), value.longValue());

            return value;
        }
        return null;
    }
    public static Number getBooleanValue(ModbusPointMapping modbusPointDTO, byte[] bts) {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeBytes(bts);

        DataTypeEnum dataType = DataTypeEnum.getInstance(modbusPointDTO.getDataType());

        String name = dataType.getValue();
        Integer bit = Integer.valueOf(name.replace("bit", ""));
        Number value = IotByteUtils.getBooleanFromStatusRegion(buffer, 0, bit);
        return value;
    }

    private static Number getValue(ModbusPointMapping modbusPointDTO, byte[] bts) {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeBytes(bts);

        ByteOrderEnum byteOrder = ByteOrderEnum.getInstance(modbusPointDTO.getByteOrder());
        DataTypeEnum dataType = DataTypeEnum.getInstance(modbusPointDTO.getDataType());
        Number value = IotByteUtils.getNumberFromRegisterRegion(buffer, 0,
                byteOrder, dataType);
        return value;
    }


    /**
     * 构建
     *
     * @param pointDTOS
     * @return
     */
    public ModbusRequest buildReadReq(ModbusPointDTO pointDTOS) {

        BaseModbusPayLoad baseModbusPayLoad = null;
        Integer amount = transAmount(pointDTOS.getDataType());
        if (Objects.isNull(amount)) {
            return null;
        }
        if (pointDTOS.getFunctionCode().equals(ModbusFCode.READ_HOLDING_REGISTER)) {
            baseModbusPayLoad = new ReadHoldingRegisterPayLoad(pointDTOS.getRegisterAddress(), amount);
        } else if (pointDTOS.getFunctionCode().equals(ModbusFCode.READ_INPUT_REGISTER)) {
            baseModbusPayLoad = new ReadInputRegisterPayLoad(pointDTOS.getRegisterAddress(), amount);
        } else if (pointDTOS.getFunctionCode().equals(ModbusFCode.READ_COIL_STATUS)) {
            baseModbusPayLoad = new ReadCoilStatusPayLoad(pointDTOS.getRegisterAddress(), amount);
        } else if (pointDTOS.getFunctionCode().equals(ModbusFCode.READ_INPUT_STATUS)) {
            baseModbusPayLoad = new ReadInputStatusPayLoad(pointDTOS.getRegisterAddress(), amount);
        }
        ModBusTcpRequest modBusTcpRequest = new ModBusTcpRequest(pointDTOS.getSlaveAddr().shortValue(), baseModbusPayLoad);
        return modBusTcpRequest;


    }

    public static void main(String[] args) {
        Boolean a = true;
        System.out.println();
    }

    public ModbusRequest buildWriteReq(ModbusPointWriteDTO modbusPointWriteDTO, ModbusPointDTO pointDTOS) {

        Object value = modbusPointWriteDTO.getValue();
        //TODO: 后续完善数据写入逻辑
        ByteBuf buffer = Unpooled.buffer();
        ByteBuf byteBuf = IotByteUtils.setNumberFromRegisterRegion(buffer, (Number) value, DataTypeEnum.getInstance(pointDTOS.getDataType()), ByteOrderEnum.getInstance(pointDTOS.getByteOrder()));

        BaseModbusPayLoad baseModbusPayLoad = null;
        if (pointDTOS.getFunctionCode().equals(ModbusFCode.WRITE_SINGLE_COIL)) {
            byte b = byteBuf.readByte();
            baseModbusPayLoad = new WriteSingleCoilPayLoad(0, true);
        } else if (pointDTOS.getFunctionCode().equals(ModbusFCode.WRITE_SINGLE_REGISTER)) {
            baseModbusPayLoad = new WriteSingleRegisterPayLoad(1, (short) 199);
        } else if (pointDTOS.getFunctionCode().equals(ModbusFCode.WRITE_MULTIPLE_COIL)) {
            baseModbusPayLoad = new WriteMultipleCoilPayLoad(0, new BinaryValue(1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1));
        } else if (pointDTOS.getFunctionCode().equals(ModbusFCode.WRITE_MULTIPLE_REGISTER)) {
            baseModbusPayLoad = new WriteMultipleRegisterPayLoad(0, new MultipleValue((short) 199, (short) 299, (short) 399, (short) 499, (short) 599, (short) 699, (short) 799, (short) 899));
        }
        ModBusTcpRequest modBusTcpRequest = new ModBusTcpRequest(baseModbusPayLoad);
        return modBusTcpRequest;
//        //            写入单个线圈 三种方式
//        new ModBusTcpRequest(new WriteSingleCoilPayLoad(0, true));
//        new ModBusTcpRequest(new WriteSingleCoilPayLoad(0, 1));
//        new ModBusTcpRequest(new WriteSingleCoilPayLoad(0, (short) 1));
////           =============================================================================
////            写入多个线圈
//        new ModBusTcpRequest(new WriteMultipleCoilPayLoad(0, new BinaryValue(1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1)));
//        new ModBusTcpRequest(new WriteMultipleCoilPayLoad(0, new BinaryValue((short) 1, (short) 1, (short) 1, (short) 1, (short) 0, (short) 0, (short) 0, (short) 1)));
////           =============================================================================
////            写入单个寄存器
//        new ModBusTcpRequest(new WriteSingleRegisterPayLoad(1, (short) 199));
////           =============================================================================
////            写入多个寄存器
//        new ModBusTcpRequest(new WriteMultipleRegisterPayLoad(0, new MultipleValue((short) 199, (short) 299, (short) 399, (short) 499, (short) 599, (short) 699, (short) 799, (short) 899)));
    }

    /**
     * 数据类型转换byte数量
     *
     * @param dataType
     * @return
     */
    private Integer transAmount(String dataType) {

        DataTypeEnum instance = DataTypeEnum.getInstance(dataType);

        if (Objects.isNull(instance)) {
            log.warn("点表数据类型异常，{}", dataType);
            return null;
        }
        return instance.getByteNum();
    }
}
