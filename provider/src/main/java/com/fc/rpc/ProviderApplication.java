package com.fc.rpc;

import com.fc.configCentre.annotation.EnableConfigurationCenterClient;
import com.fc.rpc.common.annotation.EnableRPCFeignServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRPCFeignServer(port = "8085",host = "localhost"
        ,packages = "com.fc.rpc.provider.service",enable = true,serverName = "test")
@EnableConfigurationCenterClient(enable = true)
public class ProviderApplication {

    public static void main(String[] args) {

        //改为启动类启动
        SpringApplication.run(ProviderApplication.class,args);
    }
}
