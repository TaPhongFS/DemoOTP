package com.example.demo.utils;

import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author haidv
 * @version 1.0
 */
@Getter
public class ResponseData<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String timestamp;

    private int code;

    private String message;

    private T data;

    public ResponseData() {
        this.code = 0;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        this.message = "Successful!";
    }

    public ResponseData<T> success(T data) {
        this.data = data;
        return this;
    }

    public ResponseData<T> error(int code, String message) {
        this.code = code;
        this.message = message;
        return this;
    }

    public ResponseData<T> error(int code, String message, T data) {
        this.data = data;
        this.code = code;
        this.message = message;
        return this;
    }

    public void setData(T data) {
        this.data = data;
    }
}
