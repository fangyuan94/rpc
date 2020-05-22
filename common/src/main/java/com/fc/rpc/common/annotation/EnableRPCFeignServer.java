package com.fc.rpc.common.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EnableRPCFeignServerImport.class)
public @interface EnableRPCFeignServer {

    //是否开启EnableRPCFeignProvider
    boolean enable() default true;

    //该服务在zk中名称后续负载均衡时使用
    String serverName() ;

    //对应扫描路径
    String[] packages() default {};
    //host
    String host() default "localhost";
    //端口号
    String port() default "8081";
}
