package com.wkk.insight.rpc.provider;

import com.wkk.insight.rpc.api.Add;
import com.wkk.insight.rpc.register.RegisterConfig;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
public class ProviderApp {

    public static void main(String[] args) {
        RegisterConfig registerConfig = new RegisterConfig();
        registerConfig.setRegisterType("zookeeper");
        registerConfig.setConnectString("127.0.0.1:2181");
        ProviderServer providerServer = new ProviderServer("127.0.0.1", 8887, registerConfig);
        providerServer.register(Add.class, new AddImpl());
        providerServer.start();
    }
}
