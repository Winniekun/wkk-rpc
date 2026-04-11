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
        Add consumer = new Consumer();
//        int add = consumer.add(1, 2);
//        System.out.println(add);
        System.out.println(consumer.add(1100, 2));
    }
}
