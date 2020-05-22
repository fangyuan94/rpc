package com.fc.rpc.common.handler;

import com.fc.rpc.common.heartbeat.ClientInstanceInfo;
import com.fc.rpc.common.loadBalancing.FairLoadBalance;
import com.fc.rpc.common.loadBalancing.LoadBalance;
import com.fc.rpc.common.loadBalancing.LocalLoadBalance;
import com.fc.rpc.common.mapper.RPCMapper;
import com.fc.rpc.common.mapper.RPCNettyClient;
import com.fc.rpc.common.pojo.reps.RpcResponse;
import com.fc.rpc.common.pojo.req.RpcRequest;
import com.fc.rpc.common.utils.NettyServerUtils;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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

    private LoadBalance loadBalance = null ;


    public MethodCglibInvocationHandler(Class<?> classes, LoadBalance loadBalance){
        this.classes = classes;
        this.loadBalance = loadBalance;

        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        //构建请求对象
        RpcRequest rpcRequest = RpcRequest.build(classes,method,objects);

        //负载均衡选择任意一个服务器执行
        RPCClientHandler rpcClientHandler = loadBalance.choose();

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
