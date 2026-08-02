package com.wkk.insight.rpc.register;

import lombok.Data;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
public class ZKTest {

    public static void main(String[] args) throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.builder().connectString("127.0.0.1:2181").sessionTimeoutMs(50000).connectionTimeoutMs(3000).retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
        client.start();;

        ServiceDiscovery discovery = ServiceDiscoveryBuilder.builder(Metadata.class).client(client).basePath("/wkk").serializer(new JsonInstanceSerializer<>(Metadata.class)).build();
        discovery.start();;
        Metadata metadata = new Metadata();
        metadata.setAge(1);
        metadata.setName("wkk");

        ServiceInstance<Metadata> intance = ServiceInstance.<Metadata>builder().name("kkk").address("127.0.0.1").port(8888).payload(metadata).build();

        discovery.registerService(intance);

    }


    @Data
    public static class Metadata {
        private String name;

        private Integer age;
    }
}
