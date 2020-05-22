package com.fc.rpc.common.config;

import com.fc.rpc.common.coder.RpcRequestDecoder;
import com.fc.rpc.common.coder.RpcResponseEncoder;
import com.fc.rpc.common.handler.RPCServerHandler;
import com.fc.rpc.common.heartbeat.ClientInstanceInfo;
import com.fc.rpc.common.heartbeat.DiscoveryClient;
import com.fc.rpc.common.heartbeat.ZookeeperDiscoveryClient;
import com.fc.rpc.common.loadBalancing.FairExecutor;
import com.fc.rpc.common.loadBalancing.FairUpdateTimeTask;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(ClientInstanceInfo.class)
@AutoConfigureAfter(ZookeeperAutoConfiguration.class)
public class RPCFeignClientConfiguration {

    @Autowired
    private CuratorFramework client;

    @Autowired
    private ClientInstanceInfo clientInstanceInfo;

    @Autowired
    private BeanFactory beanFactory;

    @Bean
    public DiscoveryClient discoveryClient(){
        DiscoveryClient discoveryClient = new ZookeeperDiscoveryClient(client,clientInstanceInfo);
        discoveryClient.start();
        return discoveryClient;
    }

    @Bean
    public FairUpdateTimeTask failUpdateTimeTask(){
        String serverPath = clientInstanceInfo.getServerName()+"/"+clientInstanceInfo.getIp()+":"+clientInstanceInfo.getPort();
        String path = ZookeeperDiscoveryClient.MUTEX_LOCK_PATH_PREFIX+serverPath;
        FairUpdateTimeTask failUpdateTimeTask =  new FairUpdateTimeTask(client,path,serverPath);
        //初始化
        initNettyServer(clientInstanceInfo.getIp(),clientInstanceInfo.getPort(),failUpdateTimeTask);
        return failUpdateTimeTask;
    }

    @Bean
    public FairExecutor failExecutor(FairUpdateTimeTask failUpdateTimeTask){

        String path = ZookeeperDiscoveryClient.MUTEX_LOCK_PATH_PREFIX+clientInstanceInfo.getServerName()+"/"+clientInstanceInfo.getIp()+":"+clientInstanceInfo.getPort();

        FairExecutor failExecutor = new FairExecutor(client,path,failUpdateTimeTask);
        failExecutor.start();
        return failExecutor;
    }

    /**
     * 初始化
     * @param host
     * @param port
     * @param failUpdateTimeTask
     */
    private void  initNettyServer(String host, Integer port, FairUpdateTimeTask failUpdateTimeTask){

        NioEventLoopGroup workExecutors = new NioEventLoopGroup();

        NioEventLoopGroup acceptExecutors =  new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();

        serverBootstrap.group(acceptExecutors,workExecutors)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>(){
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        //为当前SocketChannel 添加Pipeline
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new RpcRequestDecoder());
                        pipeline.addLast(new RpcResponseEncoder());
                        pipeline.addLast(new RPCServerHandler(beanFactory,failUpdateTimeTask));
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



}
