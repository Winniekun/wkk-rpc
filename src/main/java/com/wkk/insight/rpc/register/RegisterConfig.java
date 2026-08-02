package com.wkk.insight.rpc.register;

import lombok.Data;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
@Data
public class RegisterConfig {

    private String registerType = "zookeeper";
    private String connectString;
}
