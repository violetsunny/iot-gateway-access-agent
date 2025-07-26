package cn.enncloud.iot.gateway.udp;

import cn.enncloud.iot.gateway.context.DeviceContext;
import cn.enncloud.iot.gateway.message.Message;
import cn.enncloud.iot.gateway.message.enums.MessageType;
import cn.enncloud.iot.gateway.protocol.Protocol;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

import java.util.List;


public class MessageHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    DeviceContext deviceContext;
    Protocol protocol;

    public MessageHandler(DeviceContext deviceContext, Protocol protocol) {
        this.deviceContext = deviceContext;
        this.protocol = protocol;
    }


    public void exceptionCaught(ChannelHandlerContext context,Throwable cause){
        context.close();
        cause.printStackTrace();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) throws Exception {
        List<? extends Message> messages = protocol.decodeMulti(ByteBufUtil.getBytes(datagramPacket.content()));
        if(messages == null || messages.isEmpty()){
            return;
        }
        messages.forEach(m -> {
            MessageType messageType = m.getMessageType();
            switch (messageType) {
                case DEVICE_EVENT_REQ:
                case DEVICE_INFO_REQ:
                case DEVICE_REPORT_REQ:
                    deviceContext.storeMessage(m);
                    break;
                case HEARTBEAT:
                    break;
                case DEVICE_LOGIN_REQ:
                    break;
                case DEVICE_LOGIN_RSP:
                    break;
                case DEVICE_REPORT_RSP:
                    break;
                case DEVICE_STATUS_REQ:
                    break;
                case DEVICE_NTP_REQ:
                    break;
                case DEVICE_NTP_RSP:
                    break;
                case CLOUD_OPERATION_REQ:
                    break;
                case CLOUD_OPERATION_RSP:
                    break;
                case CLOUD_HISTORY_REQ:
                    break;
                case CLOUD_NTP_REQ:
                    break;
                case CLOUD_NTP_RSP:
                    break;
            }
        });
    }
}
