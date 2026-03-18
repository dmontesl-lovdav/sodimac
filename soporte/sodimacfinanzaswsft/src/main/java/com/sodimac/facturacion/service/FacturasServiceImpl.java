package com.sodimac.facturacion.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipOutputStream;

import javax.mail.AuthenticationFailedException;
import javax.net.ssl.SSLContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

//import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ClienteTicketObtenerExpRespTYPE;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2022.ConceptoImpuestosTYPE;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2022.ConceptoImpuestosTrasladosTYPE;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2022.ConceptoImpuestosTrasladosTYPE.Traslado;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2022.ConceptosTYPE;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2022.ConceptosTYPE.Concepto;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2022.ImpuestosTYPE;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2022.ImpuestosTrasladosTYPE;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.Headers;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.sodimac.facturacion.cliente.ClienteConsultarFacturaIdExpRespTYPE;
import com.sodimac.facturacion.cliente.ClienteConsultarFacturaIdExpRespTYPE.Respuesta;
import com.sodimac.facturacion.cliente.ClienteTimbrarTipoExpRespTYPE;
import com.sodimac.facturacion.cliente.ws.model.ClientResponseTYPE;
import com.sodimac.facturacion.clientews.configuracion.EmisorReq;
import com.sodimac.facturacion.clientews.configuracion.EmisorYLugarExpedicionDto;
import com.sodimac.facturacion.clientews.configuracion.ResponseBaseDto;
import com.sodimac.facturacion.clientews.configuracion.VersionTimbradoReq;
import com.sodimac.facturacion.clientews.configuracion.VersionTimbradoRes;
import com.sodimac.facturacion.clientews.wcfemision.org.datacontract.schemas._2004._07.cfdiWcfEmisionServicio_Sodimac_Clases.RespuestaXml;
import com.sodimac.facturacion.clientews.wcfemision.org.datacontract.schemas._2004._07.cfdiWcfEmisionServicio_Sodimac_Clases.Resultado;
import com.sodimac.facturacion.component.ActividadesComponent;
import com.sodimac.facturacion.component.ErrorComponent;
import com.sodimac.facturacion.entity.bct.TicketEntity;
import com.sodimac.facturacion.entity.fac.ConfDatosEmisorEntity;
import com.sodimac.facturacion.entity.fac.FacturasEntity;
import com.sodimac.facturacion.entity.fac.ListaFacturasEntity;
import com.sodimac.facturacion.entity.fac.catalogospdf.CatFormaPagoEntity;
import com.sodimac.facturacion.entity.fac.catalogospdf.CatMetodoPagoEntity;
import com.sodimac.facturacion.entity.fac.catalogospdf.CatMonedaEntity;
import com.sodimac.facturacion.entity.fac.catalogospdf.CatTipoDeComprobanteEntity;
import com.sodimac.facturacion.entity.fac.catalogospdf.PacsEntity;
import com.sodimac.facturacion.entity.fac.catalogosreb.CatDatosConceptosRebEntity;
import com.sodimac.facturacion.entity.fac.catalogosreb.CatDatosImpuestosRebEntity;
import com.sodimac.facturacion.entity.fac.catalogosreb.ConfDatosEmisorRebEntity;
import com.sodimac.facturacion.entity.fis.ComplementosEntity;
import com.sodimac.facturacion.models.ClientesTemporalModel;
import com.sodimac.facturacion.models.FacturasMultipleModel;
import com.sodimac.facturacion.models.Mes;
import com.sodimac.facturacion.models.Periodicidad;
import com.sodimac.facturacion.models.UsoDeCfdi;
import com.sodimac.facturacion.repository.bct.EstFacturasRepository;
import com.sodimac.facturacion.repository.fac.CatRfcEmisorRepository;
import com.sodimac.facturacion.repository.fac.ConfDatosEmisorRepository;
import com.sodimac.facturacion.repository.fac.FacturasRepository;
import com.sodimac.facturacion.repository.fac.catalogosreb.CatDatosConceptosRebRepository;
import com.sodimac.facturacion.repository.fac.catalogosreb.CatDatosImpuestosRebRepository;
import com.sodimac.facturacion.repository.fac.catalogosreb.ConfDatosEmisorRebRepository;
import com.sodimac.facturacion.repository.fis.ComplementosRepository;
import com.sodimac.facturacion.service.catalogospdf.CatFormaPagoService;
import com.sodimac.facturacion.service.catalogospdf.CatMetodoPagoService;
import com.sodimac.facturacion.service.catalogospdf.CatMonedaService;
import com.sodimac.facturacion.service.catalogospdf.CatRegimenFiscalService;
import com.sodimac.facturacion.service.catalogospdf.CatTipoDeComprobanteService;
import com.sodimac.facturacion.service.catalogospdf.PacsService;
import com.sodimac.facturacion.service.ws.ConfirmarService;
import com.sodimac.facturacion.service.ws.Emision40Service;
import com.sodimac.facturacion.service.ws.EmisionService;
import com.sodimac.facturacion.util.Convert;
import com.sodimac.facturacion.util.NumeroaLetras;
import com.sodimac.facturacion.util.QrCodeGenerator;
import com.sodimac.facturacion.util.UtilsFechas;
import com.sodimac.facturacion.util.UtilsFile;
import com.sodimac.facturacion.util.UtilsString;
import com.sodimac.facturacion.util.enums.EAplicacion;
import com.sodimac.facturacion.util.enums.ECodigo;
import com.sodimac.facturacion.util.enums.EFacturas;
import com.sodimac.facturacion.util.enums.EProceso;
import com.sodimac.facturacion.util.enums.EStatusConsultarFacturaId;
import com.sodimac.facturacion.util.enums.EVersionCFDI;

@Service
public class FacturasServiceImpl implements FacturasService {

	Logger logger = LoggerFactory.getLogger(FacturasServiceImpl.class);
	private static final String CFDI_VERSION_40 = "4.0"; 
	private static final String DECIMALES_PRECIO="0.00";
	private static final String DECIMALES_TASA="0.000000";
	private static final String PUNTOS_CES = "102";
	boolean esPuntosCES = false;
	
	@Autowired
	private ConfiguracionFacturacionService configFacService;
	@Autowired
	private FacturasRepository facturasRepository;
	@Autowired
	private CatUsosCfdiService catUsosCfdiService;
	@Autowired
	private CatRegimenFiscalService catRegimenFiscalService;
	@Autowired
	private CatTipoDeComprobanteService catTipoDeComprobanteService;
	@Autowired
	private CatMonedaService catMonedaService;
	@Autowired
	private PacsService pacsService;
	@Autowired
	private CatMetodoPagoService catMetodoPagoService;	
	@Autowired
	private CatFormaPagoService catFormaPagoService;
	@Autowired
	private EmisionService emisionService;
	@Autowired
    ResourceLoader resourceLoader;
	@Autowired
	private ErrorComponent errorComponent;
	@Autowired
	private SeguridadService seguridadService;
	@Autowired
	private EstFacturasRepository estFacturasRepository;
	@Autowired
	private ConfDatosEmisorRepository confDatosEmisorRepository;
	@Autowired
	private ConfDatosEmisorRebRepository confDatosEmisorRebRepository;
	@Autowired
	private CatDatosConceptosRebRepository catDatosConceptosRebRepository;
	@Autowired
	private CatDatosImpuestosRebRepository catDatosImpuestosRebRepository;
	@Autowired
	private ActividadesComponent actividadesModel;
	@Autowired
	private ClientesService clientesService;
	@Autowired
	private ListaFacturasService listaFacturasService;
	@Autowired
	private ConfirmarService confirmarService;
	@Autowired
	private MailSenderService mailSenderService;
	@Autowired
	private TicketsService ticketsService;
	@Autowired
	private RebatesService rebatesService;
	@Autowired
	private ClientesTemporalService clientesTemporalService;
	@Autowired
	private CatRfcEmisorRepository catRfcEmisorRepository;
    @Autowired
    private ModelMapper modelMapper;
	@Autowired
	private Emision40Service emision40Service;
	
	@Autowired
	private ConfiguracionService configuracionService;
	
	@Autowired
	private ComplementosRepository complementosRepository;
	
	@Autowired
	private CorreoFacturacionService correoFacturacionService;

	@Autowired
	private TicketsBctService ticketsBctService;
	
	String UrlLogin = "";
	String UrlDatosEmisorExpedicion = "";
	String UrlVersionActiva = "";
	String userName = "";
	String userPass = "";
	String headerValue = "";

	@Override
	@Transactional
	public int timbrarTipoProceso(ClientesTemporalModel model, int tipoProceso) throws AuthenticationFailedException, NumberFormatException, TransformerException {
		
		String uuid = "";
		String uuidRelacionado = "";
		String codigo = "0";
		String xml = "";
		String facturaId = "";
		int pacDefault = 0;
		com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2022.ClienteTicketObtenerExpRespTYPE resultTicket = null;
		Object[] arrResultTicket = {resultTicket};
		Resultado result = null;
		com.sodimac.facturacion.clientews.wcfemision40.org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac.Resultado result40 = null;
		int codigoRetorno = 300;
		RespuestaXml resultXml = null;
		com.sodimac.facturacion.clientews.wcfemision40.org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac.RespuestaXml resultXml40 = null;
		//List<String> datosArr = new ArrayList <String>();
		int idOrigen = 1;
		logger.info("Inicia timbrarTipoProceso ticket: " + model.getTicket());

		boolean timbradoNormal = (tipoProceso == EProceso.TimbradoNormal.getValor());
		boolean autofacturador = (tipoProceso == EProceso.Autofacturador.getValor());
		boolean pendientePac = (tipoProceso == EProceso.PendientePac.getValor());
		boolean pendienteNoBct = (tipoProceso == EProceso.NoBct.getValor());
		boolean batch = (pendienteNoBct || pendientePac);
		boolean refacturacion = (tipoProceso == EProceso.Refacturacion.getValor());
		boolean rebates = (tipoProceso == EProceso.Rebates.getValor());
		boolean sincronizacion = (tipoProceso == EProceso.Sincronizacion.getValor());

		boolean version40 = true;
		model.setVersionFacturacionSat("4.0");
		
		/*if(sincronizacion) {
			int idFactura33 = 999999;
			if (model.getIdFacturaPac() < idFactura33) {
				version40 = true;
				model.setVersionFacturacionSat("4.0");
			} else {
				version40 = false;
				model.setVersionFacturacionSat("3.3");
			}
		} else {
			version40 = (model.getVersionFacturacionSat().equals("4.0"));
		}*/
		
		logger.info("Respuesta por la versión del SAT: " + model.getVersionFacturacionSat());
		model.setUuid("");
		//Se comento por el proceso de refacturaci\u00f3n
		//model.setUuidRelacionado("");
		model.setXml("");
		
		if (rebates) idOrigen = 4;
		model.setIdOrigen(idOrigen);

		errorComponent.setTicket(model.getTicket());
		errorComponent.setRfc(model.getRfc());
		errorComponent.setPagina("timbrarTipoProceso");
		
		actividadesModel.setRfc(model.getRfc());

		pacDefault = pacsService.getIdDefault();
		model.setPac(pacDefault);
		
		//actividadesModel.registrarActividad(6, null, "timbrarTipoProceso");
		if (pacDefault == 0) {
			errorComponent.guardarLog("No hay un pac activo");
			return 54;
		}
		
		if (!sincronizacion) {
			if (!refacturacion) {
				int existFactura = ticketsService.validarTicket(model.getTicket());
				if (existFactura==130) {
					return 51;
				}			
				if (!batch && existFactura==131) {
					return 52;
				}			
			}
			
			clientesService.inicializarRfcTicket(model.getRfc(), model.getTicket());
			logger.info("insertarTemporal: " + model.getTicket());
			logger.info(model.toString());
			clientesTemporalService.insertarTemporal(model);			
		}
		
		if (pendientePac || sincronizacion) {
			errorComponent.setIdFacturaPac(model.getIdFacturaPac());
			
			if (model.getIdFacturaPac()>0) {
				facturaId = Integer.toString(model.getIdFacturaPac());
			} else {
				errorComponent.guardarLog("FacturaId no existe");
				actualizarEstatusLog(model, EFacturas.EnEspera.getValor());
				actualizarReintentos(3, 3, model.getRfc(), model.getTicket());
				return 202;
			}
			
			actualizarEstatusLog(model, EFacturas.EnProcesoFacturacion.getValor());
						
		} else {
//			datosArr.clear();
//			datosArr.add(model.getTicket());
//			actividadesModel.registrarActividad(19, datosArr, "timbrarTipoProceso");
			if (!autofacturador && !pendienteNoBct) listaFacturasService.addListaFacturas(model.getTicket(), model.getTotal(), actividadesModel.getSessionId());
					
			if (!refacturacion && !rebates) {
				actualizarEstatusLog(model, EFacturas.ConsultaTicket.getValor());
				if (pendienteNoBct) actividadesModel.registrarActividad(42, null, "timbrarTipoProceso");
				
				logger.info("Consulta WS de chile para obtener datos del ticket: " + model.getTicket());
				int resultWSChile = leerDetalleTicketWS(arrResultTicket, model, tipoProceso);
				if (model.getIdAplicacion() == EAplicacion.NotasCredito.getValor()) version40 = (model.getVersionFacturacionSat().equals("4.0"));
				if (resultWSChile > 0) return resultWSChile;
				resultTicket = (com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2022.ClienteTicketObtenerExpRespTYPE) arrResultTicket[0];
				logger.info("Regresa WS de chile para obtener datos del ticket: " + model.getTicket());
				actualizarEstatusLog(model, EFacturas.ObtieneTicket.getValor());

				// 2024-09-25 Si el proceso no es batch, el total no debe ser igual a 0 en caso de que la forma de pago sea diferente de "102" (pagado con puntos CES)
				if (!batch && !esPuntosCES && model.getTotal().compareTo(BigDecimal.ZERO) == 0) {
					return 116;
				}
				
			}
				
//			datosArr.clear();
//			datosArr.add(model.getTicket());
//			actividadesModel.registrarActividad(16, datosArr, "timbrarTipoProceso");

			if (!rebates) {
				if (refacturacion) {
					FacturasEntity factura = getFacturaByIdFacturaPac(Integer.toString(model.getIdFacturaPac()));
					xml = seguridadService.desencriptar(factura.getXml());
					xml = transformarXmlTicketXmlPac(xml, model);	//DML XXXXXXXXXXXXX			
				} else {
						xml = transformarXmlTicketXmlPac(resultTicket, model);
					}
			} else {
				xml = transformarXmlTicketXmlPac(model);
			}
			
			errorComponent.setXml(xml);
			
			if (autofacturador || timbradoNormal || pendienteNoBct || refacturacion) {
				codigo = "3";
				confirmarService.confirmar(model.getTicket(), uuid, codigo);							
			}

			actualizarEstatusLog(model, EFacturas.EnProcesoFacturacion.getValor());
			
			logger.info("Version 40: " + version40);
			if (version40) {
				try {
//					datosArr.clear();
//					datosArr.add(model.getTicket());
//					actividadesModel.registrarActividad(17, datosArr , "timbrarTipoProceso");
					
					logger.info("Valida que el ticket no este facturado ticket: " + model.getTicket());

					if (!sincronizacion) {
						if (!refacturacion) {
							int existFactura = existFactura(model.getRfc(), model.getTicket());
							if (existFactura==1) {
								return 51;
							}
							String ticketFacturado = estFacturasRepository.ValidateInvoiceByTicket(model.getTicket());
							if (ticketFacturado != null && !ticketFacturado.equals("")) {
								return 51;
							}							
						}
					}
					
					logger.info("Timbrar 4.0 ticket: " + model.getTicket());
					result40 = timbrar40(xml);
					if (result40 == null || result40.getFacturaId()==null) {
						if (result40 != null) {
							errorComponent.guardarLog(result40, model);	
						}
						
//						datosArr.clear();
//						datosArr.add(pacsService.getById(model.getPac()).getNombrePac() );
//						actividadesModel.registrarActividad(18, datosArr, "timbrarTipoProceso");
						actualizarEstatusLog(model, EFacturas.EnEspera.getValor());
						if (pendienteNoBct) actualizarReintentos(1, 1, model.getRfc(), model.getTicket());
						return 202;
					}
					
					
				} catch (Exception e) {
					errorComponent.guardarLog(e);

//					datosArr.clear();
//					datosArr.add(pacsService.getById(model.getPac()).getNombrePac() );
//					actividadesModel.registrarActividad(18, datosArr , "timbrarTipoProceso");
					
					actualizarEstatusLog(model, EFacturas.EnEspera.getValor());
					if (pendienteNoBct) actualizarReintentos(1, 1, model.getRfc(), model.getTicket());
					
					return 202;
				}
				
				facturaId = result40.getFacturaId();
				logger.info("facturaId: " + facturaId +" ticket: " + model.getTicket());
			} else {
				try {
//					datosArr.clear();
//					datosArr.add(model.getTicket());
//					actividadesModel.registrarActividad(17, datosArr , "timbrarTipoProceso");
					
					logger.info("Valida que el ticket no este facturado ticket: " + model.getTicket());

					if (!sincronizacion) {
						if (!refacturacion) {
							int existFactura = existFactura(model.getRfc(), model.getTicket());
							if (existFactura==1) {
								return 51;
							}			
							String ticketFacturado = estFacturasRepository.ValidateInvoiceByTicket(model.getTicket());
							if (ticketFacturado != null && !ticketFacturado.equals("")) {
								return 51;
							}
						}
					}
					
					logger.info("Timbrar 3.3 ticket: " + model.getTicket());
					result = timbrar(xml);
					if (result == null || result.getFacturaId()==null) {
//						datosArr.clear();
//						datosArr.add(pacsService.getById(model.getPac()).getNombrePac() );
//						actividadesModel.registrarActividad(18, datosArr, "timbrarTipoProceso");
						actualizarEstatusLog(model, EFacturas.EnEspera.getValor());
						if (pendienteNoBct) actualizarReintentos(1, 1, model.getRfc(), model.getTicket());
						return 202;
					}
					
					
				} catch (Exception e) {
					errorComponent.guardarLog(e);

//					datosArr.clear();
//					datosArr.add(pacsService.getById(model.getPac()).getNombrePac() );
//					actividadesModel.registrarActividad(18, datosArr , "timbrarTipoProceso");
					
					actualizarEstatusLog(model, EFacturas.EnEspera.getValor());
					if (pendienteNoBct) actualizarReintentos(1, 1, model.getRfc(), model.getTicket());
					
					return 202;
				}
				
				facturaId = result.getFacturaId();
				logger.info("facturaId: " + facturaId + " ticket: " + model.getTicket());
			}

			
			model.setIdFacturaPac(Integer.parseInt(facturaId));
			errorComponent.setIdFacturaPac(Integer.parseInt(facturaId));
		}

		logger.info("DETECTNO, se recupera XML y PDF ticket: " + model.getTicket());
		if (version40) {
			resultXml40 = getXml40(facturaId);
			logger.info("XML recuperado ticket: " + model.getTicket());
			if (resultXml40 == null || resultXml40.getUuid()==null || (resultXml40.getEstatusId() != null && !resultXml40.getEstatusId().equals("4"))) {
				if (resultXml40 == null) {
					errorComponent.guardarLog("ResultXml es nulo");
				} else {
					errorComponent.guardarLog(resultXml40, model);
				}
				
				if (resultXml40 != null && resultXml40.getErrorDesc() != null) {
					if (resultXml40.getErrorDesc().contains("no existe en la lista de RFC inscritos no cancelados")) {
//						datosArr.clear();
//						actividadesModel.registrarActividad(8, datosArr , "timbrarTipoProceso");
						actualizarEstatusLog(model, EFacturas.RfcInvalidoNoApto.getValor());
						if (batch) actualizarReintentos(1, 1, model.getRfc(), model.getTicket());
						return 107;
					}
				}

				actualizarEstatusLog(model, EFacturas.EnEspera.getValor());
				if (batch) actualizarReintentos(1, 1, model.getRfc(), model.getTicket());
				return 202;
			}
		
			uuid = resultXml40.getUuid();
			xml = resultXml40.getXml();
			
		} else {
			resultXml = getXml(facturaId);	
			logger.info("XML recuperado ticket: " + model.getTicket());

			if (resultXml == null || resultXml.getUuid()==null || (resultXml.getEstatusId() != null && !resultXml.getEstatusId().contentEquals("4"))) {
				if (resultXml == null) {
					errorComponent.guardarLog("ResultXml es nulo");
				} else {
					errorComponent.guardarLog(resultXml, model);
				}
				
				if (resultXml != null && resultXml.getErrorDesc() != null) {
					if (resultXml.getErrorDesc().contains("no existe en la lista de RFC inscritos no cancelados")) {
//						datosArr.clear();
//						actividadesModel.registrarActividad(8, datosArr , "timbrarTipoProceso");
						actualizarEstatusLog(model, EFacturas.RfcInvalidoNoApto.getValor());
						if (batch) actualizarReintentos(1, 1, model.getRfc(), model.getTicket());
						return 107;
					}
				}

				actualizarEstatusLog(model, EFacturas.EnEspera.getValor());
				if (batch) actualizarReintentos(1, 1, model.getRfc(), model.getTicket());
				return 202;
			}
		
			uuid = resultXml.getUuid();
			xml = resultXml.getXml();
		}


		if ((autofacturador || timbradoNormal || pendienteNoBct || refacturacion) && (uuid != null && !uuid.isEmpty())) {
			codigo = "2";
			confirmarService.confirmar(model.getTicketBct(), uuid, codigo);			
		}
		
		if (xml == null  || xml.isEmpty()) {
			errorComponent.guardarLog("El Xml esta vacio");
			actualizarEstatusLog(model, EFacturas.EnEspera.getValor());
			if (batch) actualizarReintentos(1, 1, model.getRfc(), model.getTicket());
			return 202;
		}
		
		Document document = UtilsFile.ObtenerDocumentXml (xml);
		
		if (sincronizacion) {
			String ticket = estFacturasRepository.findTicketByUuid(uuid);
			if (ticket==null) {
				errorComponent.guardarLog("No se encontro uuid en Est_Facturas");
				return 202;
			}
			model.setTicket(ticket);
			model.setTicketBct(ticket);

			Node nNode = document.getElementsByTagName("cfdi:Emisor").item(0);
	        Element eElement = (Element) nNode;
	        String rfc = eElement.getAttribute("Rfc");
	        if (catRfcEmisorRepository.findByRfc(rfc) == null) {
	        	errorComponent.guardarLog("No se encontro rfc Emisor en el catalogo de RFC validos");
				return 202;        	
	        }

	        nNode = document.getElementsByTagName("cfdi:Receptor").item(0);
	        eElement = (Element) nNode;
	        rfc = eElement.getAttribute("Rfc");
	        String razonSocial = eElement.getAttribute("Nombre");
	        String idUsoCfdi = eElement.getAttribute("UsoCFDI");
	        
	        Node nNodeComprobante = document.getElementsByTagName("cfdi:Comprobante").item(0);
	        Element eElementComprobante = (Element) nNodeComprobante;
	        String versionFacturacionSat = eElementComprobante.getAttribute("Version");
	        
	        EVersionCFDI eVersionCFDI = EVersionCFDI.getVersionByDesc(versionFacturacionSat);
			UsoDeCfdi usosCfdi = catUsosCfdiService.getUsoCfdi(idUsoCfdi, eVersionCFDI.getId());
	        
	        model.setRfc(rfc);
	        model.setRazonSocial(razonSocial);
	        model.setIdUsoCfdi(idUsoCfdi);
	        model.setIdUsoCfdiReal(usosCfdi.getIdUsoCfdi());
	        
	        clientesService.inicializarRfcTicket(model.getRfc(), model.getTicket());
	        clientesTemporalService.insertarTemporal(model);	

			errorComponent.setTicket(model.getTicket());
			errorComponent.setRfc(model.getRfc());

			actualizarEstatusLog(model, EFacturas.EnProcesoFacturacion.getValor());
		}
        
		logger.info("Preparando datos para enviar correo ticket: " + model.getTicket());
		Node nNode = document.getElementsByTagName("cfdi:Comprobante").item(0);
        Element eElement = (Element) nNode;
        String versionFacturacionSat = eElement.getAttribute("Version");
        String serie = eElement.getAttribute("Serie");
        String fechaCompra = eElement.getAttribute("Fecha").replace("T", " ");
        String tipoDeComprobante = eElement.getAttribute("TipoDeComprobante");
        String metodoPago = eElement.getAttribute("MetodoPago");
        int folio = Integer.parseInt(eElement.getAttribute("Folio"));
        BigDecimal subTotal = BigDecimal.valueOf(Double.parseDouble(eElement.getAttribute("SubTotal")));
        BigDecimal total = BigDecimal.valueOf(Double.parseDouble(eElement.getAttribute("Total")));
		
		nNode = document.getElementsByTagName("tfd:TimbreFiscalDigital").item(0);
        eElement = (Element) nNode;
        String fechaTimbrado = eElement.getAttribute("FechaTimbrado").replace("T", " ");
        String versionFactura = eElement.getAttribute("Version");
        
        String transaccion = "";
        String ticketBct = "";
        
        if (serie == "RE-ODA" && idOrigen == 1) {
        	idOrigen = 4;
        	model.setIdOrigen(idOrigen);
        }
        
        switch (tipoProceso) {
        case 0: //TimbradoNormal
        case 1: //Autofacturador
        case 3: //NoBct
            transaccion = resultTicket.getDatosExtraCFD().getExtra1();
            ticketBct = resultTicket.getDatosExtraCFD().getExtra2();        	        		
            break;
        case 2: //PendientePac
        case 6: //Sincronizacion
        	transaccion = obtenerUltimos4Digitos(model.getTicketBct());
            ticketBct = model.getTicketBct();
            break;
        case 4: //Refacturacion
        	transaccion = model.getTicketBct().substring(15, 19);
	        ticketBct = model.getTicketBct();
            break;
        case 5: //Rebates
            transaccion = model.getRebate().getTicket().substring(14, 19);
            ticketBct = model.getTicket();        	
            break;
        }
        
        		
		model.setUuid(uuid);
		if (tipoDeComprobante.equals("E") && !uuidRelacionado.isEmpty()) {
			model.setUuidRelacionado(uuidRelacionado);
		}
		model.setFechaTimbrado(fechaTimbrado);
		model.setVersionFacturacionSat(versionFacturacionSat);
		model.setFechaCompra(fechaCompra);
		model.setXml(xml);
		model.setIdEstatusFactura(EFacturas.Facturado.getValor());
		model.setTicketBct(ticketBct);
		model.setVersionFactura(versionFactura);
		model.setTransaccion(transaccion);
		model.setIdComprobante(tipoDeComprobante);
		model.setMetodoPago(metodoPago);
		
		model.setSerie(serie);
		model.setFolio(folio);
		model.setSubTotal(subTotal);
		model.setTotal(total);

		insertarFactura(model);
		
		if (pendienteNoBct) actualizarReintentos(0, 6, model.getRfc(), model.getTicket());
		
		if (rebates) {
			rebatesService.actualizaTimbrado(model.getRebate().getNumeroDocumento()
					, model.getRebate().getNumeroReferencia()
					, model.getTicket()
					, model.getUuid()
					, model.getFechaTimbrado());
		}
		
		if (sincronizacion) {
			actualizaContadorTraerPac(fechaTimbrado.substring(0, 10));
			model.setIdFacturaPac(0);
		} else {
			
			//EnviarCorreo(model);	//DML Sync
			logger.info("Enviando correo de manera sincrona: " + model.getTicket());
			this.insertarFacturaMail( model.getIdFacturaPac() );
			this.correoFacturacionService.enviarCorreoAsincrono(model);
			logger.info("Regresando correo de manera sincrona: " + model.getTicket());
		}
		
		logger.info("Fin timbrarTipoProceso ticket: " + model.getTicket());
		
//		datosArr.clear();
//		datosArr.add(model.getUuid());
//		actividadesModel.registrarActividad(26, datosArr, "timbrarTipoProceso");
		
		return codigoRetorno;
	}
		
	private int leerDetalleTicketWS(Object[] arrResultTicket, ClientesTemporalModel model, int tipoProceso) {

		final String Ws_NoInformacion = "77";
		final String Ws_Error = "99";
		final String Ws_Error2 = "100";
		final String WsObtener_TicketFacturado = "2";
		final String WsObtener_TicketEnProceso = "3";
		
		boolean pendienteNoBct = (tipoProceso == EProceso.NoBct.getValor());

		try {
			logger.info("Inicia leerDetalleTicketWS ticket: " + model.getTicket());
			 com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2022.ClienteTicketObtenerExpRespTYPE resultTicket = obtenerDetalleTicket(model);
			
			if (resultTicket == null) {
				actualizarEstatusLog(model, EFacturas.EnEspera.getValor());
				if (pendienteNoBct) actualizarReintentos(2, 2, model.getRfc(), model.getTicket()); 
				return 202;
			}
			
			if (resultTicket.getRespuesta().getCodigo().equals(Ws_NoInformacion)
			 || resultTicket.getRespuesta().getCodigo().equals(Ws_Error)
			 || resultTicket.getRespuesta().getCodigo().equals(Ws_Error2)) {
				actualizarEstatusLog(model, EFacturas.EnEspera.getValor());
				if (pendienteNoBct) actualizarReintentos(2, 2, model.getRfc(), model.getTicket());
				return 202;
			}
			Boolean validarEnProceso = Boolean.parseBoolean(configFacService.getConfig().get("WebService.Sodimac.ValidarEnProceso"));
			if (resultTicket.getRespuesta().getCodigo().equals(WsObtener_TicketFacturado)
			 || (validarEnProceso && resultTicket.getRespuesta().getCodigo().equals(WsObtener_TicketEnProceso))
			 ) {
				return 51;
			}
			
			if (resultTicket.getComprobante() == null) {
				actualizarEstatusLog(model, EFacturas.EnEspera.getValor());
				if (pendienteNoBct) actualizarReintentos(2, 2, model.getRfc(), model.getTicket());
				return 202;				
			}
			
			String montoObtener = validarImporteVacio(resultTicket.getComprobante().getTotales().getTotal().toString().trim());
			
			if (pendienteNoBct) {
				ListaFacturasEntity listaFacturasEntity = listaFacturasService.findByTicket(model.getTicket());
				if (listaFacturasEntity == null) {
					actualizarReintentos(0, 7, model.getRfc(), model.getTicket());
					return 55;
				}
				model.setTotal(listaFacturasEntity.getTotal());
			}

			
			if (!esPuntosCES) {
				BigDecimal bMontoObtener = new BigDecimal(montoObtener);
				BigDecimal bMonto = model.getTotal();
				BigDecimal bDiferencia = bMontoObtener.subtract(bMonto).abs();

				String rangoValido = this.configFacService.getConfig().get("Aplicacion.ToleranciaTicket");
				BigDecimal bRangoValido = new BigDecimal(rangoValido);

				boolean excedeRango = ( bDiferencia.compareTo(bRangoValido) > 0 );
				if ( excedeRango ) {
					if (pendienteNoBct) {
						EnviarCorreoMontoIncorrecto(model);
						ticketsService.eliminarTicket(model.getTicket());
					}
					return 55;
				}				
			}
			
			
			String tipoComprobante = resultTicket.getComprobante().getTipoComprobante();
			if (tipoComprobante.equals("E")) {
				TicketEntity ticketBctHdr = ticketsBctService.findByTicket(model.getTicket());
				if (ticketBctHdr != null) {
					Boolean deshabilitarTimbradoLibre = Boolean.parseBoolean(configFacService.getConfig().get("DeshabilitarTimbradoLibre"));
					if (deshabilitarTimbradoLibre && ticketBctHdr.getTipoTicket() == 10) {
						return 115;
					}
					String uuidRelacionado = estFacturasRepository.findUuidByTicket(ticketBctHdr.getOriginal());
					if (uuidRelacionado == null || uuidRelacionado.equals("")) {
						return 114;
					}
					model.setUuidRelacionado(uuidRelacionado);
					FacturasEntity factura = facturasRepository.findByUuid(uuidRelacionado);
					if (factura != null) {
						String version = factura.getVersionFacturacionSat();
						model.setVersionFacturacionSat(version);
					}
				}
			}
			
			arrResultTicket[0] = resultTicket;
			
			logger.info("Fin leerDetalleTicketWS ticket: " + model.getTicket());
			
			return 0;

		} catch (Exception e) {
			logger.error("Exception leerDetalleTicketWS ticket: " + model.getTicket() + " ", e);
			e.printStackTrace();	
			errorComponent.setTicket(model.getTicket());
			errorComponent.setRfc(model.getRfc());
			errorComponent.setPagina("saveClientesTemporal");
			errorComponent.guardarLog(e, model);
			actualizarEstatusLog(model, EFacturas.EnEspera.getValor());
			return 202;
		}		
	}
	
	@Transactional
	public Resultado timbrar(String xml) throws NumberFormatException {
		
		Resultado result = null;
		
		int contador = 0;
		int reintentosWs = Integer.parseInt(configFacService.getConfig().get("WebService.Emision.Reintentos"));
		
		do {
			try {
				Thread.sleep(1*100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			result = emisionService.timbrar(xml);
			contador += 1;

		} while (result == null && contador <= reintentosWs);
				
		if (result == null) {
			errorComponent.setXml(xml);
			errorComponent.setPagina("timbrar");
			errorComponent.guardarLog("El servicio del pack no esta disponible");
		} else if (result.getFacturaId()==null) {
			errorComponent.setXml(xml);
			errorComponent.setPagina("timbrar");
			errorComponent.guardarLog(result);
		}
		
		return result;
	}

	@Transactional
	public Resultado timbrar(String xmlBase64, String tipoTimbrado) throws NumberFormatException {
		
		Resultado result = null;
		
		int contador = 0;
		int reintentosWs = Integer.parseInt(configFacService.getConfig().get("WebService.Emision.Reintentos"));
		
		do {
			try {
				Thread.sleep(1*100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			result = emisionService.timbrar(xmlBase64, tipoTimbrado);
			contador += 1;

		} while (result == null && contador <= reintentosWs);
				
		if (result == null) {
			errorComponent.setXml(xmlBase64);
			errorComponent.setPagina("timbrar");
			errorComponent.guardarLog("El servicio del pack no esta disponible");
		} else if (result.getFacturaId()==null) {
			errorComponent.setXml(xmlBase64);
			errorComponent.setPagina("timbrar");
			errorComponent.guardarLog(result);
		}
		
		return result;
	}

	@Transactional
	public RespuestaXml getXml(String facturaId) {
		
		RespuestaXml resultXml = null;
		
		int contador = 0;
		int reintentosWs = Integer.parseInt(configFacService.getConfig().get("WebService.Emision.Xml.Reintentos"));
		
		do {
			try {
				Thread.sleep(1*2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			resultXml = emisionService.getComprobante(facturaId);
			contador += 1;
		} while ((resultXml==null
				|| (resultXml.getEstatusId() != null && resultXml.getEstatusId().contentEquals("1")) 
				|| (resultXml.getEstatusId() != null && resultXml.getEstatusId().contentEquals("3")) 
				|| (resultXml.getEstatusId() != null && resultXml.getEstatusId().contentEquals("5") && resultXml.getErrorDesc()==null))
			  && contador <= reintentosWs);
		
		return resultXml;
	}
	
	@Transactional
	public RespuestaXml getXml(String facturaId, String tipoTimbrado) {
		
		RespuestaXml resultXml = null;
		
		int contador = 0;
		int reintentosWs = Integer.parseInt(configFacService.getConfig().get("WebService.Emision.Xml.Reintentos"));
		
		do {
			try {
				Thread.sleep(1*2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			resultXml = emisionService.getComprobante(facturaId, tipoTimbrado);
			contador += 1;
		} while ((resultXml==null
				|| (resultXml.getEstatusId() != null && resultXml.getEstatusId().contentEquals("1")) 
				|| (resultXml.getEstatusId() != null && resultXml.getEstatusId().contentEquals("3")) 
				|| (resultXml.getEstatusId() != null && resultXml.getEstatusId().contentEquals("5") && resultXml.getErrorDesc()==null))
			  && contador <= reintentosWs);
		
		return resultXml;
	}

	@Transactional
	public Resultado getPdf(String facturaId) {
		
		return emisionService.getComprobantePdf(facturaId);
	}
	
	public void crearZipXmlPdf(String facturaId) {
		String path = configFacService.getConfig().get("Mail.PathFile");
		File fileNameXml = new File(path + facturaId + ".xml");
		File fileNamePdf = new File(path + facturaId + ".pdf");
		File fileNameZip = new File(path + facturaId + ".zip");

		try {
			FileOutputStream fos = new FileOutputStream(fileNameZip);
			ZipOutputStream zipOS = new ZipOutputStream(fos);

			UtilsFile.writeToZipFile(fileNameXml, zipOS);
			UtilsFile.writeToZipFile(fileNamePdf, zipOS);

			zipOS.close();
			fos.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public String transformarXmlTicketXmlPac(com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2022.ClienteTicketObtenerExpRespTYPE ticket, ClientesTemporalModel model) {

		String xmlConvertido =  "";
		boolean version40 = (model.getVersionFacturacionSat().equals("4.0"));

		String versionSat ="";
		String serie ="";
		String folio ="";
		String fecha = "";
		String formaPago ="";
		String condicionesDePago ="";
		String tipoComprobante ="";
		String exportacion = "";
		String metodoPago ="";
		
		String calleExpedicion = "";
		String noExteriorExpedicion = "";
		String noInteriorExpedicion = "";
		String coloniaExpedicion = "";
		String localidadExpedicion = "";
		String referenciaExpedicion = "";
		String municipioExpedicion = "";
		String estadoExpedicion = "";
		String paisExpedicion = "";
		String codigoPostalExpedicion = "";

		String rfcEmisor = "";
		ConfDatosEmisorEntity emisor = confDatosEmisorRepository.findRfcActivo();
		if (emisor != null) {
			rfcEmisor = emisor.getRfc();
		}

		versionSat = ticket.getComprobante().getVersion();
		serie = ticket.getComprobante().getSerie();
		folio = ticket.getComprobante().getFolio();
		fecha = ticket.getComprobante().getFecha();
		formaPago = ticket.getComprobante().getFormaPago();
		condicionesDePago = ticket.getComprobante().getCondicionesDePago();
		tipoComprobante = ticket.getComprobante().getTipoComprobante();
		exportacion = ticket.getComprobante().getExportacion();
		metodoPago = ticket.getComprobante().getMetodoPago();

		calleExpedicion = ticket.getComprobante().getCalle();
		noExteriorExpedicion = ticket.getComprobante().getNoExterior();
		noInteriorExpedicion = ticket.getComprobante().getNoInterior();
		coloniaExpedicion = ticket.getComprobante().getColonia();
		localidadExpedicion = ticket.getComprobante().getLocalidad();
		referenciaExpedicion = ticket.getComprobante().getReferencia();
		municipioExpedicion = ticket.getComprobante().getMunicipio();
		estadoExpedicion = ticket.getComprobante().getEstado();
		paisExpedicion = ticket.getComprobante().getPais();
		codigoPostalExpedicion = ticket.getComprobante().getLugarExpedicion();
			
				
		/*********Comprobante**********/
		xmlConvertido += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlConvertido += "<Comprobante version=\"" + versionSat + "\" ";
		xmlConvertido += "serie=\"" + serie + "\" ";
		xmlConvertido += "folio=\"" + folio + "\" ";
		xmlConvertido += "fecha=\"" + fecha + "\" ";
		xmlConvertido += "formaPago=\"" + formaPago + "\" ";
		xmlConvertido += "condicionesDePago=\"" + condicionesDePago + "\" ";
		xmlConvertido += "tipoDeComprobante=\"" + tipoComprobante + "\" ";
		if (version40) xmlConvertido += "exportacion=\"" + exportacion + "\" ";
		xmlConvertido += "metodoPago=\"" + metodoPago + "\" ";
				
		xmlConvertido += " lugarExpedicion=\"" + codigoPostalExpedicion + "\" >";	

		if (ticket.getComprobante().getTipoComprobante().equals("E") && !model.getUuidRelacionado().isEmpty()) {
			xmlConvertido += "<CfdiRelacionados tipoRelacion=\"01\" ordenador=\"1\" >";
			xmlConvertido += "<CfdiRelacionado uuid=\"" + model.getUuidRelacionado() + "\" ordenador = \"1\" ordenadorRelacionados = \"1\" />";
			xmlConvertido += "</CfdiRelacionados>";
		}
		
		if (ticket.getComprobante().getTipoComprobante().equals("I") && !model.getUuidRelacionado().isEmpty()) {
			xmlConvertido += "<CfdiRelacionados tipoRelacion=\"04\" ordenador=\"1\" >";
			xmlConvertido += "<CfdiRelacionado uuid=\"" + model.getUuidRelacionado() + "\" ordenador = \"1\" ordenadorRelacionados = \"1\" />";
			xmlConvertido += "</CfdiRelacionados>";
		}

		/*********Emisor**********/
		
		String nombreEmisor = "";
		String regimenFiscalEmisor = "";
		String calle = "";
		String noExterior = "";
		String noInterior = "";
		String colonia = "";
		String localidad = "";
		String referencia = "";
		String municipio = "";
		String estado = "";
		String pais = "";
		String codigoPostal = "";

		nombreEmisor = ticket.getComprobante().getEmisor().getNombre();
		regimenFiscalEmisor = ticket.getComprobante().getEmisor().getRegimenFiscal();
		calle = ticket.getComprobante().getEmisor().getCalle();
		noExterior = ticket.getComprobante().getEmisor().getNoExterior();
		noInterior = ticket.getComprobante().getEmisor().getNoInterior();
		colonia = ticket.getComprobante().getEmisor().getColonia();
		localidad = ticket.getComprobante().getEmisor().getLocalidad();
		referencia = ticket.getComprobante().getEmisor().getReferencia();
		municipio = ticket.getComprobante().getEmisor().getMunicipio();
		estado = ticket.getComprobante().getEmisor().getEstado();
		pais = ticket.getComprobante().getEmisor().getPais();
		codigoPostal = ticket.getComprobante().getEmisor().getCodigoPostal();
		
		xmlConvertido += "<Emisor rfc=\"" + rfcEmisor + "\"";
		xmlConvertido += " nombre=\"" + nombreEmisor + "\"";
		xmlConvertido += " regimenFiscal=\"" + regimenFiscalEmisor + "\"";
		
		xmlConvertido += " >";
		if (version40) {
			xmlConvertido += "<DomicilioFiscal";
			if (calle!=null && !calle.isEmpty()) xmlConvertido += " calle=\"" + calle + "\"";
			if (noExterior!=null && !noExterior.isEmpty()) xmlConvertido += " noExterior=\"" + noExterior + "\"";
			if (noInterior!=null && !noInterior.isEmpty()) xmlConvertido += " noInterior=\"" + noInterior + "\"";
			if (colonia!=null && !colonia.isEmpty()) xmlConvertido += " colonia=\"" + colonia + "\"";
			if (localidad!=null && !localidad.isEmpty()) xmlConvertido += " localidad=\"" + localidad + "\"";
			if (referencia!=null && !referencia.isEmpty()) xmlConvertido += " referencia=\"" + referencia + "\"";
			if (municipio!=null && !municipio.isEmpty()) xmlConvertido += " municipio=\"" + municipio + "\"";
			if (estado!=null && !estado.isEmpty()) xmlConvertido += " estado=\"" + estado + "\"";
			if (pais!=null && !pais.isEmpty()) xmlConvertido += " pais=\"" + pais + "\"";
			if (codigoPostal!=null && !codigoPostal.isEmpty()) xmlConvertido += " codigoPostal=\"" + codigoPostal + "\"";			
			xmlConvertido += " />";
			
			xmlConvertido += "<ExpedidoEn";
			if (calleExpedicion!= null && !calleExpedicion.isEmpty()) xmlConvertido += " calle=\"" + calleExpedicion + "\"";
			if (noExteriorExpedicion!= null && !noExteriorExpedicion.isEmpty()) xmlConvertido += " noExterior=\"" + noExteriorExpedicion + "\"";
			if (noInteriorExpedicion!= null && !noInteriorExpedicion.isEmpty()) xmlConvertido += " noInterior=\"" + noInteriorExpedicion + "\"";
			if (coloniaExpedicion!= null && !coloniaExpedicion.isEmpty()) xmlConvertido += " colonia=\"" + coloniaExpedicion + "\"";
			if (localidadExpedicion!= null && !localidadExpedicion.isEmpty()) xmlConvertido += " localidad=\"" + localidadExpedicion + "\"";
			if (referenciaExpedicion!= null && !referenciaExpedicion.isEmpty()) xmlConvertido += " referencia=\"" + referenciaExpedicion + "\"";
			if (municipioExpedicion!= null && !municipioExpedicion.isEmpty()) xmlConvertido += " municipio=\"" + municipioExpedicion + "\"";
			if (estadoExpedicion!= null && !estadoExpedicion.isEmpty()) xmlConvertido += " estado=\"" + estadoExpedicion + "\"";
			if (paisExpedicion!= null && !paisExpedicion.isEmpty()) xmlConvertido += " pais=\"" + paisExpedicion + "\"";
			xmlConvertido += " codigoPostal=\"" + codigoPostalExpedicion + "\"";	
			xmlConvertido += " />";
		}
		xmlConvertido += " </Emisor>";
		
		/*********Receptor**********/
		xmlConvertido += "<Receptor rfc=\"" + UtilsString.convertirCaracteresXml(model.getRfc()) + "\" ";
		xmlConvertido += "nombre=\"" + UtilsString.convertirCaracteresXml(model.getRazonSocial()) + "\" ";
		xmlConvertido += "usoCfdi=\"" + model.getIdUsoCfdi() + "\"";
		if (version40) {
			if (!model.getRegimenFiscal().isEmpty()) xmlConvertido += " regimenFiscalReceptor=\"" + model.getRegimenFiscal() + "\"";
			if (!model.getCodigoPostal().isEmpty()) xmlConvertido += " domicilioFiscalReceptor=\"" + model.getCodigoPostal() + "\"";
		}
		xmlConvertido += " >";	

		xmlConvertido += " </Receptor>";
					
		/*********Conceptos**********/
		xmlConvertido += "<Conceptos>";

		boolean tieneImpuestos = false;
		
		for (int i = 0; i < ticket.getComprobante().getConceptos().getConcepto().size(); i++)
		{
			xmlConvertido += "<Concepto claveProdServ=\"" + ticket.getComprobante().getConceptos().getConcepto().get(i).getClaveProdServ() + "\" ";
			xmlConvertido += "noIdentificacion=\"" + ticket.getComprobante().getConceptos().getConcepto().get(i).getNoIdentificacion() + "\" ";
			xmlConvertido += "cantidad=\"" + ticket.getComprobante().getConceptos().getConcepto().get(i).getCantidad() + "\" ";
			xmlConvertido += "claveUnidad=\"" + ticket.getComprobante().getConceptos().getConcepto().get(i).getClaveUnidad() + "\" ";
			xmlConvertido += "unidad=\"" + ticket.getComprobante().getConceptos().getConcepto().get(i).getUnidad() + "\" ";
			String descripcion = ticket.getComprobante().getConceptos().getConcepto().get(i).getDescripcion().trim();
			descripcion = UtilsString.convertirCaracteresXml(descripcion);
			xmlConvertido += "descripcion=\"" + descripcion + "\" ";
			xmlConvertido += "valorUnitario=\"" + ticket.getComprobante().getConceptos().getConcepto().get(i).getValorUnitario() + "\" ";
			xmlConvertido += "importe=\"" + ticket.getComprobante().getConceptos().getConcepto().get(i).getImporte() + "\" ";
			
			String sImporte = ticket.getComprobante().getConceptos().getConcepto().get(i).getImporte();
			
			String sDescuentoOrig = ticket.getComprobante().getConceptos().getConcepto().get(i).getDescuento();
			double descuento = Double.parseDouble(sDescuentoOrig);
			
			String sDescuento = ""; 
			sDescuento = UtilsString.formatearDecimales(descuento, armarFormato(obtenerNumeroDecimales(sImporte)));
			
			xmlConvertido += "descuento=\"" + sDescuento + "\" ";
			xmlConvertido += "ordenador=\"" + ticket.getComprobante().getConceptos().getConcepto().get(i).getOrdenador() + "\" ";
			xmlConvertido += "nivel=\"" + ticket.getComprobante().getConceptos().getConcepto().get(i).getNivel() + "\" ";
			xmlConvertido += "padre=\"0\" ";
			
			if (version40) {
				xmlConvertido += "objetoImp=\"" + ticket.getComprobante().getConceptos().getConcepto().get(i).getObjetoImp() + "\" ";				
			}
			xmlConvertido += " >";
			
			
			int cantidadTraslados = 0;
			
			ConceptoImpuestosTYPE concImpuesto = ticket.getComprobante().getConceptos().getConcepto().get(i).getImpuestos();
			if (concImpuesto != null && concImpuesto.getTraslados() != null) {

				/*********Conceptos**********/
				xmlConvertido += "<Impuestos>";
				/*********Traslados**********/
				xmlConvertido += "<Traslados>";
				/*********Traslados**********/
				
				cantidadTraslados = ticket.getComprobante().getConceptos().getConcepto().get(i).getImpuestos().getTraslados().getTraslado().size();
				
				for (int j = 0; j< cantidadTraslados; j++)
				{
					tieneImpuestos = true;
					xmlConvertido += "<Traslado base=\"" + ticket.getComprobante().getConceptos().getConcepto().get(i).getImpuestos().getTraslados().getTraslado().get(j).getBase() + "\" ";
					xmlConvertido += "impuesto=\"" + ticket.getComprobante().getConceptos().getConcepto().get(i).getImpuestos().getTraslados().getTraslado().get(j).getImpuesto() + "\" ";
					xmlConvertido += "tipoFactor=\"" + ticket.getComprobante().getConceptos().getConcepto().get(i).getImpuestos().getTraslados().getTraslado().get(j).getTipoFactor() + "\" ";
					xmlConvertido += "tasaOCuota=\"" + ticket.getComprobante().getConceptos().getConcepto().get(i).getImpuestos().getTraslados().getTraslado().get(j).getTasaCuota() + "\" ";
					xmlConvertido += "importe=\"" + ticket.getComprobante().getConceptos().getConcepto().get(i).getImpuestos().getTraslados().getTraslado().get(j).getImporte() + "\" ";
					xmlConvertido += "ordenador=\"" + ticket.getComprobante().getConceptos().getConcepto().get(i).getImpuestos().getTraslados().getTraslado().get(j).getOrdenador() + "\" />";
				}	

				xmlConvertido += "</Traslados>";
				xmlConvertido += "</Impuestos>";
			}
			
			
			xmlConvertido += "</Concepto>";
		}
		
		xmlConvertido += "</Conceptos>";
		
		
		/*********Traslado**********/
		
		int cantidadTraslados = 0;
		ImpuestosTYPE impuestos = ticket.getComprobante().getImpuestos();
		if (impuestos != null && impuestos.getTraslados() != null) {

			/*********Impuestos**********/
			xmlConvertido += "<Impuestos>";
			/*********Traslados**********/
			xmlConvertido += "<Traslados>";
			
			cantidadTraslados = ticket.getComprobante().getImpuestos().getTraslados().getTraslado().size();
			
			for (int k = 0; k < cantidadTraslados; k++)
			{
				xmlConvertido += "<Traslado impuesto=\"" + ticket.getComprobante().getImpuestos().getTraslados().getTraslado().get(k).getImpuesto() + "\" ";
				xmlConvertido += "base=\"" + ticket.getComprobante().getImpuestos().getTraslados().getTraslado().get(k).getBase() + "\" ";
				xmlConvertido += "tipoFactor=\"" + ticket.getComprobante().getImpuestos().getTraslados().getTraslado().get(k).getTipoFactor() + "\" ";
				xmlConvertido += "tasaOCuota=\"" + ticket.getComprobante().getImpuestos().getTraslados().getTraslado().get(k).getTasaCuota() + "\" ";
				xmlConvertido += "importe=\"" + ticket.getComprobante().getImpuestos().getTraslados().getTraslado().get(k).getImporte() + "\" ";
				xmlConvertido += "ordenador=\"" + ticket.getComprobante().getImpuestos().getTraslados().getTraslado().get(k).getOrdenador() + "\" />";
			}
			
			xmlConvertido += "</Traslados>";
			xmlConvertido += "</Impuestos>";
		}
		
		
		/*********Totales**********/
		xmlConvertido += "<Totales subTotal=\"" + ticket.getComprobante().getTotales().getSubTotal() +  "\" ";
		xmlConvertido += "descuento=\"" + ticket.getComprobante().getTotales().getDescuento() +  "\" ";
		xmlConvertido += "moneda=\"" + ticket.getComprobante().getTotales().getMoneda() +  "\" ";
		xmlConvertido += "tipoCambio=\"" + ticket.getComprobante().getTotales().getTipoCambio() +  "\" ";
		xmlConvertido += "total=\"" + ticket.getComprobante().getTotales().getTotal() +  "\" ";
		xmlConvertido += "importeLetra=\"" + ticket.getComprobante().getTotales().getImporteLetra() +  "\" ";
		if (tieneImpuestos && ticket.getComprobante().getTotales().getTotalImpuestosTrasladados() != null) {
			xmlConvertido += "totalImpuestosTrasladados=\"" + ticket.getComprobante().getTotales().getTotalImpuestosTrasladados() +  "\"";
		}
		xmlConvertido += " />";
		
		

		/*********DatosExtraCFD**********/

		xmlConvertido += "<DatosExtraCFD extra1=\"" + ticket.getDatosExtraCFD().getExtra1() + "\" ";
		xmlConvertido += " extra2=\"" + ticket.getDatosExtraCFD().getExtra2() + "\" ";
		if (version40) {
			xmlConvertido += " extra4=\"" + ticket.getDatosExtraCFD().getExtra4() + "\" ";
			xmlConvertido += " extra6=\"" + ticket.getDatosExtraCFD().getExtra6() + "\" ";
			xmlConvertido += " extra7=\"" + ticket.getDatosExtraCFD().getExtra7() + "\" ";		
		}
		
		xmlConvertido += " extra8=\"" + "SODIMAC" + "\" ";
		xmlConvertido += " extra9=\"" + ticket.getDatosExtraCFD().getExtra9() + "\" ";

		xmlConvertido += " />";


		/*********Control**********/
		//DML Se modifica por solicitud de detecno 1
        xmlConvertido += "<Control cfd=\"1\" sucursal=\"Opcional\" estatusId=\"1\" estatusIdImpresion=\"0\" estatusIdCorreo=\"1\" estatusIdArchivo=\"0\" rechazoId=\"0\" addendaId=\"0\" complementoId=\"0\" pathImpresion=\"\" correo=\"detecno@detecno.com\" formatoImpresion=\"1\" formatoCorreo=\"1\" formatoWeb=\"1\" />";

		xmlConvertido += "<Comentarios descripcion=\"Opcional\" />";
		xmlConvertido += "<CodigoBarras>";
		xmlConvertido += "<CodBar valorCodigo=\"1234567890\" tipoCodigo=\"3\" ordenador=\"1\" />";
		xmlConvertido += "</CodigoBarras>";
		xmlConvertido += "<Complementos/>";
		xmlConvertido += "<Addenda/>";
		xmlConvertido += "<Foliado automatico=\"false\" />";
		
		xmlConvertido += "</Comprobante>";

		
		return xmlConvertido;
	}
	
	private String armarFormato(int numeroDecimales) {
		String result = "0."+ StringUtils.repeat("0", numeroDecimales);		
		return result;
	}

	private int obtenerNumeroDecimales(String importe) {
		int result = 0;

		if (!importe.isEmpty() && importe.contains(".")) {
			result = importe.substring(importe.indexOf(".")).length()-1;
		}
		return result;
	}

	@Transactional(isolation=Isolation.READ_UNCOMMITTED)
	public int existFactura(String rfc, String ticket) {
		return facturasRepository.existFactura(seguridadService.encriptar(rfc), ticket);
	}
	
	@Transactional(isolation=Isolation.READ_UNCOMMITTED)
	public int existFactura(String ticket) {
		return facturasRepository.existFactura(ticket);
	}

	@Transactional(isolation=Isolation.READ_UNCOMMITTED)
	public List<FacturasMultipleModel> getFacturasFechas(String rfc, String email, String fechaInicial, String fechafinal, int start, int rowsPerPage) {
		List<FacturasMultipleModel> list = new ArrayList<FacturasMultipleModel>();
		
		facturasRepository.getFacturasFechas(seguridadService.encriptar(rfc), seguridadService.encriptar(email), fechaInicial, fechafinal, start, rowsPerPage).forEach(item -> {
			FacturasMultipleModel itemList = new FacturasMultipleModel();
			itemList.setIdFactura(Integer.parseInt(item[0].toString()));
			itemList.setUuid(item[1].toString());
			itemList.setTicket(item[2].toString());
			itemList.setRazonSocial(seguridadService.desencriptar(item[3].toString()));
			itemList.setFechaTimbrado(item[4].toString());
			itemList.setNombreEstatus(item[5].toString());
			itemList.setRfc(seguridadService.desencriptar(item[6].toString()));
			itemList.setNombreArchivo(item[7].toString());
			itemList.setChecked("".toString());
			list.add(itemList);
		});
		
		return list;
	}
	
	@Transactional
	private int insertarFactura(ClientesTemporalModel model) {
		String rfc = seguridadService.encriptar(model.getRfc());
		String razonSocial = seguridadService.encriptar(model.getRazonSocial());
		String email = "";
		if (!model.getEmail().isEmpty()) {
			email = seguridadService.encriptar(model.getEmail()); 
		}
		String xml = seguridadService.encriptar(model.getXml());

		return facturasRepository.insertarFactura(rfc
					, model.getTicket()
					, razonSocial
					, model.getIdUsoCfdiReal()
					, email
					, model.getAutorizoGuardado()
					, model.getPac()
					, model.getIdFacturaPac()
					, model.getUuid()
					, model.getFechaTimbrado()
					, model.getVersionFacturacionSat()
					, xml
					, model.getFechaCompra()
					, model.getIdEstatusFactura()
					, model.getTicketBct()
					, model.getVersionFactura()
					, model.getTransaccion()
					, model.getNombreObra()
					, model.getResponsableObra()
					, model.getIdComprobante()
					, model.getUuidRelacionado()
					, model.getSerie()
                    , model.getFolio()
                    , model.getSubTotal()
                    , model.getTotal()
                    , model.getIdOrigen()
                    , model.getMetodoPago()
					, errorComponent.getLongitud()
					, errorComponent.getLatitud()
					, errorComponent.getPagina()
					, errorComponent.getExplorador()
					, errorComponent.getSistemaOper()
					, errorComponent.getIp()
					);			
	}

	public void crearZipMultiple(String archivoZip, String archivosZip) {
		String path = configFacService.getConfig().get("Mail.PathFile");
		File fileNameZip = new File(path + archivoZip + ".zip");
		String archivo = "";

		try {
			FileOutputStream fos = new FileOutputStream(fileNameZip);
			ZipOutputStream zipOS = new ZipOutputStream(fos);

			if (archivosZip.indexOf(",") > 0) {
				
				String[] Archivos = archivosZip.split(",");
				for (String item : Archivos) {
					
					String uuid = item.substring(17);
					FacturasEntity factura = getFacturaByUuid(uuid);
					String xml = seguridadService.desencriptar(factura.getXml());
					crearArchivoXml(item, xml);
					crearPdfFromXml(item, xml);
					crearZipXmlPdf(item);
					
					File fileName = new File(path + item + ".zip");
					addToZipFile(fileName, zipOS);
				}
			} else {
				archivo = archivosZip;
				File fileName = new File(path + archivo + ".zip");
				addToZipFile(fileName, zipOS);				
			}
			
			zipOS.close();
			fos.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	void addToZipFile(File fileName, ZipOutputStream zipOS) {
		try {
			UtilsFile.writeToZipFile(fileName, zipOS);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}							
	}
	
	@Override
	@Transactional(isolation=Isolation.READ_UNCOMMITTED)
	public FacturasEntity getFacturaByUuid(String uuid) {
		return facturasRepository.findByUuid(uuid);
	}
		
	@Transactional
	public int countFacturas(String rfc, String email, String fechaInicial, String fechafinal) {
	return facturasRepository.getCountFacturas(seguridadService.encriptar(rfc), seguridadService.encriptar(email), fechaInicial, fechafinal);
	}
	
	@Transactional
	public boolean crearPdfFromXml(String nombreArchivo, String xml){
         
        try {
        		String path = configFacService.getConfig().get("Mail.PathFile");
        		
        		File pdffile = new File(path+nombreArchivo+".pdf");
        		
        		if (pdffile.exists()) {
        			return true;
        		}
        		
        		//xml = FileUtils.readFileToString(new File("C:\\Users\\g_daf01\\Desktop\\cfdi-error.xml"), "UTF-8");
        		Document document = ObtenerDocumentXml (xml);
        		
        		if (existenConceptosSinTraslados(document)) {
        			document = agregarTraslados(document);
        			xml= UtilsFile.getStringFromDocument(document);
        		}
        		
    	        Node nNode = document.getElementsByTagName("cfdi:Comprobante").item(0);
    	        Element eElement = (Element) nNode;
    	        String version = eElement.getAttribute("Version");
        		
	            // Setup input and output files
    	        String fileNameXsl = "Formato" + version + ".xsl";
    	    	Resource resource = resourceLoader.getResource("classpath:xsl/"+fileNameXsl);
    	    	File xsltfile = resource.getFile();
    	    	String xsltfilePath0 = xsltfile.getAbsolutePath();
    	    	int posicion = xsltfilePath0.lastIndexOf("xsl\\");
    	    	String xsltfilePath = xsltfilePath0.substring(0, posicion+4);
    	    	
	            // configure fopFactory as desired
	            FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
	            // configure foUserAgent as desired
	            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
	            
	            // Setup output
	            OutputStream out = new java.io.FileOutputStream(pdffile);
	            out = new java.io.BufferedOutputStream(out);
	
	            try {
	                // Construct fop with desired output format
	                Fop fop = fopFactory.newFop("application/pdf",foUserAgent, out);
	
	                // Setup XSLT
	                TransformerFactory factory = TransformerFactory.newInstance();
	                Transformer transformer = factory.newTransformer(new StreamSource(xsltfile));
	                
	                String file_path = obtenerDatosPdf(document, transformer, path);
	                transformer.setParameter("xsltfilePath", xsltfilePath);
	                transformer.setParameter("xsltfilePathQR", path);
	                	
	                // Setup input for XSLT transformation
	                Source src = new StreamSource(new StringReader(xml));
	
	                // Resulting SAX events (the generated FO) must be piped through to FOP
	                Result res = new SAXResult(fop.getDefaultHandler());
	
	                // Start XSLT transformation and FOP processing
	                transformer.transform(src, res);
	                
	                //Elimina codigoQR
	                File filePathToDelete = new File (path + "/" + file_path);
	                if (filePathToDelete.exists()) {
	                	filePathToDelete.delete();
	                }
	            } finally {
	                out.close();
	            }
                return true;
                
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
        return false;
    }
	
	private Document agregarTraslados(Document document) {
		NodeList nlConcepto = document.getElementsByTagName("cfdi:Concepto");
		for (int i = 0; i < nlConcepto.getLength(); i++) {
			Element eConcepto = (Element) nlConcepto.item(i);
			NodeList nlTraslado = eConcepto.getElementsByTagName("cfdi:Traslado");
			if (nlTraslado.getLength() == 0) {

				Element eImpuestos = document.createElement("cfdi:Impuestos");
				Element eTraslados = document.createElement("cfdi:Traslados");
				Element eTraslado = document.createElement("cfdi:Traslado");
				eTraslado.setAttribute("tipoFactor", "tipoFactor");
				eTraslados.appendChild(eTraslado);
				eImpuestos.appendChild(eTraslados);
				eConcepto.appendChild(eImpuestos);					
			}
		}			

		return document;
	}

	private boolean existenConceptosSinTraslados(Document document) {
		boolean result = false;
        
		NodeList nlConcepto = document.getElementsByTagName("cfdi:Concepto");
		for (int i = 0; i < nlConcepto.getLength(); i++) {
			Element eConcepto = (Element) nlConcepto.item(i);
			NodeList nlTraslado = eConcepto.getElementsByTagName("cfdi:Traslado");
			if (nlTraslado.getLength() == 0) {
				return true;
			}
		}
					
		return result;
	}

	@Transactional
	@Override
	public boolean crearPdfComplementoFromXml(String nombreArchivo, String xml){
        
        try {
    		String path = configFacService.getConfig().get("Mail.PathFile");
    		
    		File pdffile = new File(path+nombreArchivo+".pdf");
    		
    		if (pdffile.exists()) {
    			return true;
    		}
    		
    		Document document = ObtenerDocumentXml (xml);
            // Setup input and output files
	        String fileNameXsl = "FormatoComp4.0.xsl";
	    	Resource resource = resourceLoader.getResource("classpath:xsl/"+fileNameXsl);
	    	File xsltfile = resource.getFile();
	    	String xsltfilePath0 = xsltfile.getAbsolutePath();
	    	int posicion = xsltfilePath0.lastIndexOf("xsl\\");
	    	String xsltfilePath = xsltfilePath0.substring(0, posicion+4);
	    	
            // configure fopFactory as desired
            FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
            // configure foUserAgent as desired
            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
            
            // Setup output
            OutputStream out = new java.io.FileOutputStream(pdffile);
            out = new java.io.BufferedOutputStream(out);

            try {
                // Construct fop with desired output format
                Fop fop = fopFactory.newFop("application/pdf",foUserAgent, out);

                // Setup XSLT
                TransformerFactory factory = TransformerFactory.newInstance();
                Transformer transformer = factory.newTransformer(new StreamSource(xsltfile));
                
                String file_path = this.obtenerDatosComplementoPdf(document, transformer, path);
                transformer.setParameter("xsltfilePath", xsltfilePath);
                transformer.setParameter("xsltfilePathQR", path);
                	
                // Setup input for XSLT transformation
                Source src = new StreamSource(new StringReader(xml));

                // Resulting SAX events (the generated FO) must be piped through to FOP
                Result res = new SAXResult(fop.getDefaultHandler());

                // Start XSLT transformation and FOP processing
                transformer.transform(src, res);
                
                //Elimina codigoQR
                File filePathToDelete = new File (path + "/" + file_path);
                if (filePathToDelete.exists()) {
                	filePathToDelete.delete();
                }
            } finally {
                out.close();
            }
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
	
	Document ObtenerDocumentXml (String xml) {
		Document document = null;
		
		try {
	        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
	        InputSource is = new InputSource(new StringReader(xml));
	        document = documentBuilder.parse(is);
			
        } catch (Exception e) {
            e.printStackTrace();
            errorComponent.setXml(xml);
			errorComponent.setPagina("ObtenerDocumentXml");
			errorComponent.guardarLog(e);
        }
		
        return document;		
	}
	
	String obtenerDatosPdf(Document document, Transformer transformer, String xsltfilePath) {
		
		String rfcReceptor="";
		String uuid = null;
		String versionCFDI = null;
		try {
	        Node nNode = document.getElementsByTagName("cfdi:Comprobante").item(0);
	        Element eElement = (Element) nNode;

    		String rfcEmisor="";
    		
    		String selloCfd = "";
	        String sTotal = eElement.getAttribute("Total");
			NumeroaLetras totalLetras = new NumeroaLetras();
			String sFormaPago = eElement.getAttribute("FormaPago");
			String sMetodopago = eElement.getAttribute("MetodoPago");
			versionCFDI = eElement.getAttribute("Version");
			String tipoDeComprobante = eElement.getAttribute("TipoDeComprobante");
			String tipoDeComprobanteDesc = "";
			String tipoComprobanteDesc = "";
			String moneda = eElement.getAttribute("Moneda");
			String monedaDesc = catMonedaService.get(moneda).getDescripcion();
			String importeLetra = totalLetras.Convertir(sTotal, true, moneda);
			
			CatTipoDeComprobanteEntity comprobante = catTipoDeComprobanteService.get(tipoDeComprobante);
			if (comprobante != null) {
				tipoDeComprobanteDesc = comprobante.getDescripcionSat();
				tipoComprobanteDesc = comprobante.getDescripcionTimbrado(); 
			}

			nNode = document.getElementsByTagName("cfdi:Receptor").item(0);
	        eElement = (Element) nNode;
	        
	        String regimenFiscalReceptorDesc = "";
	        if (versionCFDI.equals(CFDI_VERSION_40)) {
	        	int idRegimenFiscalReceptor = Integer.parseInt(eElement.getAttribute("RegimenFiscalReceptor"));
	        	regimenFiscalReceptorDesc = catRegimenFiscalService.get(idRegimenFiscalReceptor).getDescripcion();
	        }

	        String usoCfdi = eElement.getAttribute("UsoCFDI");
	        rfcReceptor = eElement.getAttribute("Rfc");
        
	        EVersionCFDI eVersionCFDI = EVersionCFDI.getVersionByDesc(versionCFDI);
	        String usoCFDIDesc = catUsosCfdiService.getUsoCfdi(usoCfdi, eVersionCFDI.getId()).getDescripcionUso();

			nNode = document.getElementsByTagName("cfdi:Emisor").item(0);
	        eElement = (Element) nNode;
	        
	        int idRegimenFiscal = Integer.parseInt(eElement.getAttribute("RegimenFiscal"));
	    	rfcEmisor = eElement.getAttribute("Rfc");
	        String regimenFiscalDesc = catRegimenFiscalService.get(idRegimenFiscal).getDescripcion();
	        
	        
			nNode = document.getElementsByTagName("tfd:TimbreFiscalDigital").item(0);
	        eElement = (Element) nNode;
	        uuid = eElement.getAttribute("UUID");
	        selloCfd = eElement.getAttribute("SelloCFD");
	        FacturasEntity factura = this.getFacturaByUuid(uuid);
	        String ticketId = "";
	        String transaccion = "";
	        String nombreObra = "";
	        String responsableObra = "";
	        String uuidRelacionado = "";
	        if (factura!=null) {
		        ticketId = factura.getTicket().trim();
		        transaccion = ticketId.substring(ticketId.length()-4);
		        nombreObra = factura.getNombreObra();
		        responsableObra = factura.getResponsableObra();
		        if (factura.getUuidRelacionado() != null) {
		        	uuidRelacionado = factura.getUuidRelacionado();
		        }
	        }
	        
	        String periodicidad = "";
	        String meses = "";
	        String anio = "";
	        
	        NodeList nodeListInfGlobal  = document.getElementsByTagName("cfdi:InformacionGlobal");
	        if (nodeListInfGlobal != null && nodeListInfGlobal.getLength() > 0) {
	        	
	        	Element nodeInfGlobal = (Element) nodeListInfGlobal.item(0);
	        	ClientResponseTYPE<Periodicidad> periodicidadResponse = configuracionService.consultarPeriodicidad(nodeInfGlobal.getAttribute("Periodicidad"));
	        	if (periodicidadResponse != null && periodicidadResponse.getRespuesta().getCodigo().equals("1")) {
	        		periodicidad = periodicidadResponse.getData().getDescripcion();
	        	}
	        	
	        	ClientResponseTYPE<Mes> mesResponse = configuracionService.consultarMes(nodeInfGlobal.getAttribute("Meses"));
	        	if (mesResponse != null && mesResponse.getRespuesta().getCodigo().equals("1")) {
	        		meses = mesResponse.getData().getDescripcion();
	        	}
	        	
	        	anio = nodeInfGlobal.getAttribute("A\u00f1o");
	        }
	        
	        String emailPac = "";
	        String razonSocialPac = "";
	        String rfcPac = "";
	        int idPac = 0;
			PacsEntity pacEntity = pacsService.getById(pacsService.getIdDefault());
			if (pacEntity != null) {
				emailPac = seguridadService.desencriptar(pacEntity.geteMail());
				razonSocialPac = seguridadService.desencriptar(pacEntity.getRazonSocial());
				rfcPac = seguridadService.desencriptar(pacEntity.getRfc());
				idPac = pacEntity.getIdPacExternal();
			}
			String formaPago = "";
			String metodoPago = "";
			CatMetodoPagoEntity catMetodoPagoEntity = catMetodoPagoService.getByIdFormaPago(sMetodopago);
			CatFormaPagoEntity catFormaPagoEntity = catFormaPagoService.getByIdFormaPago(sFormaPago);
			
			if (catFormaPagoEntity != null) {
				formaPago = catFormaPagoEntity.getDescripcion();
			}
			if (catMetodoPagoEntity != null) {
				metodoPago = catMetodoPagoEntity.getDescripcion();
			}
     
            String fe = selloCfd.substring(selloCfd.length()-8, selloCfd.length());

            String urlCodeQR = "https://verificacfdi.facturaelectronica.sat.gob.mx/default.aspx?&re=" + rfcEmisor + "&rr=" + rfcReceptor 
            					+ "&tt=" + sTotal + "&id=" + uuid + "&fe=" + fe;
            System.out.println(urlCodeQR);
            QrCodeGenerator qrCodeGenerator = new QrCodeGenerator();
            qrCodeGenerator.generateQRCodeImage(urlCodeQR, 530, 530, xsltfilePath, rfcReceptor, uuid);

            transformer.setParameter("periodicidad", periodicidad);
	        transformer.setParameter("meses", meses);
	        transformer.setParameter("anio", anio);
	        
	        transformer.setParameter("usoCFDIDesc", usoCFDIDesc);
	        transformer.setParameter("regimenFiscalDesc", regimenFiscalDesc);
	        transformer.setParameter("regimenFiscalReceptorDesc", regimenFiscalReceptorDesc);
	        transformer.setParameter("tipoComprobanteDesc", tipoComprobanteDesc);
	        transformer.setParameter("tipoDeComprobanteDesc", tipoDeComprobanteDesc);
	        transformer.setParameter("transaccion", transaccion);
	        transformer.setParameter("nombreObra", nombreObra);
	        transformer.setParameter("responsableObra", responsableObra);
	        transformer.setParameter("uuidRelacionado", uuidRelacionado);
	        transformer.setParameter("monedaDesc", monedaDesc);

	        transformer.setParameter("importeLetra", importeLetra);
	        transformer.setParameter("formaPagoLetter", formaPago);
	        transformer.setParameter("metodoPagoLetter", metodoPago);
	        transformer.setParameter("ticketId", ticketId);
	        transformer.setParameter("pacEmail", emailPac);
	        transformer.setParameter("pacRZ", razonSocialPac);
	        transformer.setParameter("pacRfc", rfcPac);
	        transformer.setParameter("idPacExternal", idPac);
	        transformer.setParameter("qrCodeFileName", "QRCode" + rfcReceptor + "_" + uuid + ".png");
	        
		} catch (Exception e) {
			logger.info("XXXXXXXXXXXXX ERROR al crear PDF [" + uuid + "]");
			logger.info("versionCFDI: " + versionCFDI);
            e.printStackTrace();
        }
		return "QRCode" + rfcReceptor + "_" + uuid + ".png";
	}
	
	@SuppressWarnings("unused")
	private String obtenerDatosComplementoPdf(Document document, Transformer transformer, String xsltfilePath) {
		
		String rfcReceptor="";
		String rfcEmisor="";
		
		String selloCfd = "";
        String sTotal = "0";
        String sTotalComplemento = "";
        
		NumeroaLetras totalLetras = new NumeroaLetras();
		String sFormaPago = "";
		String sFormaPagoDesc = "";
		String sFormaPagoComplemento = "";
		String sFormaPagoComplementoDesc = "";
		String versionCFDI = "";
		String tipoDeComprobante = "";
		String tipoDeComprobanteDesc = "";
		String tipoComprobanteDesc = "";
		String moneda = "";
		String monedaDesc = "";
		String importeLetra = "";
		
		String monedaComplemento = "";
		String monedaComplementoDesc = "";
		String importeLetraComplemento = "";
		
		//No se utilizan
		String sMetodopago = null;
		String uuid = null;
		String sMetodopagoDesc = "";
		
		Double impSaldoAntTotal = 0.0; 
		Double impPagadoTotal = 0.0;
		Double impSaldoInsolutoTotal = 0.0;
		
		try {
	        Node nNode = document.getElementsByTagName("cfdi:Comprobante").item(0);
	        Element eElement = (Element) nNode;
	        
	        //Elemento comunes
	        versionCFDI = eElement.getAttribute("Version");
	        tipoDeComprobante = eElement.getAttribute("TipoDeComprobante");
	        
	        if (eElement.getAttribute("Total") != null) {
	        	sTotal = eElement.getAttribute("Total");
	        	importeLetra = totalLetras.Convertir(sTotal, true, moneda);
	        }
	        
	        if (eElement.getAttribute("FormaPago") != null) {
	        	sFormaPago = eElement.getAttribute("FormaPago");
	        }
	        
	        if(eElement.getAttribute("Moneda") != null) {
	        	moneda = eElement.getAttribute("Moneda");
	        }
	        
	        if (tipoDeComprobante != null) {
	        	CatTipoDeComprobanteEntity comprobante = catTipoDeComprobanteService.get(tipoDeComprobante);
				if (comprobante != null) {
					tipoDeComprobanteDesc = comprobante.getDescripcionSat();
					tipoComprobanteDesc = comprobante.getDescripcionTimbrado(); 
				}
	        }
	        
	        nNode = document.getElementsByTagName("cfdi:Receptor").item(0);
	        eElement = (Element) nNode;
	        
	        String regimenFiscalReceptorDesc = "";
	        if (versionCFDI.equals(CFDI_VERSION_40)) {
	        	int idRegimenFiscalReceptor = Integer.parseInt(eElement.getAttribute("RegimenFiscalReceptor"));
	        	regimenFiscalReceptorDesc = catRegimenFiscalService.get(idRegimenFiscalReceptor).getDescripcion();
	        }

	        String usoCfdi = eElement.getAttribute("UsoCFDI");
	        rfcReceptor = eElement.getAttribute("Rfc");
        
	        EVersionCFDI eVersionCFDI = EVersionCFDI.getVersionByDesc(versionCFDI);
	        String usoCFDIDesc = catUsosCfdiService.getUsoCfdi(usoCfdi, eVersionCFDI.getId()).getDescripcionUso();

			nNode = document.getElementsByTagName("cfdi:Emisor").item(0);
	        eElement = (Element) nNode;
	        
	        int idRegimenFiscal = Integer.parseInt(eElement.getAttribute("RegimenFiscal"));
	    	rfcEmisor = eElement.getAttribute("Rfc");
	        String regimenFiscalDesc = catRegimenFiscalService.get(idRegimenFiscal).getDescripcion();
	        
			nNode = document.getElementsByTagName("tfd:TimbreFiscalDigital").item(0);
	        eElement = (Element) nNode;
	        uuid = eElement.getAttribute("UUID");
	        selloCfd = eElement.getAttribute("SelloCFD");
	        
	        FacturasEntity factura = this.getFacturaByUuid(uuid);
	        String ticketId = "";
	        String transaccion = "";
	        String nombreObra = "";
	        String responsableObra = "";
	        String uuidRelacionado = "";
	        if (factura!=null) {
		        ticketId = factura.getTicket().trim();
		        transaccion = ticketId.substring(ticketId.length()-4);
		        nombreObra = factura.getNombreObra();
		        responsableObra = factura.getResponsableObra();
		        uuidRelacionado = factura.getUuidRelacionado();
	        }
	        
	        //Complemento de  pago
	        
	        NodeList listNodeComplemento = document.getElementsByTagName("cfdi:Complemento");
	        if (listNodeComplemento != null && listNodeComplemento.getLength() > 0) {
	        	Node nNodeComplemento = listNodeComplemento.item(0);
	        	if (nNodeComplemento != null) {
	        		if (nNodeComplemento.getNodeType() == Node.ELEMENT_NODE) {
	        			Element eElementComplemento = (Element) nNodeComplemento;
	        			NodeList listNodePagos = eElementComplemento.getElementsByTagName("pago20:Pagos");
	        			
	        			if (listNodePagos != null && listNodePagos.getLength() > 0) {
	        				Node nNodePago = listNodePagos.item(0);
	        				
	        				if (nNodePago.getNodeType() == Node.ELEMENT_NODE) {
	    	        			
	    	        			NodeList listNodeTotales = eElementComplemento.getElementsByTagName("pago20:Totales");
	    	        			if (listNodeTotales != null && listNodeTotales.getLength() > 0) {
	    	        				Node nNodeTotal = listNodeTotales.item(0);
	    	        				
	    	        				if (nNodeTotal.getNodeType() == Node.ELEMENT_NODE) {
	    	    	        			Element eElementTotal = (Element) nNodeTotal;
	    	    	        			sTotalComplemento = eElementTotal.getAttribute("MontoTotalPagos");
	    	        				}
	    	        			}
	    	        			
	    	        			NodeList listNodePago = eElementComplemento.getElementsByTagName("pago20:Pago");
	    	        			if (listNodePago != null && listNodePago.getLength() > 0) {
	    	        				Node nNodePagoInt = listNodePago.item(0);
	    	        				
	    	        				if (nNodePagoInt.getNodeType() == Node.ELEMENT_NODE) {
	    	    	        			Element eElementPagoInt = (Element) nNodePagoInt;
	    	    	        			
	    	    	        			sFormaPagoComplemento = eElementPagoInt.getAttribute("FormaDePagoP"); 
	    	    	        			monedaComplemento = eElementPagoInt.getAttribute("MonedaP");
	    	        				}
	    	        			} //if (listNodePago != null && listNodePago.getLength() > 0)
	    	        			
	    	        			NodeList listNodeDoctoRelacionado = eElementComplemento.getElementsByTagName("pago20:DoctoRelacionado");
	    	        			if (listNodeDoctoRelacionado != null) {
	    	        				for (int i=0; i<listNodeDoctoRelacionado.getLength(); i++) {
	    	        					Node nNodeDocumentoRel = listNodeDoctoRelacionado.item(i);
	    	        					if (nNodeDocumentoRel.getNodeType() == Node.ELEMENT_NODE) {
		    	    	        			Element eElementDocumento = (Element) nNodeDocumentoRel;
		    	    	        			
		    	    	        			String impSaldoAnt = eElementDocumento.getAttribute("ImpSaldoAnt"); 
		    	    	        			String impPagado = eElementDocumento.getAttribute("ImpPagado");
		    	    	        			String impSaldoInsoluto = eElementDocumento.getAttribute("ImpSaldoInsoluto");
		    	    	        			
		    	    	        			if (impSaldoAnt != null) {
		    	    	        				impSaldoAntTotal = impPagadoTotal + Double.valueOf(impSaldoAnt);
		    	    	        			}
		    	    	        			if (impPagado != null) {
		    	    	        				impPagadoTotal = impPagadoTotal + Double.valueOf(impPagado);
		    	    	        			}
		    	    	        			if (impSaldoInsoluto != null) {
		    	    	        				impSaldoInsolutoTotal = impSaldoInsolutoTotal + Double.valueOf(impSaldoInsoluto);
		    	    	        			}
		    	        				}
	    	        				}
	    	        			}
	    	        			
	        				}//if (nNodePago.getNodeType() == Node.ELEMENT_NODE)
	        			}//if (listNodePagos != null && listNodePagos.getLength() > 0)
	        		}//if (nNodeComplemento.getNodeType() == Node.ELEMENT_NODE)
	        	}//if (nNodeComplemento != null)
	        }//if (listNodeComplemento != null && listNodeComplemento.getLength() > 0)
	        
	        
	        //Información global	        
	        String periodicidad = "";
	        String meses = "";
	        String anio = "";
	        
	        NodeList nodeListInfGlobal  = document.getElementsByTagName("cfdi:InformacionGlobal");
	        if (nodeListInfGlobal != null && nodeListInfGlobal.getLength() > 0) {
	        	
	        	Element nodeInfGlobal = (Element) nodeListInfGlobal.item(0);
	        	ClientResponseTYPE<Periodicidad> periodicidadResponse = configuracionService.consultarPeriodicidad(nodeInfGlobal.getAttribute("Periodicidad"));
	        	if (periodicidadResponse != null && periodicidadResponse.getRespuesta().getCodigo().equals("1")) {
	        		periodicidad = periodicidadResponse.getData().getDescripcion();
	        	}
	        	
	        	ClientResponseTYPE<Mes> mesResponse = configuracionService.consultarMes(nodeInfGlobal.getAttribute("Meses"));
	        	if (mesResponse != null && mesResponse.getRespuesta().getCodigo().equals("1")) {
	        		meses = mesResponse.getData().getDescripcion();
	        	}
	        	
	        	anio = nodeInfGlobal.getAttribute("A\u00f1o");
	        }
	        
	        String emailPac = "";
	        String razonSocialPac = "";
	        String rfcPac = "";
	        int idPac = 0;
			PacsEntity pacEntity = pacsService.getById(pacsService.getIdDefault());
			if (pacEntity != null) {
				emailPac = seguridadService.desencriptar(pacEntity.geteMail());
				razonSocialPac = seguridadService.desencriptar(pacEntity.getRazonSocial());
				rfcPac = seguridadService.desencriptar(pacEntity.getRfc());
				idPac = pacEntity.getIdPacExternal();
			}
			
			if (sFormaPago != null && !sFormaPago.isEmpty()) {
				CatFormaPagoEntity catFormaPagoEntity = catFormaPagoService.getByIdFormaPago(sFormaPago);
				if (catFormaPagoEntity != null) {
					sFormaPagoDesc = catFormaPagoEntity.getDescripcion();
				}
			}
			
			
			if (moneda != null && !moneda.isEmpty()) {
				CatMonedaEntity catMonedaEntity = catMonedaService.get(moneda);
		        if (catMonedaEntity != null) {
		        	monedaDesc = catMonedaEntity.getDescripcion();
		        }
			}
			
			if (sMetodopago != null && !sMetodopago.isEmpty()) {
				CatMetodoPagoEntity catMetodoPagoEntity = catMetodoPagoService.getByIdFormaPago(sMetodopago);
				if (catMetodoPagoEntity != null) {
					sMetodopagoDesc = catMetodoPagoEntity.getDescripcion();
				}
			}
			
			if (sFormaPagoComplemento != null) {
				CatFormaPagoEntity catFormaPagoEntity = catFormaPagoService.getByIdFormaPago(sFormaPagoComplemento);
				if (catFormaPagoEntity != null) {
					sFormaPagoComplementoDesc = catFormaPagoEntity.getDescripcion();
				}
			}
			if (sTotalComplemento != null) {
				//importeLetraComplemento = totalLetras.Convertir(sTotalComplemento, true, monedaComplemento);
			}
			
			if (monedaComplemento != null && !monedaComplemento.isEmpty()) {
	        	CatMonedaEntity catMonedaEntity = catMonedaService.get(monedaComplemento);
	        	if (catMonedaEntity != null) {
	        		monedaComplementoDesc = catMonedaEntity.getDescripcion();
	        	}
	        }
     
            String fe = selloCfd.substring(selloCfd.length()-8, selloCfd.length());
            String urlCodeQR = "https://verificacfdi.facturaelectronica.sat.gob.mx/default.aspx?&re=" + rfcEmisor + "&rr=" + rfcReceptor 
            					+ "&tt=" + sTotal + "&id=" + uuid + "&fe=" + fe;
            
            QrCodeGenerator qrCodeGenerator = new QrCodeGenerator();
            qrCodeGenerator.generateQRCodeImage(urlCodeQR, 530, 530, xsltfilePath, rfcReceptor, uuid);

            transformer.setParameter("periodicidad", periodicidad);
	        transformer.setParameter("meses", meses);
	        transformer.setParameter("anio", anio);
	        
	        transformer.setParameter("usoCFDIDesc", usoCFDIDesc);
	        transformer.setParameter("regimenFiscalDesc", regimenFiscalDesc);
	        transformer.setParameter("regimenFiscalReceptorDesc", regimenFiscalReceptorDesc);
	        transformer.setParameter("tipoComprobanteDesc", tipoComprobanteDesc);
	        transformer.setParameter("tipoDeComprobanteDesc", tipoDeComprobanteDesc);
	        transformer.setParameter("transaccion", transaccion);
	        transformer.setParameter("nombreObra", nombreObra);
	        transformer.setParameter("responsableObra", responsableObra);
	        transformer.setParameter("uuidRelacionado", uuidRelacionado);
	        transformer.setParameter("moneda", moneda);
	        transformer.setParameter("monedaDesc", monedaDesc);

	        transformer.setParameter("importeLetra", importeLetra);
	        transformer.setParameter("formaPagoLetter", sFormaPagoDesc);
	        transformer.setParameter("metodoPagoLetter", sMetodopagoDesc);
	        transformer.setParameter("ticketId", ticketId);
	        transformer.setParameter("pacEmail", emailPac);
	        transformer.setParameter("pacRZ", razonSocialPac);
	        transformer.setParameter("pacRfc", rfcPac);
	        transformer.setParameter("idPacExternal", idPac);
	        
	        transformer.setParameter("sFormaPagoComplemento", sFormaPagoComplemento);
	        transformer.setParameter("sFormaPagoComplementoDesc", sFormaPagoComplementoDesc);
	        
	        transformer.setParameter("impSaldoAntTotal", impSaldoAntTotal);
	        transformer.setParameter("impPagadoTotal", impPagadoTotal);
	        transformer.setParameter("impSaldoInsolutoTotal", impSaldoInsolutoTotal);
	        transformer.setParameter("qrCodeFileName", "QRCode" + rfcReceptor + "_" + uuid + ".png");
	        
		} catch (Exception e) {
            e.printStackTrace();
        }
		return "QRCode" + rfcReceptor + "_" + uuid + ".png";
	}
	
	public boolean crearArchivoXml(String fileName, String xml) {

		String path = configFacService.getConfig().get("Mail.PathFile");
		File fileNameXml = new File(path + fileName + ".xml");

		try (FileOutputStream fos = new FileOutputStream(fileNameXml);) {
			byte[] decoder = xml.getBytes();
			fos.write(decoder);
		} catch (Exception e) {
			e.printStackTrace();

			return false;
		}
		return true;
	}
	
	public boolean crearArchivoPdf(String fileName, String pdf64) {

		String path = configFacService.getConfig().get("Mail.PathFile");
		File fileNamePdf = new File(path + fileName + ".pdf");

		try (FileOutputStream fos = new FileOutputStream(fileNamePdf);) {
			byte[] decoder = Base64.getDecoder().decode(pdf64);
			fos.write(decoder);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	@Transactional(isolation=Isolation.READ_UNCOMMITTED)
	public List<ClientesTemporalModel> obtenerDatosFactura(String rfc, String sessionid, String ticket ) {
		List<ClientesTemporalModel> list = new ArrayList<ClientesTemporalModel>();
		
		facturasRepository.obtenerDatosFactura(seguridadService.encriptar(rfc), sessionid, ticket).forEach(item -> {
			ClientesTemporalModel itemList = new ClientesTemporalModel();
			itemList.setId(Integer.parseInt(item[0].toString()));
			itemList.setUuid(item[1].toString());
			itemList.setTicket(item[2].toString());
			itemList.setRazonSocial(seguridadService.desencriptar(item[3].toString()));
			itemList.setFechaTimbrado(item[4].toString());
			itemList.setNombreEstatus(item[5].toString());
			itemList.setRfc(seguridadService.desencriptar(item[6].toString()));
			itemList.setNombreArchivo(item[7].toString());
			list.add(itemList);
		});
		
		return list;
	}

	@Override
	@Transactional(isolation=Isolation.READ_UNCOMMITTED)
	public FacturasEntity getFacturaByTicket(String ticket) {
		return facturasRepository.findByTicket(ticket);
	}
	
	public void eliminarArchivo(String fileName) {
		String path = configFacService.getConfig().get("Mail.PathFile");
		UtilsFile.EliminarArchivo(path + fileName + ".xml");
		UtilsFile.EliminarArchivo(path + fileName + ".pdf");
		UtilsFile.EliminarArchivo(path + fileName + ".zip");
	}
	
	@Transactional(isolation=Isolation.READ_UNCOMMITTED)
	public boolean existRfcEmail(String rfc, String email) {
		return !facturasRepository.findByRfcAndEmail(seguridadService.encriptar(rfc), seguridadService.encriptar(email)).isEmpty() ? true : false;
	}
	
	public String getClaveUsoCfdiNC(String ticket) {
		String result = "";
		TicketEntity ticketBctHdr = ticketsBctService.findByTicket(ticket);
		if (ticketBctHdr != null) {
			FacturasEntity factura = facturasRepository.findByTicket(ticketBctHdr.getOriginal());
			if (factura != null) {
				String xml = seguridadService.desencriptar(factura.getXml());
				Document document = ObtenerDocumentXml (xml);
				Node nNode = document.getElementsByTagName("cfdi:Receptor").item(0);
		        Element eElement = (Element) nNode;
		        result = eElement.getAttribute("UsoCFDI");				
			}
		}
		return result;
	}
		
	public void getInformacionFacturaRelacionadaByTicket(String ticket, ClientesTemporalModel model) {
		
		logger.info("getInformacionFacturaRelacionadaByTicket: " + ticket);
		
		TicketEntity ticketBctHdr = ticketsBctService.findByTicket(ticket);
		if (ticketBctHdr != null) {
			logger.info("Leer en facturas-bct ticket-devolucion encontrado en Bct: "+ ticket);
			String uuidRelacionado = estFacturasRepository.findUuidByTicket(ticketBctHdr.getOriginal());
			logger.info("uuid: "+ uuidRelacionado + " del ticket de venta " + ticketBctHdr.getOriginal());
			if (uuidRelacionado != null && !uuidRelacionado.equals("")) {
				logger.info("Leer factura en autofacturador por uuid: "+ uuidRelacionado);
				FacturasEntity facturaRelacionada = getFacturaByUuid(uuidRelacionado);
				if (facturaRelacionada != null) {
					logger.info("Factura leida por uuid: "+ uuidRelacionado);
					String rfc = seguridadService.desencriptar(facturaRelacionada.getRfc());
					String email = seguridadService.desencriptar(facturaRelacionada.getEmail());
					String nombreObra = facturaRelacionada.getNombreObra();
					String responsableObra = facturaRelacionada.getResponsableObra();
					String razonSocial = "";
					String xml = seguridadService.desencriptar(facturaRelacionada.getXml());
					Document document = ObtenerDocumentXml (xml);
					Node nNode = document.getElementsByTagName("cfdi:Receptor").item(0);
			        Element eElement = (Element) nNode;
			        String usoCfdi = eElement.getAttribute("UsoCFDI");
			        String regimenFiscal = eElement.getAttribute("RegimenFiscalReceptor");
			        String codigoPostal = eElement.getAttribute("DomicilioFiscalReceptor");
			        razonSocial = eElement.getAttribute("Nombre");
			        
			        Node nNodeComprobante = document.getElementsByTagName("cfdi:Comprobante").item(0);
			        Element eElementComprobante = (Element) nNodeComprobante;
			        String versionFacturacionSat = eElementComprobante.getAttribute("Version");
			        
			        logger.info("versionFacturacionSat:" + versionFacturacionSat);
					
			        //TODO: Agregar método para que se obtenga por medio de la base de datos
					UsoDeCfdi usosCfdi = catUsosCfdiService.getUsoCfdiNC();
					
					logger.info("Uso de CFDI: " + usosCfdi.getDescripcionUso());
			        
					model.setRfc(rfc);
					model.setRazonSocial(razonSocial);
					model.setIdUsoCfdi(usosCfdi.getClave());
					model.setIdUsoCfdiReal(usosCfdi.getIdUsoCfdi());
					model.setVersionFacturacionSat(versionFacturacionSat);
					model.setEmail(email);
					model.setNombreObra(nombreObra);
					model.setResponsableObra(responsableObra);
					model.setRegimenFiscal(regimenFiscal);
					model.setCodigoPostal(codigoPostal);
				}
			}
			
		}
	}
	
	@Transactional
	public int cancelar(String facturaId, ClientesTemporalModel model) {

		int codigoRetorno = 300;
		errorComponent.setPagina("FacturasService-cancelar");
		RespuestaXml resultXml = getXml(facturaId);

		if (resultXml == null) {
			errorComponent.guardarLog("No fue posible obtener el cfdi para cancelar");
			return 118;
		}
		
		if (resultXml.getUuid()==null || resultXml.getEstatusId() == null) {
			model.setXml("No fue posible obtener el cfdi para cancelar");
			errorComponent.guardarLog(resultXml, model);
			return 118;
		}

		if (!resultXml.getEstatusId().contentEquals("4")) {
			model.setXml("El Cfdi no esta activo");
			errorComponent.guardarLog(resultXml, model);
			return 119;
		}
		
		FacturasEntity factura = facturasRepository.findByFacturaIdPac(facturaId);
		if (factura == null) {
			errorComponent.guardarLog("El cfdi no se encuentra en el autofacturador");
			return 120;
		}
		
		Resultado result = emisionService.cancelar(facturaId);
		
		if (result == null) {
			errorComponent.guardarLog("El cfdi no pudo ser cancelado");
			return 121;			
		}
		
		RespuestaXml resultXmlCancelacion = getXml(facturaId);
		if (resultXmlCancelacion != null && resultXmlCancelacion.getEstatusId() != null) {
			if (resultXmlCancelacion.getEstatusId().contentEquals("8")) {
				facturasRepository.actualizarAcuse(Integer.parseInt(facturaId), resultXmlCancelacion.getXmlAcuseCancelacionCfdi());
			} else {
				//La cancelaci\u00f3n del cfdi fue rechazada
				errorComponent.guardarLog(resultXmlCancelacion, model);
				return 122;
			}
			
		} else {
			errorComponent.guardarLog("El cfdi no pudo ser cancelado");
			return 121;				
		}
		
		return codigoRetorno;
		
	}
	
	@Override
	@Transactional(isolation=Isolation.READ_UNCOMMITTED)
	public FacturasEntity getFacturaByIdFacturaPac(String idFacturaPac) {
		return facturasRepository.findByFacturaIdPac(idFacturaPac);
	}

	public String transformarXmlTicketXmlPac(String xml, ClientesTemporalModel clientes) throws TransformerException {
		String xmlConvertido = "";
		xml = xml.replaceAll("cfdi:", "");
		xml = xml.replace(
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:cfdi=\"http://www.sat.gob.mx/cfd/3\" xsi:schemaLocation=\"http://www.sat.gob.mx/cfd/3 http://www.sat.gob.mx/sitio_internet/cfd/3/cfdv33.xsd\"",
				"");

		Document docOri = UtilsFile.ObtenerDocumentXml(xml);

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			DOMImplementation implementation = builder.getDOMImplementation();
			Document doc3 = implementation.createDocument(null, "NewXml", null);

			// Comprobante
			Node nNodeComprobanteOld = docOri.getElementsByTagName("Comprobante").item(0);
			Element eComprobanteOld = (Element) nNodeComprobanteOld;

			boolean version40 = (eComprobanteOld.getAttribute("Version").equals("4.0"));
			
			Element comprobante = doc3.createElement("Comprobante");

			String cTipoDeComprobante = eComprobanteOld.getAttribute("TipoDeComprobante");
			
			String nvaFecha = UtilsFechas.formatear(new Date(), "yyyy-MM-dd'T'HH:mm:ss");
			
			comprobante.setAttribute("condicionesDePago", eComprobanteOld.getAttribute("CondicionesDePago"));
			comprobante.setAttribute("fecha", nvaFecha);
			comprobante.setAttribute("folio", eComprobanteOld.getAttribute("Folio"));
			comprobante.setAttribute("formaPago", eComprobanteOld.getAttribute("FormaPago"));
			comprobante.setAttribute("lugarExpedicion", eComprobanteOld.getAttribute("LugarExpedicion"));
			comprobante.setAttribute("metodoPago", clientes.getMetodoPago());
			comprobante.setAttribute("serie", eComprobanteOld.getAttribute("Serie"));
			comprobante.setAttribute("tipoDeComprobante", eComprobanteOld.getAttribute("TipoDeComprobante"));
			comprobante.setAttribute("version", eComprobanteOld.getAttribute("Version"));
			
			if (version40) {
				if (eComprobanteOld.getAttribute("calle") != null) comprobante.setAttribute("calle", eComprobanteOld.getAttribute("calle"));
				if (eComprobanteOld.getAttribute("noExterior") != null) comprobante.setAttribute("noExterior", eComprobanteOld.getAttribute("noExterior"));
				if (eComprobanteOld.getAttribute("noInterior") != null) comprobante.setAttribute("noInterior", eComprobanteOld.getAttribute("noInterior"));
				if (eComprobanteOld.getAttribute("colonia") != null) comprobante.setAttribute("colonia", eComprobanteOld.getAttribute("colonia"));
				if (eComprobanteOld.getAttribute("localidad") != null) comprobante.setAttribute("localidad", eComprobanteOld.getAttribute("localidad"));
				if (eComprobanteOld.getAttribute("referencia") != null) comprobante.setAttribute("referencia", eComprobanteOld.getAttribute("referencia"));
				if (eComprobanteOld.getAttribute("municipio") != null) comprobante.setAttribute("municipio", eComprobanteOld.getAttribute("municipio"));
				if (eComprobanteOld.getAttribute("estado") != null) comprobante.setAttribute("estado", eComprobanteOld.getAttribute("estado"));
				if (eComprobanteOld.getAttribute("pais") != null) comprobante.setAttribute("pais", eComprobanteOld.getAttribute("pais"));
			}

			if (!clientes.getUuidRelacionado().isEmpty())
			{
				Element eRelacionados = doc3.createElement("CfdiRelacionados");
				Element eRelacionado = doc3.createElement("CfdiRelacionado");
				String ctipoRelacion = "";
						
				if (cTipoDeComprobante.equals("I")) 
					ctipoRelacion = "04";
				
				if (cTipoDeComprobante.equals("E")) 
					ctipoRelacion = "01";
				
				if (cTipoDeComprobante.equals("E") || cTipoDeComprobante.equals("I")) {
					eRelacionados.setAttribute("tipoRelacion", ctipoRelacion);
					eRelacionado.setAttribute("uuid", clientes.getUuidRelacionado());
					eRelacionado.setAttribute("ordenador", "1");
					
					eRelacionados.appendChild(eRelacionado);
					comprobante.appendChild(eRelacionados);
				}
			}
			
			// Emisor
			Node nNodeEmisorOld = docOri.getElementsByTagName("Emisor").item(0);
			Element eEmisorOld = (Element) nNodeEmisorOld;
			
			Element eEmisor = doc3.createElement("Emisor");
			
			eEmisor.setAttribute("rfc", eEmisorOld.getAttribute("rfc"));
			eEmisor.setAttribute("nombre", eEmisorOld.getAttribute("nombre"));
			eEmisor.setAttribute("regimenFiscal", eEmisorOld.getAttribute("regimenFiscal"));
			
			if (version40) {
				if (eEmisorOld.getAttribute("calle") != null) eEmisor.setAttribute("calle", eEmisorOld.getAttribute("calle"));
				if (eEmisorOld.getAttribute("noExterior") != null) eEmisor.setAttribute("noExterior", eEmisorOld.getAttribute("noExterior"));
				if (eEmisorOld.getAttribute("noInterior") != null) eEmisor.setAttribute("noInterior", eEmisorOld.getAttribute("noInterior"));
				if (eEmisorOld.getAttribute("colonia") != null) eEmisor.setAttribute("colonia", eEmisorOld.getAttribute("colonia"));
				if (eEmisorOld.getAttribute("localidad") != null) eEmisor.setAttribute("localidad", eEmisorOld.getAttribute("localidad"));
				if (eEmisorOld.getAttribute("referencia") != null) eEmisor.setAttribute("referencia", eEmisorOld.getAttribute("referencia"));
				if (eEmisorOld.getAttribute("municipio") != null) eEmisor.setAttribute("municipio", eEmisorOld.getAttribute("municipio"));
				if (eEmisorOld.getAttribute("estado") != null) eEmisor.setAttribute("estado", eEmisorOld.getAttribute("estado"));
				if (eEmisorOld.getAttribute("pais") != null) eEmisor.setAttribute("pais", eEmisorOld.getAttribute("pais"));
				if (eEmisorOld.getAttribute("codigoPostal") != null) eEmisor.setAttribute("codigoPostal", eEmisorOld.getAttribute("codigoPostal"));
			}
			
			comprobante.appendChild(eEmisor);

			// Receptor
			Element eReceptor = doc3.createElement("Receptor");

			eReceptor.setAttribute("rfc", UtilsString.convertirCaracteresXml(clientes.getRfc()));
			eReceptor.setAttribute("nombre", UtilsString.convertirCaracteresXml(clientes.getRazonSocial()));
			eReceptor.setAttribute("usoCfdi", clientes.getIdUsoCfdi());
			
			if (version40) {
				eReceptor.setAttribute("regimenFiscal", clientes.getRegimenFiscal());
				eReceptor.setAttribute("codigoPostal", clientes.getCodigoPostal());
			}

			comprobante.appendChild(eReceptor);

			// Concepto
			NodeList nNodeConceptoOld = docOri.getElementsByTagName("Concepto");
			Element eConceptos = doc3.createElement("Conceptos");
			int nConta = 0; 

			for (int i = 0; i < nNodeConceptoOld.getLength(); i++) {
				Node nNodeConceptoItemOld = nNodeConceptoOld.item(i);
				nConta++;

				if (nNodeConceptoItemOld.getNodeType() == Node.ELEMENT_NODE) {
					Element eConcepto = doc3.createElement("Concepto");
					Element eImpuestos = doc3.createElement("Impuestos");
					Element eTraslados = doc3.createElement("Traslados");
					Element eTraslado = doc3.createElement("Traslado");
					
					Element eConceptoItem = (Element) nNodeConceptoItemOld;

					eConcepto.setAttribute("claveProdServ", eConceptoItem.getAttribute("ClaveProdServ"));
					eConcepto.setAttribute("noIdentificacion", eConceptoItem.getAttribute("NoIdentificacion"));
					eConcepto.setAttribute("cantidad", eConceptoItem.getAttribute("Cantidad"));
					eConcepto.setAttribute("claveUnidad", eConceptoItem.getAttribute("ClaveUnidad"));
					eConcepto.setAttribute("unidad", eConceptoItem.getAttribute("Unidad"));
					eConcepto.setAttribute("descripcion", eConceptoItem.getAttribute("Descripcion"));
					eConcepto.setAttribute("valorUnitario", eConceptoItem.getAttribute("ValorUnitario"));
					eConcepto.setAttribute("importe", eConceptoItem.getAttribute("Importe"));
					eConcepto.setAttribute("nivel", "1");
					eConcepto.setAttribute("descuento", eConceptoItem.getAttribute("Descuento"));
					eConcepto.setAttribute("ordenador", Integer.toString(nConta));
					eConcepto.setAttribute("padre", "0");
					
					if (version40) {
						eConcepto.setAttribute("objetoImp", eConceptoItem.getAttribute("ObjetoImp"));
					}

					NodeList hijos = eConceptoItem.getChildNodes();
					Node nImpuestosItem  = hijos.item(0);
					
					NodeList nTrasladosItem  = nImpuestosItem.getChildNodes();
					Node nTrasladosxItem = nTrasladosItem.item(0); 
					
					NodeList nTrasladoItem = nTrasladosxItem.getChildNodes();
					Node nTrasladoxItem = nTrasladoItem.item(0); 
					
					Element eTrasladoItemx = (Element) nTrasladoxItem;
					
					eTraslado.setAttribute("base", eTrasladoItemx.getAttribute("Base"));
					eTraslado.setAttribute("importe", eTrasladoItemx.getAttribute("Importe"));
					eTraslado.setAttribute("impuesto", eTrasladoItemx.getAttribute("Impuesto"));
					eTraslado.setAttribute("ordenador", Integer.toString(nConta));
					eTraslado.setAttribute("tasaOCuota", eTrasladoItemx.getAttribute("TasaOCuota"));
					eTraslado.setAttribute("tipoFactor", eTrasladoItemx.getAttribute("TipoFactor"));
					
					eTraslados.appendChild(eTraslado);
					eImpuestos.appendChild(eTraslados);
					eConcepto.appendChild(eImpuestos);
					
					eConceptos.appendChild(eConcepto);
				}
			}
			comprobante.appendChild(eConceptos);

			// Fin Concepto
			
			// Total de Impuestos
			Element eImpuestos = doc3.createElement("Impuestos");
			Element eTraslados = doc3.createElement("Traslados");
			Element eTraslado = doc3.createElement("Traslado");
			
			NodeList nNodeImpuestosOld = docOri.getElementsByTagName("Impuestos");
			Element eImpuestosItem = (Element) nNodeImpuestosOld.item(nNodeImpuestosOld.getLength()-1);
			NodeList hijos = eImpuestosItem.getChildNodes();
			eImpuestos.setAttribute("TotalImpuestosTrasladados", eImpuestosItem.getAttribute("TotalImpuestosTrasladados"));
			
			Node nImpuestosItem  = hijos.item(0);
			
			NodeList nTrasladosItem  = nImpuestosItem.getChildNodes();
			Node nTrasladosxItem = nTrasladosItem.item(0); 
			Element eTrasladoItemy = (Element) nTrasladosxItem;
			
			eTraslado.setAttribute("importe", eTrasladoItemy.getAttribute("Importe"));
			eTraslado.setAttribute("impuesto", eTrasladoItemy.getAttribute("Impuesto"));
			eTraslado.setAttribute("ordenador", Integer.toString(nConta+1));
			eTraslado.setAttribute("tasaOCuota", eTrasladoItemy.getAttribute("TasaOCuota"));
			eTraslado.setAttribute("tipoFactor", eTrasladoItemy.getAttribute("TipoFactor"));
			
			eTraslados.appendChild(eTraslado);
			eImpuestos.appendChild(eTraslados);
			comprobante.appendChild(eImpuestos);
			//
			
			/********* Totales **********/
			double total = Double.parseDouble(eComprobanteOld.getAttribute("Total"));
			String sTotal = "";
			sTotal = UtilsString.formatearDecimales(total, DECIMALES_PRECIO);
			NumeroaLetras totalLetras = new NumeroaLetras();
			String importeLetra = totalLetras.Convertir(sTotal, true);
			
			Element eTotales = doc3.createElement("Totales");
			eTotales.setAttribute("subTotal", eComprobanteOld.getAttribute("SubTotal"));
			eTotales.setAttribute("descuento", eComprobanteOld.getAttribute("Descuento"));
			eTotales.setAttribute("moneda", eComprobanteOld.getAttribute("Moneda"));
			eTotales.setAttribute("tipoCambio", eComprobanteOld.getAttribute("TipoCambio"));
			eTotales.setAttribute("total", eComprobanteOld.getAttribute("Total"));
			eTotales.setAttribute("importeLetra", importeLetra);
			eTotales.setAttribute("totalImpuestosTrasladados", eImpuestosItem.getAttribute("TotalImpuestosTrasladados"));
			
			comprobante.appendChild(eTotales);

			/********* Control **********/
			//DML Se modifica por solicitud de detecno 2
			Element eControl = doc3.createElement("Control");
			eControl.setAttribute("cfd", "1");
			eControl.setAttribute("sucursal", "Opcional");
			eControl.setAttribute("estatusId", "1");
			eControl.setAttribute("estatusIdImpresion", "0");
			eControl.setAttribute("estatusIdCorreo", "1");
			eControl.setAttribute("estatusIdArchivo", "0");
			eControl.setAttribute("rechazoId", "0");
			eControl.setAttribute("addendaId", "0");
			eControl.setAttribute("complementoId", "0");
			eControl.setAttribute("pathImpresion", "");
			eControl.setAttribute("correo", "detecno@detecno.com");
			eControl.setAttribute("formatoImpresion", "1");
			eControl.setAttribute("formatoCorreo", "1");
			eControl.setAttribute("formatoWeb", "1");

			comprobante.appendChild(eControl);
			
			Element eComentarios = doc3.createElement("Comentarios");
			eComentarios.setAttribute("descripcion", "Opcional");
			comprobante.appendChild(eComentarios);
			Element eCodigoBarras = doc3.createElement("CodigoBarras");
			eCodigoBarras.setAttribute("valorCodigo", "1234567890");
			eCodigoBarras.setAttribute("tipoCodigo", "3");
			eCodigoBarras.setAttribute("ordenador", "1");
			
			comprobante.appendChild(eCodigoBarras);
			
			Element eComplementos = doc3.createElement("Complementos");
			Element eAddenda = doc3.createElement("Addenda");
			comprobante.appendChild(eComplementos);
			comprobante.appendChild(eAddenda);
			Element eFoliado = doc3.createElement("Foliado");
			eFoliado.setAttribute("automatico", "false");
			comprobante.appendChild(eFoliado);
			
			doc3.getDocumentElement().appendChild(comprobante);
			xmlConvertido = UtilsFile.getStringFromDocument(doc3);
			xmlConvertido = xmlConvertido.replaceAll("<NewXml>", "");
			xmlConvertido = xmlConvertido.replaceAll("</NewXml>", "");
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		return xmlConvertido;
		
	}

	public String transformarXmlTicketXmlPac(ClientesTemporalModel model) {

		String xmlConvertido =  "";
		boolean version33 = (model.getVersionFacturacionSat().equals("3.3"));
		boolean version40 = (model.getVersionFacturacionSat().equals("4.0"));
		
		String versionSat ="";
		String serie ="";
		String folio ="";
		String fecha = "";
		String formaPago ="";
		String condicionesDePago ="";
		String tipoComprobante ="";
		String exportacion = "";
		String metodoPago ="";
		
		String calleExpedicion = "";
		String noExteriorExpedicion = "";
		String noInteriorExpedicion = "";
		String coloniaExpedicion = "";
		String localidadExpedicion = "";
		String referenciaExpedicion = "";
		String municipioExpedicion = "";
		String estadoExpedicion = "";
		String paisExpedicion = "";
		String codigoPostalExpedicion = "";

		String rfcEmisor = "";
		ConfDatosEmisorRebEntity emisor = confDatosEmisorRebRepository.findRfcActivo();
		if (emisor != null) {
			rfcEmisor = emisor.getRfc();
		}
		
		String tipoComprobanteAPI = "R";
		String formaPagoAPI = "100";
		int tienda = 9501;
		EmisorYLugarExpedicionDto datos = obtenerDatosEmisionExpedicion(rfcEmisor, tienda, model.getVersionFacturacionSat(), tipoComprobanteAPI, model.getIdAplicacion(), formaPagoAPI );
		
		if (datos.getRespuesta().getCodigo().equals("1")) {
			
			versionSat = datos.getData().getVersion();
			serie = datos.getData().getSerie();
			folio = datos.getData().getFolio();
			fecha = datos.getData().getFecha();
			tipoComprobante = datos.getData().getTipoDeComprobante();
			exportacion = datos.getData().getExportacion();
			formaPago = datos.getData().getFormaPago();
			metodoPago = datos.getData().getMetodoPago();
			condicionesDePago = datos.getData().getCondicionesDePago();
			
			calleExpedicion = datos.getData().getLugarExpedicion().getCalle();
			noExteriorExpedicion = datos.getData().getLugarExpedicion().getNoExterior();
			noInteriorExpedicion = datos.getData().getLugarExpedicion().getNoInterior();
			coloniaExpedicion = datos.getData().getLugarExpedicion().getColonia();
			localidadExpedicion = datos.getData().getLugarExpedicion().getLocalidad();
			referenciaExpedicion = datos.getData().getLugarExpedicion().getReferencia();
			municipioExpedicion = datos.getData().getLugarExpedicion().getMunicipio();
			estadoExpedicion = datos.getData().getLugarExpedicion().getEstado();
			paisExpedicion = datos.getData().getLugarExpedicion().getPais();
			codigoPostalExpedicion = datos.getData().getLugarExpedicion().getCodigoPostal();
		}

		if (version33) {
			serie = emisor.getSerie();
			folio = StringUtils.leftPad(Integer.toString(confDatosEmisorRebRepository.findFolio()), 7, '0');
			fecha = UtilsFechas.formatear(new Date(), "yyyy-MM-dd'T'HH:mm:ss");
			tipoComprobante = "I";
			//formaPago = emisor.getFormaPago();
			//metodoPago = emisor.getMetodoPago();
			condicionesDePago = "1";
			codigoPostalExpedicion = emisor.getLugarExpedicion();
		}
		
		/*********Comprobante**********/
		xmlConvertido += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlConvertido += "<Comprobante version=\"" + versionSat + "\" ";
		xmlConvertido += "serie=\"" + serie + "\" ";
		xmlConvertido += "folio=\"" + folio + "\" ";
		xmlConvertido += "fecha=\"" + fecha + "\" ";
		xmlConvertido += "formaPago=\"" + formaPago + "\" ";
		xmlConvertido += "condicionesDePago=\"" + condicionesDePago + "\" ";
		xmlConvertido += "tipoDeComprobante=\"" + tipoComprobante + "\" ";
		if (version40) xmlConvertido += "exportacion=\"" + exportacion + "\" ";
		xmlConvertido += "metodoPago=\"" + metodoPago + "\" ";
		
		xmlConvertido += " lugarExpedicion=\"" + codigoPostalExpedicion + "\" >";	
		
		
		/*********Emisor**********/
		String nombreEmisor = "";
		String regimenFiscalEmisor = "";
		String calle = "";
		String noExterior = "";
		String noInterior = "";
		String colonia = "";
		String localidad = "";
		String referencia = "";
		String municipio = "";
		String estado = "";
		String pais = "";
		String codigoPostal = "";

		if (datos.getRespuesta().getCodigo().equals("1")) {
			nombreEmisor = datos.getData().getEmisorNode().getRazonSocial();
			regimenFiscalEmisor = datos.getData().getEmisorNode().getRegimenFiscal();
			calle = datos.getData().getEmisorNode().getCalle();
			noExterior = datos.getData().getEmisorNode().getNoExterior();
			noInterior = datos.getData().getEmisorNode().getNoInterior();
			colonia = datos.getData().getEmisorNode().getColonia();
			localidad = datos.getData().getEmisorNode().getLocalidad();
			referencia = datos.getData().getEmisorNode().getReferencia();
			municipio = datos.getData().getEmisorNode().getMunicipio();
			estado = datos.getData().getEmisorNode().getEstado();
			pais = datos.getData().getEmisorNode().getPais();
			codigoPostal = datos.getData().getEmisorNode().getCodigoPostal();
		}
		
		xmlConvertido += "<Emisor rfc=\"" + rfcEmisor + "\" ";
		xmlConvertido += "nombre=\"" + nombreEmisor + "\" ";
		xmlConvertido += " regimenFiscal=\"" + regimenFiscalEmisor + "\"";
		
		xmlConvertido += " >";
		if (version40) {
			xmlConvertido += "<DomicilioFiscal";
			if (calle!=null && !calle.isEmpty()) xmlConvertido += " calle=\"" + calle + "\"";
			if (noExterior!=null && !noExterior.isEmpty()) xmlConvertido += " noExterior=\"" + noExterior + "\"";
			if (noInterior!=null && !noInterior.isEmpty()) xmlConvertido += " noInterior=\"" + noInterior + "\"";
			if (colonia!=null && !colonia.isEmpty()) xmlConvertido += " colonia=\"" + colonia + "\"";
			if (localidad!=null && !localidad.isEmpty()) xmlConvertido += " localidad=\"" + localidad + "\"";
			if (referencia!=null && !referencia.isEmpty()) xmlConvertido += " referencia=\"" + referencia + "\"";
			if (municipio!=null && !municipio.isEmpty()) xmlConvertido += " municipio=\"" + municipio + "\"";
			if (estado!=null && !estado.isEmpty()) xmlConvertido += " estado=\"" + estado + "\"";
			if (pais!=null && !pais.isEmpty()) xmlConvertido += " pais=\"" + pais + "\"";
			if (codigoPostal!=null && !codigoPostal.isEmpty()) xmlConvertido += " codigoPostal=\"" + codigoPostal + "\"";			
			xmlConvertido += " />";
			
			xmlConvertido += "<ExpedidoEn";
			if (calleExpedicion!= null && !calleExpedicion.isEmpty()) xmlConvertido += " calle=\"" + calleExpedicion + "\"";
			if (noExteriorExpedicion!= null && !noExteriorExpedicion.isEmpty()) xmlConvertido += " noExterior=\"" + noExteriorExpedicion + "\"";
			if (noInteriorExpedicion!= null && !noInteriorExpedicion.isEmpty()) xmlConvertido += " noInterior=\"" + noInteriorExpedicion + "\"";
			if (coloniaExpedicion!= null && !coloniaExpedicion.isEmpty()) xmlConvertido += " colonia=\"" + coloniaExpedicion + "\"";
			if (localidadExpedicion!= null && !localidadExpedicion.isEmpty()) xmlConvertido += " localidad=\"" + localidadExpedicion + "\"";
			if (referenciaExpedicion!= null && !referenciaExpedicion.isEmpty()) xmlConvertido += " referencia=\"" + referenciaExpedicion + "\"";
			if (municipioExpedicion!= null && !municipioExpedicion.isEmpty()) xmlConvertido += " municipio=\"" + municipioExpedicion + "\"";
			if (estadoExpedicion!= null && !estadoExpedicion.isEmpty()) xmlConvertido += " estado=\"" + estadoExpedicion + "\"";
			if (paisExpedicion!= null && !paisExpedicion.isEmpty()) xmlConvertido += " pais=\"" + paisExpedicion + "\"";
			xmlConvertido += " codigoPostal=\"" + codigoPostalExpedicion + "\"";	
			xmlConvertido += " />";
		}
		
		xmlConvertido += " </Emisor>";

		
		/*********Receptor**********/
		xmlConvertido += "<Receptor rfc=\"" + UtilsString.convertirCaracteresXml(model.getRfc()) + "\" ";
		xmlConvertido += "nombre=\"" + UtilsString.convertirCaracteresXml(model.getRazonSocial()) + "\" ";
		xmlConvertido += "usoCfdi=\"" + model.getIdUsoCfdi() + "\"";
		if (version40) {
			if (!model.getRegimenFiscal().isEmpty()) xmlConvertido += " regimenFiscalReceptor=\"" + model.getRegimenFiscal() + "\"";
			if (!model.getCodigoPostal().isEmpty()) xmlConvertido += " domicilioFiscalReceptor=\"" + model.getCodigoPostal() + "\"";			
		}
		xmlConvertido += " >";	

		xmlConvertido += " </Receptor>";
					
		/*********Conceptos**********/		
		String claveProdServ = "";
		String noIdentificacion = "";
		String cantidad = "";
		String claveUnidad = "";
		String unidad = "";
		String descripcion = "";
		String valorUnitario = "";
		String sImporteConcepto = "";
		String descuentoDefault = "0.00";
		String ordenadorDefault = "1";
		String nivelDefault = "1";
		
		CatDatosConceptosRebEntity datosConcepto = catDatosConceptosRebRepository.findRfcActivo();
		if (datosConcepto != null) {
			claveProdServ = datosConcepto.getClaveProdServ().trim();
			noIdentificacion = datosConcepto.getNoIdentificacion().trim();
			cantidad = UtilsString.formatearDecimales(datosConcepto.getCantidad(), DECIMALES_PRECIO);
			claveUnidad = datosConcepto.getClaveUnidad().trim();
			unidad = datosConcepto.getUnidad();
			descripcion = datosConcepto.getDescripcion().trim().toUpperCase();
			valorUnitario = UtilsString.formatearDecimales(model.getRebate().getSubTotal(), DECIMALES_PRECIO);
			sImporteConcepto = UtilsString.formatearDecimales(model.getRebate().getSubTotal(), DECIMALES_TASA);
		}
		
		xmlConvertido += "<Conceptos>";

		xmlConvertido += "<Concepto claveProdServ=\"" + claveProdServ + "\" ";
		xmlConvertido += "noIdentificacion=\"" + noIdentificacion + "\" ";
		xmlConvertido += "cantidad=\"" + cantidad + "\" ";
		xmlConvertido += "claveUnidad=\"" + claveUnidad + "\" ";
		xmlConvertido += "unidad=\"" + unidad + "\" ";
		descripcion = UtilsString.convertirCaracteresXml(descripcion);
		xmlConvertido += "descripcion=\"" + descripcion + "\" ";
		xmlConvertido += "valorUnitario=\"" + valorUnitario + "\" ";
		xmlConvertido += "importe=\"" + sImporteConcepto + "\" ";
		xmlConvertido += "descuento=\"" + descuentoDefault + "\" ";
		xmlConvertido += "ordenador=\"" + ordenadorDefault + "\" ";
		xmlConvertido += "nivel=\"" + nivelDefault + "\" ";
		xmlConvertido += "padre=\"0\" ";
		
		if (version40) {
			String objetoImp= "02";
			xmlConvertido += "objetoImp=\"" + objetoImp + "\" ";
			
		}
		xmlConvertido += " >";

		
		/*********Conceptos**********/
		xmlConvertido += "<Impuestos>";
		/*********Traslados**********/
		xmlConvertido += "<Traslados>";

		String impuesto = "";
		String tipoFactor = "";
		String tasaOCuota = "";
		String sImporteTraslado = "";
		String sImporteImpuesto = "";
		String ordenadorTrasladoConcepto = "";
		double impuestoCalculado = 0D;
		
		//validar formato de doubles
		CatDatosImpuestosRebEntity datosImpuesto = catDatosImpuestosRebRepository.findRfcActivo();
		if (datosImpuesto != null) {
			impuesto = datosImpuesto.getImpuesto().trim();
			tipoFactor = datosImpuesto.getTipoFactor().trim();
			tasaOCuota = UtilsString.formatearDecimales(datosImpuesto.getTasaCuota(), DECIMALES_TASA);
			double impuestoCalculadoSinRedondear = model.getRebate().getSubTotal() * datosImpuesto.getTasaCuota();
			sImporteTraslado = UtilsString.formatearDecimales(impuestoCalculadoSinRedondear, DECIMALES_TASA);
			impuestoCalculado = Double.parseDouble(UtilsString.formatearDecimales(impuestoCalculadoSinRedondear, DECIMALES_PRECIO));
			sImporteImpuesto = UtilsString.formatearDecimales(impuestoCalculado, DECIMALES_TASA);
			ordenadorTrasladoConcepto = Integer.toString(datosImpuesto.getOrdenador());
		}
		
		/*********Traslado***********/
		xmlConvertido += "<Traslado base=\"" + sImporteConcepto + "\" ";
		xmlConvertido += "impuesto=\"" + impuesto + "\" ";
		xmlConvertido += "tipoFactor=\"" + tipoFactor + "\" ";
		xmlConvertido += "tasaOCuota=\"" + tasaOCuota + "\" ";
		xmlConvertido += "importe=\"" + sImporteTraslado + "\" ";
		xmlConvertido += "ordenador=\"" + ordenadorTrasladoConcepto + "\" />";

		
		xmlConvertido += "</Traslados>";
		xmlConvertido += "</Impuestos>";
		xmlConvertido += "</Concepto>";

		xmlConvertido += "</Conceptos>";
		
		/*********Impuestos**********/
		
		xmlConvertido += "<Impuestos>";
		/*********Traslados**********/
		xmlConvertido += "<Traslados>";
		
		/*********Traslado**********/
		String ordenadorTrasladoImpuestoDefault = "2";
		xmlConvertido += "<Traslado impuesto=\"" + impuesto + "\" ";
		xmlConvertido += "base=\"" + sImporteConcepto + "\" ";
		xmlConvertido += "tipoFactor=\"" + tipoFactor + "\" ";
		xmlConvertido += "tasaOCuota=\"" + tasaOCuota + "\" ";
		xmlConvertido += "importe=\"" + sImporteImpuesto + "\" ";
		xmlConvertido += "ordenador=\"" + ordenadorTrasladoImpuestoDefault + "\" />";

		xmlConvertido += "</Traslados>";
		xmlConvertido += "</Impuestos>";
		
		double total = model.getRebate().getSubTotal() + impuestoCalculado;
		String sTotal = UtilsString.formatearDecimales(total, DECIMALES_PRECIO);
		NumeroaLetras totalLetras = new NumeroaLetras();
		String importeLetra = totalLetras.Convertir(sTotal, true);
		
		/*********Totales**********/
		String monedaDefault = model.getRebate().getMoneda();
		double tipocambio = model.getRebate().getTipoCambio();
		String tipoCambioDefault = UtilsString.formatearDecimales(tipocambio, DECIMALES_PRECIO);
		
		xmlConvertido += "<Totales subTotal=\"" + sImporteConcepto +  "\" ";
		xmlConvertido += "descuento=\"" + descuentoDefault +  "\" ";
		xmlConvertido += "moneda=\"" + monedaDefault +  "\" ";
		xmlConvertido += "tipoCambio=\"" + tipoCambioDefault +  "\" ";
		xmlConvertido += "total=\"" + sTotal +  "\" ";
		xmlConvertido += "importeLetra=\"" + importeLetra +  "\" ";
		xmlConvertido += "totalImpuestosTrasladados=\"" + sImporteImpuesto +  "\" />";
		
		/*********DatosExtraCFD**********/
		
		String extra1 = model.getRebate().getTicket().substring(14, 19);		
		String extra2 = model.getTicket();
		
		xmlConvertido += "<DatosExtraCFD extra1=\"" + extra1 + "\" ";
		xmlConvertido += " extra2=\"" + extra2 + "\" ";
		if (version40) {
			String tipoDocumento = "V";
			String fechaCompra = model.getTicket().substring(0, 8);
			fechaCompra = fechaCompra.substring(6, 8) + "-" + fechaCompra.substring(4, 6) + "-" + fechaCompra.substring(0, 4);
			String oc = "";
			if (model.getTicket().length() < 16) {
				oc = model.getTicket();
			}			

			xmlConvertido += " extra4=\"" + tipoComprobanteAPI + "\" ";
			xmlConvertido += " extra6=\"" + fechaCompra + "\" ";
			xmlConvertido += " extra7=\"" + tipoDocumento + "\" ";
			xmlConvertido += " extra8=\"" + "SODIMAC" + "\" ";
			xmlConvertido += " extra9=\"" + oc + "\" ";
		}

		xmlConvertido += " />";


		/*********Control**********/
		//DML Se modifica por solicitud de detecno 3
        xmlConvertido += "<Control cfd=\"1\" sucursal=\"Opcional\" estatusId=\"1\" estatusIdImpresion=\"0\" estatusIdCorreo=\"1\" estatusIdArchivo=\"0\" rechazoId=\"0\" addendaId=\"0\" complementoId=\"0\" pathImpresion=\"\" correo=\"detecno@detecno.com\" formatoImpresion=\"1\" formatoCorreo=\"1\" formatoWeb=\"1\" />";

		xmlConvertido += "<Comentarios descripcion=\"Opcional\" />";
		xmlConvertido += "<CodigoBarras>";
		xmlConvertido += "<CodBar valorCodigo=\"1234567890\" tipoCodigo=\"3\" ordenador=\"1\" />";
		xmlConvertido += "</CodigoBarras>";
		xmlConvertido += "<Complementos/>";
		xmlConvertido += "<Addenda/>";
		xmlConvertido += "<Foliado automatico=\"false\" />";
		
		xmlConvertido += "</Comprobante>";
		
		return xmlConvertido;

	}
	
	public void crearZip(String uuid) {

		String fileName = "";
		String xml = "";
		
		FacturasEntity factura = getFacturaByUuid(uuid);
		if (factura != null) {
			xml = seguridadService.desencriptar(factura.getXml());
			fileName = factura.getNombreArchivo();
		}

		crearArchivoXml(fileName, xml);
		crearPdfFromXml(fileName, xml);
		crearZipXmlPdf(fileName);

	}

	public void crearPdf(String uuid) {

		String fileName = "";
		String xml = "";
		
		FacturasEntity factura = getFacturaByUuid(uuid);
		if (factura != null) {
			xml = seguridadService.desencriptar(factura.getXml());
			fileName = factura.getNombreArchivo();
		}

		crearPdfFromXml(fileName, xml);

	}

	public void crearPdfComplemento(String uuid) {

		String fileName = "";
		String xml = "";
		
		ComplementosEntity complemento = complementosRepository.findByUuid(uuid);
		if (complemento != null) {
			xml = seguridadService.desencriptar(complemento.getXml());
			fileName = complemento.getNombreArchivo();
		}

		crearPdfComplementoFromXml(fileName, xml);

	}

	public String obtenerBase64(String uuid) {
		
		String result = "";

		FacturasEntity factura = getFacturaByUuid(uuid);
		if (factura == null) {
			return result;			
		}
		
		String path = configFacService.getConfig().get("Mail.PathFile");
		String xml = seguridadService.desencriptar(factura.getXml());

		crearPdfFromXml(factura.getNombreArchivo(), xml);
		String fileName = path + factura.getNombreArchivo() + ".pdf";
		result =  Convert.fileToB64(fileName);
		UtilsFile.EliminarArchivo(fileName);				
		
		
		return result;
	}
	
	public String obtenerBase64Complemento(String uuid) {
		
		String result = "";

		ComplementosEntity complemento = complementosRepository.findByUuid(uuid);
		if (complemento == null) {
			return result;			
		}
		
		String path = configFacService.getConfig().get("Mail.PathFile");
		String xml = seguridadService.desencriptar(complemento.getXml());

		crearPdfComplementoFromXml(complemento.getNombreArchivo(), xml);
		String fileName = path + complemento.getNombreArchivo() + ".pdf";
		result =  Convert.fileToB64(fileName);
		UtilsFile.EliminarArchivo(fileName);				
		
		
		return result;
	}

	@Transactional
	public int actualizarReintentos (int contadorid, int valorestatus, String rfc, String ticket) {
		return facturasRepository.actualizarReintentos(contadorid, valorestatus, seguridadService.encriptar(rfc), ticket);		
	}

	String obtenerUltimos4Digitos (String ticket) {
		String result = "";
		int largo = ticket.length();
		result = ticket.substring(largo-4, largo);
		return result;
	}
	
	@Transactional
	private int insertarLogFacturacion(ClientesTemporalModel model) {
		String rfc = seguridadService.encriptar(model.getRfc());
		String email = seguridadService.encriptar(model.getEmail());

		return facturasRepository.insertarLogFacturacion(rfc
					, model.getTicket()
					, email
					, model.getPac()
					, model.getIdFacturaPac()
					, model.getIdEstatusFactura()
					);			
	}
	
	public void actualizarEstatusLog (ClientesTemporalModel model, int estatus) {
		model.setIdEstatusFactura(estatus);
		insertarLogFacturacion(model);		
	}
	
	@Transactional
	private int actualizaContadorTraerPac(String fechaTimbrado) {
		return facturasRepository.actualizaContadorTraerPac(fechaTimbrado);
	}
	
	public EmisorYLugarExpedicionDto obtenerDatosEmisionExpedicion (String rfc, int idTienda, String version, String tipoComprobanteAPI, int idAplicacion, String formaPago) {
		
		EmisorYLugarExpedicionDto response = new EmisorYLugarExpedicionDto();	
		EmisorReq req = new EmisorReq();
		String tipoOperacion = "T";
		
		
		req.setRfc(rfc);
		req.setSucursal(idTienda);
		req.setTipoDeComprobante(tipoComprobanteAPI);
		req.setTipoDeOperacion(tipoOperacion);
		req.setVersion(version);
		req.setIdAplicacion(idAplicacion);
		req.setFormaPago(formaPago);
		
		inicializarWsft();
		
		if (headerValue.isEmpty()) obtenerToken();

    	int contador=0;
		HttpResponse<String> responseTimbrar = null;
		ObjectMapper objectMapper = new ObjectMapper();
				
		do {
			try {
				
				String reqString = objectMapper.writeValueAsString(req);
				logger.info(UrlDatosEmisorExpedicion);
				logger.info(reqString);
				
				responseTimbrar = Unirest.post(UrlDatosEmisorExpedicion)
					.header("Authorization", headerValue)
					.header("Content-Type", "application/json")
					.body(reqString)
					.asString();
			} catch (Exception e) {
				logger.error("rfc " + req.getRfc() + ": ", e);
			}
			
			if (responseTimbrar != null) {
				try {
					response = objectMapper.readValue(responseTimbrar.getBody(), EmisorYLugarExpedicionDto.class);
					if (response.getRespuesta().getCodigo().equals("5")) {
						obtenerToken();
						responseTimbrar = null;
						response = new EmisorYLugarExpedicionDto();
					}
				} catch (JsonProcessingException e) {
					logger.error("rfc " + req.getRfc() + ": ", e);
				}
				
			}
			contador += 1;

		} while (responseTimbrar == null && contador <= 4);
							
		if (response == null || response.getRespuesta() == null) {
			
			ResponseBaseDto respuesta = new EmisorYLugarExpedicionDto().getRespuesta();
			respuesta.setCodigo(Integer.toString(ECodigo.Error.getValor()));
        	respuesta.setDescripcion("La solicitud es inv\u00e1lida u ocurri\u00f3 un error");
        	response.setRespuesta(respuesta);
		}

		String lugarExpedicion = response.getData().getLugarExpedicion().getCodigoPostal().toString();
		response.getData().getLugarExpedicion().setCodigoPostal(StringUtils.leftPad(lugarExpedicion, 5, '0'));
		
		return response;
	}
	
	private void inicializarWsft() {

		if (UrlLogin == "") UrlLogin = configFacService.getConfig().get("WebService.Configuracion.Url.Login");
		if (UrlDatosEmisorExpedicion == "") UrlDatosEmisorExpedicion = configFacService.getConfig().get("WebService.Configuracion.Url.Emisor");
		if (UrlVersionActiva == "") UrlVersionActiva = configFacService.getConfig().get("WebService.Configuracion.Url.VersionTimbrado");
		if (userName == "") userName = configFacService.getConfig().get("WebService.Facturacion.Usuario");
		if (userPass == "") userPass = configFacService.getConfig().get("WebService.Facturacion.Password");

		try {
			Unirest.setHttpClient(ClientSSl());
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			e.printStackTrace();
		}
	}
	
	/**
	* Metodo que permite realizar una llamada a un      
	* Servicio HTTPS      
	*/
	private CloseableHttpClient ClientSSl() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException
	{
		SSLContext sslcontext = SSLContexts.custom()
	            .loadTrustMaterial(null, new TrustSelfSignedStrategy())
	            .build();

	    @SuppressWarnings("deprecation")
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext,SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	    CloseableHttpClient httpclient = HttpClients.custom()
	            .setSSLSocketFactory(sslsf)
	            .build();
	    
	    return httpclient;
	}
	
	private void obtenerToken() {
 		int contador = 0;
 		HttpResponse<String> responseLogin = null;
 		
    	do {
			try {
				responseLogin = Unirest.post(UrlLogin)
					.header("Content-Type", "application/json")
					.body("{\"username\": \""+userName+"\",\r\n  \"password\": \""+userPass+"\"\r\n}")
					.asString();		
			} catch (Exception e) {
				logger.error("Token: ", e);
			}
			contador += 1;

		} while (responseLogin == null 
				&& contador <= 4);
		
    	if (responseLogin != null) {
    		Headers headers = responseLogin.getHeaders();
    		headerValue =  headers.getFirst("authorization");				    		
    	}
	}

	public ClienteTimbrarTipoExpRespTYPE timbrarTipo(String tipoTimbrado, String xmlBase64) {
		
		ClienteTimbrarTipoExpRespTYPE response = new ClienteTimbrarTipoExpRespTYPE();
		String errorTimbrar = "";
		String errorBuscar = "";
		
		Resultado result = timbrar(xmlBase64, tipoTimbrado);
		
		if(result == null || result.getFacturaId()==null) {
			
			if (result != null && result.getErrorMessage() != null) errorTimbrar = result.getErrorMessage();
			
			ClienteTimbrarTipoExpRespTYPE.Respuesta respuesta = new ClienteTimbrarTipoExpRespTYPE.Respuesta();
			respuesta.setCodigo("2");
        	respuesta.setDescripcion("Error al intentar timbrar: " + errorTimbrar);
	    	response.setRespuesta(respuesta);
	    	response.setFacturaId("-1");
			return response;
		}
				
		RespuestaXml resultXml = getXml(result.getFacturaId(), tipoTimbrado);

		
		if (resultXml == null || resultXml.getUuid()==null || (resultXml.getEstatusId() != null && !resultXml.getEstatusId().contentEquals("4"))) {
			if (resultXml == null) {
				errorComponent.guardarLog("ResultXml es nulo");
			}
						
			if (resultXml != null && resultXml.getErrorDesc() != null) errorBuscar = resultXml.getErrorDesc();
			if (resultXml != null && resultXml.getErrorMessage() != null) errorBuscar += " " + resultXml.getErrorMessage();

			ClienteTimbrarTipoExpRespTYPE.Respuesta respuesta = new ClienteTimbrarTipoExpRespTYPE.Respuesta();
			respuesta.setCodigo("0");
        	respuesta.setDescripcion("En Proceso de Timbrado: " + errorBuscar);
	    	response.setRespuesta(respuesta);
	    	response.setFacturaId(result.getFacturaId());
			return response;
		}
		
		ClienteTimbrarTipoExpRespTYPE.Respuesta respuesta = new ClienteTimbrarTipoExpRespTYPE.Respuesta();
		respuesta.setCodigo(Integer.toString(ECodigo.Ok.getValor()));
    	respuesta.setDescripcion("ok");
    	response.setRespuesta(respuesta);
    	response.setFacturaId(result.getFacturaId());
    	response.setUuid(resultXml.getUuid());
    	
    	return response;
	}
	
	public ClienteTimbrarTipoExpRespTYPE timbrarTipo40(String tipoTimbrado, String xmlBase64) {
		
		ClienteTimbrarTipoExpRespTYPE response = new ClienteTimbrarTipoExpRespTYPE();
		String errorTimbrar = "";
		String errorBuscar = "";
		
		com.sodimac.facturacion.clientews.wcfemision40.org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac.Resultado result = timbrar40(xmlBase64, tipoTimbrado);
		
		if(result == null || result.getFacturaId()==null) {
			
			if (result != null && result.getErrorMessage() != null) errorTimbrar = result.getErrorMessage();
			
			ClienteTimbrarTipoExpRespTYPE.Respuesta respuesta = new ClienteTimbrarTipoExpRespTYPE.Respuesta();
			respuesta.setCodigo("2");
        	respuesta.setDescripcion("Error al intentar timbrar: " + errorTimbrar);
	    	response.setRespuesta(respuesta);
	    	response.setFacturaId("-1");
			return response;
		}
				
		com.sodimac.facturacion.clientews.wcfemision40.org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac.RespuestaXml resultXml = getXml40(result.getFacturaId(), tipoTimbrado);

		
		if (resultXml == null || resultXml.getUuid()==null || (resultXml.getEstatusId() != null && !resultXml.getEstatusId().contentEquals("4"))) {
			if (resultXml == null) {
				errorComponent.guardarLog("ResultXml es nulo");
			}
						
			if (resultXml != null && resultXml.getErrorDesc() != null) errorBuscar = resultXml.getErrorDesc();
			if (resultXml != null && resultXml.getErrorMessage() != null) errorBuscar += " " + resultXml.getErrorMessage();

			ClienteTimbrarTipoExpRespTYPE.Respuesta respuesta = new ClienteTimbrarTipoExpRespTYPE.Respuesta();
			respuesta.setCodigo("0");
        	respuesta.setDescripcion("En Proceso de Timbrado: " + errorBuscar);
	    	response.setRespuesta(respuesta);
	    	response.setFacturaId(result.getFacturaId());
			return response;
		}
		
		ClienteTimbrarTipoExpRespTYPE.Respuesta respuesta = new ClienteTimbrarTipoExpRespTYPE.Respuesta();
		respuesta.setCodigo(Integer.toString(ECodigo.Ok.getValor()));
    	respuesta.setDescripcion("ok");
    	response.setRespuesta(respuesta);
    	response.setFacturaId(result.getFacturaId());
    	response.setUuid(resultXml.getUuid());
    	
    	return response;
	}

	public VersionTimbradoRes obtenerVersionActiva (int idAplicacion) {
		
		VersionTimbradoRes response = new VersionTimbradoRes();	
		VersionTimbradoReq req = new VersionTimbradoReq();
		
		req.setIdAplicacion(idAplicacion);
		
		inicializarWsft();
		
		if (headerValue.isEmpty()) obtenerToken();

    	int contador=0;
		HttpResponse<String> responseTimbrar = null;
		ObjectMapper objectMapper = new ObjectMapper();
				
		do {
			try {
				responseTimbrar = Unirest.post(UrlVersionActiva)
					.header("Authorization", headerValue)
					.header("Content-Type", "application/json")
					.body(objectMapper.writeValueAsString(req))
					.asString();
			} catch (Exception e) {
				logger.error("Aplicacion " + idAplicacion + ": ", e);
			}
			
			if (responseTimbrar != null) {
				try {
					response = objectMapper.readValue(responseTimbrar.getBody(), VersionTimbradoRes.class);
					if (response.getRespuesta().getCodigo().equals("5")) {
						obtenerToken();
						responseTimbrar = null;
						response = new VersionTimbradoRes();
					}
				} catch (JsonProcessingException e) {
					logger.error("Aplicacion " + idAplicacion + ": ", e);
				}
				
			}
			contador += 1;

		} while (responseTimbrar == null && contador <= 4);
							
		if (response == null || response.getRespuesta() == null) {
			
			ResponseBaseDto respuesta = new VersionTimbradoRes().getRespuesta();
			respuesta.setCodigo(Integer.toString(ECodigo.Error.getValor()));
        	respuesta.setDescripcion("La solicitud es inv\u00e1lida u ocurri\u00f3 un error");
        	response.setRespuesta(respuesta);
		}
		
		return response;
	}
	
	public com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2022.ClienteTicketObtenerExpRespTYPE obtenerDetalleTicket(ClientesTemporalModel model) {
	
		final String Ws_OK = "1";
		esPuntosCES = false;

		ClienteTicketObtenerExpRespTYPE resultTicketOld = ticketsService.getResultTicketWS(model);
		
		if (resultTicketOld == null) {
			return null;
		}
		
		com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2022.ClienteTicketObtenerExpRespTYPE resultTicketNew = modelMapper.map(resultTicketOld, com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2022.ClienteTicketObtenerExpRespTYPE.class);
		
		if (!resultTicketNew.getRespuesta().getCodigo().equals(Ws_OK)) {
			return resultTicketNew;
		}
		
		//DML [STM-709] Ajustar proceso de reenvió de facturas y notas de crédito para evitar duplicidad de timbrado
		//Hacer validación que el ticket no se encuentre timbrado en Facturación inHouse, Si se encuentra en facturas
		FacturasEntity ticketInHouse = this.getFacturaByTicket(model.getTicketBct());
		if (ticketInHouse != null) {
			resultTicketNew.getRespuesta().setCodigo("2");
			resultTicketNew.getRespuesta().setDescripcion("El ticket {ticket} ya fue facturado.".replace("{ticket}", model.getTicketBct()));
		} else {
			int existFactura = ticketsService.validarTicket(model.getTicketBct());
			
			if (existFactura == 130) {
				resultTicketNew.getRespuesta().setCodigo("2");
				resultTicketNew.getRespuesta().setDescripcion("El ticket {ticket} ya fue facturado.".replace("{ticket}", model.getTicketBct()));
			}
			if (existFactura==131) {
				resultTicketNew.getRespuesta().setCodigo("3");
				resultTicketNew.getRespuesta().setDescripcion("El ticket {ticket} está en proceso.".replace("{ticket}", model.getTicketBct()));
			}
		}	
				
		if (!resultTicketNew.getRespuesta().getCodigo().equals(Ws_OK)) {
			return resultTicketNew;
		}
		
		ConceptosTYPE conceptosNew = new ConceptosTYPE();
		
		int cantidadConceptos = resultTicketOld.getComprobante().getConceptos().getConcepto().size();
		for (int i = 0; i < cantidadConceptos; i++)
		{
			Concepto conceptoNew = modelMapper.map(resultTicketOld.getComprobante().getConceptos().getConcepto().get(i), Concepto.class);

			
			String sImporte = conceptoNew.getImporte();
			
			String sDescuentoOrig = conceptoNew.getDescuento();
			double descuento = Double.parseDouble(sDescuentoOrig);
			
			String sDescuento = ""; 
			sDescuento = UtilsString.formatearDecimales(descuento, armarFormato(obtenerNumeroDecimales(sImporte)));
			
			conceptoNew.setDescuento(sDescuento);
			
			
			conceptosNew.getConcepto().add(conceptoNew);			
		}
		resultTicketNew.getComprobante().setConceptos(conceptosNew);

		
		for (int i = 0; i < cantidadConceptos; i++)
		{
			com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ConceptoImpuestosTYPE concImpuesto = resultTicketOld.getComprobante().getConceptos().getConcepto().get(i).getImpuestos();
			if (concImpuesto != null && concImpuesto.getTraslados() != null) {

				ConceptoImpuestosTYPE conceptoImpuestosNew = new ConceptoImpuestosTYPE();
				ConceptoImpuestosTrasladosTYPE trasladosNew = new ConceptoImpuestosTrasladosTYPE();
				
				int cantidadTraslados = resultTicketOld.getComprobante().getConceptos().getConcepto().get(i).getImpuestos().getTraslados().getTraslado().size();
				for (int j = 0; j< cantidadTraslados; j++) {
					Traslado traslado = modelMapper.map(resultTicketOld.getComprobante().getConceptos().getConcepto().get(i).getImpuestos().getTraslados().getTraslado().get(j), Traslado.class);
					trasladosNew.getTraslado().add(traslado);
				}
				
				conceptoImpuestosNew.setTraslados(trasladosNew);
				resultTicketNew.getComprobante().getConceptos().getConcepto().get(i).setImpuestos(conceptoImpuestosNew);				
			}
			
		}
		
		

		com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ImpuestosTYPE impuestos = resultTicketOld.getComprobante().getImpuestos();
		if (impuestos != null && impuestos.getTraslados() != null) {
			
			ImpuestosTrasladosTYPE trasladadosNew = new ImpuestosTrasladosTYPE();
			ImpuestosTYPE impuestosNew = new ImpuestosTYPE();
			
			int cantidadTraslados = resultTicketOld.getComprobante().getImpuestos().getTraslados().getTraslado().size();
			for (int k = 0; k < cantidadTraslados; k++)
			{
				ImpuestosTrasladosTYPE.Traslado trasladoNew = modelMapper.map(resultTicketOld.getComprobante().getImpuestos().getTraslados().getTraslado().get(k), ImpuestosTrasladosTYPE.Traslado.class);
				trasladadosNew.getTraslado().add(trasladoNew);
			}
			
			impuestosNew.setTraslados(trasladadosNew);
			resultTicketNew.getComprobante().setImpuestos(impuestosNew);			
		}
		
		transformarXmlTicketXmlPacDetecno(resultTicketNew, model);
		
		// 2024-09-25 Si el proceso no es batch, el total no debe ser igual a 0 en caso de que la forma de pago sea diferente de "102" (pagado con puntos CES) 
		String formaPago = resultTicketOld.getComprobante().getFormaPago();
		if (formaPago.equals(PUNTOS_CES)) {
			esPuntosCES = true;
		}

		return resultTicketNew;
		
	}

	private void transformarXmlTicketXmlPacDetecno(com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2022.ClienteTicketObtenerExpRespTYPE ticket, ClientesTemporalModel model) {

		boolean version33 = (model.getVersionFacturacionSat().equals("3.3"));
		boolean version40 = (model.getVersionFacturacionSat().equals("4.0"));

		String versionSat ="";
		String serie ="";
		String folio ="";
		String fecha = "";
		String exportacion = "";
		String formaPago ="";
		String condicionesDePago ="";
		String tipoComprobante ="";
		String metodoPago ="";
		String lugarExpedicion = "";
		
		String calleExpedicion = "";
		String noExteriorExpedicion = "";
		String noInteriorExpedicion = "";
		String coloniaExpedicion = "";
		String localidadExpedicion = "";
		String referenciaExpedicion = "";
		String municipioExpedicion = "";
		String estadoExpedicion = "";
		String paisExpedicion = "";
		String codigoPostalExpedicion = "";
		
		String rfcEmisor = "";
		ConfDatosEmisorEntity emisor = confDatosEmisorRepository.findRfcActivo();
		if (emisor != null) {
			rfcEmisor = emisor.getRfc();
		}

		String tipoComprobanteAPI = "";
		if (ticket.getComprobante().getTipoComprobante().equals("I")) tipoComprobanteAPI = "F";
		if (ticket.getComprobante().getTipoComprobante().equals("E")) tipoComprobanteAPI = "DC";
		if (ticket.getComprobante().getTipoComprobante().equals("P")) tipoComprobanteAPI = "C";
		String formaPagoBCT = ticket.getComprobante().getFormaPago();

		logger.info("obtenerDatosEmisionExpedicion: " + model.getTicketBct());
		
		logger.info("transformarXmlTicketXmlPacDetecno");
		logger.info("model.getTicketBct(): " + model.getTicketBct());
		EmisorYLugarExpedicionDto datos = obtenerDatosEmisionExpedicion(rfcEmisor, Integer.parseInt(model.getTicketBct().substring(8, 12)), model.getVersionFacturacionSat(), tipoComprobanteAPI, model.getIdAplicacion(), formaPagoBCT);
		
		if (datos.getRespuesta().getCodigo().equals("1")) {
			
			versionSat = datos.getData().getVersion();
			serie = datos.getData().getSerie();
			folio = datos.getData().getFolio();
			fecha = datos.getData().getFecha();
			tipoComprobante = datos.getData().getTipoDeComprobante();
			exportacion = datos.getData().getExportacion();
			formaPago = datos.getData().getFormaPago();
			metodoPago = datos.getData().getMetodoPago();
			condicionesDePago = datos.getData().getCondicionesDePago();
			
			calleExpedicion = datos.getData().getLugarExpedicion().getCalle();
			noExteriorExpedicion = datos.getData().getLugarExpedicion().getNoExterior();
			noInteriorExpedicion = datos.getData().getLugarExpedicion().getNoInterior();
			coloniaExpedicion = datos.getData().getLugarExpedicion().getColonia();
			localidadExpedicion = datos.getData().getLugarExpedicion().getLocalidad();
			referenciaExpedicion = datos.getData().getLugarExpedicion().getReferencia();
			municipioExpedicion = datos.getData().getLugarExpedicion().getMunicipio();
			estadoExpedicion = datos.getData().getLugarExpedicion().getEstado();
			paisExpedicion = datos.getData().getLugarExpedicion().getPais();
			codigoPostalExpedicion = datos.getData().getLugarExpedicion().getCodigoPostal();
		}
		
		if (version33) {
			versionSat = ticket.getComprobante().getVersion();
			serie = ticket.getComprobante().getSerie();
			folio = ticket.getComprobante().getFolio();
			fecha = ticket.getComprobante().getFecha();
			tipoComprobante = ticket.getComprobante().getTipoComprobante();
			//formaPago = ticket.getComprobante().getFormaPago();
			//metodoPago = ticket.getComprobante().getMetodoPago();
			condicionesDePago = ticket.getComprobante().getCondicionesDePago();
			lugarExpedicion = ticket.getComprobante().getLugarExpedicion();
		}
		
		/*********Comprobante**********/
		
		ticket.getComprobante().setVersion(versionSat);
		ticket.getComprobante().setSerie(serie);
		ticket.getComprobante().setFolio(folio);
		ticket.getComprobante().setFecha(fecha);
		ticket.getComprobante().setFormaPago(formaPago);
		ticket.getComprobante().setCondicionesDePago(condicionesDePago);
		ticket.getComprobante().setTipoComprobante(tipoComprobante);
		if (version40) ticket.getComprobante().setExportacion(exportacion);
		ticket.getComprobante().setMetodoPago(metodoPago);
		
		if (version33) ticket.getComprobante().setLugarExpedicion(lugarExpedicion);
		
		if (version40) {
			
			if (!calleExpedicion.isEmpty()) ticket.getComprobante().setCalle(calleExpedicion);
			if (!noExteriorExpedicion.isEmpty()) ticket.getComprobante().setNoExterior(noExteriorExpedicion);
			if (!noInteriorExpedicion.isEmpty()) ticket.getComprobante().setNoInterior(noInteriorExpedicion);
			if (!coloniaExpedicion.isEmpty()) ticket.getComprobante().setColonia(coloniaExpedicion);
			if (!localidadExpedicion.isEmpty()) ticket.getComprobante().setLocalidad(localidadExpedicion);
			if (!referenciaExpedicion.isEmpty()) ticket.getComprobante().setReferencia(referenciaExpedicion);
			if (!municipioExpedicion.isEmpty()) ticket.getComprobante().setMunicipio(municipioExpedicion);
			if (!estadoExpedicion.isEmpty()) ticket.getComprobante().setEstado(estadoExpedicion);
			if (!paisExpedicion.isEmpty()) ticket.getComprobante().setPais(paisExpedicion);
			ticket.getComprobante().setLugarExpedicion(codigoPostalExpedicion);
		}
		
		if (ticket.getComprobante().getTipoComprobante().equals("E") && !model.getUuidRelacionado().isEmpty()) {
//			xmlConvertido += "<CfdiRelacionados tipoRelacion=\"01\">";
//			xmlConvertido += "<CfdiRelacionado uuid=\"" + model.getUuidRelacionado() + "\" ordenador = \"1\" />";
//			xmlConvertido += "</CfdiRelacionados>";
		}
		
		if (ticket.getComprobante().getTipoComprobante().equals("I") && !model.getUuidRelacionado().isEmpty()) {
//			xmlConvertido += "<CfdiRelacionados tipoRelacion=\"04\">";
//			xmlConvertido += "<CfdiRelacionado uuid=\"" + model.getUuidRelacionado() + "\" ordenador = \"1\" />";
//			xmlConvertido += "</CfdiRelacionados>";
		}

		/*********Emisor**********/
		
		String nombreEmisor = "";
		String regimenFiscalEmisor = "";
		String calle = "";
		String noExterior = "";
		String noInterior = "";
		String colonia = "";
		String localidad = "";
		String referencia = "";
		String municipio = "";
		String estado = "";
		String pais = "";
		String codigoPostal = "";

		if (datos.getRespuesta().getCodigo().equals("1")) {
			nombreEmisor = datos.getData().getEmisorNode().getRazonSocial();
			regimenFiscalEmisor = datos.getData().getEmisorNode().getRegimenFiscal();
			calle = datos.getData().getEmisorNode().getCalle();
			noExterior = datos.getData().getEmisorNode().getNoExterior();
			noInterior = datos.getData().getEmisorNode().getNoInterior();
			colonia = datos.getData().getEmisorNode().getColonia();
			localidad = datos.getData().getEmisorNode().getLocalidad();
			referencia = datos.getData().getEmisorNode().getReferencia();
			municipio = datos.getData().getEmisorNode().getMunicipio();
			estado = datos.getData().getEmisorNode().getEstado();
			pais = datos.getData().getEmisorNode().getPais();
			codigoPostal = datos.getData().getEmisorNode().getCodigoPostal();
		}
		
		ticket.getComprobante().getEmisor().setRfc(rfcEmisor);
		ticket.getComprobante().getEmisor().setNombre(nombreEmisor);
		ticket.getComprobante().getEmisor().setRegimenFiscal(regimenFiscalEmisor);
		
		if (version40) {

			if (!calle.isEmpty()) ticket.getComprobante().getEmisor().setCalle(calle);
			if (!noExterior.isEmpty()) ticket.getComprobante().getEmisor().setNoExterior(noExterior);
			if (!noInterior.isEmpty()) ticket.getComprobante().getEmisor().setNoInterior(noInterior);
			if (!colonia.isEmpty()) ticket.getComprobante().getEmisor().setColonia(colonia);
			if (!localidad.isEmpty()) ticket.getComprobante().getEmisor().setLocalidad(localidad);
			if (!referencia.isEmpty()) ticket.getComprobante().getEmisor().setReferencia(referencia);
			if (!municipio.isEmpty()) ticket.getComprobante().getEmisor().setMunicipio(municipio);
			if (!estado.isEmpty()) ticket.getComprobante().getEmisor().setEstado(estado);
			if (!pais.isEmpty()) ticket.getComprobante().getEmisor().setPais(pais);
			if (!codigoPostal.isEmpty()) ticket.getComprobante().getEmisor().setCodigoPostal(codigoPostal);		
		}
		
					
		/*********Conceptos**********/
		
		int cantidadConceptos = 0;
		
		cantidadConceptos = ticket.getComprobante().getConceptos().getConcepto().size();
		HashMap<String, Double> sumaImpuestoTasa = new HashMap<>();
		
		double total = Double.parseDouble(validarImporteVacio(ticket.getComprobante().getTotales().getTotal()));
		boolean totalRangoCero = evaluaTotalCES(total);
		
		for (int i = 0; i < cantidadConceptos; i++)
		{			
			/*********Conceptos-impuestos**********/
			if (version40) {
				String objetoImp= "01";  // No objeto de impuesto
				if (ticket.getComprobante().getConceptos().getConcepto().get(i).getImpuestos() != null 
						&& ticket.getComprobante().getConceptos().getConcepto().get(i).getImpuestos().getTraslados() != null
						&& ticket.getComprobante().getConceptos().getConcepto().get(i).getImpuestos().getTraslados().getTraslado().size() > 0) {
					objetoImp= "02";     // Si objeto de impuesto, lleva impuestos

				}
				if (formaPagoBCT.equals(PUNTOS_CES) && totalRangoCero) {
					objetoImp= "04"; // Clave para puntosCES	
				}
				ticket.getComprobante().getConceptos().getConcepto().get(i).setObjetoImp(objetoImp);				
			}
			
			/*********Traslados**********/
			
			int cantidadTraslados = 0;
			
			ConceptoImpuestosTYPE concImpuesto = ticket.getComprobante().getConceptos().getConcepto().get(i).getImpuestos();
			if (concImpuesto != null && concImpuesto.getTraslados() != null) {
				cantidadTraslados = ticket.getComprobante().getConceptos().getConcepto().get(i).getImpuestos().getTraslados().getTraslado().size();
				
				for (int j = 0; j< cantidadTraslados; j++)
				{
					String simporte = ticket.getComprobante().getConceptos().getConcepto().get(i).getImpuestos().getTraslados().getTraslado().get(j).getImporte();
					//simporte = UtilsString.formatearDecimales(Double.parseDouble(simporte), DECIMALES_PRECIO);
					ticket.getComprobante().getConceptos().getConcepto().get(i).getImpuestos().getTraslados().getTraslado().get(j).setImporte(simporte);
					
					String impuesto = ticket.getComprobante().getConceptos().getConcepto().get(i).getImpuestos().getTraslados().getTraslado().get(j).getImpuesto();
					String tasa = ticket.getComprobante().getConceptos().getConcepto().get(i).getImpuestos().getTraslados().getTraslado().get(j).getTasaCuota();
					Double base = Double.parseDouble(ticket.getComprobante().getConceptos().getConcepto().get(i).getImpuestos().getTraslados().getTraslado().get(j).getBase());

					String impuestoTasa = impuesto + "&" + tasa;
					if (sumaImpuestoTasa.containsKey(impuestoTasa)) {
						Double suma = sumaImpuestoTasa.get(impuestoTasa);
						suma += base;
						sumaImpuestoTasa.replace(impuestoTasa, suma);
					} else {
						sumaImpuestoTasa.put(impuestoTasa, base);
					}
				}							
			}
		}
				
		/*********Traslado**********/
		
		int cantidadTraslados = 0;
		ImpuestosTYPE impuestos = ticket.getComprobante().getImpuestos();
		if (impuestos != null && impuestos.getTraslados() != null) {
			cantidadTraslados = ticket.getComprobante().getImpuestos().getTraslados().getTraslado().size();
			
			for (int k = 0; k < cantidadTraslados; k++)
			{
				String impuesto = ticket.getComprobante().getImpuestos().getTraslados().getTraslado().get(k).getImpuesto();

				String stasa = ticket.getComprobante().getImpuestos().getTraslados().getTraslado().get(k).getTasaCuota();
				stasa = UtilsString.formatearDecimales(Double.parseDouble(stasa), DECIMALES_TASA);
				ticket.getComprobante().getImpuestos().getTraslados().getTraslado().get(k).setTasaCuota(stasa);
				
				String impuestoTasa = impuesto + "&" + stasa;
				
				Double base = sumaImpuestoTasa.getOrDefault(impuestoTasa, 0.0d);
				String sbase = UtilsString.formatearDecimales(base, DECIMALES_TASA);
				ticket.getComprobante().getImpuestos().getTraslados().getTraslado().get(k).setBase(sbase);
				
				String simporte = ticket.getComprobante().getImpuestos().getTraslados().getTraslado().get(k).getImporte();
				//simporte = UtilsString.formatearDecimales(Double.parseDouble(simporte), DECIMALES_PRECIO);
				simporte = UtilsString.formatearDecimales(Double.parseDouble(simporte), DECIMALES_TASA);
				ticket.getComprobante().getImpuestos().getTraslados().getTraslado().get(k).setImporte(simporte);
			}			
		}
		
		// Se mantiene el valor de totalImpuestosTrasladados que viene del Middleware
		// No se sobrescribe aunque no haya traslados a nivel comprobante
		
		String sTotal = ""; 
		sTotal = UtilsString.formatearDecimales(total, DECIMALES_PRECIO);
		NumeroaLetras totalLetras = new NumeroaLetras();
		String importeLetra = totalLetras.Convertir(sTotal, true);
		ticket.getComprobante().getTotales().setImporteLetra(importeLetra);
		
				
		/*********DatosExtraCFD**********/

		if (version40) {
			String tipoDocumento = "";
			if (ticket.getComprobante().getTipoComprobante().equals("I")) tipoDocumento = "V";	
			if (ticket.getComprobante().getTipoComprobante().equals("E")) tipoDocumento = "D";	
			String fechaCompra = model.getTicketBct().substring(0, 8);
			fechaCompra = fechaCompra.substring(6, 8) + "-" + fechaCompra.substring(4, 6) + "-" + fechaCompra.substring(0, 4);
			
			ticket.getDatosExtraCFD().setExtra4(tipoComprobanteAPI);
			ticket.getDatosExtraCFD().setExtra6(fechaCompra);
			ticket.getDatosExtraCFD().setExtra7(tipoDocumento);
		}
		
		String extra8 = "SODIMAC";
		if (model.getIdAplicacion() == EAplicacion.Detecno.getValor()) extra8 = "DETECNO";
		ticket.getDatosExtraCFD().setExtra8(extra8);
		
		String oc = "";
		if (model.getTicket().length() < 16) {
			oc = model.getTicket();
		}
		ticket.getDatosExtraCFD().setExtra9(oc);
			
	}

	@Transactional
	public com.sodimac.facturacion.clientews.wcfemision40.org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac.Resultado timbrar40(String xml) throws NumberFormatException {
		
		com.sodimac.facturacion.clientews.wcfemision40.org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac.Resultado result = null;
		
		int contador = 0;
		int reintentosWs = Integer.parseInt(configFacService.getConfig().get("WebService.Emision.Reintentos"));
		
		do {
			try {
				Thread.sleep(1*100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			result = emision40Service.timbrar(xml);
			contador += 1;

		} while (result == null && contador <= reintentosWs);
				
		if (result == null) {
			errorComponent.setXml(xml);
			errorComponent.setPagina("timbrar");
			errorComponent.guardarLog("El servicio del pack no esta disponible");
		} else if (result.getFacturaId()==null) {
			errorComponent.setXml(xml);
			errorComponent.setPagina("timbrar");
			errorComponent.guardarLog(result);
		}
		
		return result;
	}
	
	@Transactional
	public com.sodimac.facturacion.clientews.wcfemision40.org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac.RespuestaXml getXml40(String facturaId) {
		
		com.sodimac.facturacion.clientews.wcfemision40.org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac.RespuestaXml resultXml = null;
		
		int contador = 0;
		int reintentosWs = Integer.parseInt(configFacService.getConfig().get("WebService.Emision.Xml.Reintentos"));
		
		do {
			try {
				Thread.sleep(1*2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			resultXml = emision40Service.getComprobante(facturaId);
			contador += 1;
		} while ((resultXml==null
				|| (resultXml.getEstatusId() != null && resultXml.getEstatusId().equals("1")) 
				|| (resultXml.getEstatusId() != null && resultXml.getEstatusId().equals("3")) 
				|| (resultXml.getEstatusId() != null && resultXml.getEstatusId().equals("5") && resultXml.getErrorDesc()==null))
			  && contador <= reintentosWs);
		
		return resultXml;
	}
	
	public ClienteConsultarFacturaIdExpRespTYPE consultarTipo(String facturaId, String tipo) {
		
		ClienteConsultarFacturaIdExpRespTYPE response = new ClienteConsultarFacturaIdExpRespTYPE();
		com.sodimac.facturacion.clientews.wcfemision40.org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac.RespuestaXml resultXml40 = null;
		String uuid = "";
		String xml = "";
		String estatus = "";
		String mensaje = "";
		Respuesta respuesta = new Respuesta();
		
		resultXml40 = getXml40(facturaId, tipo);
		
		if (resultXml40 == null) {
			errorComponent.guardarLog("ResultXml es nulo");
			respuesta.setCodigo(Integer.toString(ECodigo.Error.getValor()));
        	respuesta.setDescripcion("La solicitud es inv\u00e1lida u ocurri\u00f3 un error");
        	response.setRespuesta(respuesta);
			return response;
		}
		
		estatus = Integer.toString(EStatusConsultarFacturaId.TimbradoError.getValor());
		if (resultXml40.getErrorMessage() != null && resultXml40.getErrorMessage().equals("No se encontraron registros")) {
			estatus = Integer.toString(EStatusConsultarFacturaId.NoExisteFactura.getValor());
		} else {
			if (resultXml40.getEstatusId() != null && resultXml40.getEstatusId().equals("4")) {
				estatus = Integer.toString(EStatusConsultarFacturaId.TimbradoOk.getValor());
				if (resultXml40.getUuid() != null) uuid = resultXml40.getUuid();
				if (resultXml40.getXml() != null) xml = resultXml40.getXml();
			}
		}
		
		if (resultXml40.getErrorMessage() != null) mensaje += resultXml40.getErrorMessage();
		if (!mensaje.isEmpty()) mensaje += ". ";
		if (resultXml40.getErrorDesc() != null) mensaje += resultXml40.getErrorDesc();
		
		response.setFacturaId(facturaId);
		response.setUuid(uuid);
		response.setXml(xml);
		response.setEstatus(estatus);
		response.setMensaje(mensaje);

		respuesta.setCodigo(Integer.toString(ECodigo.Ok.getValor()));
    	respuesta.setDescripcion("ok");
		response.setRespuesta(respuesta );

		return response;
	}
	
	@Transactional
	public com.sodimac.facturacion.clientews.wcfemision40.org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac.Resultado timbrar40(String xml, String tipoTimbrado) throws NumberFormatException {
		
		com.sodimac.facturacion.clientews.wcfemision40.org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac.Resultado result = null;
		
		int contador = 0;
		int reintentosWs = Integer.parseInt(configFacService.getConfig().get("WebService.Emision.Reintentos"));
		
		do {
			try {
				Thread.sleep(1*100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			result = emision40Service.timbrar(xml, tipoTimbrado);
			contador += 1;

		} while (result == null && contador <= reintentosWs);
				
		if (result == null) {
			errorComponent.setXml(xml);
			errorComponent.setPagina("timbrar");
			errorComponent.guardarLog("El servicio del pack no esta disponible");
		} else if (result.getFacturaId()==null) {
			errorComponent.setXml(xml);
			errorComponent.setPagina("timbrar");
			errorComponent.guardarLog(result);
		}
		
		return result;
	}
	
	@Transactional
	public com.sodimac.facturacion.clientews.wcfemision40.org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac.RespuestaXml getXml40(String facturaId, String tipoTimbrado) {
		
		com.sodimac.facturacion.clientews.wcfemision40.org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac.RespuestaXml resultXml = null;
		
		int contador = 0;
		int reintentosWs = Integer.parseInt(configFacService.getConfig().get("WebService.Emision.Xml.Reintentos"));
		
		do {
			try {
				Thread.sleep(1*2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			resultXml = emision40Service.getComprobante(facturaId, tipoTimbrado);
			contador += 1;
		} while ((resultXml==null
				|| (resultXml.getEstatusId() != null && resultXml.getEstatusId().equals("1")) 
				|| (resultXml.getEstatusId() != null && resultXml.getEstatusId().equals("3")) 
				|| (resultXml.getEstatusId() != null && resultXml.getEstatusId().equals("5") && resultXml.getErrorDesc()==null))
			  && contador <= reintentosWs);
		
		return resultXml;
	}

	private void EnviarCorreoMontoIncorrecto(ClientesTemporalModel model) {
		
		String asunto = configFacService.getConfig().get("Mail.Usuario.MontoIncorrecto.Subject");
		String mensajeCorreo = configFacService.getConfig().get("Mail.Usuario.MontoIncorrecto.BodyMessage");
		mensajeCorreo = mensajeCorreo.replace("{nombreCliente}", model.getRazonSocial());
		boolean esHtml = Boolean.parseBoolean(configFacService.getConfig().get("Mail.Usuario.MontoIncorrecto.IsHtml"));

		try {
			if (!model.getEmail().isEmpty()) {
				mailSenderService.enviarTokenMultiple(model.getEmail(), asunto, mensajeCorreo, esHtml);
			}
						
		} catch (Exception e) {
			e.printStackTrace();
			errorComponent.guardarLog(e, model);
		}
		
	}

	@Override
	public int insertarFacturaMail(Integer pIdFacturaPac) {
		return this.facturasRepository.insertarFacturaMail(pIdFacturaPac);
	}

	@Override
	public int actualizarFacturaMail(Integer pIdFacturaPac, Integer pEstatus) {
		return this.facturasRepository.actualizarFacturaMail(pIdFacturaPac, pEstatus);
	}
	
	private boolean evaluaTotalCES (double total) {
		double tolerancia = Double.parseDouble(configFacService.getConfig().get("Aplicacion.ToleranciaTicket"));
		return 0 <= total && total <= tolerancia;
		
	}
	
	private String validarImporteVacio (String importe) {
		
		if (importe == null || importe.isEmpty()) {
			importe = "0";
		}
		
		return importe;
	}

}
