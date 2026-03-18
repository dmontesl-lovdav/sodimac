package com.sodimac.facturacion.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.facturacion.cliente.ws.model.ClientResponseTYPE;
import com.sodimac.facturacion.models.UsoDeCfdi;
import com.sodimac.facturacion.util.enums.EVersionCFDI;

@Service
public class CatUsosCfdiServiceImpl implements CatUsosCfdiService {

	private static final String USO_CFDI_NC = "G02";

	@Autowired
	private ConfiguracionService configuracionService;
	
	private List<UsoDeCfdi> listCfdi33;
	private List<UsoDeCfdi> listCfdi40;
	
	private void inicializarCatalogos() {
		if (this.listCfdi33 == null) {
			ClientResponseTYPE<List<UsoDeCfdi>> resp = new ClientResponseTYPE<>();
			resp = this.configuracionService.consultarUsoCfdi33All();
			if (resp.getRespuesta().getCodigo().equals("1")) {
				this.listCfdi33 = resp.getData();
        	}
		}
		
		if (this.listCfdi40 == null) {
			ClientResponseTYPE<List<UsoDeCfdi>> resp = new ClientResponseTYPE<>();
			resp = this.configuracionService.consultarUsoCfdi40All();
			if (resp.getRespuesta().getCodigo().equals("1")) {
				this.listCfdi40 = resp.getData();
        	}
		}
	}
	
	@Override
	public List<UsoDeCfdi> getAll(String tipo, Integer idVersionCfdi) {
		
		this.inicializarCatalogos();
		if (EVersionCFDI.VERSION_40.getId() == idVersionCfdi) {
			return this.listCfdi40;
		}  
		else if (EVersionCFDI.VERSION_33.getId() == idVersionCfdi) {
			return this.listCfdi33;
		}
		return null;
	}

	@Override
	public UsoDeCfdi getUsoCfdi(int id, Integer idVersionCfdi) {
		this.inicializarCatalogos();
		if (EVersionCFDI.VERSION_40.getId() == idVersionCfdi) {
			return this.getUsoCfdiById(this.listCfdi40, id);
		}  
		else if (EVersionCFDI.VERSION_33.getId() == idVersionCfdi) {
			return this.getUsoCfdiById(this.listCfdi33, id);
		}
		return null;
	}

	@Override
	public UsoDeCfdi getUsoCfdi(String clave, Integer idVersionCfdi) {
		this.inicializarCatalogos();
		if (EVersionCFDI.VERSION_40.getId() == idVersionCfdi) {
			return this.getUsoCfdiByClave(this.listCfdi40, clave);
		}  
		else if (EVersionCFDI.VERSION_33.getId() == idVersionCfdi) {
			return this.getUsoCfdiByClave(this.listCfdi33, clave);
		}
		return null;
	}
	
	@Override
	public UsoDeCfdi getUsoCfdiNC() {
		this.inicializarCatalogos();
		return this.getUsoCfdiByClave(this.listCfdi40, USO_CFDI_NC);
	}
	
	private UsoDeCfdi getUsoCfdiByClave(List<UsoDeCfdi> listCfdi, String clave) {
		if (listCfdi != null) {
			for (UsoDeCfdi usoCfdi : listCfdi) {
				if(usoCfdi.getClave().equals(clave)) {
					return usoCfdi;
				}
			}
		}
		return null;
	}
	
	private UsoDeCfdi getUsoCfdiById(List<UsoDeCfdi> listCfdi, int id) {
		if (listCfdi != null) {
			for (UsoDeCfdi usoCfdi : listCfdi) {
				if(usoCfdi.getIdUsoCfdi() == id) {
					return usoCfdi;
				}
			}
		}
		return null;
	}
	
}
