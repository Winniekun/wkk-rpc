package com.wkk.insight.rpc.loadbalance;

import com.wkk.insight.rpc.register.ServiceMetadata;

import java.util.List;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
public interface LoadBalancer {

    ServiceMetadata select(List<ServiceMetadata> metadataList);

}
