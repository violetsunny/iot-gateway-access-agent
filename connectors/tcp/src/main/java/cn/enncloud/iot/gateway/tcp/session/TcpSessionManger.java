package cn.enncloud.iot.gateway.tcp.session;

import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hanyilong@enn.cn
 * @since 2021-02-14 11:55:48
 */
@Data
@Slf4j
@Component("sessionManager")
public class TcpSessionManger {
    private ConcurrentHashMap<String, ServerSession> sessionMap = new ConcurrentHashMap<>();

    // 单例模式
    private static TcpSessionManger singleInstance = null;

    public static void setSingleInstance(TcpSessionManger singleInstance) {
        TcpSessionManger.singleInstance = singleInstance;
    }

    public static TcpSessionManger getInstance(){
        return singleInstance;
    }

    /**
     * 增加session对象
     */
    public void addLocalSession(String deviceId, LocalSession localSession) {
        // 保存到本地Map
        sessionMap.put(deviceId,localSession);

        // 缓存session到Redis

        // 增加用户session信息到用户缓存

        // 增加用户数

        // 增加用户数

    }

    public LocalSession getSession(String deviceId){
        return (LocalSession)sessionMap.get(deviceId);
    }

    //关闭连接
    public void closeSession(ChannelHandlerContext ctx) {

        if (ctx==null){
            log.info("ctx is null");
            return;
        }

        boolean removed = ctx.isRemoved();
        if (removed){
            log.info("ctx is removed");
        }

        if (ctx.channel()==null){
            log.info("ctx.channel is null");
            return;
        }

        LocalSession session = ctx.channel().attr(LocalSession.SESSION_KEY).get();

        if (null == session || session.isValid()) {
            log.warn("session is null or isValid");
            return;
        }

        session.close();
        //删除本地的会话和远程会话
        this.removeSession(session.getSessionId());

        /**
         * 通知其他节点 ，用户下线
         */
//        notifyOtherImNodeOffLine(session);

    }

    /**
     * 删除session
     */
    public void removeSession(String sessionId)
    {
        if (!sessionMap.containsKey(sessionId)) return;
        ServerSession session = sessionMap.get(sessionId);
        //减少用户数
        //分布式：分布式保存user和所有session，根据 sessionId 删除用户的会话

        //step2:删除缓存session

        //本地：从会话集合中，删除会话
        sessionMap.remove(sessionId);
    }
}
