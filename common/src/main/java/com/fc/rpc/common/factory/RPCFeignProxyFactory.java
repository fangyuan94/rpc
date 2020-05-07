package com.fc.rpc.common.factory;

import com.fc.rpc.common.coder.RpcRequestDecoder;
import com.fc.rpc.common.coder.RpcRequestEncoder;
import com.fc.rpc.common.coder.RpcResponseDecoder;
import com.fc.rpc.common.coder.RpcResponseEncoder;
import com.fc.rpc.common.handler.MethodCglibInvocationHandler;
import com.fc.rpc.common.handler.RPCClientHandler;
import com.fc.rpc.common.mapper.RPCMapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.sf.cglib.proxy.Enhancer;


/**
 *
 * @author fangyuan
 * 代理对象工厂：生成代理对象的
 */
public class RPCFeignProxyFactory {

    private  RPCClientHandler rpcClientHandler;

    /**
     * 使用cglib动态代理生成代理对象
     * @return
     */
    public  Object getCglibProxy(Class<?> classes, RPCMapper rpcMapper) {
        //构建netty服务端
        initNettyClient(rpcMapper);
        return  Enhancer.create(classes, new MethodCglibInvocationHandler(classes,rpcClientHandler));
    }

    //使用netty连接
    private void initNettyClient( RPCMapper rpcMapper){

        String host = rpcMapper.getHost();

        String port = rpcMapper.getPort();

        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();

        rpcClientHandler = new RPCClientHandler();

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
            bootstrap.connect(host,Integer.valueOf(port)).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

}
