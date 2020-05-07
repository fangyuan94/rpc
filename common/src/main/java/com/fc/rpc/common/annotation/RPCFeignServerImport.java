package com.fc.rpc.common.annotation;

import com.fc.rpc.common.scanner.RPCFeignClassPathSacnner;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * 扫描包初始化各访问rpc
 */
public class RPCFeignServerImport implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {

    private  ResourceLoader resourceLoader;

    private  Environment environment;
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        //获取注解对应配置信息
        AnnotationAttributes enableRPCFeignServer = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(EnableRPCFeignServer.class.getName()));

        if(enableRPCFeignServer.getBoolean("enable")){

            RPCFeignClassPathSacnner scanner =  new RPCFeignClassPathSacnner(registry,true,environment,resourceLoader);

            String[] packages  = enableRPCFeignServer.getStringArray("packages");

            //找到该路径下所有包含RPCFeignService注解类生成代理实例化
            if(packages.length == 0){
                //默认取最上层目录
                packages = new String[]{"com.fc.rpc"};
            }
            scanner.setAnnotationClass(RPCFeign.class);
            //设置哪些类可以被实例bean
            scanner.registerFilters();
            //扫描注册
            scanner.scan(packages);
        }
    }



    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {

        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;

    }
}
