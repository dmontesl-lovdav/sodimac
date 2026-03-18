package com.sodimac.facturacion.repository.bct;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository("estFacturasRepository")
public class EstFacturasRepository {

	Logger logger = LoggerFactory.getLogger(EstFacturasRepository.class);
	
	public String findTicketByUuid(String uuid) {

		String sql = "select NUM_TRX from EST_FACTURAS where NUM_FACTURA = '{uuid}' and NUM_TIENDA is null order by NUM_TRX";
		sql = sql.replace("{uuid}", uuid);
		@SuppressWarnings("unchecked")
		List<String> list = (List<String>) QueryBctRepository.executeGetResultList(sql);

		for (String ent : list) {
			return ent;
		}
				
		return null;
	}
	
	public String findUuidByTicket(String ticket) {

		String sql = "select NUM_FACTURA from EST_FACTURAS where NUM_TRX = '{ticket}' and NUM_TIENDA is null order by NUM_FACTURA";
		sql = sql.replace("{ticket}", ticket);
		@SuppressWarnings("unchecked")
		List<String> list = (List<String>) QueryBctRepository.executeGetResultList(sql);
		
		for (String ent : list) {
			return ent;
		}
				
		return null;
	}
	
	public String ValidateStatusPendingByTicket(String ticket) {

		String sql = "SELECT X.NUM_TRX FROM EST_FACTURAS X WHERE X.FECHA_CIERRE IS NULL AND X.NUM_TIENDA IS NULL and NUM_FACTURA IS NULL AND NUM_TRX = '{ticket}'";
		sql = sql.replace("{ticket}", ticket);
		@SuppressWarnings("unchecked")
		List<String> list = (List<String>) QueryBctRepository.executeGetResultList(sql);
				
		for (String ent : list) {
			return ent;
		}
				
		return null;
	}
	
}
