package com.wkk.insight.rpc.provider;

import com.wkk.insight.rpc.api.Add;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
@Slf4j
public class AddTimeountImpl implements Add  {
    @Override
    public int add(int a, int b) {
        long start = System.currentTimeMillis();
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(4));
        log.info("超时 响应: " + (System.currentTimeMillis() - start));
        return a + b;
    }
}
