package com.wkk.insight.rpc.register;

import java.util.List;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
public class RedisServiceRegister implements ServiceRegister {
    @Override
    public void init(RegisterConfig config) throws Exception {
        throw new UnsupportedOperationException("待实现");
    }

    @Override
    public void registerService(ServiceMetadata metadata) {
        throw new UnsupportedOperationException("待实现");

    }

    @Override
    public List<ServiceMetadata> fetchServiceList(String serviceName) throws Exception {
        return List.of();
    }
}
