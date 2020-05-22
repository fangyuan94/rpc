package com.fc.rpc.common.service;

import com.alibaba.fastjson.JSONObject;
import com.fc.rpc.common.annotation.RPCFeign;
import org.springframework.stereotype.Component;

/**
 * @author fangyuan
 * 对外提供user服务
 */
@RPCFeign(serverName = "test")
public interface UserService {

    /**
     * 条件查询用户信息
     * @param param
     * @return
     */
    public JSONObject selectUserByParam(JSONObject param);
}
