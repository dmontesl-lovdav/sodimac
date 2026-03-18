package com.sodimac.wsconfiguracion.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.sodimac.wsconfiguracion.repository.config.entitymanager.UsersWsRepository;
import com.sodimac.wsconfiguracion.service.config.TokenService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;


@Component
public class JwtUtil {

	private static Logger logger = LoggerFactory.getLogger(JwtUtil.class);

	private final TokenService tokenService;
	private final UsersWsRepository usersWsRepository;

	public JwtUtil(TokenService tokenService, UsersWsRepository usersWsRepository) {
		this.tokenService = tokenService;
		this.usersWsRepository = usersWsRepository;
	}

	public void addAuthentication(HttpServletRequest req, HttpServletResponse res, String username) {
		String sessionId = "sesion";
		String token = tokenService.getToken(sessionId, username);

        //agregamos al encabezado el token
        res.addHeader("Authorization", token);
    }

    // Método para validar el token enviado por el cliente
	public Authentication getAuthentication(HttpServletRequest req) {

        // Obtenemos el token que viene en el encabezado de la peticion
        String token = req.getHeader("Authorization");

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