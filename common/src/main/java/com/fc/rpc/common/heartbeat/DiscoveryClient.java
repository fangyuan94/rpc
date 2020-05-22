package com.fc.rpc.common.heartbeat;


import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 *服务发现客户端
 * @author fangyuan
 */
public abstract class DiscoveryClient {

    protected final ClientInstanceInfo clientInstanceInfo;

    //定义心跳线程池
    private final ThreadPoolExecutor heartbeatExecutor;

    //定义调度执行器
    private final ScheduledExecutorService scheduler;

    //每多少秒
    private final long renewalIntervalInSecs = 30l;

    //初始时间
    private final long initialDelay = 2l;


    public DiscoveryClient(ClientInstanceInfo clientInstanceInfo) {
        this.clientInstanceInfo = clientInstanceInfo;
        //初始化心跳检测
        this.heartbeatExecutor = new ThreadPoolExecutor(1, 2,
                0L, TimeUnit.SECONDS, new SynchronousQueue(), (new ThreadFactoryBuilder()).setNameFormat("DiscoveryClient-HeartbeatExecutor-%d").setDaemon(true).build());

        this.scheduler = Executors.newScheduledThreadPool(2, (new ThreadFactoryBuilder()).setNameFormat("DiscoveryClient-%d").setDaemon(true).build());
    }

    public void start(){
        this.scheduler.scheduleWithFixedDelay(this.newHeartThread(),initialDelay, renewalIntervalInSecs, TimeUnit.SECONDS);
    }

    public abstract Runnable newHeartThread();

}
