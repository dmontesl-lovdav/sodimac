package com.sodimac.facturacion.service;

import java.util.List;

import com.sodimac.facturacion.clientews.model.ClientResponseTYPE;
import com.sodimac.facturacion.clientews.wsft.ClienteObtenerTicketRespTYPE;
import com.sodimac.facturacion.clientews.wsft.ClienteTicketTimbrarExpRespTYPE;
import com.sodimac.facturacion.entity.FacturasEntity;
import com.sodimac.facturacion.models.ClientesTemporalModel;
import com.sodimac.facturacion.models.FacturasMultipleModel;

public interface FacturasService {
	
	public int existFactura(String rfc, String ticket);
	public int existFactura(String ticket);
	public List<FacturasMultipleModel> getFacturasFechas(String rfc, String email, String fechaInicial, String fechafinal, int start, int rowsPerPage);
	public int countFacturas(String rfc, String email, String fechaInicial, String fechafinal);
	public void crearZipMultiple(String archivoZip, String archivosZip);
	public FacturasEntity getFacturaByUuid(String uuid);
	public List<ClientesTemporalModel> obtenerDatosFactura(String rfc, String sessionId, String ticket);
	public FacturasEntity getFacturaByTicket(String ticket);
	public void eliminarArchivo(String fileName);
	public boolean existRfcEmail(String rfc, String email);
	public String getClaveUsoCfdiNC(String ticket);
	public ClienteTicketTimbrarExpRespTYPE timbrarTicketWs (ClientesTemporalModel model);
	public int insertarLogFacturacion(ClientesTemporalModel model);
	public ClienteTicketTimbrarExpRespTYPE crearZipWs (String uuid);
	public ClienteTicketTimbrarExpRespTYPE crearPdfWs (String uuid);
	
	public ClienteObtenerTicketRespTYPE obtenerTicket(String ordenCompra);
	
	public void liberarTicket(String ticket);
	
	public ClientResponseTYPE<String> enviarCorreo(ClientesTemporalModel model);
	
	public ClientResponseTYPE<String> enviarCorreoToken(ClientesTemporalModel model);
	
	public String getDatosCfdiNC(String ticket);
}
