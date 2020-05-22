package com.fc.rpc.common.utils;

import com.fc.rpc.common.coder.RpcRequestEncoder;
import com.fc.rpc.common.coder.RpcResponseDecoder;
import com.fc.rpc.common.handler.RPCClientHandler;
import com.fc.rpc.common.mapper.RPCNettyClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;


/**
 * 构建netty服务
 * @author fangyuan
 */
@Slf4j
public class NettyServerUtils {

    public  static RPCNettyClient initNettyClient(String host, Integer port){

        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();

        RPCClientHandler rpcClientHandler = new RPCClientHandler();

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY,true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new RpcRequestEncoder());
                        pipeline.addLast(new RpcResponseDecoder());
                        pipeline.addLast(rpcClientHandler);
                    }
                });
        try {
            bootstrap.connect(host,port).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            try {
                //关闭线程
                group.shutdownGracefully().sync();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        return RPCNettyClient.builder().eventLoopGroup(group).rpcClientHandler(rpcClientHandler).build();
    }

    /**
     * 关闭服务
     * @param rpcNettyClient
     */
    public static void stopNettyServer(RPCNettyClient rpcNettyClient){

        try {
            rpcNettyClient.getEventLoopGroup().shutdownGracefully().sync();
        } catch (InterruptedException e) {
            log.error("关闭客户端是比");
            e.printStackTrace();
        }
    }


}
