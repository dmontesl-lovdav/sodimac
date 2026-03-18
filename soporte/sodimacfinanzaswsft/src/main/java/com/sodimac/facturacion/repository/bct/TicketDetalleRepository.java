package com.sodimac.facturacion.repository.bct;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.bct.TicketDetalleEntity;

@Repository("ticketDetalleRepository")
public class TicketDetalleRepository {

	Logger logger = LoggerFactory.getLogger(TicketRepository.class);
	
	public List<TicketDetalleEntity> findByDocumento(String documento, String fechaPeriodo) {

		String sql = "select NUM_TRX, NUM_LINEA, SKU, DV_SKU, CANTIDAD, UM, DESCRIPCION, nvl(PRECIO_UNITARIO, 0) as PRECIO_UNITARIO, nvl(PRECIO_TOTAL, 0) as PRECIO_TOTAL, nvl(NUM_DOC_CANAL, 0) as NUM_DOC_CANAL, CASE WHEN NUM_DOC_CANAL IS NULL THEN 'Y' ELSE 'N' END AS COD_PROVEEDOR from TRX_DET where NUM_DOC_CANAL LIKE '%{documento}' and TO_DATE(FECHA_TRX, 'dd-mm-yy') >= TO_DATE('{fechaPeriodo}', 'dd-mm-yy') order by NUM_DOC_CANAL, PRECIO_UNITARIO desc";
		sql = sql.replace("{documento}", documento).replace("{fechaPeriodo}", fechaPeriodo);
		@SuppressWarnings("unchecked")
		List<Object[]> list = (List<Object[]>) QueryBctRepository.executeGetResultList(sql);
		
		List<TicketDetalleEntity> result = new ArrayList<>();
				
		for (Object[] ent : list) {
			TicketDetalleEntity item = new TicketDetalleEntity();
			item.setTicket((String) ent[0]);
			item.setId(Integer.parseInt(ent[1].toString()));
			item.setSku(Integer.parseInt(ent[2].toString()));
			item.setDvSku(String.valueOf((char)ent[3]));
			item.setCantidad(Float.parseFloat(ent[4].toString()));
			item.setUm((String) ent[5]);
			item.setDescripcion((String) ent[6]);
			item.setPrecioTotal(Float.parseFloat(ent[7].toString()));
			item.setPrecioUnitario(Float.parseFloat(ent[8].toString()));
			item.setNumeroDocumento(Long.parseLong(ent[9].toString()));
			item.setPortable(String.valueOf((char)ent[10]));

			result.add(item);
		}
				
		return result;
	}
	
	public List<TicketDetalleEntity> findByTicket(String ticket) {

		String sql = "select NUM_TRX, NUM_LINEA, SKU, DV_SKU, CANTIDAD, UM, DESCRIPCION, nvl(PRECIO_UNITARIO, 0) as PRECIO_UNITARIO, nvl(PRECIO_TOTAL, 0) as PRECIO_TOTAL, nvl(NUM_DOC_CANAL, 0) as NUM_DOC_CANAL, CASE WHEN NUM_DOC_CANAL IS NULL THEN 'Y' ELSE 'N' END AS COD_PROVEEDOR from TRX_DET where NUM_TRX = '{ticket}' order by NUM_DOC_CANAL, PRECIO_UNITARIO desc";
		sql = sql.replace("{ticket}", ticket);
		@SuppressWarnings("unchecked")
		List<Object[]> list = (List<Object[]>) QueryBctRepository.executeGetResultList(sql);
		
		List<TicketDetalleEntity> result = new ArrayList<>();
		
		for (Object[] ent : list) {
			TicketDetalleEntity item = new TicketDetalleEntity();
			item.setTicket((String) ent[0]);
			item.setId(Integer.parseInt(ent[1].toString()));
			item.setSku(Integer.parseInt(ent[2].toString()));
			item.setDvSku(String.valueOf((char)ent[3]));
			item.setCantidad(Float.parseFloat(ent[4].toString()));
			item.setUm((String) ent[5]);
			item.setDescripcion((String) ent[6]);
			item.setPrecioUnitario(Float.parseFloat(ent[7].toString()));
			item.setPrecioTotal(Float.parseFloat(ent[8].toString()));
			item.setNumeroDocumento(Long.parseLong(ent[9].toString()));
			item.setPortable(String.valueOf((char)ent[10]));

			result.add(item);
		}
				
		return result;
	}

}
