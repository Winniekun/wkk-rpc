package com.wkk.insight.rpc.consumer;

import com.wkk.insight.rpc.register.RegisterConfig;
import lombok.Data;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
@Data
public class ConsumerProperties {

    private Integer workThreadNum = 4;
    private Integer connectTimeoutMs = 3000;
    private Integer requestTimeoutMs = 3000;
    private RegisterConfig registryConfig = new RegisterConfig();

}
