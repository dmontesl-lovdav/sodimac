package com.sodimac.wsconfiguracion.config;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private CustomAuthenticationProvider authProvider;

    @Autowired
    private com.sodimac.wsconfiguracion.service.config.TokenService tokenService;

    @Autowired
    private com.sodimac.wsconfiguracion.repository.config.entitymanager.UsersWsRepository usersWsRepository;

    @Bean
    LoginFilter loginFilter() throws Exception {
    	LoginFilter loginFilter = new LoginFilter("/api/login", authenticationManager(), tokenService);
      return loginFilter;
    }

    @Bean
    JwtFilter jwtFilter() throws Exception {
    	JwtFilter jwtFilter = new JwtFilter(tokenService, usersWsRepository);
      return jwtFilter;
    }
	
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .cors().and()  // Habilita CORS
            .authorizeRequests()
            .antMatchers("/api/login").permitAll() //permitimos el acceso a /login a cualquiera
            //.antMatchers( "/api/test", "/api/emisor").hasAuthority("ADMIN")
            //.antMatchers("/api/obtenerTicketContactoCallCenter").hasAuthority("USER")
            .anyRequest().authenticated() //cualquier otra peticion requiere autenticacion
            .and()
            .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint())
            .and()
            .exceptionHandling().accessDeniedHandler(accessDeniedHandler())
            .and()
            // Las peticiones /login pasaran previamente por este filtro
            .addFilterBefore(loginFilter() ,
                    UsernamePasswordAuthenticationFilter.class)
            // Las demás peticiones pasarán por este filtro para validar el token
            .addFilterBefore(jwtFilter(),
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
        auth.authenticationProvider(authProvider);
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