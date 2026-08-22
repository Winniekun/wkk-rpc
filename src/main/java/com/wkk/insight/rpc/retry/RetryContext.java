package com.wkk.insight.rpc.retry;

import com.wkk.insight.rpc.protocol.Response;
import com.wkk.insight.rpc.loadbalance.LoadBalancer;
import com.wkk.insight.rpc.register.ServiceMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
@Data
@Builder
@AllArgsConstructor
public class RetryContext {

    private final ServiceMetadata failService;
    private final List<ServiceMetadata> serviceMetadataList;
    private final LoadBalancer loadBalancer;
    private long methodTimeoutMs;
    private final long requestTimeoutMs;
    private final Function<ServiceMetadata, CompletableFuture<Response>> doRpcFunction;

    public CompletableFuture<Response> doRpc(ServiceMetadata serviceMetadata) {
        return doRpcFunction.apply(serviceMetadata);
    }
}
