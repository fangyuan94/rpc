package com.fc.configCentre.annotation;


import com.fc.configCentre.ZookeeperConfigurationCLoseWarp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.*;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

import java.io.IOException;
import java.util.Optional;

/**
 * 扫描包初始化各访问rpc
 */
@Slf4j
public class EnableConfigurationCenterServerImport implements  ResourceLoaderAware,ImportBeanDefinitionRegistrar {

    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    private DefaultListableBeanFactory defaultListableBeanFactory;

    private ApplicationContext applicationContext;

    private ZookeeperConfigurationCLoseWarp zookeeperConfigurationCLoseWarp;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        //获取注解对应配置信息
        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(EnableConfigurationCenterServer.class.getName()));

        if(annotationAttributes.getBoolean("enable")){

            zookeeperConfigurationCLoseWarp = new ZookeeperConfigurationCLoseWarp();
            this.instertSpringBean("zookeeperConfigurationCLoseWarp",zookeeperConfigurationCLoseWarp);

            //注册钩子函数 当服务关闭时触发
            Runtime.getRuntime().addShutdownHook(new Thread(()->{
                log.info("配置系统被关闭了===========================>");
                //关闭时 优雅关闭
                Optional.ofNullable(zookeeperConfigurationCLoseWarp.getNodeCache()).ifPresent(nodeCache -> {
                    try {
                        nodeCache.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }));
        }
    }

    private void instertSpringBean(String name, Object obj) {

        defaultListableBeanFactory.registerSingleton(name, obj);
        autowireCapableBeanFactory.autowireBean(obj);
    }



    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {

        this.applicationContext = (ApplicationContext) resourceLoader;
        this.autowireCapableBeanFactory = applicationContext.getAutowireCapableBeanFactory();
        ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;
        this.defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
    }

}
