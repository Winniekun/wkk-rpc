package com.wkk.insight.rpc.consumer;

import com.wkk.insight.rpc.api.Add;
import com.wkk.insight.rpc.register.RegisterConfig;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
public class ConsumerApp {

    public static void main(String[] args) throws Exception {
        RegisterConfig registryConfig = new RegisterConfig();
        registryConfig.setRegisterType("zookeeper");
        registryConfig.setConnectString("127.0.0.1:2181");
        ConsumerProperties consumerProperties = new ConsumerProperties();
        consumerProperties.setRegistryConfig(registryConfig);
        ConsumerProxyFactory consumerProxyFactory = new ConsumerProxyFactory(consumerProperties);
        Add addConsumer = consumerProxyFactory.createConsumerProxy(Add.class);
        System.out.println(addConsumer.add(1, 2));
        System.out.println(addConsumer.add(1, 2));

//        while (true){
//            try {
//                System.out.println(addConsumer.add(1, 2));
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//            Thread.sleep(1000);
//        }
    }
}
