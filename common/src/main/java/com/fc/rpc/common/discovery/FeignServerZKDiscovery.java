package com.fc.rpc.common.discovery;

import com.fc.rpc.common.heartbeat.ClientInstanceInfo;
import com.fc.rpc.common.loadBalancing.FairLoadBalance;
import com.fc.rpc.common.loadBalancing.FairServerStat;
import com.fc.rpc.common.mapper.RPCMapper;
import com.fc.rpc.common.mapper.RPCNettyClient;
import com.fc.rpc.common.utils.NettyServerUtils;
import com.fc.rpc.common.utils.SerializeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import org.apache.curator.framework.recipes.cache.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author fangyuan
 */
@Slf4j
public class FeignServerZKDiscovery {

    private final CuratorFramework client;

    private List<RPCMapper> rpcMappers;

    public final static String MUTEX_LOCK_PATH_PREFIX = "/registrationCenter/servers/";

    //是否拉取zk中服务服务列表是否完成
    public FeignServerZKDiscovery(CuratorFramework client, List<RPCMapper> rpcMappers) {
        this.client = client;
        this.rpcMappers = rpcMappers;
    }


    public void start() {

        rpcMappers.forEach(rpcMapper -> {

            String serverName = rpcMapper.getName();

            if (serverName != null && !serverName.isEmpty()) {
                //创建watch 监控

                try {
                    String path = MUTEX_LOCK_PATH_PREFIX+serverName;

                    //监测服务节点 变化
                    TreeCache treeCache = TreeCache.newBuilder(client,path).setCacheData(true).build();
                    treeCache.getListenable().addListener(new PullServerListenable(rpcMapper,path));
                    treeCache.start();
                    rpcMapper.setTreeCache(treeCache);

                    //监测 fair节点变化

                    String fairPath = FairLoadBalance.FAIL_ZK_PATH_PREFIX+serverName;

                    TreeCache failRespTimeTreeCache = TreeCache.newBuilder(client, fairPath).setCacheData(true).build();
                    failRespTimeTreeCache.getListenable().addListener(new FairLoadBalanceListenable(rpcMapper,fairPath));
                    failRespTimeTreeCache.start();
                    rpcMapper.setFailRespTimeTreeCache(failRespTimeTreeCache);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     *
     * 定期拉取一个服务
     */
    class PullServerListenable implements TreeCacheListener{

        private RPCMapper rpcMapper;

        private String parentPath;

        public PullServerListenable(RPCMapper rpcMapper,String parentPath) {
            this.rpcMapper = rpcMapper;
            this.parentPath = parentPath;
        }

        @Override
        public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {

            TreeCacheEvent.Type type = event.getType();

            if(type == TreeCacheEvent.Type.INITIALIZED){
                log.info("当前监听已经初始化完成了==================>：{}",type);
                return;
            }
            if(type == TreeCacheEvent.Type.CONNECTION_RECONNECTED
                    || type == TreeCacheEvent.Type.CONNECTION_LOST || type == TreeCacheEvent.Type.CONNECTION_SUSPENDED){
                log.info("当前监听收到的的事件为：{}",type);
                return;
            }

            ChildData childData = event.getData();

            String path = childData.getPath();
            //发生该节点变动时 触发

            if ( type== TreeCacheEvent.Type.NODE_UPDATED) {

                log.info("{}子节点发生变更了=====================>",path);

            }else if(type == TreeCacheEvent.Type.NODE_REMOVED) {
                //删除事件
                log.info(path+"发生节点移除事件===============");
                if(!this.parentPath.equals(path) && rpcMapper.getClientInstanceInfos().containsKey(path)) {
                    //移除旧节点
                    this.rmNode(rpcMapper,path);
                }
            }else if(type == TreeCacheEvent.Type.NODE_ADDED){
                log.info(path+"发生节点新增事件===============");
                //如果是父节点
                if(!this.parentPath.equals(path)){
                    //发生子节点新增事件 初始化对应服务
                    ClientInstanceInfo clientInstanceInfo = SerializeUtil.deserialize(childData.getData(),ClientInstanceInfo.class);
                    this.addNewNode(rpcMapper,path,clientInstanceInfo);
                }
            }
        }

        /**
         * 移除旧数据并 关闭nettyclient连接
         * @param rpcMapper
         * @param path
         */
        private void rmNode(RPCMapper rpcMapper,String path) {
            ClientInstanceInfo clientInstanceInfo = rpcMapper.getClientInstanceInfos().get(path);
            log.info("=========={}:{}服务下线======",clientInstanceInfo.getIp(),clientInstanceInfo.getPort());
            rpcMapper.getClientInstanceInfos().remove(path);

            //关闭对应的netty服务
            RPCNettyClient rpcNettyClient = rpcMapper.getRpcNettyClients().get(path);
            NettyServerUtils.stopNettyServer(rpcNettyClient);
            rpcMapper.getRpcNettyClients().remove(path);
        }

        /**
         * 写入节点
         * @param rpcMapper
         * @param path
         * @param clientInstanceInfo
         */
        private void addNewNode(RPCMapper rpcMapper,String path,ClientInstanceInfo clientInstanceInfo){

            ConcurrentHashMap<String, ClientInstanceInfo> clientInstanceInfos = Optional.ofNullable(rpcMapper.getClientInstanceInfos()).orElse(new ConcurrentHashMap<>(8));

            log.info("=========={}:{}服务上线======",clientInstanceInfo.getIp(),clientInstanceInfo.getPort());

            clientInstanceInfos.put(path,clientInstanceInfo);
            //新建netty服务
            ConcurrentHashMap<String, RPCNettyClient> rpcNettyClients = rpcMapper.getRpcNettyClients();
            if(!rpcNettyClients.containsKey(path)){

                RPCNettyClient rpcNettyClient = NettyServerUtils.initNettyClient(clientInstanceInfo.getIp(), clientInstanceInfo.getPort());
                rpcNettyClients.put(path,rpcNettyClient);
            }
        }
    }

    /**
     *
     * 监控各服务节点负载变化
     */
    class FairLoadBalanceListenable implements TreeCacheListener{

        private RPCMapper rpcMapper;

        private String parentPath;

        public FairLoadBalanceListenable(RPCMapper rpcMapper,String parentPath) {
            this.rpcMapper = rpcMapper;
            this.parentPath = parentPath;
        }

        @Override
        public void childEvent(CuratorFramework client, TreeCacheEvent event) {

            TreeCacheEvent.Type type = event.getType();

            if(type == TreeCacheEvent.Type.INITIALIZED){
                log.info("当前FairLoadBalanceListenable监听已经初始化完成了==================>：{}",type);
                return;
            }
            if(type == TreeCacheEvent.Type.CONNECTION_RECONNECTED
                    || type == TreeCacheEvent.Type.CONNECTION_LOST || type == TreeCacheEvent.Type.CONNECTION_SUSPENDED){
                log.info("当前FairLoadBalanceListenable监听收到的的事件为：{}",type);
                return;
            }

            ChildData childData = event.getData();

            String path = childData.getPath();
            //发生该节点变动时 触发

            if ( type== TreeCacheEvent.Type.NODE_UPDATED) {
                log.info("{}FairLoadBalanceListenable子节点发生变更了=====================>",path);
                //判断是哪种类型的变更 是父节点还是子节点
                if(!this.parentPath.equals(path)){

                    FairServerStat fairServerStat = SerializeUtil.deserialize(childData.getData(), FairServerStat.class);
                    Long minResponseTime = fairServerStat.getMinResponseTime();
                    rpcMapper.getMinResTimeSet().add(minResponseTime);
                    //获取旧值
                    ConcurrentHashMap<String,Long> minResTimePath = rpcMapper.getMinResTimePath();
                    Long minResponseTimeOld = minResTimePath.get(path);
                    if(minResponseTime != minResponseTimeOld ){
                        //变更
                        //写入新值
                        minResTimePath.put(path,minResponseTime);
                        Map<String, FairServerStat> map = rpcMapper.getMinResTimeServer().get(minResponseTimeOld);
                        if(map.size()==1 && map.containsKey(path)){
                            rpcMapper.getMinResTimeSet().remove(minResponseTimeOld);
                            rpcMapper.getMinResTimeServer().remove(minResponseTimeOld);
                        }else {
                            map.remove(path);
                        }

                        //插入新值
                        Map<String, FairServerStat> mapNew = Optional.ofNullable(rpcMapper.getMinResTimeServer().get(minResponseTime)).orElse(new HashMap<>());
                        mapNew.put(path,fairServerStat);
                        rpcMapper.getMinResTimeServer().put(minResponseTime,mapNew);
                    }

                }

            }else if(type == TreeCacheEvent.Type.NODE_REMOVED) {
                if(!this.parentPath.equals(path)) {
                    //删除服务中缓存数据
                    Long score = rpcMapper.getMinResTimePath().get(path);
                    rpcMapper.getMinResTimeSet().remove(score);
                    rpcMapper.getMinResTimeServer().get(score).remove(score);
                }

            }else if(type == TreeCacheEvent.Type.NODE_ADDED){
                //子节点添加
                if(!this.parentPath.equals(path)) {
                    FairServerStat fairServerStat = SerializeUtil.deserialize(childData.getData(), FairServerStat.class);
                    Long minResponseTime = fairServerStat.getMinResponseTime();

                    rpcMapper.getMinResTimeSet().add(minResponseTime);

                    ConcurrentHashMap<Long, Map<String, FairServerStat>> resTimeServer = rpcMapper.getMinResTimeServer();

                    Map<String, FairServerStat> map =  Optional.ofNullable(resTimeServer.get(minResponseTime)).orElse(new HashMap<>());
                    rpcMapper.getMinResTimePath().put(path,minResponseTime);
                    map.put(path,fairServerStat);
                    resTimeServer.put(minResponseTime,map);
                    log.info(path+"FairLoadBalanceListenable发生节点新增事件===============");
                }
            }
        }

    }



}
