package com.wkk.insight.rpc.provider;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
public class ProviderApp {

    public static void main(String[] args) {
        ProviderServer providerServer = new ProviderServer(8888);
        providerServer.start();
    }
}
