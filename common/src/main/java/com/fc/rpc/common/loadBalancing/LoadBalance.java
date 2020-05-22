package com.fc.rpc.common.loadBalancing;

import com.fc.rpc.common.handler.RPCClientHandler;
import com.fc.rpc.common.heartbeat.ClientInstanceInfo;
import com.fc.rpc.common.mapper.RPCMapper;

/**
 * 负载均衡器
 * @author fangyuan
 */
public interface LoadBalance {

    /**
     *
     * @return
     */
    public RPCClientHandler  choose();
}
