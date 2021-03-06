package me.tony.base.thrift.spring.server.factory;

import me.tony.base.thrift.spring.config.server.ServerConfig;

public class TThreadPoolServerFactory extends AbstractServerFactory {

    @Override
    public ServerConfig.ServerType supportedServerType() {
        return ServerConfig.ServerType.POOL;
    }

}

