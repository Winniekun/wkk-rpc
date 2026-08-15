package com.wkk.insight.rpc.loadbalance;

import com.wkk.insight.rpc.exception.RpcException;
import com.wkk.insight.rpc.register.ServiceMetadata;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
public class RandomLoadBalancer implements LoadBalancer {

    @Override
    public ServiceMetadata select(List<ServiceMetadata> metadataList) {
        if (metadataList == null || metadataList.isEmpty()) {
            throw new RpcException("没有可用的 Provider");
        }

        int index = ThreadLocalRandom.current().nextInt(metadataList.size());
        return metadataList.get(index);
    }
}
