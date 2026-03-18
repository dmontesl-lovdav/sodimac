package com.sodimac.facturacion.service.ws;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.facturacion.clientews.wcfrfccheck.org.datacontract.schemas._2004._07.WCFRfcCheck_Clases.Resultado;
import com.sodimac.facturacion.clientews.wcfrfccheck.org.tempuri.IService1;
import com.sodimac.facturacion.clientews.wcfrfccheck.org.tempuri.Service1;
import com.sodimac.facturacion.clientews.wcfrfccheck.org.tempuri.Service1Locator;
import com.sodimac.facturacion.component.ActividadesComponent;
import com.sodimac.facturacion.service.ConfiguracionFacturacionService;

@Service
public class CheckRfcServiceImpl implements CheckRfcService {

	@Autowired
	private ConfiguracionFacturacionService configFacService;
	@Autowired
	private ActividadesComponent actividadesModel;
	
	private static final String OK = "RFC Valido";

	private String url = "";
	private String licencia = "";
	private String usuario = "";
	private String password = "";
	
	public boolean validarRfc(String rfc) {
		
		URL portAddress;
		url =configFacService.getConfig().get("WebService.RfcCheck.Url");
		licencia =configFacService.getConfig().get("WebService.RfcCheck.Licencia");
		usuario =configFacService.getConfig().get("WebService.RfcCheck.Usuario");
		password =configFacService.getConfig().get("WebService.RfcCheck.Password");
		
		Service1 wsRfcCheck = new Service1Locator();
		IService1 iWsRfcCheck;
		List<String> datosArr = new ArrayList <String>();
		boolean result = false; 
		try {
			portAddress = new URL(url);
			iWsRfcCheck = wsRfcCheck.getBasicHttpsBinding_IService1(portAddress);
			Resultado serviceResponse = iWsRfcCheck.consultaRFC(usuario, password, rfc, licencia);
			if (serviceResponse.getEstatusRfc().equalsIgnoreCase(OK)) {
				result = true;
				datosArr.clear();
				datosArr.add(rfc);
				actividadesModel.registrarActividad(10, datosArr, "GenerarFactura");
			}
		} catch (ServiceException | RemoteException | MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return result;
		
	}
}
