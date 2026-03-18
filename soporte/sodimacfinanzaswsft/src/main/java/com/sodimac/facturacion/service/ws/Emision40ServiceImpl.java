package com.sodimac.facturacion.service.ws;

import java.net.URL;
import java.util.Base64;

import javax.xml.namespace.QName;
import javax.xml.ws.soap.AddressingFeature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.facturacion.clientews.wcfemision40.org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac.RespuestaXml;
import com.sodimac.facturacion.clientews.wcfemision40.org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac.Resultado;
import com.sodimac.facturacion.clientews.wcfemision40.org.tempuri.Detecno;
import com.sodimac.facturacion.clientews.wcfemision40.org.tempuri.IDetecno;
import com.sodimac.facturacion.component.ErrorComponent;
import com.sodimac.facturacion.entity.fac.catalogospdf.PacsEntity;
import com.sodimac.facturacion.service.ConfiguracionFacturacionService;
import com.sodimac.facturacion.service.SeguridadService;
import com.sodimac.facturacion.service.catalogospdf.PacsService;

@Service
public class Emision40ServiceImpl implements Emision40Service {

	private static final QName SERVICE_NAME = new QName("http://tempuri.org/", "Detecno");
	private Logger logger = LoggerFactory.getLogger(Emision40ServiceImpl.class);
	
	@Autowired
	private PacsService pacsService;
	@Autowired
	private ErrorComponent errorComponent;
	@Autowired
	private SeguridadService seguridadService;
    @Autowired
    private ConfiguracionFacturacionService configFacService;
	
	private String url = "";
	private String licencia = "";
	private String urlOld = "";
	private String urlGlobal = "";
	private String licenciaGlobal = "";
	
	public Resultado timbrar(String xml) {
		
		PacsEntity pac = pacsService.getById(pacsService.getIdDefault());
		if (pac != null && pac.getEndPoint40() != urlOld) {
			url = pac.getEndPoint40();
			urlOld = url;
			licencia = seguridadService.desencriptar(pac.getLicencia());
		}

		String cerBytes = "";
		String keyBytes = "";
		String passBytes = "";
		
		byte[] data = xml.getBytes();
		xml = Base64.getEncoder().encodeToString(data);
		Resultado result = null;

        try {
        	logger.info("########### DETECNO 4.0");
        	logger.info("URL DETECNO: " + url);
        	logger.info("SERVICE_NAME: " + SERVICE_NAME);
        	URL portAddress = new URL(url);
        	Detecno wsEmision = new Detecno(portAddress, SERVICE_NAME);
        	IDetecno iWsEmision = wsEmision.getWSHttpBindingIDetecno(new AddressingFeature(true));
			result = iWsEmision.comprobanteGenerar40(licencia, cerBytes, keyBytes, passBytes, xml);
			logger.info("########### END DETECNO 4.0");
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			errorComponent.setXml(xml);
			errorComponent.setPagina("timbrar");
			errorComponent.guardarLog(e);
		}
		
        return result;
	}
	
	public Resultado timbrar(String xml, String tipoTimbrado) {
		
		PacsEntity pac = pacsService.getById(pacsService.getIdDefault());
		if (pac != null && pac.getEndPoint40() != urlOld) {
			url = pac.getEndPoint40();
			urlOld = url;
			licencia = seguridadService.desencriptar(pac.getLicencia());
			urlGlobal = configFacService.getConfig().get("WebService.Emision.Global.Url40");
			licenciaGlobal = seguridadService.desencriptar(configFacService.getConfig().get("WebService.Emision.Global.Licencia40"));
		}
		
		String urlTipoTimbrado = url;
		String licenciaTipoTimbrado = licencia;
		
		if (tipoTimbrado.contains("G")) {
			urlTipoTimbrado = urlGlobal;
			licenciaTipoTimbrado = licenciaGlobal;
		} 

		String cerBytes = "";
		String keyBytes = "";
		String passBytes = "";
		
		Resultado result = null;

        try {
        	URL portAddress = new URL(urlTipoTimbrado);
        	Detecno wsEmision = new Detecno(portAddress, SERVICE_NAME);
        	IDetecno iWsEmision = wsEmision.getWSHttpBindingIDetecno(new AddressingFeature(true));
			result = iWsEmision.comprobanteGenerar40(licenciaTipoTimbrado, cerBytes, keyBytes, passBytes, xml);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			errorComponent.setXml(xml);
			errorComponent.setPagina("timbrarTipo");
			errorComponent.guardarLog(e);
		}
		
        return result;
	}

	public RespuestaXml getComprobante(String facturaId) {
		
		PacsEntity pac = pacsService.getById(pacsService.getIdDefault());
		if (pac != null && pac.getEndPoint40() != urlOld) {
			url = pac.getEndPoint40();
			urlOld = url;
			licencia = seguridadService.desencriptar(pac.getLicencia());			
		}

		RespuestaXml result = null;
		
		try {
	    	URL portAddress = new URL(url);
	    	Detecno wsEmision = new Detecno(portAddress, SERVICE_NAME);
	    	IDetecno iWsEmision = wsEmision.getWSHttpBindingIDetecno(new AddressingFeature(true));
			result = iWsEmision.comprobanteBuscar40(licencia, facturaId);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			errorComponent.setPagina("getComprobante");
			errorComponent.guardarLog(e);
		}
		return result;
	}
	
	@Override
	public RespuestaXml getComprobante(String facturaId, String tipoTimbrado) {
		
		PacsEntity pac = pacsService.getById(pacsService.getIdDefault());
		if (pac != null && pac.getEndPoint40() != urlOld) {
			url = pac.getEndPoint40();
			urlOld = url;
			licencia = seguridadService.desencriptar(pac.getLicencia());			
			urlGlobal = configFacService.getConfig().get("WebService.Emision.Global.Url40");
			licenciaGlobal = seguridadService.desencriptar(configFacService.getConfig().get("WebService.Emision.Global.Licencia40"));
		}

		String urlTipoTimbrado = url;
		String licenciaTipoTimbrado = licencia;
		
		if (tipoTimbrado.contains("G")) {
			urlTipoTimbrado = urlGlobal;
			licenciaTipoTimbrado = licenciaGlobal;
		} 

		RespuestaXml result = null;
		
		try {
	    	URL portAddress = new URL(urlTipoTimbrado);
	    	Detecno wsEmision = new Detecno(portAddress, SERVICE_NAME);
	    	IDetecno iWsEmision = wsEmision.getWSHttpBindingIDetecno(new AddressingFeature(true));
			result = iWsEmision.comprobanteBuscar40(licenciaTipoTimbrado, facturaId);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			errorComponent.setPagina("getComprobante");
			errorComponent.guardarLog(e);
		}
		return result;
	}

	public Resultado getComprobantePdf(String facturaId) {
		
		Resultado result = null;
		
		PacsEntity pac = pacsService.getById(pacsService.getIdDefault());
		if (pac != null && pac.getEndPoint40() != urlOld) {
			url = pac.getEndPoint40();
			urlOld = url;
			licencia = seguridadService.desencriptar(pac.getLicencia());			
		}

		try {
	    	URL portAddress = new URL(url);
	    	Detecno wsEmision = new Detecno(portAddress, SERVICE_NAME);
	    	IDetecno iWsEmision = wsEmision.getWSHttpBindingIDetecno(new AddressingFeature(true));
			result = iWsEmision.comprobanteBuscarPdf40(licencia, facturaId);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			errorComponent.setPagina("getComprobantePdf");
			errorComponent.guardarLog(e);
		}
		return result;
	}

	public Resultado cancelar(String facturaId) {
		
		Resultado result = null;
		
		PacsEntity pac = pacsService.getById(pacsService.getIdDefault());
		if (pac != null && pac.getEndPoint40() != urlOld) {
			url = pac.getEndPoint40();
			urlOld = url;
			licencia = seguridadService.desencriptar(pac.getLicencia());			
		}

		try {
	    	URL portAddress = new URL(url);
			String motivoCancelacion = "";
			String folioSustitucion = "";
			Detecno wsEmision = new Detecno(portAddress, SERVICE_NAME);
			IDetecno iWsEmision = wsEmision.getWSHttpBindingIDetecno(new AddressingFeature(true));
			result = iWsEmision.comprobanteCancelar40(licencia, facturaId, motivoCancelacion, folioSustitucion);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			errorComponent.setPagina("EmisionService-cancelar");
			errorComponent.guardarLog(e);
		}
		return result;
	}

}
