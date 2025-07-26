package simulate.codec;

import cn.enncloud.iot.gateway.message.LoginRequest;
import cn.enncloud.iot.gateway.message.Message;
import cn.enncloud.iot.gateway.message.Metric;
import cn.enncloud.iot.gateway.message.MetricReportRequest;
import cn.enncloud.iot.gateway.message.enums.MessageType;
import cn.enncloud.iot.gateway.utils.CommonUtils;
import cn.enncloud.iot.gateway.utils.JsonUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hanyilong@enn.cn
 * @since 2021-02-13 23:08:07
 */
@Slf4j
@ChannelHandler.Sharable
public class SimulateHandler extends ChannelInboundHandlerAdapter {

    String deviceId;
    public SimulateHandler(String deviceId){
        this.deviceId = deviceId;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        LoginRequest req = new LoginRequest();
        req.setDeviceId(deviceId);
        req.setMessageId(CommonUtils.getUUID());
        req.setUsername("test");
        req.setPassword("123456");
        ctx.writeAndFlush(JsonUtil.object2JsonBytes(req));

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        log.error("发生异常", cause);
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof Message)) {
            super.channelRead(ctx, msg);
            return;
        }

        Message message = (Message) msg;
        MessageType type = message.getMessageType();
        if (type == MessageType.DEVICE_LOGIN_RSP) {
            new Thread(() -> {
                while (true) {
                    try {
                        MetricReportRequest req = new MetricReportRequest();
                        req.setDeviceId("1001");
                        req.setMessageId(CommonUtils.getUUID());
                        req.setTimeStamp(System.currentTimeMillis());
                        List<Metric> metrics = new ArrayList<>();
                        metrics.add(new Metric(12222211, "test", "aa"));
                        req.setMetrics(metrics);
                        ctx.writeAndFlush(JsonUtil.object2JsonBytes(req));
                        log.info("发送数据上报请求：{}", req);
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
