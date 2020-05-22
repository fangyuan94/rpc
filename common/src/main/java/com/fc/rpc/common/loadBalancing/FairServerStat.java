package com.fc.rpc.common.loadBalancing;

import com.fc.rpc.common.heartbeat.ClientInstanceInfo;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 保存该负载均衡状态
 * @author fangyuan
 */
@Getter
@Setter

public class FairServerStat implements Serializable {

    private static final long serialVersionUID = 9020738775916648217L;

    public final static Long defaultResponseTime = 9999999L;

    private String path;

    //记录当前服务中最小时间 默认为最大
    private Long minResponseTime = defaultResponseTime;

    @Override
    public String toString() {
        return "FairServerStat{" +
                "path='" + path + '\'' +
                ", minResponseTime=" + minResponseTime +
                '}';
    }
}
