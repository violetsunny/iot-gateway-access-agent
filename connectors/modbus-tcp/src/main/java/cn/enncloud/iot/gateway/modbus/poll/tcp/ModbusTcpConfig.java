package cn.enncloud.iot.gateway.modbus.poll.tcp;

import cn.enncloud.iot.gateway.modbus.ModbusConnectorStater;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.concurrent.ExecutorService;

public class ModbusTcpConfig {
    private final NioEventLoopGroup group;
    private final ExecutorService executor;
    private String configId;
    private final Bootstrap bootstrap;
    private final String address;
    private final int port;

    private final int maxRetries; // 最大重试次数
    private final int retryDelay;
    private final int connectTimeout;

    public ModbusTcpConfig(NioEventLoopGroup group, ExecutorService executor, Bootstrap bootstrap,String configId, String address, int port, int maxRetries, int retryDelay, int connectTimeout) {
        this.group = group;
        this.executor = executor;
        this.bootstrap = bootstrap;
        this.configId = configId;
        this.address = address;
        this.port = port;
        this.maxRetries = maxRetries;
        this.retryDelay = retryDelay;
        this.connectTimeout = connectTimeout;
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public int getRetryDelay() {
        return retryDelay;
    }

    public NioEventLoopGroup getGroup() {
        return group;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public static class Builder {
        private String configId;
        private final String address;
        private int port = 502;
        private int maxRetries = 100; // 最大重试次数
        private int retryDelay = 5;
        private int connectTimeout = 5000;

        public Builder(String configId,String address, int port, int maxRetries, int retryDelay, int connectTimeout) {
            this.configId = configId;
            this.address = address;
            this.port = port;
            this.maxRetries = maxRetries;
            this.retryDelay = retryDelay;
            this.connectTimeout = connectTimeout;
        }

        public Builder(String address, int port) {
            this.address = address;
            this.port = port;
        }

        public Builder(String address) {
            this.address = address;
        }

        public ModbusTcpConfig build() {
            return new ModbusTcpConfig(
                    new NioEventLoopGroup(),
                    ModbusConnectorStater.getExecutorService(),
                    new Bootstrap(),
                    this.configId,
                    this.address,
                    this.port,
                    this.maxRetries,
                    this.retryDelay,
                    this.connectTimeout);
        }
    }
}
