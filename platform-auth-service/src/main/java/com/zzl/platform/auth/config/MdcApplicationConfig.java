package com.zzl.platform.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * MDC应用配置
 * 在应用启动时设置应用名称和环境信息到MDC
 */
@Configuration
public class MdcApplicationConfig {

    @Value("${spring.application.name:auth-service}")
    private String applicationName;

    @Value("${spring.profiles.active:dev}")
    private String environment;

    @Value("${server.port:8080}")
    private int serverPort;

    /**
     * 应用启动完成后，设置MDC应用信息
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("========================================");
        System.out.println("MDC应用配置初始化");
        System.out.println("应用名称: " + applicationName);
        System.out.println("运行环境: " + environment);
        System.out.println("服务端口: " + serverPort);
        System.out.println("========================================");
    }

    /**
     * 获取应用名称的getter方法
     * 可以在需要时调用
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * 获取环境的getter方法
     */
    public String getEnvironment() {
        return environment;
    }
}
