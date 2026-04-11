package com.wkk.insight.rpc.provider;

import com.wkk.insight.rpc.api.Add;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
public class AddImpl implements Add {
    @Override
    public int add(int a, int b) {
        return a + b;
    }
}
