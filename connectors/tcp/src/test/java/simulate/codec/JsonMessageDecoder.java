package simulate.codec;


import cn.enncloud.iot.gateway.message.*;
import cn.enncloud.iot.gateway.message.enums.MessageType;
import cn.enncloud.iot.gateway.utils.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


/**
 * @author hanyilong@enn.cn
 * @description: 消息解码
 * @date 2018-12-27 14:39
 *
 */
@Slf4j
public class JsonMessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        byte[] readDatas = new byte[msg.readableBytes()];
        msg.readBytes(readDatas);
        Message message = JsonUtil.jsonBytes2Object(readDatas,Message.class);
        if(message.getMessageType() == MessageType.DEVICE_LOGIN_RSP){
            message = JsonUtil.jsonBytes2Object(readDatas, LoginResponse.class);
        } else if(message.getMessageType() == MessageType.CLOUD_OPERATION_REQ){
            message = JsonUtil.jsonBytes2Object(readDatas, OperationRequest.class);
            //todo 发送指令回执
        }

        if (message != null) {
            log.info("收到消息: {}", message);
            out.add(message);
        }

    }
}
