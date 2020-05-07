package com.fc.rpc.common.annotation;

import com.fc.rpc.common.coder.RpcRequestDecoder;
import com.fc.rpc.common.coder.RpcResponseEncoder;
import com.fc.rpc.common.handler.RPCServerHandler;
import com.fc.rpc.common.scanner.RPCFeignServiceClassPathSacnner;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
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
public class EnableRPCFeignProviderImport implements BeanFactoryAware, ResourceLoaderAware, EnvironmentAware,ImportBeanDefinitionRegistrar {

    private  ResourceLoader resourceLoader;

    private  Environment environment;

    private BeanFactory beanFactory;


    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        //获取注解对应配置信息
        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(EnableRPCFeignProvider.class.getName()));

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
            initNettyServer(host,port);
        }
    }

    /**
     * 初始化
     * @param host
     * @param port
     */
    private void  initNettyServer(String host,Integer port){

        NioEventLoopGroup workExecutors = new NioEventLoopGroup();

        NioEventLoopGroup acceptExecutors =  new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();

        serverBootstrap.group(acceptExecutors,workExecutors)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>(){
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        //为当前SocketChannel 添加Pipeline
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new RpcRequestDecoder());
                        pipeline.addLast(new RpcResponseEncoder());
                        pipeline.addLast(new RPCServerHandler(beanFactory));
                    }
                } )
                ;

        try {
            serverBootstrap.bind(host,port).sync();
            System.out.println("==============netty服务器启动成功=================");
        } catch (InterruptedException e) {
            e.printStackTrace();
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


    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {

        this.beanFactory = beanFactory;
    }
}
