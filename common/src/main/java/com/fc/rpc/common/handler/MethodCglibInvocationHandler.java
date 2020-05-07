package com.fc.rpc.common.handler;

import com.fc.rpc.common.mapper.RPCMapper;
import com.fc.rpc.common.pojo.reps.RpcResponse;
import com.fc.rpc.common.pojo.req.RpcRequest;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author fangyuan
 * 代理执行
 */
public class MethodCglibInvocationHandler implements MethodInterceptor {

    //创建线程池对象
    private  ExecutorService executor;

    private Class<?> classes;

    private RPCClientHandler rpcClientHandler;

    public MethodCglibInvocationHandler(Class<?> classes, RPCClientHandler rpcClientHandler){
        this.classes = classes;
        this.rpcClientHandler = rpcClientHandler;
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {

        //构建请求对象
        RpcRequest rpcRequest = RpcRequest.build(classes,method,objects);

        // 发送请求
        rpcClientHandler.setPara(rpcRequest);

        RpcResponse rpcResponse = (RpcResponse) executor.submit(rpcClientHandler).get();
        if(rpcResponse.getCode()!=200 && "success".equals(rpcResponse.getSuccess())){
            throw  new RuntimeException("请求异常");
        }
        //提交任务
      return rpcResponse.getData();

    }
}
