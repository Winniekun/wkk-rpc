package com.wkk.insight.rpc.provider;

import com.wkk.insight.rpc.api.Add;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
public class ProviderApp {

    public static void main(String[] args) {
        ProviderServer providerServer = new ProviderServer(8888);
        providerServer.register(Add.class, new AddImpl());
        providerServer.start();
    }
}
