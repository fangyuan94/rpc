package com.fc.rpc.common.annotation;

import java.lang.annotation.*;

/**
 * @author
 * 标示哪些接口feign调用 客户端使用
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RPCFeign {

    //rpc提供host
    String host();

    //rpc服务端提供port号
    String port() ;

}
