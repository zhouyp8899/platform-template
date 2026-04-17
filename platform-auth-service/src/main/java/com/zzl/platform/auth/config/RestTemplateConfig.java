package com.zzl.platform.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置
 * 配置带有MDC拦截器的RestTemplate，确保服务间调用传递链路追踪信息
 */
@Configuration
public class RestTemplateConfig {

    private final MdcRestTemplateInterceptor mdcRestTemplateInterceptor;

    public RestTemplateConfig(MdcRestTemplateInterceptor mdcRestTemplateInterceptor) {
        this.mdcRestTemplateInterceptor = mdcRestTemplateInterceptor;
    }

    /**
     * 配置带有MDC拦截器的RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        ClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        SimpleClientHttpRequestFactory simpleFactory = (SimpleClientHttpRequestFactory) factory;
        simpleFactory.setConnectTimeout(5000);  // 连接超时5秒
        simpleFactory.setReadTimeout(10000);    // 读取超时10秒

        RestTemplate restTemplate = new RestTemplate(factory);

        // 添加MDC拦截器
        restTemplate.getInterceptors().add(mdcRestTemplateInterceptor);

        return restTemplate;
    }
}
