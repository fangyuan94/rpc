package com.fc.configCentre.config;

import com.fc.configCentre.ConfigurationCentre;
import com.fc.rpc.common.config.CuratorZookeeperProperties;
import com.fc.rpc.common.config.ZookeeperAutoConfiguration;
import com.fc.rpc.common.utils.SerializeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author fangyuan
 */
@Configuration
@ConditionalOnBean(ZookeeperConfigurationCentreProperties.class)
@AutoConfigureAfter(ZookeeperAutoConfiguration.class)
@ConditionalOnProperty(prefix = "fc.configurationCentreServer",name = "enable",havingValue = "true",matchIfMissing = false)
@Slf4j
public class ZKConfigurationCentreServerAutoConfiguration {

    @Autowired(required = false)
    private CuratorZookeeperProperties curatorZookeeperProperties;

    @Autowired(required = false)
    private ZookeeperConfigurationCentreProperties zookeeperConfigurationCentreProperties;

    @Value("${fc.configurationCentre.serverName:local}")
    private String configServerName;

    private final  static String CONFIG_BASE_PATH ="/fc/configurationCentre/";

    @Bean
    @ConditionalOnMissingBean(CuratorFramework.class)
    public CuratorFramework client(){

        //重试策略
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(curatorZookeeperProperties.getBaseSleepTimeMs(),curatorZookeeperProperties.getMaxRetries());

        CuratorFramework client = CuratorFrameworkFactory.newClient(curatorZookeeperProperties.getConnectString(),curatorZookeeperProperties.getSessionTimeoutMs(),curatorZookeeperProperties.getConnectionTimeoutMs(),retryPolicy);

        client.start();

        return client;
    }

    /**
     *  初始化
     * @param client
     */
    @Bean
    public ConfigurationCentre configurationCentre(CuratorFramework client){

        String path = ZKConfigurationCentreServerAutoConfiguration.CONFIG_BASE_PATH+configServerName;

        try {
            Stat stat = client.checkExists().forPath(path);

            if(stat !=null){
                client.delete().forPath(path);
            }
            log.info("当前");
            client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path,SerializeUtil.serialize(zookeeperConfigurationCentreProperties));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ConfigurationCentre();
    }

}
