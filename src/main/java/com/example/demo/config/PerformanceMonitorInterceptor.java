package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.springframework.aop.interceptor.AbstractMonitoringInterceptor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PerformanceMonitorInterceptor extends AbstractMonitoringInterceptor {

    public PerformanceMonitorInterceptor() {
    }

    public PerformanceMonitorInterceptor(boolean useDynamicLogger) {
        setUseDynamicLogger(useDynamicLogger);
    }

    @Override
    protected Object invokeUnderTrace(MethodInvocation invocation, Log log)
            throws Throwable {

        String name = createInvocationTraceName(invocation);
        long start = System.currentTimeMillis();
        try {
            return invocation.proceed();
        }
        finally {
            long end = System.currentTimeMillis();
            long time = end - start;
            log.info(String.format("Method %s execution: %d ms", name, time));
        }
    }
}
