package com.fc.rpc.common.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 扫描路径下对应的服务
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EnableRPCFeignClientImport.class)
public @interface EnableRPCFeignClient {
    //是否开启RPCFeignServer
    boolean enable() default true;

    //对应扫描路径
    String[] packages() default {};
}
