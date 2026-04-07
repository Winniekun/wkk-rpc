package com.wkk.insight.rpc;

import java.util.concurrent.ExecutionException;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
public class Main {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Consumer consumer = new Consumer();
        System.out.println(consumer.add(1, 3));
    }
}
