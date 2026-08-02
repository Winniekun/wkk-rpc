package com.wkk.insight.rpc.register;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 类描述: 默认register 装饰器
 * - 添加本地缓存，避免注册中心挂了
 *
 * @author weikunkun
 */
@Slf4j
public class DefaultServiceRegister implements ServiceRegister {

    /**
     * 注册器的核心实现
     */
    private ServiceRegister delegate;

    private final Map<String, List<ServiceMetadata>> cache = new ConcurrentHashMap<>();


    @Override
    public void init(RegisterConfig config) throws Exception {
        this.delegate = createServiceRegister(config);
        this.delegate.init(config);
    }

    @Override
    public void registerService(ServiceMetadata metadata) {
        log.info("向{} 注册了一个Service{}", delegate.getClass(), metadata.getServiceName());
        delegate.registerService(metadata);
    }

    @Override
    public List<ServiceMetadata> fetchServiceList(String serviceName) throws Exception {
        try {
            List<ServiceMetadata> serviceMetadata = delegate.fetchServiceList(serviceName);
            cache.put(serviceName, serviceMetadata);
            return serviceMetadata;
        } catch (Exception e) {
            log.error("{}注册中心查询{}出现异常", delegate.getClass().getSimpleName(), serviceName, e);
            return cache.getOrDefault(serviceName, new ArrayList<>());
        }
    }

    private ServiceRegister createServiceRegister(RegisterConfig config) {
        if (config.getRegisterType().equals("zookeeper")) {
            return new ZookeeperServiceRegister();
        }
        if (config.getRegisterType().equals("redis")) {
            return new RedisServiceRegister();
        }
        throw new IllegalArgumentException(config.getRegisterType() + "没有实现");
    }
}
