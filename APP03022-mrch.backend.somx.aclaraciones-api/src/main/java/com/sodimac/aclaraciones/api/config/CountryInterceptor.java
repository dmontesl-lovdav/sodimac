package com.sodimac.aclaraciones.api.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class CountryInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        String country = request.getHeader("X-Country");
        String commerce = request.getHeader("X-Commerce");

        CountryContextHolder.setCountry(
                country != null && !country.isBlank() ? country.toUpperCase() : "MX");

        CountryContextHolder.setCommerce(
                commerce != null && !commerce.isBlank() ? commerce.toUpperCase() : "SO");

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {
        CountryContextHolder.clear();
    }
}
