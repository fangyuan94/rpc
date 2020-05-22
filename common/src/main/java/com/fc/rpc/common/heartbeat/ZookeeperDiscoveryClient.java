package com.fc.rpc.common.heartbeat;

import com.fc.rpc.common.utils.SerializeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

/**
 * 使用zk作为服务发现
 * @author fangyuan
 */
@Slf4j
public class ZookeeperDiscoveryClient extends DiscoveryClient {

    public final static String  MUTEX_LOCK_PATH_PREFIX = "/registrationCenter/servers/";

    private final CuratorFramework client;

    public ZookeeperDiscoveryClient(CuratorFramework client,ClientInstanceInfo clientInstanceInfo) {
        super(clientInstanceInfo);
        this.client = client;
    }

    @Override
    public Runnable newHeartThread() {

        return ()-> {
            try{
                String path = MUTEX_LOCK_PATH_PREFIX+clientInstanceInfo.getServerName()+"/"+clientInstanceInfo.getIp()+":"+clientInstanceInfo.getPort();

                Stat stat = client.checkExists().forPath(path);
                byte[] bytes = SerializeUtil.serialize(clientInstanceInfo);

                //如果当前节点已经存在更新版本
                if(stat==null ){
                    //将节点数据变更
                    client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path,bytes);
                }else {
                    client.setData().withVersion(stat.getVersion()).forPath(path,bytes);
                    log.info("{}中状态信息：{}",path,stat);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }


}
