package com.sodimac.facturacion.repository.bct;

import java.math.BigDecimal;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.bct.TicketEntity;

@Repository("ticketRepository")
public class TicketRepository {
	
	Logger logger = LoggerFactory.getLogger(TicketRepository.class);
	
	public TicketEntity findByTicket(String ticket, String fechaPeriodo) {

		TicketEntity result = new TicketEntity();
		String sql = "select NUM_TRX, FECHA_TRX, NUM_TIENDA, NUM_CAJA, COD_CAJERO, NUM_TICKET, TIPO_TRX, MNT_TOTAL_A_PAGAR, MNT_TOT_SN_IMPTOS, TRX_ORIGINAL, CASE WHEN TRX_ORIGINAL IS NULL THEN 'N' ELSE 'Y' END AS ID_CLIENTE from TRX_HDR where NUM_TRX = '{ticket}' and TIPO_TRX in (1, 9, 10) and TO_DATE(FECHA_TRX, 'dd-mm-yy') >= TO_DATE('{FechaPeriodo}', 'dd-mm-yy')";
		sql = sql.replace("{ticket}", ticket).replace("{FechaPeriodo}", fechaPeriodo);
		Object[] ent = (Object[]) QueryBctRepository.executeGetSingleResult(sql);
		
		if (ent == null) {
			return null;
		}
		
		result.setTicket((String) ent[0]);
		result.setFecha((Date) ent[1]);
		result.setTienda(Integer.parseInt(ent[2].toString()));
		result.setCaja(Integer.parseInt(ent[3].toString()));
		result.setCajero(Float.parseFloat(ent[4].toString()));
		result.setNumTicket(Integer.parseInt(ent[5].toString()));
		result.setTipoTicket(Integer.parseInt(ent[6].toString()));
		result.setTotalPagar( new BigDecimal(ent[7].toString()));
		result.setTotalSinImpuestos( new BigDecimal(ent[8].toString()) );
		result.setOriginal((String) ent[9]);
		result.setDevolucion(String.valueOf((char)ent[10]));			

		return result;
	}
	
	public TicketEntity findByTicket(String ticket) {

		TicketEntity result = new TicketEntity();
		String sql = "select NUM_TRX, FECHA_TRX, NUM_TIENDA, NUM_CAJA, COD_CAJERO, NUM_TICKET, TIPO_TRX, MNT_TOTAL_A_PAGAR, MNT_TOT_SN_IMPTOS, TRX_ORIGINAL, CASE WHEN TRX_ORIGINAL IS NULL THEN 'N' ELSE 'Y' END AS ID_CLIENTE from TRX_HDR where NUM_TRX = '{ticket}' and TIPO_TRX in (1, 9, 10)";
		sql = sql.replace("{ticket}", ticket);
		Object[] ent = (Object[]) QueryBctRepository.executeGetSingleResult(sql);

		if (ent == null) {
			return null;
		}
		
		result.setTicket((String) ent[0]);
		result.setFecha((Date) ent[1]);
		result.setTienda(Integer.parseInt(ent[2].toString()));
		result.setCaja(Integer.parseInt(ent[3].toString()));
		result.setCajero(Float.parseFloat(ent[4].toString()));
		result.setNumTicket(Integer.parseInt(ent[5].toString()));
		result.setTipoTicket(Integer.parseInt(ent[6].toString()));
		result.setTotalPagar(new BigDecimal(ent[7].toString()));
		result.setTotalSinImpuestos(new BigDecimal(ent[8].toString()));
		result.setOriginal((String) ent[9]);
		result.setDevolucion(String.valueOf((char)ent[10]));			

		return result;
	}
	
	public TicketEntity findByTicketNC(String ticket) {

		TicketEntity result = new TicketEntity();
		String sql = "select NUM_TRX, FECHA_TRX, NUM_TIENDA, NUM_CAJA, COD_CAJERO, NUM_TICKET, TIPO_TRX, MNT_TOTAL_A_PAGAR, MNT_TOT_SN_IMPTOS, TRX_ORIGINAL, CASE WHEN TRX_ORIGINAL IS NULL THEN 'N' ELSE 'Y' END AS ID_CLIENTE from TRX_HDR where NUM_TRX = '{ticket}' and TIPO_TRX in (9, 10)";
		sql = sql.replace("{ticket}", ticket);
		Object[] ent = (Object[]) QueryBctRepository.executeGetSingleResult(sql);

		if (ent == null) {
			return null;
		}
		
		result.setTicket((String) ent[0]);
		result.setFecha((Date) ent[1]);
		result.setTienda(Integer.parseInt(ent[2].toString()));
		result.setCaja(Integer.parseInt(ent[3].toString()));
		result.setCajero(Float.parseFloat(ent[4].toString()));
		result.setNumTicket(Integer.parseInt(ent[5].toString()));
		result.setTipoTicket(Integer.parseInt(ent[6].toString()));
		result.setTotalPagar( new BigDecimal(ent[7].toString()));
		result.setTotalSinImpuestos( new BigDecimal(ent[8].toString() ));
		result.setOriginal((String) ent[9]);
		result.setDevolucion(String.valueOf((char)ent[10]));			

		return result;
	}
	
}
