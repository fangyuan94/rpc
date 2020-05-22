package com.fc.rpc.common.loadBalancing;

import com.fc.rpc.common.handler.RPCClientHandler;
import com.fc.rpc.common.mapper.RPCNettyClient;

/**
 * 默认本地
 * @author fangyuan
 */
public class LocalLoadBalance implements LoadBalance {

    private RPCNettyClient rpcNettyClient;

    public LocalLoadBalance(RPCNettyClient rpcNettyClient) {
        this.rpcNettyClient = rpcNettyClient;
    }

    @Override
    public RPCClientHandler choose() {
        return rpcNettyClient.getRpcClientHandler();
    }
}
