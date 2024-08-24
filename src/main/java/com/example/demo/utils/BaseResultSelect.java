package com.example.demo.utils;

import lombok.Data;

import java.util.List;

@Data
public class BaseResultSelect {
    private Long count;
    private List<Object> listData;
}
