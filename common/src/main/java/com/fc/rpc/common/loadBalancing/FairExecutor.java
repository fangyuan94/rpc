package com.fc.rpc.common.loadBalancing;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.curator.framework.CuratorFramework;

import java.util.concurrent.*;

/**
 * 更新zk执行器
 * @author fangyuan
 */
public class FairExecutor {

    private CuratorFramework client;

    private String path;

    //定义updateMinRespTime
    private final ThreadPoolExecutor minRespTimeExecutor;

    //定义调度执行器
    private final ScheduledExecutorService scheduler;

    //每多少秒
    private final long renewalIntervalInSecs = 5l;

    //初始时间
    private final long initialDelay = 5l;

    private FairUpdateTimeTask failUpdateTimeTask;

    public FairExecutor(CuratorFramework client, String  path, FairUpdateTimeTask failUpdateTimeTask) {
        //初始化心跳检测
        this.minRespTimeExecutor = new ThreadPoolExecutor(1, 2,
                0L, TimeUnit.SECONDS, new SynchronousQueue(), (new ThreadFactoryBuilder()).setNameFormat("DiscoveryClient-HeartbeatExecutor-%d").setDaemon(true).build());

        this.scheduler = Executors.newScheduledThreadPool(2, (new ThreadFactoryBuilder()).setNameFormat("DiscoveryClient-%d").setDaemon(true).build());
        this.client = client;
        this.path = path;
        this.failUpdateTimeTask = failUpdateTimeTask;
    }

    public void start(){
        this.scheduler.scheduleWithFixedDelay(failUpdateTimeTask,initialDelay, renewalIntervalInSecs, TimeUnit.SECONDS);
    }

}
