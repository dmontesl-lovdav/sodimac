package com.sodimac.facturacion.service;

import java.util.List;

import javax.mail.AuthenticationFailedException;
import javax.xml.transform.TransformerException;

import com.sodimac.facturacion.cliente.ClienteConsultarFacturaIdExpRespTYPE;
import com.sodimac.facturacion.cliente.ClienteTimbrarTipoExpRespTYPE;
import com.sodimac.facturacion.clientews.configuracion.EmisorYLugarExpedicionDto;
import com.sodimac.facturacion.clientews.configuracion.VersionTimbradoRes;
import com.sodimac.facturacion.clientews.wcfemision.org.datacontract.schemas._2004._07.cfdiWcfEmisionServicio_Sodimac_Clases.RespuestaXml;
import com.sodimac.facturacion.clientews.wcfemision.org.datacontract.schemas._2004._07.cfdiWcfEmisionServicio_Sodimac_Clases.Resultado;
import com.sodimac.facturacion.entity.fac.FacturasEntity;
import com.sodimac.facturacion.models.ClientesTemporalModel;
import com.sodimac.facturacion.models.FacturasMultipleModel;

public interface FacturasService {
	
	public int timbrarTipoProceso(ClientesTemporalModel clientesTemporal, int tipoProceso) throws NumberFormatException, AuthenticationFailedException, TransformerException;
	public Resultado timbrar(String xml) throws NumberFormatException;
	public RespuestaXml getXml(String facturaId);
	public Resultado getPdf(String facturaId);
	public void crearZipXmlPdf(String facturaId);
	public String transformarXmlTicketXmlPac(com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2022.ClienteTicketObtenerExpRespTYPE ticket, ClientesTemporalModel clientes);
	public String transformarXmlTicketXmlPac(String xml, ClientesTemporalModel clientes) throws TransformerException;
	public int existFactura(String rfc, String ticket);
	public int existFactura(String ticket);
	public List<FacturasMultipleModel> getFacturasFechas(String rfc, String email, String fechaInicial, String fechafinal, int start, int rowsPerPage);
	public int countFacturas(String rfc, String email, String fechaInicial, String fechafinal);
	public void crearZipMultiple(String archivoZip, String archivosZip);
	public FacturasEntity getFacturaByUuid(String uuid);
	public boolean crearPdfFromXml(String nombreArchivo, String xml);
	public boolean crearArchivoXml(String fileName, String xml);
	public boolean crearArchivoPdf(String fileName, String pdf64);
	public List<ClientesTemporalModel> obtenerDatosFactura(String rfc, String sessionId, String ticket);
	public FacturasEntity getFacturaByTicket(String ticket);
	public void eliminarArchivo(String fileName);
	public boolean existRfcEmail(String rfc, String email);
	public String getClaveUsoCfdiNC(String ticket);
	public void getInformacionFacturaRelacionadaByTicket(String ticket, ClientesTemporalModel model);
	public int cancelar(String facturaId, ClientesTemporalModel model);
	public FacturasEntity getFacturaByIdFacturaPac(String IdFacturaPac);
	public String transformarXmlTicketXmlPac(ClientesTemporalModel clientes);
	public void crearZip(String uuid);
	public void crearPdf(String uuid);
	public void actualizarEstatusLog (ClientesTemporalModel model, int estatus);
	public String obtenerBase64(String uuid);
	public EmisorYLugarExpedicionDto obtenerDatosEmisionExpedicion (String rfc, int idTienda, String version, String tipoComprobanteAPI, int idAplicacion, String formaPago);
	public ClienteTimbrarTipoExpRespTYPE timbrarTipo(String tipoTimbrado, String xmlBase64);
	public ClienteTimbrarTipoExpRespTYPE timbrarTipo40(String tipoTimbrado, String xmlBase64);
	public Resultado timbrar(String xmlBase64, String tipoTimbrado) throws NumberFormatException;
	public RespuestaXml getXml(String facturaId, String tipoTimbrado);
	public VersionTimbradoRes obtenerVersionActiva (int idAplicacion);
	public com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2022.ClienteTicketObtenerExpRespTYPE obtenerDetalleTicket(ClientesTemporalModel model);
	public com.sodimac.facturacion.clientews.wcfemision40.org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac.Resultado timbrar40(String xml) throws NumberFormatException;
	public com.sodimac.facturacion.clientews.wcfemision40.org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac.Resultado timbrar40(String xml, String tipoTimbrado) throws NumberFormatException;
	public ClienteConsultarFacturaIdExpRespTYPE consultarTipo(String facturaId, String tipo);
	public com.sodimac.facturacion.clientews.wcfemision40.org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac.RespuestaXml getXml40(String facturaId);
	public com.sodimac.facturacion.clientews.wcfemision40.org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac.RespuestaXml getXml40(String facturaId, String tipoTimbrado);
	public boolean crearPdfComplementoFromXml(String nombreArchivo, String xml);
	public void crearPdfComplemento(String uuid);
	public String obtenerBase64Complemento(String uuid);
	
	public int insertarFacturaMail(Integer pIdFacturaPac);
	
	public int actualizarFacturaMail(Integer pIdFacturaPac, Integer pEstatus);
}
