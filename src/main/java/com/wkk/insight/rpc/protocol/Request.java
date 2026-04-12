package com.wkk.insight.rpc.protocol;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
@Data
public class Request {

    private final AtomicInteger REQUEST_COUNTER = new AtomicInteger(0);

    private int requestId = REQUEST_COUNTER.getAndIncrement();

    private String serviceName;

    private String methodName;

    private Class<?>[] parameterTypes;

    private Object[] params;

}
