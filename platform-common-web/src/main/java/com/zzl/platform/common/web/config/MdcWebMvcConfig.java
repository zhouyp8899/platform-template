package com.zzl.platform.common.web.config;

import com.zzl.platform.common.web.interceptor.MdcInterceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MDC Web MVC配置
 * 注册MDC拦截器，确保所有HTTP请求都经过链路追踪处理
 */
@Configuration
public class MdcWebMvcConfig implements WebMvcConfigurer {

    private final MdcInterceptor mdcInterceptor;

    public MdcWebMvcConfig(MdcInterceptor mdcInterceptor) {
        this.mdcInterceptor = mdcInterceptor;
    }

    /**
     * 注册MDC拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(mdcInterceptor)
                .addPathPatterns("/**")  // 拦截所有路径
                .excludePathPatterns("/actuator/**",  // 排除监控端点
                        "/error",            // 排除错误页面
                        "/favicon.ico");     // 排除favicon请求
    }
}
