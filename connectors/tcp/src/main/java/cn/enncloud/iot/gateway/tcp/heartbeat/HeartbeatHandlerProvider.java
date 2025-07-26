package cn.enncloud.iot.gateway.tcp.heartbeat;

import io.netty.channel.ChannelHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class HeartbeatHandlerProvider {

    public static Optional<ChannelHandler> newIdleStateHandler(int interval, IdleState idleState){
        if(interval == 0){
            return Optional.empty();
        }
        ChannelHandler idleStateHandler;
        switch (idleState){
            case READER_IDLE:
                idleStateHandler =  new IdleStateHandler(interval, 0, 0, TimeUnit.SECONDS);
                break;
            case WRITER_IDLE:
                idleStateHandler = new IdleStateHandler(0, interval, 0, TimeUnit.SECONDS);
                break;
            case ALL_IDLE:
            default:
                idleStateHandler =  new IdleStateHandler(0, 0, interval, TimeUnit.SECONDS);
                break;

        }
        return Optional.of(idleStateHandler);
    }

}
