package com.fc.rpc;

import com.fc.configCentre.annotation.EnableConfigurationCenterServer;
import com.fc.rpc.common.annotation.EnableRPCFeignClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRPCFeignClient(port = "8085",host = "localhost"
        ,packages = "com.fc.rpc.provider.service",enable = true,serverName = "test")
@EnableConfigurationCenterServer(enable = true)
public class ProviderApplication {

    public static void main(String[] args) {

        //改为启动类启动
        SpringApplication.run(ProviderApplication.class,args);
    }
}
