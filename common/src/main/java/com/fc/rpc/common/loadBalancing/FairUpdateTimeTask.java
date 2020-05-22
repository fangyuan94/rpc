package com.fc.rpc.common.loadBalancing;

import com.fc.rpc.common.utils.SerializeUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import java.util.*;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 *这里会存储所有
 * @author fangyuan
 */
@Slf4j
public class FairUpdateTimeTask implements Runnable {

    private CuratorFramework client;

    private String  path;

    private String serverPath;

    //记录上一次值 写入到zk中的值
    private final AtomicLong minResponseTime = new AtomicLong(0);

    //标明
    private final AtomicBoolean failFlag = new AtomicBoolean();

    //桶
    private final List<Long> bucket = new ArrayList<>();

    private final ThreadPoolExecutor bucketExecutor;

    public FairUpdateTimeTask(CuratorFramework client, String  path,String serverPath) {
        this.client = client;
        this.path = path;
        this.serverPath = serverPath;
        this.bucketExecutor = new ThreadPoolExecutor(1, 2,
                0L, TimeUnit.SECONDS, new SynchronousQueue(), (new ThreadFactoryBuilder()).setNameFormat("Fail-BucketExecutor-%d").setDaemon(true).build());
    }

    @Override
    public void run() {

        try {
            failFlag.set(false);

            Long avgTime = this.getAvgTime( this.bucket);
            //清除数据
            this.bucket.clear();
            failFlag.set(true);

            if(avgTime == minResponseTime.get()){
                return;
            }else{
                minResponseTime.set(avgTime);
            }
                    //初始化
            FairServerStat  failServerStat = new FairServerStat();
            failServerStat.setMinResponseTime(avgTime);
            failServerStat.setPath(path);

            log.info("当前服务集群对应Fail状态============>{}",failServerStat);
            String wzServerPath = FairLoadBalance.FAIL_ZK_PATH_PREFIX + this.serverPath;
            Stat stat = client.checkExists().forPath(wzServerPath);
            byte[] failServerStatBytes = SerializeUtil.serialize(failServerStat);
            if(stat == null){
                client.create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(wzServerPath, failServerStatBytes);
            }else{
                client.setData().withVersion(stat.getVersion()).forPath(wzServerPath, failServerStatBytes);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存系统中最短时间
     * @param newResponseTime
     */
    public void setMinResponseTime(final long newResponseTime){
        bucketExecutor.submit(()->{
            if(failFlag.get()){
                bucket.add(newResponseTime);
            }
        });
    }


    /**
     * 计算
     * @param buketData
     * @return
     */
    private  Long getAvgTime(List<Long> buketData){

        if(buketData.size() == 0){
            return FairServerStat.defaultResponseTime;
        }
        //计算平均时间
         Long sumTime = 0L ;

         for(Long time:buketData){
             sumTime+=time;
         }

        return sumTime / buketData.size();
    }



}
