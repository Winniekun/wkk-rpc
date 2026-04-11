package com.wkk.insight.rpc.exception;

import lombok.Data;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
@Data
public class RpcException extends RuntimeException{

    private String errorMessage;

    public RpcException(String errorMessage){
        super(errorMessage);
    }
}
