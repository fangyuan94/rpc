package com.fc.rpc.common.handler;

import com.fc.rpc.common.loadBalancing.FairUpdateTimeTask;
import com.fc.rpc.common.pojo.reps.RpcResponse;
import com.fc.rpc.common.pojo.req.RpcRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.beans.factory.BeanFactory;

import java.lang.reflect.Method;

/**
 * 获取
 * @author fangyuan
 */
public class RPCServerHandler extends ChannelInboundHandlerAdapter {

    private BeanFactory beanFactory;

    private FairUpdateTimeTask failUpdateTimeTask;

    public RPCServerHandler(BeanFactory beanFactory, FairUpdateTimeTask failUpdateTimeTask) {
        this.beanFactory = beanFactory;
        this.failUpdateTimeTask = failUpdateTimeTask;
    }

    //
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if(msg instanceof RpcRequest){
            // 获取对应数据信息
            Long startTime = System.currentTimeMillis();
            RpcRequest rpcRequest = (RpcRequest) msg;

            Object object = beanFactory.getBean(rpcRequest.getClasses());
            //获取对应对象

            Method method = object.getClass().getMethod(rpcRequest.getMethodName(),rpcRequest.getParameterTypes());

            Object rs =  method.invoke(object,rpcRequest.getParameters());

            RpcResponse rpcResponse = new RpcResponse();
            rpcResponse.setCode(200);
            rpcResponse.setData(rs);
            rpcResponse.setSuccess("success");

            Long endTime = System.currentTimeMillis();
            //记录整体响应时间 该服务影响只想响应5s
            Long responseTime = endTime - startTime;
            //开启另外线程处理 监控任务
            failUpdateTimeTask.setMinResponseTime(responseTime);

            ctx.writeAndFlush(rpcResponse);
        }

    }

}
