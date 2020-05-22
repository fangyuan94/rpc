package com.fc.rpc.common.mapper;

import com.fc.rpc.common.handler.RPCClientHandler;
import io.netty.channel.EventLoopGroup;
import lombok.Builder;
import lombok.Data;

/**
 * rpc netty客户端
 * @author
 */
@Data
@Builder
public class RPCNettyClient {

    private RPCClientHandler rpcClientHandler;

    private EventLoopGroup eventLoopGroup;

}
