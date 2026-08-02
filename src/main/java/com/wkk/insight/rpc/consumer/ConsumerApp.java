package com.wkk.insight.rpc.consumer;

import com.wkk.insight.rpc.api.Add;
import com.wkk.insight.rpc.register.RegisterConfig;

import java.util.concurrent.ExecutionException;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
public class ConsumerApp {

    public static void main(String[] args) throws Exception {
        RegisterConfig registerConfig = new RegisterConfig();
        registerConfig.setRegisterType("zookeeper");
        registerConfig.setConnectString("127.0.0.1:2181");
        ConsumerProxyFactory consumerProxyFactory = new ConsumerProxyFactory(registerConfig);
        Add addConsumer = consumerProxyFactory.getConsumerProxy(Add.class);
        while (true){
            try {
                System.out.println(addConsumer.add(1, 2));
            }catch (Exception e){
                e.printStackTrace();
            }
            Thread.sleep(1000);
        }
    }
}
