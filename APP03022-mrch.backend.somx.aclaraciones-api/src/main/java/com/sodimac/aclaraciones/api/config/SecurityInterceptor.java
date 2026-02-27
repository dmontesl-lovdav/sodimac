package com.sodimac.aclaraciones.api.config;

import com.sodimac.aclaraciones.api.security.JwtTokenInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class SecurityInterceptor implements WebMvcConfigurer {

    private final JwtTokenInterceptor interceptor;

    public SecurityInterceptor(JwtTokenInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/error",
                        "/favicon.ico",
                        "/swagger-ui",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api-docs",
                        "/api-docs.yaml",
                        "/api-docs.json");
    }
}
