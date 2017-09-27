package me.tony.base.thrift.spring.server.factory;

import me.tony.base.thrift.spring.config.server.ServerConfig;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TServerTransport;
import org.springframework.beans.BeanUtils;

import java.util.Objects;

public abstract class AbstractServerFactory implements ServerFactory {

    @Override
    public final TServer buildServer(ServerConfig config, Class<? extends TProcessor> processorType, Class<?> ifaceType, Object provider) {
        Objects.requireNonNull(config, "config cannot be null!");
        Objects.requireNonNull(provider, "provider connot be null!");
        checkServerType(config);
        TServerTransport transport = createTransport(config);
        TProcessor processor = createProcessor(processorType, ifaceType, provider);
        TServer.AbstractServerArgs args = createArgs(config, transport);
        args.processor(processor);
        return createServer(config, args);
    }

    private void checkServerType(ServerConfig config) {
        if (config.getServerType() != supportedServerType()) {
            throw new IllegalArgumentException(config.getServerType() + " not fit for " + supportedServerType());
        }
    }

    private TServer createServer(ServerConfig config, TServer.AbstractServerArgs args) {
        Class<? extends TServer> serverClass = config.getServerType().getServerClass();
        Class<? extends TServer.AbstractServerArgs> argsClass = config.getServerType().getArgsClass();
        try {
            return BeanUtils.instantiateClass(serverClass.getConstructor(argsClass), args);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("cannot find target constructor for " + serverClass.getName(), e);
        }
    }

    private TProcessor createProcessor(Class<? extends TProcessor> processorType, Class<?> ifaceType, Object provider) {
        try {
            return BeanUtils.instantiateClass(processorType.getConstructor(ifaceType), provider);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("cannot find target constructor for " + processorType.getName(), e);
        }
    }

    private TServer.AbstractServerArgs createArgs(ServerConfig config, TServerTransport transport) {
        Class<? extends TServer.AbstractServerArgs> argsClass = config.getServerType().getArgsClass();
        try {
            TServer.AbstractServerArgs args = BeanUtils.instantiateClass(argsClass.getConstructor(TServerTransport.class), transport);
            args.protocolFactory(createProtocalFactory(config.getProtocol().getProtocolType().getFactoryClass()));
            return args;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("class " + argsClass.getClass().getName() + " is not a thrift iface provider");
        }
    }

    private TProtocolFactory createProtocalFactory(Class<? extends TProtocolFactory> factoryType) {
        try {
            return BeanUtils.instantiateClass(factoryType.getConstructor());
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("cannot instantiate protocol factory " + factoryType.getName());
        }
    }

    protected TServerTransport createTransport(ServerConfig config) {
        Class<? extends TServerTransport> transportClass = config.getServerType().getTransportClass();
        try {
            return BeanUtils.instantiateClass(transportClass.getConstructor(Integer.TYPE, Integer.TYPE),
                    config.getPort(), config.getTimeout());
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("cannot find target constructor for " + transportClass.getName(), e);
        }
    }

//    protected abstract TServer buildServer(Class<? extends TServer> serverClass, ServerConfig config);

}
