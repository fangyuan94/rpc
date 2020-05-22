package com.fc.configCentre.annotation;


import com.fc.configCentre.config.ZookeeperConfigurationCentreProperties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 扫描包初始化各访问rpc
 */
public class EnableConfigurationCenterServerImport implements BeanFactoryAware, ResourceLoaderAware, EnvironmentAware,ImportBeanDefinitionRegistrar {

    private  ResourceLoader resourceLoader;

    private  Environment environment;

    private BeanFactory beanFactory;

    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    private DefaultListableBeanFactory defaultListableBeanFactory;

    private ApplicationContext applicationContext;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        //获取注解对应配置信息
        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(EnableConfigurationCenterServer.class.getName()));

        if(annotationAttributes.getBoolean("enable")){

            ZookeeperConfigurationCentreProperties zookeeperConfigurationCentreProperties = new ZookeeperConfigurationCentreProperties();

            zookeeperConfigurationCentreProperties.setJdbcUrl("jdbc:mysql://localhost:3306/blog_system?useUnicode=true&useSSL=false&characterEncoding=UTF-8");

            zookeeperConfigurationCentreProperties.setUsername("root");
            zookeeperConfigurationCentreProperties.setPassword("123456");

            ZookeeperConfigurationCentreProperties.Hikari hikari = new ZookeeperConfigurationCentreProperties.Hikari();
            zookeeperConfigurationCentreProperties.setHikari(hikari);
            this.instertSpringBean("zookeeperConfigurationCentreProperties",zookeeperConfigurationCentreProperties);

        }
    }

    private void instertSpringBean(String name, Object obj) {

        defaultListableBeanFactory.registerSingleton(name, obj);
        autowireCapableBeanFactory.autowireBean(obj);
    }



    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {

        this.resourceLoader = resourceLoader;
        this.applicationContext = (ApplicationContext) resourceLoader;
        this.autowireCapableBeanFactory = applicationContext.getAutowireCapableBeanFactory();
        ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;
        this.defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }


    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {

        this.beanFactory = beanFactory;
    }
}
