package com.fc.rpc.common.scanner;

import com.fc.rpc.common.annotation.RPCFeign;
import com.fc.rpc.common.annotation.RPCFeignServiceFactoryBean;
import com.fc.rpc.common.factory.RPCFeignProxyFactory;
import com.fc.rpc.common.mapper.RPCMapper;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.util.*;

public class RPCFeignClassPathSacnner extends ClassPathBeanDefinitionScanner {

    private Class<? extends Annotation> annotationClass;

    private List<RPCMapper> rpcMappers    = new ArrayList<>(8);


    public RPCFeignClassPathSacnner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    public RPCFeignClassPathSacnner(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
        super(registry, useDefaultFilters);
    }

    public RPCFeignClassPathSacnner(BeanDefinitionRegistry registry, boolean useDefaultFilters, Environment environment) {
        super(registry, useDefaultFilters, environment);
    }

    public RPCFeignClassPathSacnner(BeanDefinitionRegistry registry, boolean useDefaultFilters, Environment environment, ResourceLoader resourceLoader) {
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
        //
        ScannedGenericBeanDefinition definition;

        for(BeanDefinitionHolder holder:beanDefinitions) {
            definition = (ScannedGenericBeanDefinition) holder.getBeanDefinition();

            Map<String, Object> rpcFeignParam = definition.getMetadata().getAnnotationAttributes(RPCFeign.class.getName());

            RPCMapper rpcMapper = new RPCMapper();

            rpcMapper.setName("" + rpcFeignParam.get("serverName"));

            rpcMapper.setHost("" + rpcFeignParam.get("host"));

            rpcMapper.setPort("" + rpcFeignParam.get("port"));

            rpcMappers.add(rpcMapper);
            //获取beanClassName全名
            String beanClassName = definition.getBeanClassName();
            //设置对应的构造函数参数
            definition.setBeanClass(RPCFeignServiceFactoryBean.class);

            definition.getConstructorArgumentValues().addGenericArgumentValue(beanClassName);
            //根据类型Autowire注入
            definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

            //设置RPCFeignServiceFactoryBean属性参数依赖参数
            definition.getPropertyValues().add("rpcMapper", rpcMapper);

            //创建代理对象工厂类
            definition.getPropertyValues().add("proxyFactory", new RPCFeignProxyFactory());
        }
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

    /**
     * 重写isCandidateComponent 接口 标明哪些类可以被进行初始化
     * @param beanDefinition
     * @return
     */
    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        //代表通过cglib生成代理对象
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }

    public List<RPCMapper> getRpcMappers() {
        return rpcMappers;
    }
}
