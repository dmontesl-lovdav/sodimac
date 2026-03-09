package com.sodimac.cfdi.service;

import java.util.List;

import org.w3c.dom.Document;

import com.sodimac.cfdi.clientews.wsft.ClienteTicketTimbrarExpRespTYPE;
import com.sodimac.cfdi.entity.fiscal.FacturasEntity;
import com.sodimac.cfdi.model.ClientResponseTYPE;
import com.sodimac.cfdi.model.BodyComplementoCorreoTYPE;
import com.sodimac.cfdi.models.ClientesTemporalModel;
import com.sodimac.cfdi.models.FacturasMultipleModel;

public interface FacturasService {
	
	public void crearZipXmlPdf(String facturaId);
	public int existFactura(String rfc, String ticket);
	public int existFactura(String ticket);
	public List<FacturasMultipleModel> getFacturasFechas(String rfc, String email, String fechaInicial, String fechafinal, int start, int rowsPerPage);
	public int countFacturas(String rfc, String email, String fechaInicial, String fechafinal);
	public int actualizarDatos(ClientesTemporalModel clientes);
	public void crearZipMultiple(String archivoZip, String archivosZip);
	public FacturasEntity getFacturaByUuid(String uuid);
	public List<ClientesTemporalModel> obtenerDatosFactura(String rfc, String sessionId, String ticket);
	public FacturasEntity getFacturaByTicket(String ticket);
	public void eliminarArchivo(String fileName);
	public boolean existRfcEmail(String rfc, String email);
	public List<FacturasMultipleModel> getCfdiFechas(String fechaInicial, String fechafinal, String rfcEncriptado, String uuid, String ticket, int start, int rowsPerPage,String tipoComprobante, int pidOrigen, String monto);
	public boolean getCfdiExcelFechas(String fechaInicial, String fechafinal, String rfcEncriptado, String uuid, String ticket,String nombreArchivo,String tipoComprobante, int pidOrigen, String pmonto) throws Exception;
	public int countCfdi(String fechaInicial, String fechafinal, String rfcEncriptado, String uuid, String ticket);
	public int cancelar(int idFacturaPac);
	public int refacturar(ClientesTemporalModel clientes);
	public FacturasEntity getFacturaByIdFacturaPac(int IdFacturaPac);
	public String getTipodeComprobante();
	
	public String getTipoOrigen();
	
	public ClienteTicketTimbrarExpRespTYPE timbrarTicketWs (ClientesTemporalModel model);
	public int insertarLogFacturacion(ClientesTemporalModel model);
	public ClienteTicketTimbrarExpRespTYPE crearZipWs (String uuid);
	public ClienteTicketTimbrarExpRespTYPE crearBase64Ws (String uuid);
	public boolean crearZip (String uuid);
	
	public FacturasEntity getFacturaById(Integer idFactura);
	
	public Document obtenerDocumentXml(String xml);
	
	public ClienteTicketTimbrarExpRespTYPE crearBase64WsComplemento(String uuid);
	
	public boolean crearZipComplemento(String uuid);
	
	public ClientResponseTYPE<String> enviarCorreoComplemento(BodyComplementoCorreoTYPE model);
	
	public ClientResponseTYPE<String> enviarCorreoFactura(BodyComplementoCorreoTYPE model);
	
	public ClientResponseTYPE<String> enviarCorreoFactura(ClientesTemporalModel model);
	
}
