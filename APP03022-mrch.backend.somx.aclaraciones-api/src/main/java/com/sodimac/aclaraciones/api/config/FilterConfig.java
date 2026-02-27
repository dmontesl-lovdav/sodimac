package com.sodimac.aclaraciones.api.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.sodimac.aclaraciones.api.repository.AuthorRepository;
import com.sodimac.aclaraciones.api.security.AuthorAutoRegisterFilter;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<NoCacheFilter> noCacheFilter() {

        FilterRegistrationBean<NoCacheFilter> reg = new FilterRegistrationBean<>();

        reg.setFilter(new NoCacheFilter());
        reg.addUrlPatterns("/*");
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return reg;
    }

    @Bean
    public FilterRegistrationBean<AuthorAutoRegisterFilter> authorAutoRegisterFilter(
            AuthorRepository authorRepository) {

        FilterRegistrationBean<AuthorAutoRegisterFilter> reg = new FilterRegistrationBean<>();

        reg.setFilter(new AuthorAutoRegisterFilter(authorRepository));
        reg.addUrlPatterns("/*");
        reg.setOrder(Ordered.LOWEST_PRECEDENCE);

        return reg;
    }
}
