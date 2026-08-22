package com.wkk.insight.rpc.retry;

import java.util.Map;
import java.util.function.Supplier;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
public class RetryPolicyManager {

    private final Map<String, Supplier<RetryPolicy>> retryPolicyFactories = Map.of(
            "retrySame", RetrySame::new,
            "failover", FailoverRetryPolicy::new,
            "forking", ForkingRetryPolicy::new
    );

    public RetryPolicy getRetryPolicy(String retryPolicy) {
        Supplier<RetryPolicy> factory = retryPolicyFactories.get(retryPolicy);
        return factory == null ? null : factory.get();
    }
}
