package cn.enncloud.iot.gateway.modbus.poll;

import cn.enncloud.iot.gateway.config.connectors.ModbusServerConfig;
import cn.enncloud.iot.gateway.entity.cloud.ModbusPointMapping;
import cn.enncloud.iot.gateway.exception.DecodeMessageException;
import cn.enncloud.iot.gateway.exception.EncodeMessageException;
import cn.enncloud.iot.gateway.message.Message;
import cn.enncloud.iot.gateway.message.OperationRequest;
import cn.enncloud.iot.gateway.modbus.core.requests.ModBusTcpRequest;
import cn.enncloud.iot.gateway.modbus.core.requests.ModbusRequest;
import cn.enncloud.iot.gateway.modbus.poll.tcp.ModBusTcpPoll;
import cn.enncloud.iot.gateway.modbus.poll.tcp.ModbusTcpConfig;
import cn.enncloud.iot.gateway.protocol.Protocol;
import cn.enncloud.iot.gateway.utils.JsonUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ModbusTcpClient {


    private ModbusTcpClientConfig modbusTcpClientConfig;
    private ModbusDataHandler modbusDataHandler;
    private Protocol protocol;
    private ModbusServerConfig modbusServerConfig;


    public ModbusTcpClient(ModbusDataHandler modbusDataHandler, ModbusTcpClientConfig modbusTcpClientConfig, Protocol protocol, ModbusServerConfig modbusServerConfig) {
        this.modbusDataHandler = modbusDataHandler;
        this.modbusTcpClientConfig = modbusTcpClientConfig;
        this.protocol = protocol;
        this.modbusServerConfig = modbusServerConfig;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    /**
     * 拉取 modbus client
     */
    ModBusTcpPoll poll;

    /**
     * 请求事务缓存
     */
    ConcurrentHashMap<Integer, String> REQUEST_CACHE = new ConcurrentHashMap<>();

    /**
     * 点表缓存
     */
    ConcurrentHashMap<String, ModbusPointMapping> POINT_CACHE = new ConcurrentHashMap<>();


    public void send(OperationRequest operationRequest) {
        byte[] encode = null;
        try {
            encode = protocol.encodeMulti(Collections.singletonList(operationRequest),POINT_CACHE);
        } catch (EncodeMessageException e) {
            throw new RuntimeException(e);
        }
        String s = JsonUtil.JsonBytes2Json(encode);
        List<ModBusTcpRequest> modbusRequests = JSONUtil.toList(s, ModBusTcpRequest.class);
//        modbusPointWriteDTOS.forEach(request -> {
//            ModbusPointDTO modbusPointDTO = POINT_CACHE.get(request.getPointId());
//            if (Objects.isNull(modbusPointDTO)) {
//                log.warn("{} 下发点位写指令失败，点位不存在，info {}",modbusTcpClientConfig.getConfigId(), request);
//                return;
//            }
//            ModbusRequest modbusRequest = modbusDataHandler.buildWriteReq(request, modbusPointDTO);
//
//        });
        modbusRequests.forEach(request -> {
            try {
                poll.send(request);
            } catch (ExecutionException e) {
                log.warn("{} modbus request execute error",modbusTcpClientConfig.getConfigId(), e);
            } catch (InterruptedException e) {
                log.warn("{} modbus request interrupt error",modbusTcpClientConfig.getConfigId(), e);
            }
        });


    }

    public String getStatus() {
        return poll.getStatus();
    }


    public void run() {
        ModbusTcpConfig config = new ModbusTcpConfig.Builder(modbusTcpClientConfig.getConfigId(),modbusTcpClientConfig.getHost(), modbusTcpClientConfig.getPort(), modbusTcpClientConfig.getMaxRetries(), modbusTcpClientConfig.getRetryDelay(), modbusTcpClientConfig.getConnectTimeout()).build();
        poll = new ModBusTcpPoll(config, (resp) -> {
            int flag = resp.flag();
            String pointKey = REQUEST_CACHE.remove(flag);
            if (StringUtils.isNotBlank(pointKey)) {
                ModbusPointMapping modbusPointDTO = POINT_CACHE.get(pointKey);
//                Number number = modbusDataHandler.respHandler(resp, modbusPointDTO);
//                upModbusHandler.handler(resp, modbusPointDTO);
                // modbus数据解析为设备物模型数据
                List<? extends Message> messages = null;
                try {
                    messages = protocol.decodeMulti(JSONObject.toJSONBytes(resp), modbusPointDTO);
                    log.info("{} 数据上报，{}", modbusServerConfig.getClientId(),JSONObject.toJSONString(messages));
                } catch (DecodeMessageException e) {
                    log.warn("{} 设备数据decode异常，error",modbusServerConfig.getClientId(), e);
                }
                if (CollectionUtils.isEmpty(messages)) {
                    return;
                }
                // 数据存储
                messages.forEach(message -> protocol.getDeviceContext().storeMessage(message));
            }else {
                log.warn("{} modbus数据事务号解析异常，info：{}",modbusTcpClientConfig.getConfigId(), resp);
            }
        }, this::closeListener, this::readTask);

        poll.connect();

        List<ModbusPointMapping> pointDTOS = protocol.getDeviceContext().getModbusPoint(modbusTcpClientConfig.getGatewayCode());

        if (CollectionUtils.isNotEmpty(pointDTOS)) {
            pointDTOS.forEach(pointDTO -> POINT_CACHE.put(pointDTO.getPointId().toString(), pointDTO));
            modbusTcpClientConfig.setPointDTOS(pointDTOS);
        }

    }


    private void readTask(ChannelFuture channelFuture) {

        ScheduledExecutorService executorService = poll.getExecutorService();

        executorService.scheduleWithFixedDelay(() -> {

            if (channelFuture.channel().isOpen()) {
                log.info("{} {} modbus client read task execute",modbusTcpClientConfig.getConfigId(),channelFuture.channel().remoteAddress());
                POINT_CACHE.forEach((key, value) -> {
                    try {
                        Integer send = poll.send(modbusDataHandler.buildReadReq(value));
                        if (Objects.nonNull(send)) {
                            REQUEST_CACHE.put(send, key);
                        }
                    } catch (ExecutionException | InterruptedException e) {
                        log.warn("{} {} read task exception",modbusTcpClientConfig.getConfigId(),channelFuture.channel().remoteAddress(), e);
                    }

                });
            }
        }, 2, modbusTcpClientConfig.getReadDiff(), TimeUnit.SECONDS);
    }


    private void closeListener(ChannelFuture context) {
        log.info("{} 已断开连接,remoteAddr{}-localAddr:{}",modbusTcpClientConfig.getConfigId(), context.channel().remoteAddress(), context.channel().localAddress());
    }

    public void closeModbusClient() {
        try {
            poll.shutdownClient();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


}