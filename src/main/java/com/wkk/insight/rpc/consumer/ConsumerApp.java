package com.wkk.insight.rpc.consumer;

import com.wkk.insight.rpc.api.Add;

import java.util.concurrent.ExecutionException;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
public class ConsumerApp {

    public static void main(String[] args) {
        ConsumerProxyFactory consumerProxyFactory = new ConsumerProxyFactory();
        Add consumerProxy = consumerProxyFactory.getConsumerProxy(Add.class);
        System.out.println(consumerProxy.add(1, 2));
        System.out.println(consumerProxy.add(1, 2));
        System.out.println(consumerProxy.add(1100, 2));
        System.out.println(consumerProxy.add(1100, 2));
        System.out.println(consumerProxy.add(1100, 2));
        System.out.println(consumerProxy.add(1100, 2));
        System.out.println(consumerProxy.add(1100, 2));
    }
}
