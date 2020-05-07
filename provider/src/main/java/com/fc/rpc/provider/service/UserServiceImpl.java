package com.fc.rpc.provider.service;

import com.alibaba.fastjson.JSONObject;
import com.fc.rpc.common.annotation.RPCFeignService;
import com.fc.rpc.common.service.UserService;

@RPCFeignService(name = "userService")
public class UserServiceImpl implements UserService {

    @Override
    public JSONObject selectUserByParam(JSONObject param) {

        JSONObject jsonObject = new JSONObject();
        System.out.println("=========请求参数为======>"+param.toJSONString());
        jsonObject.put("结果","来了");
        return jsonObject;
    }
}
