package com.fc.rpc.common.heartbeat;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 客户端信息
 * @author
 *
 */
@Getter
@Setter
@Builder
public class ClientInstanceInfo implements Serializable {

    private static final long serialVersionUID = 8016367904870443687L;

    private String serverName;

    private String ip;

    private Integer port;

    @Override
    public String toString() {
        return "ClientInstanceInfo{" +
                "serverName='" + serverName + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}
