package com.example.demo.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class BaseException extends RuntimeException {
    HttpStatus status;
    String message;

    public BaseException(String message) {
        super();
        this.status = HttpStatus.BAD_REQUEST;
        this.message = message;
    }
}
