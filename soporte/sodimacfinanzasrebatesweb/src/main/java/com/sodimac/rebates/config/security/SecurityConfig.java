package com.sodimac.rebates.config.security;

import java.util.Arrays;

import javax.servlet.Filter;
import javax.servlet.http.HttpSessionListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.sodimac.rebates.enums.ESeguridad;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private CustomAuthenticationProvider authProvider;

	@Autowired
	private AuthenticationSuccessHandler authenticationSuccessHandler;

	@Autowired
	private AuthenticationFailureHandler authenticationFailureHandler;

	@Autowired
	private AccessDeniedHandler accessDeniedHandler;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider((AuthenticationProvider) this.authProvider);
	}

	@Override
	public void configure(WebSecurity webSecurity) throws Exception {
		StrictHttpFirewall firewall = new StrictHttpFirewall();
		firewall.setAllowSemicolon(true);
		webSecurity.httpFirewall(firewall);
		webSecurity.ignoring().antMatchers(new String[] { "/", "/401", "/recover", "login", "logout", "/static/**", "/css/**", "/font/**", "/fontawesome-5.15.1/**", "/img/**", "/js/**" });
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		((HttpSecurity) ((HttpSecurity) ((HttpSecurity) ((HttpSecurity) ((HttpSecurity) ((FormLoginConfigurer) ((FormLoginConfigurer) ((FormLoginConfigurer) ((FormLoginConfigurer) ((HttpSecurity) ((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl) ((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl) ((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl) ((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl) ((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl) ((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl) ((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl) ((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl) ((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl) ((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl) ((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl) ((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl) ((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl) ((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl) ((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl) 
				((HttpSecurity) http.csrf().disable())
				.authorizeRequests()

				.antMatchers(ESeguridad.DOCUMENTOS_MENU.getUrl())).hasAnyAuthority(ESeguridad.DOCUMENTOS_MENU.getAuthority())
				.antMatchers(ESeguridad.FILLRATE_MENU.getUrl())).hasAnyAuthority(ESeguridad.FILLRATE_MENU.getAuthority())
				.antMatchers(ESeguridad.AUTHORIZATION_MENU.getUrl())).hasAnyAuthority(ESeguridad.AUTHORIZATION_MENU.getAuthority())
				.antMatchers(ESeguridad.ORDER_COMPRA_MENU.getUrl())).hasAnyAuthority(ESeguridad.ORDER_COMPRA_MENU.getAuthority())
				.antMatchers(ESeguridad.ORDER_COMPRA_FILL_MENU.getUrl())).hasAnyAuthority(ESeguridad.ORDER_COMPRA_FILL_MENU.getAuthority())
				.antMatchers(ESeguridad.REBASTES_MSI_MENU.getUrl())).hasAnyAuthority(ESeguridad.REBASTES_MSI_MENU.getAuthority())
				.antMatchers(ESeguridad.REBATES_MSI3_MENU.getUrl())).hasAnyAuthority(ESeguridad.REBATES_MSI3_MENU.getAuthority())
				.antMatchers(ESeguridad.REBATES_USUARIO_MENU.getUrl())).hasAnyAuthority(ESeguridad.REBATES_USUARIO_MENU.getAuthority())
				.antMatchers(ESeguridad.REBATES_USUARIO_FILLRATE_MENU.getUrl())).hasAnyAuthority(ESeguridad.REBATES_USUARIO_FILLRATE_MENU.getAuthority())
				.antMatchers(ESeguridad.ACUERDOS_MENU.getUrl())).hasAnyAuthority(ESeguridad.ACUERDOS_MENU.getAuthority())
				.antMatchers(ESeguridad.EXCLUSIONES_MENU.getUrl())).hasAnyAuthority(ESeguridad.EXCLUSIONES_MENU.getAuthority())
				.antMatchers(ESeguridad.CATALOGOS_MENU.getUrl())).hasAnyAuthority(ESeguridad.CATALOGOS_MENU.getAuthority())
				.antMatchers(ESeguridad.POLIZAS_MENU.getUrl())).hasAnyAuthority(ESeguridad.POLIZAS_MENU.getAuthority())
				.antMatchers(ESeguridad.REPORTE_FINANCIERO_MENU.getUrl())).hasAnyAuthority(ESeguridad.REPORTE_FINANCIERO_MENU.getAuthority())
				

				.anyRequest()).authenticated().and()).formLogin().loginPage("/")
				.loginProcessingUrl("/login")).successHandler(this.authenticationSuccessHandler))
				.failureHandler(this.authenticationFailureHandler)).permitAll()).and()).exceptionHandling()
				.accessDeniedHandler(this.accessDeniedHandler).and()).logout()

				.invalidateHttpSession(true).logoutSuccessUrl("/").logoutUrl("/logout").and()).cors()
				.and()).sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).invalidSessionUrl("/401")
//				.and()).sessionManagement().invalidSessionUrl("/")
				.sessionAuthenticationErrorUrl("/").and()).addFilter((Filter) expiredSessionFilter());
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList(new String[] { "*" }));
		configuration
				.setAllowedMethods(Arrays.asList(new String[] { "HEAD", "GET", "POST", "PUT", "DELETE", "PATCH" }));
		configuration.setAllowCredentials(Boolean.valueOf(true));
		configuration
				.setAllowedHeaders(Arrays.asList(new String[] { "Authorization", "Cache-Control", "Content-Type" }));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return (CorsConfigurationSource) source;
	}

	private SessionManagementFilter expiredSessionFilter() {
		SessionManagementFilter sessionManagementFilter = new SessionManagementFilter(
				(SecurityContextRepository) new HttpSessionSecurityContextRepository());
		sessionManagementFilter.setInvalidSessionStrategy((request, response) -> {
			String referrer = request.getHeader("referer");
			if(referrer!= null && referrer.endsWith(request.getContextPath() + "/")) {
				response.sendRedirect(request.getContextPath() + "/");
			} else if (referrer!= null && !referrer.endsWith(request.getContextPath() + "/")) {
				response.sendRedirect(request.getContextPath() + "/401");
			} else {
				response.sendRedirect(request.getContextPath() + "/");
			}
		});
		return sessionManagementFilter;
	}
	
	@Bean
	public HttpSessionListener getHttpSessionListener(){
	    return new SessionListener();
	}

	
	@Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }
}
