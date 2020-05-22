package com.fc.rpc.common.factory;

import com.fc.rpc.common.handler.MethodCglibInvocationHandler;
import com.fc.rpc.common.handler.RPCClientHandler;
import com.fc.rpc.common.heartbeat.ClientInstanceInfo;
import com.fc.rpc.common.loadBalancing.FairLoadBalance;
import com.fc.rpc.common.loadBalancing.LoadBalance;
import com.fc.rpc.common.loadBalancing.LocalLoadBalance;
import com.fc.rpc.common.mapper.RPCMapper;
import com.fc.rpc.common.mapper.RPCNettyClient;
import com.fc.rpc.common.utils.NettyServerUtils;
import net.sf.cglib.proxy.Enhancer;

import java.util.concurrent.ConcurrentHashMap;


/**
 *
 * @author fangyuan
 * 代理对象工厂：生成代理对象的
 */
public class RPCFeignProxyFactory {

    private LoadBalance loadBalance;

    /**
     * 使用cglib动态代理生成代理对象
     * @return
     */
    public  Object getCglibProxy(Class<?> classes, RPCMapper rpcMapper) {
        //构建 客户端
        initNettyClient(rpcMapper);
        return  Enhancer.create(classes, new MethodCglibInvocationHandler(classes,loadBalance));
    }

    //使用netty连接
    private void initNettyClient( RPCMapper rpcMapper){

        String host = rpcMapper.getHost();

        String port = rpcMapper.getPort();

        //使用ip地址访问
        if(host == null ||host.isEmpty() || port == null ||port.isEmpty()){

//            ConcurrentHashMap<String, ClientInstanceInfo> clientInstanceInfos = rpcMapper.getClientInstanceInfos();
//
//            ConcurrentHashMap<String, RPCNettyClient> rpcNettyClients   = new ConcurrentHashMap<>(clientInstanceInfos.size());
//            //构建服务
//            clientInstanceInfos.forEach((k,v)->{
//                rpcNettyClients.put(k,NettyServerUtils.initNettyClient(v.getIp(),v.getPort()));
//            });
//
//            rpcMapper.setRpcNettyClients(rpcNettyClients);

            //获取所有节点信息 构建netty服务
            loadBalance = new FairLoadBalance(rpcMapper);
        }else{
            RPCNettyClient rpcNettyClient = NettyServerUtils.initNettyClient(host,Integer.valueOf(port));
            loadBalance = new LocalLoadBalance(rpcNettyClient);
        }
    }
}
