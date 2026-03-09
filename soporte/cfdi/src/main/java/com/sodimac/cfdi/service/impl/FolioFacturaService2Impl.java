package com.sodimac.cfdi.service.impl;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.cfdi.models.FolioFacturaModel;
import com.sodimac.cfdi.repository.fiscal.FolioFactura2Repository;
import com.sodimac.cfdi.service.FolioFacturaService2;
import com.sodimac.cfdi.service.SeguridadService;

@Service
public class FolioFacturaService2Impl implements FolioFacturaService2 {
	
	private DecimalFormat df = new DecimalFormat("000000");
	private NumberFormat dfCurrency = NumberFormat.getCurrencyInstance(new Locale("en", "US"));

	@Autowired
	private FolioFactura2Repository folioFactura2Repository;
	
	@Autowired
	private SeguridadService seguridadService;
	
	@Override
	public Integer obtenerIdFolioFactura(Integer pFolioFactura) {
		return this.folioFactura2Repository.getIdFolioFactura(pFolioFactura);
	}

	@Override
	public Double obtenerTotalFolioFactura(Integer pIdFolioFactura) {
		return this.folioFactura2Repository.getTotalFolioFactura(pIdFolioFactura);
	}

	@Override
	public Double obtenerTotalPagosFolioFactura(Integer pIdFolioFactura) {
		return this.folioFactura2Repository.getTotalPagosFolioFactura(pIdFolioFactura);
	}
	
	@Override
	public Double obtenerTotalOtrosPagosFolioFactura(Integer pIdFolioFactura, Integer pIdPagoComplemento) {
		return this.folioFactura2Repository.getTotalOtrosPagosFolioFactura(pIdFolioFactura, pIdPagoComplemento);
	}

	@Override
	public List<FolioFacturaModel> obtenerFacturasByIdFolioFactura(Integer pIdFolioFactura) {
		List<FolioFacturaModel> listFoliosFactura = new ArrayList<FolioFacturaModel>(0);
		List<Object[]> listFoliosFacturaObj = this.folioFactura2Repository.getFacturasByIdFolioFactura(pIdFolioFactura);
		if (listFoliosFacturaObj != null) {
			for (Object[] obj : listFoliosFacturaObj) {
				
				FolioFacturaModel folioFac = new FolioFacturaModel();
				Double montoTotalFactura = (obj[3] != null ) ? Double.valueOf(obj[3].toString()) : 0.0;
				Double montoTotalNC = (obj[7] != null ) ? Double.valueOf(obj[7].toString()) : 0.0;
				Double montoRealFactura = (obj[8] != null ) ? Double.valueOf(obj[8].toString()) : 0.0;
				String uuid = this.getValue(obj[4]);
				
				folioFac.setIdFolioFactura( (obj[0] != null ) ? Integer.valueOf(obj[0].toString()) : null );
				folioFac.setIdFactura( (obj[1] != null ) ? Integer.valueOf(obj[1].toString()) : null );
				folioFac.setFolioFactura( (obj[2] != null) ? df.format( Integer.valueOf(obj[2].toString())) : null );
				folioFac.setMontoFactura(montoTotalFactura);
				folioFac.setMontoFacturaStr( dfCurrency.format(montoTotalFactura) );
				folioFac.setRazonSocial( (obj[5] != null ) ? this.seguridadService.desencriptar(obj[5].toString()) : null);
				folioFac.setRfc( (obj[6] != null ) ? this.seguridadService.desencriptar(obj[6].toString()) : null);
				folioFac.setUuid( uuid );
				folioFac.setMontoTotalNC(montoTotalNC);
				folioFac.setMontoTotalNCStr( dfCurrency.format(montoTotalNC) );
				folioFac.setMontoRealFactura(montoRealFactura);
				folioFac.setMontoRealFacturaStr( dfCurrency.format(montoRealFactura) );
				folioFac.setOrden( (obj[9] != null ) ? Integer.valueOf(obj[9].toString()) : null );
				listFoliosFactura.add(folioFac);
			}
		}
		return listFoliosFactura;
	}
	
	private String getValue(Object obj) {
		if (obj != null) {
			return obj.toString();
		}
		return null;
	}

}
