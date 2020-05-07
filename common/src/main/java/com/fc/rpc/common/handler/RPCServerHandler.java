package com.fc.rpc.common.handler;

import com.fc.rpc.common.pojo.reps.RpcResponse;
import com.fc.rpc.common.pojo.req.RpcRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;

/**
 * 获取
 * @author fangyuan
 */
public class RPCServerHandler extends ChannelInboundHandlerAdapter {


    private BeanFactory beanFactory;

    public RPCServerHandler(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    //
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        // 获取对应数据信息

        if(msg instanceof RpcRequest){
            RpcRequest rpcRequest = (RpcRequest) msg;

            Object object = beanFactory.getBean(rpcRequest.getClasses());
            //获取对应对象

            Method method = object.getClass().getMethod(rpcRequest.getMethodName(),rpcRequest.getParameterTypes());

           Object rs =  method.invoke(object,rpcRequest.getParameters());

            RpcResponse rpcResponse = new RpcResponse();
            rpcResponse.setCode(200);
            rpcResponse.setData(rs);
            rpcResponse.setSuccess("success");
            ctx.writeAndFlush(rpcResponse);
        }

    }

}
