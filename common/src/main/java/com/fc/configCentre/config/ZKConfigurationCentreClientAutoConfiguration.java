package com.fc.configCentre.config;

import com.fc.configCentre.ZookeeperConfigurationCLoseWarp;
import com.fc.rpc.common.config.CuratorZookeeperProperties;
import com.fc.rpc.common.config.ZookeeperAutoConfiguration;
import com.fc.rpc.common.utils.SerializeUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.lang.reflect.Field;

/**
 * 依赖配置中心
 * @author fangyuan
 */
@Configuration
@ConditionalOnClass({CuratorFramework.class, ZooKeeper.class, DataSource.class})
//在DataSourceAutoConfiguration之前执行
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@AutoConfigureAfter(ZookeeperAutoConfiguration.class)
@ConditionalOnProperty(prefix = "fc.configurationCentre",name = "enable",havingValue = "true",matchIfMissing = false)
class ZKConfigurationCentreClientAutoConfiguration {

    @Autowired(required = false)
    private CuratorZookeeperProperties curatorZookeeperProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${fc.configurationCentre.serverName:local}")
    private String configServerName;

    @Autowired(required = false)
    private ZookeeperConfigurationCLoseWarp zookeeperConfigurationCLoseWarp;


    //配置中心所在路径
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

    @Bean
    public ZookeeperConfigurationCentreProperties zookeeperConfigurationCentreProperties(CuratorFramework client){

        //读取zk配置文件属性
        String path = ZKConfigurationCentreClientAutoConfiguration.CONFIG_BASE_PATH+configServerName;

        //监控节点数据变更
        NodeCache nodeCache = new NodeCache(client,path);

        zookeeperConfigurationCLoseWarp.setNodeCache(nodeCache);

        nodeCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {

                //节点发生变化
                ZookeeperConfigurationCentreProperties zookeeperConfigurationCentreProperties = SerializeUtil.deserialize(nodeCache.getCurrentData().getData(),ZookeeperConfigurationCentreProperties.class);
                //旧的DataSource
                if(applicationContext.containsBean("hikariDataSource")){
                    HikariDataSource hikariDataSourceOld = applicationContext.getBean("hikariDataSource",HikariDataSource.class);
                    //改变配置中心基础属性
                    conversionData(zookeeperConfigurationCentreProperties,hikariDataSourceOld);
                }

            }
        });
        try {
            //读取配置文件信息 然后再转化为spring 有关 这里简化处理 直接读取
            nodeCache.start(true);
            return SerializeUtil.deserialize(nodeCache.getCurrentData().getData(),ZookeeperConfigurationCentreProperties.class);
        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException("无法从zk中拉取对应配置信息请验证");
        }
    }

    /**
     * 转化数据
     * @param zookeeperConfigurationCentreProperties
     * @param hikariDataSourceOld
     */
    private void conversionData(ZookeeperConfigurationCentreProperties zookeeperConfigurationCentreProperties, HikariDataSource hikariDataSourceOld) throws NoSuchFieldException, IllegalAccessException {

        Class<? extends HikariDataSource> aClass = hikariDataSourceOld.getClass();
        //获取
        Field[] declaredFields = zookeeperConfigurationCentreProperties.getClass().getDeclaredFields();

        //迭代字段 与 HikariDataSource进行对比
        for(Field fieldNew:declaredFields){
            fieldNew.setAccessible(true);
            String name = fieldNew.getName();

            Field fieldOld = aClass.getField(name);
            Object valueNew = fieldNew.get(zookeeperConfigurationCentreProperties);
            //对应配置
            if(!valueNew.equals(fieldOld.get(hikariDataSourceOld))){
                //如果不同进行变更
                fieldOld.setAccessible(true);
                fieldOld.set(hikariDataSourceOld,valueNew);
            }
        }

    }

    @ConditionalOnClass(HikariDataSource .class)
    @ConditionalOnMissingBean(DataSource.class)
    static class Hikari {
        @Bean
        HikariDataSource dataSource(ZookeeperConfigurationCentreProperties zookeeperConfigurationCentreProperties) {
            return createDataSource(zookeeperConfigurationCentreProperties);
        }

    }

    /**
     * 创建一个新的
     * @param zookeeperConfigurationCentreProperties
     * @return
     */
    private static HikariDataSource createDataSource(ZookeeperConfigurationCentreProperties zookeeperConfigurationCentreProperties){

        //初始化连接pool
        HikariConfig hikariConfig = new HikariConfig();
        ZookeeperConfigurationCentreProperties.Hikari hikari = zookeeperConfigurationCentreProperties.getHikari();
        hikariConfig.setDriverClassName(zookeeperConfigurationCentreProperties.getDriverClassName());
        hikariConfig.setJdbcUrl(zookeeperConfigurationCentreProperties.getJdbcUrl());
        hikariConfig.setUsername(zookeeperConfigurationCentreProperties.getUsername());
        hikariConfig.setPassword(zookeeperConfigurationCentreProperties.getPassword());
        hikariConfig.setMaximumPoolSize(hikari.getMaximumPoolSize());
        hikariConfig.setMinimumIdle(hikari.getMinimumIdle());
        hikariConfig.setMaxLifetime(hikari.getMaxLifetime());
        hikariConfig.setAutoCommit(hikari.getAutoCommit());
        hikariConfig.setConnectionTestQuery(hikari.getConnectionTestQuery());
        hikariConfig.setConnectionTimeout(hikari.getConnectionTimeout());
        hikariConfig.setValidationTimeout(hikari.getValidationTimeout());
        hikariConfig.setReadOnly(hikari.getReadOnly());
        hikariConfig.setIdleTimeout(hikari.getIdleTimeout());
        hikariConfig.setPoolName(zookeeperConfigurationCentreProperties.getPoolName());
        HikariDataSource dataSource = new HikariDataSource(hikariConfig);
        return dataSource;
    }


}
