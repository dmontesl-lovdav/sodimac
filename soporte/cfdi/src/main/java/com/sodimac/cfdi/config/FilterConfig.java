package com.sodimac.cfdi.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sodimac.cfdi.filter.AnonymousUserFilter;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<AnonymousUserFilter> loggingFilter() {
        FilterRegistrationBean<AnonymousUserFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new AnonymousUserFilter());
        registrationBean.addUrlPatterns("/inicio");
        registrationBean.addUrlPatterns("/consultar");
        registrationBean.addUrlPatterns("/complementoPagos/index");
        registrationBean.addUrlPatterns("/descarga/index");
        registrationBean.addUrlPatterns("/folios/index");
        registrationBean.addUrlPatterns("/pagos/index");
        registrationBean.addUrlPatterns("/reporteComplemento/index");
        registrationBean.addUrlPatterns("/reportes/index");
        registrationBean.addUrlPatterns("/wsadministracion/index");
        registrationBean.addUrlPatterns("/polizascontables/index");

        return registrationBean;
    }

}