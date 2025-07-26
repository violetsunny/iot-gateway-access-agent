/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package cn.enncloud.iot.gateway.tcp.handler;

import cn.enncloud.iot.gateway.message.LoginRequest;
import cn.enncloud.iot.gateway.message.LoginResponse;
import cn.enncloud.iot.gateway.message.Message;
import cn.enncloud.iot.gateway.message.MetricCloudCallResponse;
import cn.enncloud.iot.gateway.message.MetricReportRequest;
import cn.enncloud.iot.gateway.protocol.Protocol;
import cn.enncloud.iot.gateway.tcp.process.LoginProcesser;
import cn.enncloud.iot.gateway.tcp.session.LocalSession;
import cn.enncloud.iot.gateway.tcp.session.TcpSessionManger;
import io.netty.channel.Channel;
import cn.hutool.core.collection.CollectionUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles both client-side and server-side handler depending on which
 * constructor was called.
 */
@Slf4j
public class MessageHandler extends ChannelInboundHandlerAdapter {

    Protocol protocol;
    TcpSessionManger tcpSessionManger;

    LoginProcesser loginProcesser;

    public MessageHandler(Protocol protocol, TcpSessionManger tcpSessionManger, LoginProcesser loginProcesser) {
        this.protocol = protocol;
        this.tcpSessionManger = tcpSessionManger;
        this.loginProcesser = loginProcesser;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Message message = (Message) msg;
        if (message.getDeviceId() == null) {
            log.warn("上报数据 deviceId为空{}", message);
            return;
        }
        LocalSession localSession = null;
        if (message instanceof LoginRequest) {
            LoginResponse response = loginProcesser.action(protocol, (LoginRequest) message);
            if (response.isLogin()) {
                localSession = new LocalSession(ctx.channel(), message.getDeviceId());
                localSession.setProtocol(protocol);
                tcpSessionManger.addLocalSession(message.getDeviceId(), localSession);
            } else {
                TcpSessionManger.getInstance().closeSession(ctx);
            }
            ctx.write(response);

        } else {
            localSession = tcpSessionManger.getSession(message.getDeviceId());
            if (localSession == null) {
                localSession = new LocalSession(ctx.channel(), message.getDeviceId());
                localSession.setProtocol(protocol);
                tcpSessionManger.addLocalSession(message.getDeviceId(), localSession);
            }
            if (message instanceof MetricReportRequest) {
                protocol.getDeviceContext().storeMessage(message);
                if (message.getResponse() != null) {
                    sendResponse(ctx.channel(), message);
                }
            } else {
                ctx.write(message);
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


    private void sendResponse(Channel channel, Message msg) {
        String hex = msg.getResponse();
        if (!(msg instanceof MetricReportRequest)) {
            return;
        }
        Message res = new MetricCloudCallResponse();
        res.setResponse(hex);
        if (channel == null) {
            return;
        }
        if (!channel.isActive()) {
            return;
        }
        if (channel.isWritable()) {
            channel.writeAndFlush(res)
                    .addListener(cf -> {
                        if (cf.isSuccess()) {
                            log.info("send response success, {}", hex);
                        } else {
                            log.error("send response failed, " + hex + ",", cf.cause());
                        }
                    });
        } else {
            try {
                channel.writeAndFlush(res)
                        .sync()
                        .addListener(cf -> {
                            if (cf.isSuccess()) {
                                log.info("send response success, {}", hex);
                            } else {
                                log.error("send response failed, " + hex + ",", cf.cause());
                            }
                        });
            } catch (InterruptedException e) {
                log.error("send response was interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
