package com.wkk.insight.rpc.retry;

import com.wkk.insight.rpc.exception.RpcException;
import com.wkk.insight.rpc.protocol.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 类描述: 同Provider重试
 *
 * @author weikunkun
 */
@Slf4j
public class RetrySame implements RetryPolicy {

    final int retryMax = 3;

    private final Random random = new Random();

    @Override
    public Response doRetry(RetryContext context) throws Exception {
        long deadlineNanos = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(context.getMethodTimeoutMs());
        int retryCount = 0;
        while (retryCount < retryMax) {
            // 最大延迟1s
            long nextDelay = Math.min(1000, nextDelay(retryCount));
            long remainingTimeoutMs = remainingTimeoutMs(deadlineNanos);
            if (remainingTimeoutMs <= 0 || nextDelay >= remainingTimeoutMs) {
                log.info("剩余时间不够");
                throw new TimeoutException();
            }
            Thread.sleep(nextDelay);
            remainingTimeoutMs = remainingTimeoutMs(deadlineNanos);
            if (remainingTimeoutMs <= 0) {
                log.info("执行指数退避之后，剩余时间不够");
                throw new TimeoutException();
            }
            try {
                log.info("开始重试 retryCount: {}", retryCount);
                CompletableFuture<Response> responseFuture = context.doRpc(context.getFailService());
                return responseFuture.get(remainingTimeoutMs, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.error("重试异常 ", e);
            }
            retryCount++;
        }
        throw new RpcException("重试失败");
    }

    private long remainingTimeoutMs(long deadlineNanos) {
        return TimeUnit.NANOSECONDS.toMillis(deadlineNanos - System.nanoTime());
    }

    private long nextDelay(int retryCount) {
        return 100 * (1 << retryCount) + random.nextInt(0, 100);
    }
}
