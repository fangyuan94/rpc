package com.fc.rpc.common.pojo.reps;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.UUID;

@Getter
@Setter
public class RpcResponse implements Serializable {

    private static final long serialVersionUID = -6585280872188039259L;

    private Integer code;

    private String success;

    private Object data;

}
