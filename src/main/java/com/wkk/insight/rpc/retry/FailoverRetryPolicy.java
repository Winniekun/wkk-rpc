package com.wkk.insight.rpc.retry;

import com.wkk.insight.rpc.exception.RpcException;
import com.wkk.insight.rpc.protocol.Response;
import com.wkk.insight.rpc.register.ServiceMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
public class FailoverRetryPolicy implements RetryPolicy {

    @Override
    public Response doRetry(RetryContext context) throws Exception {
        List<ServiceMetadata> serviceMetadataList = new ArrayList<>(context.getServiceMetadataList());
        serviceMetadataList.remove(context.getFailService());
        if (serviceMetadataList.isEmpty()) {
            throw new RpcException("没有可重试的provider");
        }

        long deadlineNanos = System.nanoTime()
                + TimeUnit.MILLISECONDS.toNanos(context.getMethodTimeoutMs());
        Exception lastException = null;
        while (!serviceMetadataList.isEmpty()) {
            long remainingTimeoutMs = remainingTimeoutMs(deadlineNanos);
            if (remainingTimeoutMs <= 0) {
                throw new TimeoutException("超过方法超时预算");
            }

            ServiceMetadata failoverService = context.getLoadBalancer().select(serviceMetadataList);
            serviceMetadataList.remove(failoverService);
            CompletableFuture<Response> future = context.doRpc(failoverService);
            try {
                long attemptTimeoutMs = Math.min(context.getRequestTimeoutMs(), remainingTimeoutMs);
                return future.get(attemptTimeoutMs, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            } catch (ExecutionException | TimeoutException e) {
                lastException = e;
            }
        }

        RpcException rpcException = new RpcException("所有备选provider重试失败");
        rpcException.initCause(lastException);
        throw rpcException;
    }

    private long remainingTimeoutMs(long deadlineNanos) {
        return TimeUnit.NANOSECONDS.toMillis(deadlineNanos - System.nanoTime());
    }
}
