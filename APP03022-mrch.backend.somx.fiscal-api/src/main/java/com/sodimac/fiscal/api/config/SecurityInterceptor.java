package com.sodimac.fiscal.api.config;

import com.sodimac.fiscal.api.security.JwtTokenInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *
 * @author ggalvan
 */
@Component
public class SecurityInterceptor implements WebMvcConfigurer {

    private final JwtTokenInterceptor interceptor;

    @Value("${security.enabled:true}")
    private boolean securityEnabled;

    public SecurityInterceptor(JwtTokenInterceptor interceptors) {
        this.interceptor = interceptors;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (securityEnabled) {
            registry.addInterceptor(interceptor);
        }
    }

}

