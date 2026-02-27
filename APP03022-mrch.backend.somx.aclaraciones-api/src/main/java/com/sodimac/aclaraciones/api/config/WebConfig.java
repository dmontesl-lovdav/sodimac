// src/main/java/com/sodimac/aclaraciones/api/config/WebConfig.java
package com.sodimac.aclaraciones.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.sodimac.aclaraciones.api.security.JwtTokenInterceptor;
import com.sodimac.aclaraciones.api.security.AuthorAutoRegisterInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final CountryInterceptor countryInterceptor;
    private final JwtTokenInterceptor jwtTokenInterceptor;
    private final AuthorAutoRegisterInterceptor authorAutoRegisterInterceptor;

    public WebConfig(
            CountryInterceptor countryInterceptor,
            JwtTokenInterceptor jwtTokenInterceptor,
            AuthorAutoRegisterInterceptor authorAutoRegisterInterceptor) {

        this.countryInterceptor = countryInterceptor;
        this.jwtTokenInterceptor = jwtTokenInterceptor;
        this.authorAutoRegisterInterceptor = authorAutoRegisterInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 1️⃣ Define país / tenant
        registry.addInterceptor(countryInterceptor);

        // 2️⃣ Lee JWT y crea Session
        registry.addInterceptor(jwtTokenInterceptor);

        // 3️⃣ Garantiza que el author exista
        registry.addInterceptor(authorAutoRegisterInterceptor);
    }
}
