package com.sodimac.facturacion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeRequests()
            .antMatchers("/api/login").permitAll() //permitimos el acceso a /login a cualquiera
            .antMatchers("/api/timbrarTicket", "/api/timbrarNCTicket", "/api/cancelarCfdi", "/api/retimbrarTicket", "/api/obtenerConfiguracion").hasAuthority("ADMIN")
            .antMatchers("/api/obtenerTicketContactoCallCenter").hasAuthority("USER")
            .anyRequest().authenticated() //cualquier otra peticion requiere autenticacion
            .and()
            .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint())
            .and()
            .exceptionHandling().accessDeniedHandler(accessDeniedHandler())
            .and()
            // Las peticiones /login pasaran previamente por este filtro
            .addFilterBefore(new LoginFilter("/api/login", authenticationManager()),
                    UsernamePasswordAuthenticationFilter.class)
            // Las demás peticiones pasarán por este filtro para validar el token
            .addFilterBefore(new JwtFilter(),
                    UsernamePasswordAuthenticationFilter.class);
    }
   
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/v2/api-docs",
                                   "/configuration/ui",
                                   "/swagger-resources/**",
                                   "/configuration/security",
                                   "/swagger-ui.html",
                                   "/webjars/**");
    }
    
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(new CustomAuthenticationProvider());
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(){
        return new CustomAuthenticationEntryPoint();
    }   
    
    @Bean
    public AccessDeniedHandler accessDeniedHandler(){
        return new CustomAccessDeniedHandler();
    }
    
}