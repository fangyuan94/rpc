package com.fc.configCentre.annotation;


import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 标为一个配置中心服务端
 * @author fangyuan
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EnableConfigurationCenterServerImport.class)
public @interface EnableConfigurationCenterServer {

    //是否开启配置中心 若使用注解代表
    boolean enable() default true;
}


