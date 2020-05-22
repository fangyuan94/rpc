package com.fc.rpc.common.mapper;

import com.fc.rpc.common.heartbeat.ClientInstanceInfo;
import com.fc.rpc.common.loadBalancing.FairServerStat;
import lombok.Getter;
import lombok.Setter;
import org.apache.curator.framework.recipes.cache.TreeCache;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 记录每一个服务对应
 */
@Getter
@Setter
public class RPCMapper {

    //服务集群名称
    private String name;

    private  String host;

    //rpc服务端提供port号
    private  String port ;

    //对于服务集群地址
    private final ConcurrentHashMap<String,ClientInstanceInfo> clientInstanceInfos = new ConcurrentHashMap<>(8);

    //记录服务与 rpc之间关系
    private final ConcurrentHashMap<String ,RPCNettyClient>  rpcNettyClients = new ConcurrentHashMap<>(8);

    //记录各个服务最短时间
    private final TreeSet<Long> minResTimeSet = new TreeSet();

    //记录各个时间与服务关系
    private final ConcurrentHashMap<Long, Map<String,FairServerStat>> minResTimeServer = new ConcurrentHashMap<>(8);

    //
    private final ConcurrentHashMap<String,Long> minResTimePath = new ConcurrentHashMap<>(8);
    //当前
    private TreeCache treeCache;

    //监控服务端影响时间服务
    private TreeCache failRespTimeTreeCache;

}
