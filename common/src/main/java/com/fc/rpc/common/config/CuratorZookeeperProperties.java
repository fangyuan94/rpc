package com.fc.rpc.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = CuratorZookeeperProperties.PREFIX)
public class CuratorZookeeperProperties {

    public final static String PREFIX = "fc.zk";

    private String connectString = "127.0.0.1:2181";

    private int sessionTimeoutMs = 6000;

    private int connectionTimeoutMs = 30000;

    //首次重试睡眠时间
    private int baseSleepTimeMs = 2000;
    //重试最大次数
    private int maxRetries = 3;

}
