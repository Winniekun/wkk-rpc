package com.wkk.insight.rpc.provider;

import com.wkk.insight.rpc.register.RegisterConfig;
import lombok.Data;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
@Data
public class ProviderProperties {

    private String host;

    private int port;

    private int workerThreads = 4;

    private RegisterConfig registerConfig;
}
