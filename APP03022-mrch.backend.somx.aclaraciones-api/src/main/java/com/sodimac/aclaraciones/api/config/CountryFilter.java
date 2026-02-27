package com.sodimac.aclaraciones.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class CountryFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String country = request.getHeader("X-Country");
        String commerce = request.getHeader("X-Commerce");

        // 🔧 FBC manda DEFAULT → lo normalizamos a SO
        if ("DEFAULT".equalsIgnoreCase(commerce)) {
            commerce = "SO";
        }

        CountryContextHolder.setCountry(
                country != null && !country.isBlank()
                        ? country.toUpperCase()
                        : "MX");

        CountryContextHolder.setCommerce(
                commerce != null && !commerce.isBlank()
                        ? commerce.toUpperCase()
                        : "SO");

        try {
            filterChain.doFilter(request, response);
        } finally {
            CountryContextHolder.clear();
        }
    }
}
