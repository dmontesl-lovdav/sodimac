package com.sodimac.facturacion.service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.facturacion.models.DescuentosRebatesModel;
import com.sodimac.facturacion.repository.reb.CatTipoRebateRepository;
import com.sodimac.facturacion.util.UtilsFechas;

@Service
public class RebatesServiceImpl implements RebatesService {

	@Autowired
	private CatTipoRebateRepository catTipoRebateRepository;
	
	@Transactional(isolation=Isolation.READ_UNCOMMITTED)
	public List<DescuentosRebatesModel> getDescuentos() {
		List<DescuentosRebatesModel> list = new ArrayList<DescuentosRebatesModel>();
			
		catTipoRebateRepository.getDescuentos().forEach(item -> {
			DescuentosRebatesModel itemList = new DescuentosRebatesModel();
			
			itemList.setRfcProveedor(item[0].toString());
			itemList.setNombreProveedor(item[1].toString());
			itemList.setRegimenFiscalProveedor(item[2].toString());
			itemList.setCodigoPostalProveedor(item[3].toString());
			itemList.setNumeroDocumento(item[4].toString());
			itemList.setNumeroReferencia(item[5].toString());
			itemList.setTicket(item[6].toString());
			itemList.setMoneda(item[7].toString());
			itemList.setTipoCambio(Double.parseDouble(item[8].toString()));
			itemList.setSubTotal(Double.parseDouble(item[9].toString()));
			itemList.setIva(Double.parseDouble(item[10].toString()));
			itemList.setTotal(Double.parseDouble(item[11].toString()));

			try {
				itemList.setFechaRecepcion(UtilsFechas.convertirDate(item[12].toString(), "yyyy-MM-dd"));
				itemList.setFechaContable(UtilsFechas.convertirDate(item[13].toString(), "yyyy-MM-dd"));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			itemList.setCorreo(item[14].toString());
						
			list.add(itemList);
		});
		
		return list;
	}
	
	@Transactional
	public int actualizaTimbrado (String numeroDocumento, String numeroReferencia, String ticket, String uuid, String fechaTimbrado) {
		return catTipoRebateRepository.actualizaTimbrado(numeroDocumento, numeroReferencia, ticket, uuid, fechaTimbrado);
	}
	
}
