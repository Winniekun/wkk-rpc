package com.wkk.insight.rpc.protocol;

import lombok.Data;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
@Data
public class Request {

    private String serviceName;

    private String methodName;

    private String[] parameterTypes;

    private Object[] params;

}
