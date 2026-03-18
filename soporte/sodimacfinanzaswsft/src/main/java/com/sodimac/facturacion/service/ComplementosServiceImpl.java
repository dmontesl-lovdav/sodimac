package com.sodimac.facturacion.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sodimac.facturacion.clientews.configuracion.EmisorYLugarExpedicionDto;
import com.sodimac.facturacion.clientews.wcfemision40.org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac.RespuestaXml;
import com.sodimac.facturacion.clientews.wcfemision40.org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac.Resultado;
import com.sodimac.facturacion.component.ErrorComponent;
import com.sodimac.facturacion.entity.fac.ConfDatosEmisorEntity;
import com.sodimac.facturacion.entity.fis.CatCuentasEntity;
import com.sodimac.facturacion.entity.fis.FolioFacturaImpuestosEntity;
import com.sodimac.facturacion.models.CompTemporalModel;
import com.sodimac.facturacion.models.FacturasCompModel;
import com.sodimac.facturacion.models.PagosCompModel;
import com.sodimac.facturacion.repository.fac.ConfDatosEmisorRepository;
import com.sodimac.facturacion.repository.fis.CatCuentasRepository;
import com.sodimac.facturacion.repository.fis.ComplementosRepository;
import com.sodimac.facturacion.repository.fis.FolioFacturaImpuestosRepository;
import com.sodimac.facturacion.service.catalogospdf.PacsService;
import com.sodimac.facturacion.util.UtilsFechas;
import com.sodimac.facturacion.util.UtilsFile;
import com.sodimac.facturacion.util.UtilsString;
import com.sodimac.facturacion.util.enums.EFacturas;

@Service
public class ComplementosServiceImpl implements ComplementosService {

	Logger logger = LoggerFactory.getLogger(ComplementosServiceImpl.class);
	
	@Autowired
	private ErrorComponent errorComponent;
	@Autowired
	private PacsService pacsService;
	@Autowired
	private ConfDatosEmisorRepository confDatosEmisorRepository;
	@Autowired
	private ComplementosRepository complementosRepository;
	@Autowired
	private FacturasService facturasService;
	@Autowired
	private SeguridadService seguridadService;
    @Autowired
    private ConfiguracionFiscalService configFisService;
	@Autowired
	private CatCuentasRepository catCuentasRepository;
	@Autowired
	private FolioFacturaImpuestosRepository folioFacturaImpuestosRepository;
	
	@Autowired
    ResourceLoader resourceLoader;

    
	private static final String DECIMALES_PRECIO="0.00";
	private static final String DECIMALES_TASA="0.000000";
	
	@Transactional
	public int timbrarTipoProceso(CompTemporalModel model, int tipoProceso) {

		String uuid = "";
		String xml = "";
		String facturaId = "";
		int pacDefault = 0;
		Resultado result40 = null;
		int codigoRetorno = 300;
		RespuestaXml resultXml40 = null;
		int idOrigen = 1;
		
//		boolean timbradoNormal = (tipoProceso == EProceso.TimbradoNormal.getValor());
//		boolean autofacturador = (tipoProceso == EProceso.Autofacturador.getValor());
//		boolean pendientePac = (tipoProceso == EProceso.PendientePac.getValor());
//		boolean pendienteNoBct = (tipoProceso == EProceso.NoBct.getValor());
//		boolean batch = (pendienteNoBct || pendientePac);
//		boolean refacturacion = (tipoProceso == EProceso.Refacturacion.getValor());
//		boolean rebates = (tipoProceso == EProceso.Rebates.getValor());
//		boolean sincronizacion = (tipoProceso == EProceso.Sincronizacion.getValor());

		//Se comento por el proceso de refacturación
		//model.setUuidRelacionado("");
		
		model.setIdOrigen(idOrigen);
		
		int idPagoComp = Integer.parseInt(model.getTicket());
		List<FacturasCompModel> facturas = getFacturas(idPagoComp);
		
		if (facturas.size() == 0) {
			//No existe el idTransaccionPago o bien no hay facturas relacionadas al pago
			return 51;
		}

		model.setRfc(facturas.get(0).getRfc());
		model.setRazonSocial(facturas.get(0).getRazonSocial());
		model.setEmail(facturas.get(0).getEmail());
		model.setCodigoPostal(facturas.get(0).getCodigoPostal());
		model.setRegimenFiscal(facturas.get(0).getRegimenFiscal());
		model.setFacturas(facturas);
		
		errorComponent.setTicket(model.getTicket());
		errorComponent.setRfc(model.getRfc());
		errorComponent.setPagina("timbrarTipoProceso");
		
		pacDefault = pacsService.getIdDefault();
		
		if (pacDefault == 0) {
			errorComponent.guardarLog("No hay un pac activo");
			return 54;
		}
		model.setPac(pacDefault);

		int existComp = existComp(model.getRfc(), model.getTicket());
		if (existComp==1) {
			return 51;
		}			
		
		xml = transformarXmlTicketXmlPac(model);
							
		errorComponent.setXml(xml);
		
		actualizarEstatusLog(model, EFacturas.EnProcesoFacturacion.getValor());

		try {
			result40 = facturasService.timbrar40(xml);
			if (result40 == null || result40.getFacturaId()==null) {
				actualizarEstatusLog(model, EFacturas.EnEspera.getValor());
				return 202;
			}
							
		} catch (Exception e) {
			errorComponent.guardarLog(e);
			actualizarEstatusLog(model, EFacturas.EnEspera.getValor());
			return 202;
		}
		
		facturaId = result40.getFacturaId();
			
		model.setIdFacturaPac(Integer.parseInt(facturaId));
		errorComponent.setIdFacturaPac(Integer.parseInt(facturaId));

		resultXml40 = facturasService.getXml40(facturaId);

		if (resultXml40 == null || resultXml40.getUuid()==null || (resultXml40.getEstatusId() != null && !resultXml40.getEstatusId().equals("4"))) {
			model.setIdFacturaPac(0);
			if (resultXml40 == null) {
				errorComponent.guardarLog("ResultXml es nulo");
			} else {
				errorComponent.guardarLog(resultXml40, model);
			}
			
			if (resultXml40 != null && resultXml40.getErrorDesc() != null) {
				if (resultXml40.getErrorDesc().contains("no existe en la lista de RFC inscritos no cancelados")) {
					actualizarEstatusLog(model, EFacturas.RfcInvalidoNoApto.getValor());
					return 107;
				}
			}

			actualizarEstatusLog(model, EFacturas.EnEspera.getValor());
			return 202;
		}
	
		uuid = resultXml40.getUuid();
		xml = resultXml40.getXml();
			
		if (xml == null  || xml.isEmpty()) {
			errorComponent.guardarLog("El Xml esta vacio");
			actualizarEstatusLog(model, EFacturas.EnEspera.getValor());
			return 202;
		}
		
		Document document = UtilsFile.ObtenerDocumentXml (xml);
		        
		Node nNode = document.getElementsByTagName("cfdi:Comprobante").item(0);
        Element eElement = (Element) nNode;
        String versionFacturacionSat = eElement.getAttribute("Version");
        String serie = eElement.getAttribute("Serie");
        String fechaCompra = eElement.getAttribute("Fecha").replace("T", " ");
        String tipoDeComprobante = eElement.getAttribute("TipoDeComprobante");
        int folio = Integer.parseInt(eElement.getAttribute("Folio"));
        BigDecimal subTotal = BigDecimal.valueOf(Double.parseDouble(eElement.getAttribute("SubTotal")));
        BigDecimal total = BigDecimal.valueOf(Double.parseDouble(eElement.getAttribute("Total")));
		
		nNode = document.getElementsByTagName("tfd:TimbreFiscalDigital").item(0);
        eElement = (Element) nNode;
        String fechaTimbrado = eElement.getAttribute("FechaTimbrado").replace("T", " ");
        String versionFactura = eElement.getAttribute("Version");
        
        String transaccion = "";
        String ticketBct = "";
               
//        switch (tipoProceso) {
//        case 0: //TimbradoNormal
//        case 1: //Autofacturador
//        case 3: //NoBct
//            transaccion = resultTicket.getDatosExtraCFD().getExtra1();
//            ticketBct = resultTicket.getDatosExtraCFD().getExtra2();        	        		
//            break;
//        case 2: //PendientePac
//        case 6: //Sincronizacion
//            break;
//        case 4: //Refacturacion
//            transaccion = model.getTicket().substring(15, 19);
//            ticketBct = model.getTicket();        		
//            break;
//        case 5: //Rebates
//            transaccion = model.getRebate().getTicket().substring(14, 19);
//            ticketBct = model.getTicket();        	
//            break;
//        }
        		
		model.setUuid(uuid);
		model.setFechaTimbrado(fechaTimbrado);
		model.setVersionFacturacionSat(versionFacturacionSat);
		model.setFechaCompra(fechaCompra);
		model.setXml(xml);
		model.setIdEstatusFactura(EFacturas.Facturado.getValor());
		model.setTicketBct(ticketBct);
		model.setVersionFactura(versionFactura);
		model.setTransaccion(transaccion);
		model.setIdComprobante(tipoDeComprobante);
		
		model.setSerie(serie);
		model.setFolio(folio);
		model.setSubTotal(subTotal);
		model.setTotal(total);

		insertarComp(model);
				
		//this.correoComplementoService.enviarCorreo(model);
		return codigoRetorno;

	}

	public String transformarXmlTicketXmlPac(CompTemporalModel model) {

		String xmlConvertido =  "";
		
		String versionSat ="";
		String serie ="";
		String folio ="";
		String fecha = "";
		String formaPago ="";
		//String condicionesDePago ="";
		String tipoComprobante ="";
		String exportacion = "";
		//String metodoPago ="";
		
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
		
		int idPagoComp = Integer.parseInt(model.getTicket());
		List<PagosCompModel> pago = getPagoComp(idPagoComp);

		String numeroCuenta = "";
		String fechaPago = "";
		String tipoDivisa = "";
		String importe = "";
		String folioOperacion = "";
		String folioCliente = "";
		if (pago != null && pago.size() > 0) {
			formaPago=StringUtils.leftPad(Integer.toString(pago.get(0).getFormaPago()), 2, '0');
			numeroCuenta = pago.get(0).getNumeroCuenta();
			fechaPago = UtilsFechas.formatear(pago.get(0).getFechaHoraMovimiento(), "yyyy-MM-dd'T'HH:mm:ss");
			tipoDivisa = pago.get(0).getTipoDivisa();
			importe = UtilsString.formatearDecimales(pago.get(0).getImporte(), DECIMALES_PRECIO);
			folioOperacion = pago.get(0).getFolioOperacion();
			folioCliente = pago.get(0).getFolioCliente();
		}
		
		String tipoComprobanteAPI = "C";
		String formaPagoAPI = formaPago;
		int tienda = Integer.parseInt(configFisService.getConfig().get("Complemento.ApiConfiguracion.Tienda"));
		String versionFacturacionSat = "4.0";
		int idAplicacion = 9;
		
		EmisorYLugarExpedicionDto datos = facturasService.obtenerDatosEmisionExpedicion(rfcEmisor, tienda, versionFacturacionSat, tipoComprobanteAPI, idAplicacion, formaPagoAPI );
		
		if (datos.getRespuesta().getCodigo().equals("1")) {
			
			versionSat = datos.getData().getVersion();
			serie = datos.getData().getSerie();
			folio = datos.getData().getFolio();
			fecha = datos.getData().getFecha();
			tipoComprobante = datos.getData().getTipoDeComprobante();
			exportacion = datos.getData().getExportacion();
			//formaPago = datos.getData().getFormaPago();
			//metodoPago = datos.getData().getMetodoPago();
			//condicionesDePago = datos.getData().getCondicionesDePago();
			
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
	
		/*********Comprobante**********/
		xmlConvertido += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlConvertido += "<Comprobante version=\"" + versionSat + "\"";
		xmlConvertido += " serie=\"" + serie + "\"";
		xmlConvertido += " folio=\"" + folio + "\"";
		xmlConvertido += " fecha=\"" + fecha + "\"";
		//xmlConvertido += " formaPago=\"" + formaPago + "\"";
		//xmlConvertido += " condicionesDePago=\"" + condicionesDePago + "\"";
		xmlConvertido += " tipoDeComprobante=\"" + tipoComprobante + "\"";
		xmlConvertido += " exportacion=\"" + exportacion + "\"";
		//xmlConvertido += " metodoPago=\"" + metodoPago + "\"";
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
		
//		xmlConvertido += "<CfdiRelacionados tipoRelacion=\"04\" ordenador=\"1\">";
//		int contUuidRel = 0;
//		for (FacturasCompModel item : model.getFacturas()) {
//			contUuidRel += 1;
//			String uuidRel = item.getUuid();
//			xmlConvertido += "<CfdiRelacionado uuid=\"" + uuidRel + "\" ordenador=\"1\" ordenadorRelacionados=\"" + contUuidRel + "\"/>";
//		}
//		xmlConvertido += " </CfdiRelacionados>";
		
		xmlConvertido += "<Emisor rfc=\"" + rfcEmisor + "\"";
		xmlConvertido += " nombre=\"" + nombreEmisor + "\"";
		xmlConvertido += " regimenFiscal=\"" + regimenFiscalEmisor + "\"";		
		xmlConvertido += " >";

//		xmlConvertido += "<DomicilioFiscal";
//		if (calle!=null && !calle.isEmpty()) xmlConvertido += " calle=\"" + calle + "\"";
//		if (noExterior!=null && !noExterior.isEmpty()) xmlConvertido += " noExterior=\"" + noExterior + "\"";
//		if (noInterior!=null && !noInterior.isEmpty()) xmlConvertido += " noInterior=\"" + noInterior + "\"";
//		if (colonia!=null && !colonia.isEmpty()) xmlConvertido += " colonia=\"" + colonia + "\"";
//		if (localidad!=null && !localidad.isEmpty()) xmlConvertido += " localidad=\"" + localidad + "\"";
//		if (referencia!=null && !referencia.isEmpty()) xmlConvertido += " referencia=\"" + referencia + "\"";
//		if (municipio!=null && !municipio.isEmpty()) xmlConvertido += " municipio=\"" + municipio + "\"";
//		if (estado!=null && !estado.isEmpty()) xmlConvertido += " estado=\"" + estado + "\"";
//		if (pais!=null && !pais.isEmpty()) xmlConvertido += " pais=\"" + pais + "\"";
//		if (codigoPostal!=null && !codigoPostal.isEmpty()) xmlConvertido += " codigoPostal=\"" + codigoPostal + "\"";			
//		xmlConvertido += " />";
		
//		xmlConvertido += "<ExpedidoEn";
//		if (calleExpedicion!= null && !calleExpedicion.isEmpty()) xmlConvertido += " calle=\"" + calleExpedicion + "\"";
//		if (noExteriorExpedicion!= null && !noExteriorExpedicion.isEmpty()) xmlConvertido += " noExterior=\"" + noExteriorExpedicion + "\"";
//		if (noInteriorExpedicion!= null && !noInteriorExpedicion.isEmpty()) xmlConvertido += " noInterior=\"" + noInteriorExpedicion + "\"";
//		if (coloniaExpedicion!= null && !coloniaExpedicion.isEmpty()) xmlConvertido += " colonia=\"" + coloniaExpedicion + "\"";
//		if (localidadExpedicion!= null && !localidadExpedicion.isEmpty()) xmlConvertido += " localidad=\"" + localidadExpedicion + "\"";
//		if (referenciaExpedicion!= null && !referenciaExpedicion.isEmpty()) xmlConvertido += " referencia=\"" + referenciaExpedicion + "\"";
//		if (municipioExpedicion!= null && !municipioExpedicion.isEmpty()) xmlConvertido += " municipio=\"" + municipioExpedicion + "\"";
//		if (estadoExpedicion!= null && !estadoExpedicion.isEmpty()) xmlConvertido += " estado=\"" + estadoExpedicion + "\"";
//		if (paisExpedicion!= null && !paisExpedicion.isEmpty()) xmlConvertido += " pais=\"" + paisExpedicion + "\"";
//		xmlConvertido += " codigoPostal=\"" + codigoPostalExpedicion + "\"";	
//		xmlConvertido += " />";

		
		xmlConvertido += " </Emisor>";

		String rfcCliente = model.getRfc();
		String razonSocialCliente = model.getRazonSocial();
		String usoCFDIComplemento = configFisService.getConfig().get("Complemento.Receptor.UsoCFDI");
		String regimenFiscalCliente = model.getRegimenFiscal();
		String codigoPostalCliente = model.getCodigoPostal();
		
		
		/*********Receptor**********/
		xmlConvertido += "<Receptor rfc=\"" + UtilsString.convertirCaracteresXml(rfcCliente) + "\"";
		xmlConvertido += " nombre=\"" + UtilsString.convertirCaracteresXml(razonSocialCliente) + "\"";
		xmlConvertido += " usoCfdi=\"" + usoCFDIComplemento + "\"";
		xmlConvertido += " regimenFiscalReceptor=\"" + regimenFiscalCliente + "\"";
		xmlConvertido += " domicilioFiscalReceptor=\"" + codigoPostalCliente + "\"";			
		xmlConvertido += " >";	

		xmlConvertido += " </Receptor>";
					
		/*********Conceptos**********/		
		String claveProdServ = "";
		String cantidad = "";
		String claveUnidad = "";
		String descripcion = "";
		String valorUnitario = "";
		String sImporteConcepto = "";
		String ordenadorDefault = "1";
		String nivelDefault = "1";
		String objetoImp= "";
		
		claveProdServ = configFisService.getConfig().get("Complemento.Concepto.ClaveProductoServicio");
		cantidad = "1";
		claveUnidad = configFisService.getConfig().get("Complemento.Concepto.ClaveUnidad");
		descripcion = configFisService.getConfig().get("Complemento.Concepto.Descripcion");
		valorUnitario = "0.00";
		sImporteConcepto = "0.00";
		ordenadorDefault = "1";
		nivelDefault = "1";
		objetoImp= "01";
		
		xmlConvertido += "<Conceptos>";

		xmlConvertido += "<Concepto claveProdServ=\"" + claveProdServ + "\"";
		xmlConvertido += " cantidad=\"" + cantidad + "\"";
		xmlConvertido += " claveUnidad=\"" + claveUnidad + "\"";
		xmlConvertido += " descripcion=\"" + descripcion + "\"";
		xmlConvertido += " valorUnitario=\"" + valorUnitario + "\"";
		xmlConvertido += " importe=\"" + sImporteConcepto + "\"";
		xmlConvertido += " ordenador=\"" + ordenadorDefault + "\"";
		xmlConvertido += " nivel=\"" + nivelDefault + "\"";
		xmlConvertido += " padre=\"0\"";
		xmlConvertido += " objetoImp=\"" + objetoImp + "\"";
		xmlConvertido += " >";
		xmlConvertido += "</Concepto>";
		xmlConvertido += "</Conceptos>";


		xmlConvertido += "<Totales";
		xmlConvertido += " subTotal=\"0\"";
		xmlConvertido += " moneda=\"XXX\"";
		xmlConvertido += " total=\"0.00\"";
		xmlConvertido += " />";
		

		/*********Complemento**********/
		xmlConvertido += "<Complementos>";
		xmlConvertido += "<Pagos version=\"2.0\">";
		

		String nombreBanco = "";
		String rfcBanco = "";
		CatCuentasEntity cuenta = catCuentasRepository.findCuentaByCuenta(numeroCuenta);	
		if (cuenta != null) {
			nombreBanco = cuenta.getNombreBanco();
			rfcBanco = cuenta.getRfc();
		}

		String formaDePagoP = formaPago;
		String monedaP = tipoDivisa;
		String tipoCambioP = "1";
		String numOperacion = folioOperacion;
		String rfcEmisorCtaOrd = rfcEmisor;
		String nomBancoOrdExt = nombreBanco;
		String ctaOrdenante = folioCliente;
		String rfcEmisorCtaBen = rfcBanco;
		String ctaBeneficiario = numeroCuenta;
		
		String baseIva = "totalTrasladosBaseIVA";
		String baseIeps = "totalTrasladosBaseIEPS";
		String impuestoIva = "totalTrasladosImpuestoIVA";
		String impuestoIeps = "totalTrasladosImpuestoIEPS";
		BigDecimal cien = new BigDecimal("100");
		
		xmlConvertido += "<Totales";

		List<Object[]> impuestos = folioFacturaImpuestosRepository.obtenerTotalesImpuestosFolioFactura(idPagoComp);
		for (Object[] impuestoObj : impuestos) {
			String tipoImpuesto = impuestoObj[4].toString();
			BigDecimal tasaDb = new BigDecimal( impuestoObj[5].toString() ); 
			BigDecimal baseDb = new BigDecimal( impuestoObj[3].toString() );
			BigDecimal montoDb = new BigDecimal( impuestoObj[6].toString() );
			
			//FolioFacturaImpuestosEntity item
			if (tipoImpuesto.equals("002")) {
				int tasa = tasaDb.multiply(cien).intValue();
				String baseSumatoria = UtilsString.formatearDecimales(baseDb.doubleValue() , DECIMALES_PRECIO);
				String impuestoSumatoria = UtilsString.formatearDecimales(montoDb.doubleValue() , DECIMALES_PRECIO);
				xmlConvertido += " " + baseIva + tasa + "=\"" + baseSumatoria + "\"";
				xmlConvertido += " " + impuestoIva + tasa + "=\"" + impuestoSumatoria + "\"";
			}
			if (tipoImpuesto.equals("003")) {
				int tasa = tasaDb.multiply(cien).intValue();
				String baseSumatoria = UtilsString.formatearDecimales(baseDb.doubleValue() , DECIMALES_PRECIO);
				String impuestoSumatoria = UtilsString.formatearDecimales(montoDb.doubleValue() , DECIMALES_PRECIO);
				xmlConvertido += " " + baseIeps + tasa + "=\"" + baseSumatoria + "\"";
				xmlConvertido += " " + impuestoIeps + tasa + "=\"" + impuestoSumatoria + "\"";
			}
		}
		xmlConvertido += " montoTotalPagos=\"" + importe + "\"";

		xmlConvertido += " />";

		boolean esformaPagoBancaria = confDatosEmisorRepository.esFormaPagoBancaria(formaDePagoP) == 1? true : false;
		
		int ordenadorPago = 1;
		xmlConvertido += "<Pago";
		xmlConvertido += " fechaPago=\"" + fechaPago + "\"";
		xmlConvertido += " formaDePagoP=\"" + formaDePagoP + "\"";
		xmlConvertido += " monedaP=\"" + monedaP + "\"";
		xmlConvertido += " tipoCambioP=\"" + tipoCambioP + "\"";
		xmlConvertido += " monto=\"" + importe + "\"";
		xmlConvertido += " numOperacion=\"" + numOperacion + "\"";
		
		if (esformaPagoBancaria) {
			xmlConvertido += " rfcEmisorCtaOrd=\"" + rfcEmisorCtaOrd + "\"";
			xmlConvertido += " nomBancoOrdExt=\"" + nomBancoOrdExt + "\"";
			xmlConvertido += " ctaOrdenante=\"" + ctaOrdenante + "\"";
			xmlConvertido += " rfcEmisorCtaBen=\"" + rfcEmisorCtaBen + "\"";
			xmlConvertido += " ctaBeneficiario=\"" + ctaBeneficiario + "\"";
		}
		
		xmlConvertido += " ordenador=\"" + ordenadorPago + "\"";
		xmlConvertido += " >";
		
		String uuidDR = "";
		String serieDR = "";
		String folioDR = "";
		String monedaDR = "";
		String equivalenciaDR = "";
		String numParcialidad = "";
		String impSaldoAnt = "";
		String impPagado = "";
		String impSaldoInsoluto = "";
		String objetoImpDR = "02";
		int ordenadorDoctoRelacionado = 0;
		
		for (FacturasCompModel item : model.getFacturas()) {
			uuidDR = item.getUuid();
			serieDR = item.getSerie();
			folioDR = String.valueOf(item.getFolio());
			monedaDR = "MXN";
			equivalenciaDR = "1";
			numParcialidad = String.valueOf(item.getParcialidad());
			impSaldoAnt = UtilsString.formatearDecimales(item.getImporteSaldoAnterior() , DECIMALES_PRECIO);
			impPagado = UtilsString.formatearDecimales(item.getImportePagado() , DECIMALES_PRECIO);
			impSaldoInsoluto = UtilsString.formatearDecimales(item.getImportePosterior() , DECIMALES_PRECIO);
			ordenadorDoctoRelacionado += 1;

			xmlConvertido += "<DoctoRelacionado";
			xmlConvertido += " idDocumento=\"" + uuidDR + "\"";
			xmlConvertido += " serie=\"" + serieDR + "\"";
			xmlConvertido += " folio=\"" + folioDR + "\"";
			xmlConvertido += " monedaDR=\"" + monedaDR + "\"";
			xmlConvertido += " equivalenciaDR=\"" + equivalenciaDR + "\"";
			xmlConvertido += " numParcialidad=\"" + numParcialidad + "\"";
			xmlConvertido += " impSaldoAnt=\"" + impSaldoAnt + "\"";
			xmlConvertido += " impPagado=\"" + impPagado + "\"";
			xmlConvertido += " impSaldoInsoluto=\"" + impSaldoInsoluto + "\"";
			xmlConvertido += " objetoImpDR=\"" + objetoImpDR + "\"";
			xmlConvertido += " ordenador=\"" + ordenadorDoctoRelacionado + "\"";
			xmlConvertido += " ordenadorPago=\"" + ordenadorPago + "\"";
			xmlConvertido += " >";
			
			List<FolioFacturaImpuestosEntity> impuestosFactura = folioFacturaImpuestosRepository.obtenerImpuestosUuid(uuidDR);
			
			int ordenadorImpuestoDocRel = 0;

			xmlConvertido += "<ImpuestosDR>";
			xmlConvertido += "<TrasladosDR>";
			for (FolioFacturaImpuestosEntity item2 : impuestosFactura) {
				ordenadorImpuestoDocRel += 1;
				String base = UtilsString.formatearDecimales(item2.getBase().doubleValue() , DECIMALES_PRECIO);
				String tipoImpuesto = item2.getTipoImpuesto();
				String tasa = UtilsString.formatearDecimales(item2.getTasa().doubleValue() , DECIMALES_TASA);
				String monto2 = UtilsString.formatearDecimales(item2.getMonto().doubleValue() , DECIMALES_TASA);
				xmlConvertido += "<TrasladoDR baseDR=\"" + base + "\" impuestoDR=\"" + tipoImpuesto + "\" tipoFactorDR=\"Tasa\" tasaOCuotaDR=\"" + tasa + "\" importeDR=\"" + monto2 + "\" ordenador=\"" + ordenadorImpuestoDocRel + "\" ordenadorDocRel=\"" + ordenadorDoctoRelacionado + "\" ordenadorPago=\"" + ordenadorPago + "\"/>";
			}
			xmlConvertido += "</TrasladosDR>";
			xmlConvertido += "</ImpuestosDR>";	
			
			xmlConvertido += "</DoctoRelacionado>";
		
		}

		
		xmlConvertido += "<ImpuestosP>";
		xmlConvertido += "<TrasladosP>";
		int ordenadorTraslados = 0;
		for (Object[] impuestoObj : impuestos) {
			
			String tipoImpuesto = impuestoObj[4].toString();
			BigDecimal tasaDb = new BigDecimal( impuestoObj[5].toString() ); 
			BigDecimal baseDb = new BigDecimal( impuestoObj[3].toString() );
			BigDecimal montoDb = new BigDecimal( impuestoObj[6].toString() );
			
			ordenadorTraslados += 1;
			String base = UtilsString.formatearDecimales(baseDb.doubleValue() , DECIMALES_PRECIO);
			String tasa = UtilsString.formatearDecimales(tasaDb.doubleValue() , DECIMALES_TASA);
			String monto2 = UtilsString.formatearDecimales(montoDb.doubleValue() , DECIMALES_PRECIO);
			xmlConvertido += "<TrasladoP baseP=\"" + base + "\" impuestoP=\"" + tipoImpuesto + "\" tipoFactorP=\"Tasa\" tasaOCuotaP=\"" + tasa + "\" importeP=\"" + monto2 + "\" tipoCambioP=\"1\"  ordenador=\"" + ordenadorTraslados + "\" ordenadorPago=\"" + ordenadorPago + "\"/>";
		}
		xmlConvertido += "</TrasladosP>";
		xmlConvertido += "</ImpuestosP>";
				
		
		xmlConvertido += "</Pago>";
		
		xmlConvertido += "</Pagos>";
		xmlConvertido += "</Complementos>";
		
		/*********DatosExtraCFD**********/
		
		String extra1 = model.getTicket();		
		String extra2 = "";
		
		xmlConvertido += "<DatosExtraCFD extra1=\"" + extra1 + "\" ";
		xmlConvertido += " extra2=\"" + extra2 + "\" ";

		String tipoDocumento = "CP";

		xmlConvertido += " extra4=\"" + tipoComprobanteAPI + "\" ";
		xmlConvertido += " extra6=\"" + fechaPago + "\" ";
		xmlConvertido += " extra7=\"" + tipoDocumento + "\" ";
		xmlConvertido += " extra8=\"" + "SODIMAC" + "\" ";

		xmlConvertido += " />";

		//DML Se modifica por solicitud de detecno 4
		xmlConvertido += "<Control cfd=\"8\" estatusId=\"1\" estatusIdImpresion=\"0\" estatusIdCorreo=\"0\" estatusIdArchivo=\"0\" rechazoId=\"0\" addendaId=\"0\" complementoId=\"32\" correo=\"iscortesz@sodimac.com.mx\" />";


		xmlConvertido += "</Comprobante>";
		
		return xmlConvertido;

	}
	
	@Override
	public void actualizarEstatusLog (CompTemporalModel model, int estatus) {
		model.setIdEstatusFactura(estatus);
		insertarLogFacturacion(model);		
	}
	
	@Transactional
	private int insertarLogFacturacion(CompTemporalModel model) {
		String rfc = seguridadService.encriptar(model.getRfc());
		String email = "";

		return complementosRepository.insertarLogFacturacion(rfc
					, model.getTicket()
					, email
					, model.getPac()
					, model.getIdFacturaPac()
					, model.getIdEstatusFactura()
					);			
	}	

	@Transactional(isolation=Isolation.READ_UNCOMMITTED)
	public int existComp(String rfc, String ticket) {
		return complementosRepository.existComp(seguridadService.encriptar(rfc), ticket);
	}

	@Transactional(isolation=Isolation.READ_UNCOMMITTED)
	public List<FacturasCompModel> getFacturas(int idTransaccionPago) {
		List<FacturasCompModel> list = new ArrayList<FacturasCompModel>();
		
		complementosRepository.obtenerFacturas(idTransaccionPago).forEach(item -> {
			FacturasCompModel itemList = new FacturasCompModel();
			itemList.setId(Integer.parseInt(item[0].toString()));
			itemList.setRfc(seguridadService.desencriptar(item[1].toString()));
			itemList.setEmail(seguridadService.desencriptar(item[2].toString()));
			itemList.setFechaTimbrado(item[3].toString());
			itemList.setUuid(item[4].toString());
			itemList.setXml(seguridadService.desencriptar(item[5].toString()));
			itemList.setSerie(item[6].toString());
			itemList.setFolio(Integer.parseInt(item[7].toString()));
			itemList.setSubTotal(Double.parseDouble(item[8].toString()));
			itemList.setTotal(Double.parseDouble(item[9].toString()));
			itemList.setMontoFactura(Double.parseDouble(item[10].toString()));
		    itemList.setPagoComplemento(Double.parseDouble(item[11].toString()));
		    itemList.setMonedaPago(item[12].toString());
		    itemList.setEquivalencia(Double.parseDouble(item[13].toString()));
		    itemList.setParcialidad(Integer.parseInt(item[14].toString()));
		    itemList.setImporteSaldoAnterior(Double.parseDouble(item[15].toString()));
		    itemList.setImportePagado(Double.parseDouble(item[16].toString()));
		    itemList.setImportePosterior(Double.parseDouble(item[17].toString()));
		    itemList.setSaldoControl(Double.parseDouble(item[18].toString()));
		    
		    list.add(itemList);
		});
		
		if (list.size() > 0) {
			Document document = UtilsFile.ObtenerDocumentXml (list.get(0).getXml());
	        Node nNode = document.getElementsByTagName("cfdi:Receptor").item(0);
	        Element eElement = (Element) nNode;
	        String razonSocial = eElement.getAttribute("Nombre");
	        String regimenFiscalReceptor = eElement.getAttribute("RegimenFiscalReceptor");
	        String domicilioFiscalReceptor = eElement.getAttribute("DomicilioFiscalReceptor");
	        for (FacturasCompModel item : list) {
	        	item.setRazonSocial(razonSocial);
	            item.setCodigoPostal(domicilioFiscalReceptor);
	            item.setRegimenFiscal(regimenFiscalReceptor);
	        }
		}
			
		return list;
	}
	
	@Transactional(isolation=Isolation.READ_UNCOMMITTED)
	public List<PagosCompModel> getPagoComp(int idTransaccionPago) {
		List<PagosCompModel> list = new ArrayList<PagosCompModel>();
		
		complementosRepository.obtenerPagoComp(idTransaccionPago).forEach(item -> {
			PagosCompModel itemList = new PagosCompModel();
			itemList.setId(Integer.parseInt(item[0].toString()));
			itemList.setNumeroCuenta(item[1].toString());
			try {
				String fecha = item[2].toString();
				itemList.setFechaHoraMovimiento(UtilsFechas.convertirDate(fecha, "yyyy-MM-dd HH:mm:ss"));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			itemList.setImporte(Double.parseDouble(item[3].toString()));
			itemList.setFolioBanco(item[4].toString());
			itemList.setRefInterbancaria(item[5].toString());
			itemList.setFolioCliente(item[6].toString());
			itemList.setTipoDivisa(item[7].toString());
			itemList.setFolioOperacion(item[8].toString());
			itemList.setFormaPago(Integer.parseInt(item[9].toString()));
			itemList.setFolioFactura(0);
			
			list.add(itemList);
		});
					
		return list;
	}
	
	@Transactional
	private int insertarComp(CompTemporalModel model) {
		String rfc = seguridadService.encriptar(model.getRfc());
		String razonSocial = seguridadService.encriptar(model.getRazonSocial());
		String email = "";
		if (!model.getEmail().isEmpty()) {
			email = seguridadService.encriptar(model.getEmail()); 
		}
		String xml = seguridadService.encriptar(model.getXml());

		return complementosRepository.insertarComp(rfc
					, model.getTicket()
					, razonSocial
					, model.getIdUsoCfdi()
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
					, errorComponent.getLongitud()
					, errorComponent.getLatitud()
					, errorComponent.getPagina()
					, errorComponent.getExplorador()
					, errorComponent.getSistemaOper()
					, errorComponent.getIp()
					);			
	}
	
	
	
}
