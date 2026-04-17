package com.zzl.platform.auth;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 认证服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.zzl.platform")
@EnableDiscoveryClient
@MapperScan("com.zzl.platform.auth.mapper")
@Slf4j
public class AuthApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        String started = """
                -----------------auth-service started-----------------
                """;
        log.warn(started);
    }
}
