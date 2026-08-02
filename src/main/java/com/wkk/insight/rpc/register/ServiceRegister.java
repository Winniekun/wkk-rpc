package com.wkk.insight.rpc.register;

import java.util.List;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
public interface ServiceRegister {

    void init(RegisterConfig config) throws Exception;

    void registerService(ServiceMetadata metadata);

    List<ServiceMetadata> fetchServiceList(String serviceName) throws Exception;
}
