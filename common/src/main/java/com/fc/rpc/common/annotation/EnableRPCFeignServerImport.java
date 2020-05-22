package com.fc.rpc.common.annotation;

import com.fc.rpc.common.mapper.RPCMapper;
import com.fc.rpc.common.mapper.RPCMappersWarp;
import com.fc.rpc.common.mapper.RPCNettyClient;
import com.fc.rpc.common.scanner.RPCFeignClassPathSacnner;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.*;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 扫描包初始化各访问rpc
 */
@Slf4j
public class EnableRPCFeignServerImport implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware  {


    private ResourceLoader resourceLoader;

    private ApplicationContext applicationContext;

    private Environment environment;

    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    private DefaultListableBeanFactory defaultListableBeanFactory;

    private RPCMappersWarp rpcMappersWarp;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        //获取注解对应配置信息
        AnnotationAttributes enableRPCFeignServer = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(EnableRPCFeignServer.class.getName()));

        List<RPCMapper> rpcMappers = null;

        if (enableRPCFeignServer.getBoolean("enable")) {  //必须保证有有使用到服务

                RPCFeignClassPathSacnner scanner = new RPCFeignClassPathSacnner(registry, true, environment, resourceLoader);

                String[] packages = enableRPCFeignServer.getStringArray("packages");

                //找到该路径下所有包含RPCFeignService注解类生成代理实例化
                if (packages.length == 0) {
                    //默认取最上层目录
                    packages = new String[]{"com.fc.rpc"};
                }
                scanner.setAnnotationClass(RPCFeign.class);
                //设置哪些类可以被实例bean
                scanner.registerFilters();
                //扫描注册
                scanner.scan(packages);
                rpcMappers = scanner.getRpcMappers();

                if(rpcMappers == null || rpcMappers.size()==0){
                    throw new RuntimeException("当前服务未配置");
                }

                rpcMappersWarp = new RPCMappersWarp();

                rpcMappersWarp.setRpcMappers(rpcMappers);

                instertSpringBean("rpcMappersWarp", rpcMappersWarp);

                //注册关闭的钩子事件
                Runtime.getRuntime().addShutdownHook(new Thread(()->{

                    log.info("springboot服务已被关闭==============>");
                    //关闭
                    Optional.ofNullable(rpcMappersWarp).ifPresent(rmm->{
                        //迭代关闭
                        rmm.getRpcMappers().forEach(rpcMapper -> {

                            TreeCache treeCache = rpcMapper.getTreeCache();

                            if(treeCache!=null){
                                treeCache.close();
                            }

                            TreeCache failRespTimeTreeCache = rpcMapper.getFailRespTimeTreeCache();

                            if(failRespTimeTreeCache!=null){
                                failRespTimeTreeCache.close();
                            }
                            ConcurrentHashMap<String, RPCNettyClient> rpcNettyClients = rpcMapper.getRpcNettyClients();

                            if(rpcNettyClients!=null){
                                rpcNettyClients.forEach((k,rpcNettyClient)->{
                                    rpcNettyClient.getEventLoopGroup().shutdownGracefully();
                                });
                            }

                        });
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

}
