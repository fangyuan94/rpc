package com.fc.rpc.common.annotation;


import com.fc.rpc.common.heartbeat.ClientInstanceInfo;

import com.fc.rpc.common.scanner.RPCFeignServiceClassPathSacnner;

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
public class EnableRPCFeignServerImport implements BeanFactoryAware, ResourceLoaderAware, EnvironmentAware,ImportBeanDefinitionRegistrar {

    private  ResourceLoader resourceLoader;

    private  Environment environment;

    private BeanFactory beanFactory;

    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    private DefaultListableBeanFactory defaultListableBeanFactory;

    private ApplicationContext applicationContext;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        //获取注解对应配置信息
        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(EnableRPCFeignServer.class.getName()));

        if(annotationAttributes.getBoolean("enable")){

            RPCFeignServiceClassPathSacnner scanner =  new RPCFeignServiceClassPathSacnner(registry,true,environment,resourceLoader);

            String host = annotationAttributes.getString("host");

            Integer port =  Integer.valueOf(annotationAttributes.getString("port"));

            String[] packages  = annotationAttributes.getStringArray("packages");

            //找到该路径下所有包含RPCFeignService注解类生成代理实例化
            if(packages.length == 0){
                //默认取最上层目录
                packages = new String[]{"com.fc.rpc"};
            }
            scanner.setAnnotationClass(RPCFeignService.class);
            //设置哪些类可以被实例bean
            scanner.registerFilters();
            //扫描注册
            scanner.scan(packages);

            //开启注册并实现心跳检测机制 定时向ZK注册实例信息
            String serverName = annotationAttributes.getString("serverName");

            ClientInstanceInfo clientInstanceInfo = ClientInstanceInfo.builder().serverName(serverName).ip(host)
                    .port(port).build();

            instertSpringBean("clientInstanceInfo",clientInstanceInfo);

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
