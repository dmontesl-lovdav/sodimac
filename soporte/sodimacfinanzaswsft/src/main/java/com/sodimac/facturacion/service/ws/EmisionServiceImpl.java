package com.sodimac.facturacion.service.ws;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Base64;

import javax.xml.rpc.ServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.facturacion.clientews.wcfemision.org.datacontract.schemas._2004._07.cfdiWcfEmisionServicio_Sodimac_Clases.RespuestaXml;
import com.sodimac.facturacion.clientews.wcfemision.org.datacontract.schemas._2004._07.cfdiWcfEmisionServicio_Sodimac_Clases.Resultado;
import com.sodimac.facturacion.clientews.wcfemision.org.tempuri.Detecno;
import com.sodimac.facturacion.clientews.wcfemision.org.tempuri.DetecnoLocator;
import com.sodimac.facturacion.clientews.wcfemision.org.tempuri.IDetecno;
import com.sodimac.facturacion.component.ErrorComponent;
import com.sodimac.facturacion.entity.fac.catalogospdf.PacsEntity;
import com.sodimac.facturacion.service.ConfiguracionFacturacionService;
import com.sodimac.facturacion.service.SeguridadService;
import com.sodimac.facturacion.service.catalogospdf.PacsService;

@Service
public class EmisionServiceImpl implements EmisionService {

	private Logger logger = LoggerFactory.getLogger(EmisionServiceImpl.class);
	
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
		if (pac != null && pac.getEndPoint() != urlOld) {
			url = pac.getEndPoint();
			urlOld = url;
			licencia = seguridadService.desencriptar(pac.getLicencia());			
		}

		String cerBytes = "";
		String keyBytes = "";
		String passBytes = "";
		
		byte[] data = xml.getBytes();
		xml = Base64.getEncoder().encodeToString(data);
		Detecno wsEmision = new DetecnoLocator();
		Resultado result = null;

        try {
        	logger.info("####### DETECNO 3.3 ########");
        	logger.info("URL DETECNO: " + url);
        	URL portAddress = new URL(url);
        	IDetecno iWsEmision = wsEmision.getBasicHttpBinding_IDetecno(portAddress);
			result = iWsEmision.comprobanteGenerar33(licencia, cerBytes, keyBytes, passBytes, xml);
			logger.info("####### END DETECNO 3.3 ########");
			return result;
		} catch (RemoteException | MalformedURLException | ServiceException e) {
			e.printStackTrace();
			errorComponent.setXml(xml);
			errorComponent.setPagina("timbrar");
			errorComponent.guardarLog(e);
		}
		
        return result;
	}
	
	public Resultado timbrar(String xml, String tipoTimbrado) {
		
		PacsEntity pac = pacsService.getById(pacsService.getIdDefault());
		if (pac != null && pac.getEndPoint() != urlOld) {
			url = pac.getEndPoint();
			urlOld = url;
			licencia = seguridadService.desencriptar(pac.getLicencia());
			urlGlobal = configFacService.getConfig().get("WebService.Emision.Global.Url");
			licenciaGlobal = seguridadService.desencriptar(configFacService.getConfig().get("WebService.Emision.Global.Licencia"));
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
		
		Detecno wsEmision = new DetecnoLocator();
		Resultado result = null;

        try {
        	URL portAddress = new URL(urlTipoTimbrado);
        	IDetecno iWsEmision = wsEmision.getBasicHttpBinding_IDetecno(portAddress);
			result = iWsEmision.comprobanteGenerar33(licenciaTipoTimbrado, cerBytes, keyBytes, passBytes, xml);
			return result;
		} catch (RemoteException | MalformedURLException | ServiceException e) {
			e.printStackTrace();
			errorComponent.setXml(xml);
			errorComponent.setPagina("timbrarTipo");
			errorComponent.guardarLog(e);
		}
		
        return result;
	}

	public RespuestaXml getComprobante(String facturaId) {
		
		PacsEntity pac = pacsService.getById(pacsService.getIdDefault());
		if (pac != null && pac.getEndPoint() != urlOld) {
			url = pac.getEndPoint();
			urlOld = url;
			licencia = seguridadService.desencriptar(pac.getLicencia());			
		}

		Detecno wsEmision = new DetecnoLocator();
		RespuestaXml result = null;
		
		try {
	    	URL portAddress = new URL(url);
	    	IDetecno iWsEmision = wsEmision.getBasicHttpBinding_IDetecno(portAddress);
			result = iWsEmision.comprobanteBuscar33(licencia, facturaId);
			return result;
		} catch (RemoteException | ServiceException | MalformedURLException e) {
			e.printStackTrace();
			errorComponent.setPagina("getComprobante");
			errorComponent.guardarLog(e);
		}
		return result;
	}
	
	public RespuestaXml getComprobante(String facturaId, String tipoTimbrado) {
		
		PacsEntity pac = pacsService.getById(pacsService.getIdDefault());
		if (pac != null && pac.getEndPoint() != urlOld) {
			url = pac.getEndPoint();
			urlOld = url;
			licencia = seguridadService.desencriptar(pac.getLicencia());			
			urlGlobal = configFacService.getConfig().get("WebService.Emision.Global.Url");
			licenciaGlobal = seguridadService.desencriptar(configFacService.getConfig().get("WebService.Emision.Global.Licencia"));
		}

		String urlTipoTimbrado = url;
		String licenciaTipoTimbrado = licencia;
		
		if (tipoTimbrado.contains("G")) {
			urlTipoTimbrado = urlGlobal;
			licenciaTipoTimbrado = licenciaGlobal;
		} 

		Detecno wsEmision = new DetecnoLocator();
		RespuestaXml result = null;
		
		try {
	    	URL portAddress = new URL(urlTipoTimbrado);
	    	IDetecno iWsEmision = wsEmision.getBasicHttpBinding_IDetecno(portAddress);
			result = iWsEmision.comprobanteBuscar33(licenciaTipoTimbrado, facturaId);
			return result;
		} catch (RemoteException | ServiceException | MalformedURLException e) {
			e.printStackTrace();
			errorComponent.setPagina("getComprobante");
			errorComponent.guardarLog(e);
		}
		return result;
	}

	public Resultado getComprobantePdf(String facturaId) {
		
		Detecno wsEmision = new DetecnoLocator();
		Resultado result = null;
		
		PacsEntity pac = pacsService.getById(pacsService.getIdDefault());
		if (pac != null && pac.getEndPoint() != urlOld) {
			url = pac.getEndPoint();
			urlOld = url;
			licencia = seguridadService.desencriptar(pac.getLicencia());			
		}

		try {
	    	URL portAddress = new URL(url);
	    	IDetecno iWsEmision = wsEmision.getBasicHttpBinding_IDetecno(portAddress);
			result = iWsEmision.comprobante_BuscarPdf33(licencia, facturaId);
			return result;
		} catch (RemoteException | ServiceException | MalformedURLException e) {
			e.printStackTrace();
			errorComponent.setPagina("getComprobantePdf");
			errorComponent.guardarLog(e);
		}
		return result;
	}

	public Resultado cancelar(String facturaId) {
		
		Detecno wsEmision = new DetecnoLocator();
		Resultado result = null;
		
		PacsEntity pac = pacsService.getById(pacsService.getIdDefault());
		if (pac != null && pac.getEndPoint() != urlOld) {
			url = pac.getEndPoint();
			urlOld = url;
			licencia = seguridadService.desencriptar(pac.getLicencia());			
		}

		try {
	    	URL portAddress = new URL(url);
	    	IDetecno iWsEmision = wsEmision.getBasicHttpBinding_IDetecno(portAddress);
			result = iWsEmision.comprobanteCancelar33(licencia, facturaId);
			return result;
		} catch (RemoteException | ServiceException | MalformedURLException e) {
			e.printStackTrace();
			errorComponent.setPagina("EmisionService-cancelar");
			errorComponent.guardarLog(e);
		}
		return result;
	}

}
