package com.fc.rpc.common.annotation;

import java.lang.annotation.*;

/**
 *标明哪些服务需要对外提供 并生成对应代理对象注入到spring中
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RPCFeignService {

    String name();
}
