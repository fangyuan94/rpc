package com.fc.rpc.common.config;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({CuratorFramework.class, ZooKeeper.class})
public class ZookeeperAutoConfiguration {

    @Autowired
    private CuratorZookeeperProperties curatorZookeeperProperties;

    @Bean
    public CuratorFramework client(){

        //重试策略
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(curatorZookeeperProperties.getBaseSleepTimeMs(),curatorZookeeperProperties.getMaxRetries());

        CuratorFramework client = CuratorFrameworkFactory.newClient(curatorZookeeperProperties.getConnectString(),curatorZookeeperProperties.getSessionTimeoutMs(),curatorZookeeperProperties.getConnectionTimeoutMs(),retryPolicy);

        client.start();

        return client;
    }


}
