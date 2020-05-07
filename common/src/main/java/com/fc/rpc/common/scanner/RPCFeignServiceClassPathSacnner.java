package com.fc.rpc.common.scanner;

import com.fc.rpc.common.annotation.RPCFeign;
import com.fc.rpc.common.annotation.RPCFeignServiceFactoryBean;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

/**
 * @author fangyuan
 * RPCFeignServiceClassPathSacnner 扫描
 */
public class RPCFeignServiceClassPathSacnner extends ClassPathBeanDefinitionScanner {

    private Class<? extends Annotation> annotationClass;

    public RPCFeignServiceClassPathSacnner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    public RPCFeignServiceClassPathSacnner(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
        super(registry, useDefaultFilters);
    }

    public RPCFeignServiceClassPathSacnner(BeanDefinitionRegistry registry, boolean useDefaultFilters, Environment environment) {
        super(registry, useDefaultFilters, environment);
    }

    public RPCFeignServiceClassPathSacnner(BeanDefinitionRegistry registry, boolean useDefaultFilters, Environment environment, ResourceLoader resourceLoader) {
        super(registry, useDefaultFilters, environment, resourceLoader);
    }

    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Assert.notEmpty(basePackages, "packages 不能为空");

        //扫描路径下所对应接口 并实例化
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

        return beanDefinitions;
    }


    /**
     * 注册过滤 标明哪些类可以被进行初始化
     *
     */
    public void registerFilters() {
        //包含对应注解
        addIncludeFilter(new AnnotationTypeFilter(this.annotationClass));

        addExcludeFilter((metadataReader, metadataReaderFactory) -> {
            String className = metadataReader.getClassMetadata().getClassName();
            return className.endsWith("package-info");
        });
    }

}
