package com.sodimac.rebates.config.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.sodimac.rebates.dto.CatPerfilDto;
import com.sodimac.rebates.dto.CatRolDto;
import com.sodimac.rebates.mapper.CatPerfilMapper;
import com.sodimac.rebates.model.CatEventoDto;
import com.sodimac.rebates.model.Sesion;
import com.sodimac.rebates.model.Usuario;
import com.sodimac.rebates.model.entity.CatPerfilEntity;
import com.sodimac.rebates.model.entity.CatPermisoEntity;
import com.sodimac.rebates.repository.CatPerfilPermisoRepository;
import com.sodimac.rebates.repository.CatPerfilRepository;
import com.sodimac.rebates.service.ICatRolService;
import com.sodimac.rebates.service.IEventoPermisoRolService;
import com.sodimac.rebates.service.IUsuarioService;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

	@Autowired
	private IUsuarioService serviceUsuario;

	@Autowired
	private CatPerfilRepository catPerfilRepository;

	@Autowired
	private CatPerfilPermisoRepository catPerfilPermisoRepository;
	
	@Autowired
	private IEventoPermisoRolService eventoPermisoRolService;
	
	@Autowired
	private ICatRolService catRolService;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		
		Usuario user = serviceUsuario.getUser(authentication.getName(), authentication.getCredentials().toString());
		if (user != null) {
			if (!user.isActivo()) {
				throw new BadCredentialsException("La cuenta con la que desea ingresar se encuentra suspendida");
			} else {
				Sesion sesion = this.getSesion(user);
				return (Authentication) new UsernamePasswordAuthenticationToken(sesion, authentication.getCredentials(), this.getGrantedAuthorities(sesion));
			}
		} else {
			throw new BadCredentialsException("Usuario y/o contraseña incorrectos, favor de validar");
		}

	}

	private Sesion getSesion(Usuario user) {
		
		List<CatPerfilEntity> catPerfilList = catPerfilRepository.findPerfilesByUser(user.getId());
		List<CatPermisoEntity> catPermisoList = catPerfilPermisoRepository.findPermisosByUser(user.getId());
		List<CatEventoDto> eventos = this.eventoPermisoRolService.getEventos(user.getId(), catPermisoList);
		List<CatRolDto> roles = this.catRolService.getRoles(user.getId());
		List<CatPerfilDto> listPerfiles = CatPerfilMapper.convertToDtos(catPerfilList);
		
		Sesion sesion = new Sesion();
		sesion.setIdUser(user.getId());
		sesion.setEmail(user.getUsuario());
		sesion.setPerfiles( catPerfilList != null && !catPerfilList.isEmpty() ? catPerfilList.stream().map(n -> n.getNombre()).collect(Collectors.toList() ) : Arrays.asList("NO ASIGNADO"));
		sesion.setNombre(user.getNombre() + " " + user.getApellidoPaterno());
		sesion.setPermisos(catPermisoList.stream().map(item -> item.getGrantedAuthority()).collect(Collectors.toList()));
		sesion.setEventos(eventos);
		sesion.setPerfilesDto(listPerfiles);
		sesion.setRoles(roles);
		return sesion;
	}

	private List<GrantedAuthority> getGrantedAuthorities(Sesion sesion) {
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		if (sesion.getPermisos() != null && !sesion.getPermisos().isEmpty()) {
			sesion.getPermisos().stream().forEach(item -> {
				
				authorities.add(new SimpleGrantedAuthority(item));
			});
		}
		if (sesion.getEventos() != null && !sesion.getEventos().isEmpty()) {
			sesion.getEventos().stream().forEach(item -> {
				
				authorities.add(new SimpleGrantedAuthority(item.getClave()));
			});
		}
		return authorities;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

}
