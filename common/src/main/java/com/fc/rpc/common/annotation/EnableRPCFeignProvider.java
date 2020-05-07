package com.fc.rpc.common.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EnableRPCFeignProviderImport.class)
public @interface EnableRPCFeignProvider {

    //是否开启EnableRPCFeignProvider
    boolean enable() default true;
    //对应扫描路径
    String[] packages() default {};
    //host
    String host() default "localhost";
    //端口号
    String port() default "8081";
}
