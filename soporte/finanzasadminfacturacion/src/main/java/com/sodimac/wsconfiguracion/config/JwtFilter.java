package com.sodimac.wsconfiguracion.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import com.sodimac.wsconfiguracion.repository.config.entitymanager.UsersWsRepository;
import com.sodimac.wsconfiguracion.service.config.TokenService;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * Las peticiones que no sean /login pasarán por este filtro
 * el cuál se encarga de pasar el "request" a nuestra clase de utilidad JwtUtil
 * para que valide el token.
 */
public class JwtFilter extends GenericFilterBean {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    private final TokenService tokenService;
    private final UsersWsRepository usersWsRepository;

    public JwtFilter(TokenService tokenService, UsersWsRepository usersWsRepository) {
        this.tokenService = tokenService;
        this.usersWsRepository = usersWsRepository;
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain filterChain)
            throws IOException, ServletException {

    	Authentication authentication = getAuthentication((HttpServletRequest)request);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request,response);
    }

    // Método para validar el token enviado por el cliente
	private Authentication getAuthentication(HttpServletRequest req) {

        // Obtenemos el token que viene en el encabezado de la peticion
        String token = req.getHeader("Authorization");
        logger.info("JwtFilter - Token recibido del header: {}", token);

        // si hay un token presente, entonces lo validamos
        if (token != null) {

    		try {
    			String sessionId = "sesion";
    			String user = tokenService.validateToken(sessionId, token);

                // Recordamos que para las demás peticiones que no sean /login
                // no requerimos una autenticacion por username/password
                // por este motivo podemos devolver un UsernamePasswordAuthenticationToken sin password

    			if (user != null && !user.isEmpty()) {
    				String userTmp = user;
    				Optional<User> authenticatedUser = usersWsRepository.getUsers().stream()
    						.filter(x -> x.getUsername().equals(userTmp))
    						.findFirst();

    				if(authenticatedUser.isPresent()){
    					Collection<GrantedAuthority> authorities = new ArrayList<>();
    					authorities.add(new SimpleGrantedAuthority(authenticatedUser.get().getRole()));

    					return new UsernamePasswordAuthenticationToken(user, null, authorities);
    				}
    			}

    		} catch (Exception e) {
    			logger.error("getAuthentication: ", e);
    		}

        }
        return null;
    }
}