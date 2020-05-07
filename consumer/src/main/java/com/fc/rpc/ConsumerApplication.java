package com.fc.rpc;

import com.fc.rpc.common.annotation.EnableRPCFeignServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRPCFeignServer(packages = "com.fc.rpc.common.service",enable = true)
public class ConsumerApplication {
    public static void main(String[] args) {

        //改为启动类启动
        SpringApplication.run(ConsumerApplication.class,args);
    }
}
