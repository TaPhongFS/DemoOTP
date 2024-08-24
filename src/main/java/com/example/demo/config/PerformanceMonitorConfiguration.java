package com.example.demo.config;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        value="time.monitor.enabled",
        havingValue = "true",
        matchIfMissing = true)
@Aspect
public class PerformanceMonitorConfiguration {
    @Pointcut(
            "within(@org.springframework.stereotype.Service *) || within(@org.springframework.web.bind.annotation.RestController *)"
    )
    public void monitorServiceMethods() { }

    @Bean
    public PerformanceMonitorInterceptor performanceMonitorInterceptor() {
        return new PerformanceMonitorInterceptor(false);
    }

    @Bean
    public Advisor performanceMonitorAdvisor() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("com.example.demo.config.PerformanceMonitorConfiguration.monitorServiceMethods()");
        return new DefaultPointcutAdvisor(pointcut, performanceMonitorInterceptor());
    }
}
