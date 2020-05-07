package com.fc.rpc.common.annotation;

import com.fc.rpc.common.factory.RPCFeignProxyFactory;
import com.fc.rpc.common.mapper.RPCMapper;
import org.springframework.beans.factory.FactoryBean;


/**
 *
 * @param <T>
 */
public class RPCFeignServiceFactoryBean<T> implements FactoryBean<T> {

    private Class<T> serviceInterface;

    private RPCMapper rpcMapper;

    private RPCFeignProxyFactory proxyFactory;

    public RPCFeignServiceFactoryBean(Class<T> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public void setRpcMapper(RPCMapper rpcMapper) {
        this.rpcMapper = rpcMapper;
    }

    public void setProxyFactory(RPCFeignProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }

    @Override
    public T getObject() throws Exception {
        //这里生成代理对象 该类的
        return (T) proxyFactory.getCglibProxy(this.getObjectType(), rpcMapper);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public Class<T> getObjectType() {
        return serviceInterface;
    }
}
