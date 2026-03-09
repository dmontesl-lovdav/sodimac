package com.sodimac.cfdi.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.cfdi.component.ActividadesComponent;
import com.sodimac.cfdi.entity.fiscal.ClientesEntity;
import com.sodimac.cfdi.repository.fiscal.ClientesRepository;

@Service
public class ClientesServiceImpl implements ClientesService {

	@Autowired
	private CatConfiguracionService catConfiguracionService;
	
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
	public boolean validarRZExpresionRegular(String razonSocial) {
		boolean result = true;
		int longitudRazonSocial = Integer.parseInt(catConfiguracionService.findParameterByKey("Request.Timbrado.RazonSocial.longitud"));
		
		razonSocial = razonSocial.trim();
		
		if (razonSocial.isEmpty() || razonSocial.length() > longitudRazonSocial) {
			result = false;
		} else {
	        Pattern pat = Pattern.compile(catConfiguracionService.findParameterByKey("ExpresionRegular.RazonSocial.Caracteres"));
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
		int longitudRfcMinima = Integer.parseInt(catConfiguracionService.findParameterByKey("Request.Timbrado.Rfc.longitud.Minima"));
		int longitudRfcMaxima = Integer.parseInt(catConfiguracionService.findParameterByKey("Request.Timbrado.Rfc.longitud.Maxima"));
		
		rfc = rfc.trim();
		
		if (rfc.isEmpty() || (rfc.length() < longitudRfcMinima || rfc.length() > longitudRfcMaxima)) {
			result = false;
		} else {
	        Pattern pat = Pattern.compile(catConfiguracionService.findParameterByKey("ExpresionRegular.Rfc"));
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
		int longitudEmail = Integer.parseInt(catConfiguracionService.findParameterByKey("Request.Timbrado.Correo.longitud"));
		
		email = email.trim();
		
		if (email.isEmpty() || email.length() > longitudEmail) {
			result = false;
		} else {
	        Pattern pat = Pattern.compile(catConfiguracionService.findParameterByKey("ExpresionRegular.Email"));
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
		int longitudNombreObra = Integer.parseInt(catConfiguracionService.findParameterByKey("Request.Timbrado.NombreObra.longitud"));
		
		nombreObra = nombreObra.trim();
		
		if (nombreObra.isEmpty() || nombreObra.length() > longitudNombreObra) {
			result = false;
		} else {
			Pattern pat = Pattern.compile(catConfiguracionService.findParameterByKey("ExpresionRegular.NombreObra.Caracteres"));
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
		int longitudResponsableObra = Integer.parseInt(catConfiguracionService.findParameterByKey("Request.Timbrado.ResponsableObra.longitud"));
		
		responsableObra = responsableObra.trim();
		
		if (responsableObra.isEmpty() || responsableObra.length() > longitudResponsableObra) {
			result = false;
		} else {
			Pattern pat = Pattern.compile(catConfiguracionService.findParameterByKey("ExpresionRegular.ResponsableObra.Caracteres"));
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
