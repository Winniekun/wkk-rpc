package com.wkk.insight.rpc.protocol;

import lombok.Data;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
@Data
public class Response {

    private Object result;

    private int code;

    private String errorMessage;

    public static Response success(Object result) {
        Response response = new Response();
        response.setResult(result);
        response.setCode(200);
        return response;
    }

    public static Response fail(String errorMsg) {
        Response response = new Response();
        response.setCode(400);
        response.setErrorMessage(errorMsg);
        return response;
    }
}
