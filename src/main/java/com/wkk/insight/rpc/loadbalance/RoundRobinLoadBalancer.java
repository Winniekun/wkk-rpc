package com.wkk.insight.rpc.loadbalance;

import com.wkk.insight.rpc.exception.RpcException;
import com.wkk.insight.rpc.register.ServiceMetadata;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
public class RoundRobinLoadBalancer implements LoadBalancer {

    private final AtomicInteger sequence = new AtomicInteger();

    @Override
    public ServiceMetadata select(List<ServiceMetadata> metadataList) {
        if (metadataList == null || metadataList.isEmpty()) {
            throw new RpcException("没有可用的 Provider");
        }

        if (metadataList.size() == 1) {
            return metadataList.get(0);
        }

        int index = Math.floorMod(
                sequence.getAndIncrement(),
                metadataList.size()
        );

        return metadataList.get(index);
    }
}
