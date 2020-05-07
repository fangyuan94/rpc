package com.fc.rpc.common.pojo.reps;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.UUID;

@Getter
@Setter
public class RpcResponse {

    private Integer code;

    private String success;

    private Object data;

}
