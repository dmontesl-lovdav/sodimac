package com.sodimac.facturacion.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.facturacion.component.ActividadesComponent;
import com.sodimac.facturacion.entity.fac.ClientesEntity;
import com.sodimac.facturacion.repository.fac.ClientesRepository;

@Service
public class ClientesServiceImpl implements ClientesService {

	@Autowired
	private ConfiguracionFacturacionService configFacService;
	@Autowired
	private ClientesRepository clientesRepository;
	@Autowired
	private ActividadesComponent actividadesModel;
	@Autowired
	private SeguridadService seguridadService;
	
	@Override
	@Transactional
	public ClientesEntity getCliente(String rfc) {
		return clientesRepository.findByRfc(seguridadService.encriptar(rfc));
	}
	
	@Transactional
	public int existRfcFactura(String rfc) {
		return clientesRepository.existRfcFactura(seguridadService.encriptar(rfc));
	}
	
	@Transactional
	public int inicializarRfcTicket(String rfc, String ticket) {
		return clientesRepository.inicializarRfcTicket(seguridadService.encriptar(rfc), ticket);
	}
	
	@Transactional
	public boolean validarRZExpresionRegular(String razonSocial) {
		boolean result = true;
		int longitudRazonSocial = Integer.parseInt(configFacService.getConfig().get("Request.Timbrado.RazonSocial.longitud"));
		
		razonSocial = razonSocial.trim();
		
		if (razonSocial.isEmpty() || razonSocial.length() > longitudRazonSocial) {
			result = false;
		} else {
	        Pattern pat = Pattern.compile(configFacService.getConfig().get("ExpresionRegular.RazonSocial.Caracteres"));
	        Matcher mat = pat.matcher(razonSocial);
	        if (!mat.matches()) {
	        	result = false;
	        }					
		}
		return result;
	}
	
	@Transactional
	public boolean validarRfcExpresionRegular(String rfc) {
		boolean result = true;
		int longitudRfcMinima = Integer.parseInt(configFacService.getConfig().get("Request.Timbrado.Rfc.longitud.Minima"));
		int longitudRfcMaxima = Integer.parseInt(configFacService.getConfig().get("Request.Timbrado.Rfc.longitud.Maxima"));
		
		rfc = rfc.trim();
		
		if (rfc.isEmpty() || (rfc.length() < longitudRfcMinima || rfc.length() > longitudRfcMaxima)) {
			result = false;
		} else {
	        Pattern pat = Pattern.compile(configFacService.getConfig().get("ExpresionRegular.Rfc"));
	        Matcher mat = pat.matcher(rfc);
	        if (!mat.matches()) {
	        	result = false;
	        }					
		}
		return result;
	}
	
	@Transactional
	public boolean validarEmailExpresionRegular(String email) {
		boolean result = true;
		int longitudEmail = Integer.parseInt(configFacService.getConfig().get("Request.Timbrado.Correo.longitud"));
		
		email = email.trim();
		
		if (email.isEmpty() || email.length() > longitudEmail) {
			result = false;
		} else {
	        Pattern pat = Pattern.compile(configFacService.getConfig().get("ExpresionRegular.Email"));
	        Matcher mat = pat.matcher(email);
	        if (!mat.matches()) {
	        	result = false;
	        }					
		}
		return result;
	}
	
	@Transactional
	public boolean validarObraExpresionRegular(String nombreObra) {
		boolean result = true;
		int longitudNombreObra = Integer.parseInt(configFacService.getConfig().get("Request.Timbrado.NombreObra.longitud"));
		
		nombreObra = nombreObra.trim();
		
		if (nombreObra.isEmpty() || nombreObra.length() > longitudNombreObra) {
			result = false;
		} else {
			Pattern pat = Pattern.compile(configFacService.getConfig().get("ExpresionRegular.NombreObra.Caracteres"));
	        Matcher mat = pat.matcher(nombreObra);
	        if (!mat.matches()) {
	        	result = false;
	        }					
		}
		return result;
	}
	
	@Transactional
	public boolean validarResponsableObraExpresionRegular(String responsableObra) {
		boolean result = true;
		int longitudResponsableObra = Integer.parseInt(configFacService.getConfig().get("Request.Timbrado.ResponsableObra.longitud"));
		
		responsableObra = responsableObra.trim();
		
		if (responsableObra.isEmpty() || responsableObra.length() > longitudResponsableObra) {
			result = false;
		} else {
			Pattern pat = Pattern.compile(configFacService.getConfig().get("ExpresionRegular.ResponsableObra.Caracteres"));
	        Matcher mat = pat.matcher(responsableObra);
	        if (!mat.matches()) {
	        	result = false;
	        }					
		}
		return result;
	}
	
	@Override
	@Transactional
	public void saveClientes(ClientesEntity model) {
		clientesRepository.save(model);
		actividadesModel.registrarActividad(14, null, "GuardaEdicionCliente");
	}
	
	@Transactional
	public boolean isExistRfc(String rfc) {
		boolean result = false;
		ClientesEntity cliente = clientesRepository.findByRfc(seguridadService.encriptar(rfc));
		if (cliente != null) result = true;
		return result;
	}
	
}
