package com.example.demo.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * @author haidv
 * @version 1.0
 */
@Slf4j
public class ResponseUtils {

    private ResponseUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> ResponseEntity<ResponseData<T>> success() {
        return ResponseEntity.ok(new ResponseData<>());
    }

    public static <T> ResponseEntity<ResponseData<T>> success(T o) {
        return ResponseEntity.ok(new ResponseData<T>().success(o));
    }

    public static <T> ResponseEntity<ResponseData<T>> error(String message) {
        return ResponseEntity.ok(new ResponseData<T>().error(HttpStatus.BAD_REQUEST.value(), message));
    }
}
