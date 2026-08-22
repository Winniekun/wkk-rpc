package com.wkk.insight.rpc.retry;

import com.wkk.insight.rpc.protocol.Response;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
public interface RetryPolicy {

    Response doRetry(RetryContext context) throws Exception;
}
