package com.fc.configCentre.annotation;


import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EnableConfigurationCenterServerImport.class)
public @interface EnableConfigurationCenterServer {

    //是否开启配置中心 若使用注解代表
    boolean enable() default true;


}


