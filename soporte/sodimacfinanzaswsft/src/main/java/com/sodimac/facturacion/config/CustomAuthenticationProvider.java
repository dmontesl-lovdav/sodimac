package com.sodimac.facturacion.config;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.sodimac.facturacion.repository.ws.UsersWsRepository;
import com.sodimac.facturacion.util.UtilsApi;
import com.sodimac.lib.cifrado.service.CryptoService;
import com.sodimac.lib.cifrado.service.CryptoServiceImpl;

public class CustomAuthenticationProvider implements AuthenticationProvider {

	private static Logger logger = LoggerFactory.getLogger(CustomAuthenticationProvider.class);
	CryptoService crypto = new CryptoServiceImpl(UtilsApi.getPathCifradoProperties());

	public CustomAuthenticationProvider() {

	}

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    	
        String name = authentication.getName();
        String passwordClaro = authentication.getCredentials().toString();
        String passwordTmp = "";
		try {
			passwordTmp = crypto.encrypt(passwordClaro);
		} catch (DataLengthException | IllegalStateException | InvalidCipherTextException
				| UnsupportedEncodingException e) {
			logger.error("Error al encriptar el password: ", e);
		}
        String password = passwordTmp;

        Optional<User> authenticatedUser = new UsersWsRepository().getUsers().stream().filter(
                user -> user.getUsername().equals(name) && user.getPassword().equals(password)
        ).findFirst();

        if(!authenticatedUser.isPresent()){
            throw new BadCredentialsException("Usuario y/o password erroneos");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(authenticatedUser.get().getRole()));
        Authentication auth = new UsernamePasswordAuthenticationToken(name, password, authorities);
        return auth;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }    
}
