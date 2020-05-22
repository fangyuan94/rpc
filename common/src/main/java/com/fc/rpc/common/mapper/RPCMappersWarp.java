package com.fc.rpc.common.mapper;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * RPCMappersWarp 的包装类
 * @author fangyuan
 */
@Getter
@Setter
public class RPCMappersWarp {

    private List<RPCMapper> rpcMappers = new ArrayList<>();
}
