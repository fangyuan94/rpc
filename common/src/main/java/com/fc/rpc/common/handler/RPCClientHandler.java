package com.fc.rpc.common.handler;

import com.fc.rpc.common.pojo.reps.RpcResponse;
import com.fc.rpc.common.pojo.req.RpcRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.Callable;

/**
 * @author fangyuan
 * 任务执行器
 */
public class RPCClientHandler extends ChannelInboundHandlerAdapter implements Callable {

    private ChannelHandlerContext context;

    private RpcRequest para;

    private RpcResponse result;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.context = ctx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        synchronized(this){
            result = (RpcResponse) msg;
            //收到请求 唤醒this对象锁上沉睡线程
            notify();
        }

    }


    @Override
    public  Object call() throws Exception {
        //每次执行一个线程
        synchronized(this){
            context.writeAndFlush(para);
            //发送请求 等待其他结果返回
            wait();
            return result;
        }
    }

    public void setPara(RpcRequest para) {
        this.para = para;
    }
}
