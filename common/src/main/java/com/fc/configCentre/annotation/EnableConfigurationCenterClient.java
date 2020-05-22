package com.fc.configCentre.annotation;


import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EnableConfigurationCenterClientImport.class)
public @interface EnableConfigurationCenterClient {

    //是否开启配置中心 若使用注解代表
    boolean enable() default true;


}


