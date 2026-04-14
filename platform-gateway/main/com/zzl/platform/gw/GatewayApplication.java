package com.zzl.platform.gw;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.zzl.platform.gw", "com.zzl.platform.common"})
@Slf4j
@EnableDiscoveryClient
public class GatewayApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        String started = """
                -----------------gateway started-----------------
                """;
        log.info(started);
    }
}
