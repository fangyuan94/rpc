package com.fc.rpc.common.config;

import com.fc.rpc.common.discovery.FeignServerZKDiscovery;
import com.fc.rpc.common.mapper.RPCMappersWarp;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@AutoConfigureAfter(ZookeeperAutoConfiguration.class)
@ConditionalOnBean(RPCMappersWarp.class)
public class RPCFeignServerZkDiscoveryConfiguration {

    @Autowired
    private RPCMappersWarp rpcMappersWarp;

    @Autowired
    private CuratorFramework client;

    @Bean
    public FeignServerZKDiscovery feignServerDiscovery(){

        //服务发现 定时从zk中获取服务器地址
        FeignServerZKDiscovery feignServerDiscovery = new FeignServerZKDiscovery(client,rpcMappersWarp.getRpcMappers());
        feignServerDiscovery.start();
        return feignServerDiscovery;
    }

}
