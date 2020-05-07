package com.fc.rpc.consumer.Controller;

import com.alibaba.fastjson.JSONObject;
import com.fc.rpc.common.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/test")
    public void test(){
        JSONObject param = new JSONObject();
        param.put("test","test");
        System.out.println(userService.selectUserByParam(param));
    }

}
