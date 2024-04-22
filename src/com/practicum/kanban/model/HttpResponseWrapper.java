package com.practicum.kanban.model;

public class HttpResponseWrapper {
    private int code;
    private String body;

    public HttpResponseWrapper(int code, String body) {
        this.code = code;
        this.body = body;
    }

    public int getCode() {
        return code;
    }

    public String getBody() {
        return body;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
