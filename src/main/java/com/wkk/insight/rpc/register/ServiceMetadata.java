package com.wkk.insight.rpc.register;

import lombok.Data;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
@Data
public class ServiceMetadata {

    private String host;
    private int port;
    private String serviceName;
}
