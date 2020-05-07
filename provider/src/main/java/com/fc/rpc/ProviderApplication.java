package com.fc.rpc;

import com.fc.rpc.common.annotation.EnableRPCFeignProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRPCFeignProvider(port = "8081",host = "localhost",packages = "com.fc.rpc.provider.service",enable = true)
public class ProviderApplication {

    public static void main(String[] args) {

        //改为启动类启动
        SpringApplication.run(ProviderApplication.class,args);
    }
}
