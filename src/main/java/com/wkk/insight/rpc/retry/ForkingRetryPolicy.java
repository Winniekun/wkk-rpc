package com.wkk.insight.rpc.retry;

import com.wkk.insight.rpc.exception.RpcException;
import com.wkk.insight.rpc.protocol.Response;
import com.wkk.insight.rpc.register.ServiceMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
public class ForkingRetryPolicy implements RetryPolicy {

    @Override
    public Response doRetry(RetryContext context) throws Exception {
        List<ServiceMetadata> serviceMetadataList = new ArrayList<>(context.getServiceMetadataList());
        serviceMetadataList.remove(context.getFailService());
        if (serviceMetadataList.isEmpty()) {
            throw new RpcException("没有可重试的provider");
        }
        CompletableFuture<Response> firstSuccess = new CompletableFuture<>();
        AtomicInteger failureCount = new AtomicInteger();
        AtomicReference<Throwable> lastFailure = new AtomicReference<>();
        for (ServiceMetadata serviceMetadata : serviceMetadataList) {
            context.doRpc(serviceMetadata).whenComplete((response, throwable) -> {
                if (throwable == null) {
                    firstSuccess.complete(response);
                    return;
                }

                lastFailure.set(throwable);
                if (failureCount.incrementAndGet() == serviceMetadataList.size()) {
                    firstSuccess.completeExceptionally(
                            new CompletionException("所有备选provider请求失败", lastFailure.get()));
                }
            });
        }

        return firstSuccess.get(Math.min(context.getRequestTimeoutMs(), context.getMethodTimeoutMs()),
                TimeUnit.MILLISECONDS);
    }
}
