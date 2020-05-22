package com.fc.rpc.common.pojo.req;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.UUID;

@Getter
@Setter
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = -6367686790363353703L;
    /**
     *      * 请求对象的ID
     *      
     */
    private String requestId;
    /**
     *类名
     *      
     */
    private Class<?> classes;

    /**
     * 方法名
     *      
     */
    private String methodName;
    /**
     * 参数类型
     *      
     */
    private Class<?>[] parameterTypes;
    /**
     * 入参
     *      
     */
    private Object[] parameters;

    public static RpcRequest build(Class<?> classes, Method method, Object[] objects) {

        RpcRequest rpcRequest = new RpcRequest();

        //可以雪花算法来获取一个id 这里随便使用uuid
        rpcRequest.setRequestId(UUID.randomUUID().toString());
        rpcRequest.setClasses(classes);
        rpcRequest.setMethodName(method.getName());
        rpcRequest.setParameters(objects);
        rpcRequest.setParameterTypes(method.getParameterTypes());
        return rpcRequest;
    }
}
