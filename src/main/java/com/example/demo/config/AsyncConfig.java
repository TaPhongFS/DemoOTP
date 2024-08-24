package com.example.demo.config;

import com.example.demo.utils.MdcForkJoinPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ForkJoinPool;

@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean(name = "mdcForkJoinPool")
    public MdcForkJoinPool mdcForkJoinPool() {
        return new MdcForkJoinPool(4, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true);
    }

}
