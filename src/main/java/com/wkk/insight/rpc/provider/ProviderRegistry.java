package com.wkk.insight.rpc.provider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
public class ProviderRegistry {

    private final Map<String, Invocation<?>> serviceInstanceMap = new ConcurrentHashMap<>();

    public <I> void register(Class<I> interfaceClass, I serviceInstance) {
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException("注册的类型必须是一个接口");
        }
        // 已注册过 不注册
        if (serviceInstanceMap.putIfAbsent(interfaceClass.getName(), new Invocation<>(interfaceClass, serviceInstance)) != null) {
            throw new IllegalArgumentException(interfaceClass.getName() + "重复注册了!");
        }

    }

    public Invocation<?> findService(String serviceName) {
        return serviceInstanceMap.get(serviceName);
    }

    public static class Invocation<I> {

        final I serviceInstance;

        final Class<I> interfaceClass;

        public Invocation(Class<I> interfaceClass, I serviceInstance) {
            this.serviceInstance = serviceInstance;
            this.interfaceClass = interfaceClass;
        }

        public Object invoke(String methodName, Class<?>[] paramClass, Object[] params) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            // 使用接口类获取方法 避免直接使用实例 导致访问错私有方法
            Method invokeMethod = interfaceClass.getDeclaredMethod(methodName, paramClass);
            return invokeMethod.invoke(serviceInstance, params);

        }

    }

}
