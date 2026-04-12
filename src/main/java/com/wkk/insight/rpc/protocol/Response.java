package com.wkk.insight.rpc.protocol;

import lombok.Data;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
@Data
public class Response {

    private int requestId;

    private Object result;

    private int code;

    private String errorMessage;

    public static Response success(Object result, int requestId) {
        Response response = new Response();
        response.setResult(result);
        response.setCode(200);
        response.setRequestId(requestId);
        return response;
    }

    public static Response fail(String errorMsg, int requestId) {
        Response response = new Response();
        response.setCode(400);
        response.setRequestId(requestId);
        response.setErrorMessage(errorMsg);
        return response;
    }
}
