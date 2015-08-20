package com.qianmi.hack.network;

/**
 * HTTP返回基类，返回正确结果status>0，错误结果status<0，
 * message为服务器端返回消息，客户端直接显示
 * T data为返回的json对象或数组，自动解析为Java对象
 * errorCodes为服务器端返回的Dubbo接口错误码，逗号分隔，仅做调试用途
 */
public class BaseResponse<T> {
    public int status;
    public String message;
    public String errorCodes;
    public T data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCodes() {
        return errorCodes;
    }

    public void setErrorCodes(String errorCodes) {
        this.errorCodes = errorCodes;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}