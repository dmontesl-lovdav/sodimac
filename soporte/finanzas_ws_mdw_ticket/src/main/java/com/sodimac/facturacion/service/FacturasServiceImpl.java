package com.sodimac.facturacion.service;

import java.math.BigInteger;
import java.sql.Date;
import java.text.ParseException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;

import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.factura.confirmar.resp_v2018.ClienteFacturaConfirmarExpRespTYPE;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.factura.confirmar.resp_v2018.ResponseTYPE;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ClienteTicketObtenerExpRespTYPE;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ClienteTicketObtenerExpRespTYPE.Comprobante;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ClienteTicketObtenerExpRespTYPE.DatosExtraCFD;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ClienteTicketObtenerExpRespTYPE.Respuesta;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ComprobanteTYPE.Control;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ComprobanteTYPE.Emisor;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ComprobanteTYPE.Totales;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ConceptoImpuestosTYPE;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ConceptoImpuestosTrasladosTYPE;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ConceptosTYPE;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ConceptosTYPE.Concepto;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ImpuestosTYPE;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ImpuestosTrasladosTYPE;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ImpuestosTrasladosTYPE.Traslado;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ObjectFactory;
import com.sodimac.facturacion.exception.DataBaseException;
import com.sodimac.facturacion.models.Cabecera;
import com.sodimac.facturacion.models.ConceptoTicket;
import com.sodimac.facturacion.models.DatosControl;
import com.sodimac.facturacion.models.ErrorBase;
import com.sodimac.facturacion.models.Ticket;
import com.sodimac.facturacion.models.TotalesTicket;
import com.sodimac.facturacion.models.TrasladoConcepto;
import com.sodimac.facturacion.repository.bct.QueryBctRepository;
import com.sodimac.facturacion.util.Constantes;
import com.sodimac.facturacion.util.UtilsFechas;

public class FacturasServiceImpl implements FacturasService {
	
	public ClienteTicketObtenerExpRespTYPE obtenerTicket (String numeroOrden, String transaccion, String tienda, String caja, String fecha) throws DataBaseException {
		
    	ClienteTicketObtenerExpRespTYPE response = new ClienteTicketObtenerExpRespTYPE();
		Respuesta respuesta = new Respuesta();
		Integer noTicketInt = 0;
		Date fechaSql = null;
		
		if (numeroOrden.equals("0")) numeroOrden = null;
		if (!transaccion.isEmpty()) noTicketInt = Integer.parseInt(transaccion);
		if (tienda.isEmpty()) tienda = null;
		if (caja.isEmpty()) caja = null;
		
		if (!fecha.isEmpty()) {
			try {
				fechaSql = new java.sql.Date(UtilsFechas.convertirDate(fecha, "dd-MM-yyyy").getTime());
			} catch (ParseException e) {
			}
		}
		QueryBctRepository qry = new QueryBctRepository();
		Ticket comprobanteBct = qry.getDatosTicket(numeroOrden, noTicketInt, tienda, caja, fechaSql);
		
		if (comprobanteBct == null) {
			respuesta.setCodigo("99");
			respuesta.setDescripcion(Constantes.ERROR_99);						
		} else {
			respuesta.setCodigo(comprobanteBct.getError().getCodigoError());
			respuesta.setDescripcion(comprobanteBct.getError().getMensajeError());			

			if (comprobanteBct !=null) {
				response = convertirTicketAComprobante(comprobanteBct);
			}
		}
		response.setRespuesta(respuesta);
		
		return response;
	}
    
    ClienteTicketObtenerExpRespTYPE convertirTicketAComprobante(Ticket ticket){
    	
    	ClienteTicketObtenerExpRespTYPE result = new ClienteTicketObtenerExpRespTYPE();
    	Comprobante comprobante = new Comprobante();
        
    	comprobante = obtenerDatosCabecera(comprobante, ticket);
        comprobante = obtenerDatosEmisor(comprobante, ticket); 
		comprobante = obtenerDatosConceptos(comprobante, ticket);
		comprobante = obtenerDatosImpuestos(comprobante, ticket.getImpuestos());
		comprobante = obtenerDatosTotales(comprobante, ticket);
		comprobante = obtenerDatosControl(comprobante, ticket.getControl());
					
		DatosExtraCFD datosExtra = new DatosExtraCFD();
		datosExtra.setExtra1(ticket.getDatosExtra().getExtra1());
		datosExtra.setExtra2(ticket.getDatosExtra().getExtra2());
		result.setDatosExtraCFD(datosExtra);
		
		result.setComprobante(comprobante);

		try {

			JAXBContext context = JAXBContext.newInstance(result.getClass());
			Marshaller marshaller = context.createMarshaller();

			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			JAXBElement<ClienteTicketObtenerExpRespTYPE> root = new ObjectFactory().createClienteTicketObtenerExpResp(result);

			marshaller.marshal(root, System.out);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
    	
    	return result;
    }
    
    Comprobante obtenerDatosCabecera (Comprobante comprobante, Ticket ticket) {
    	
    	Cabecera cabecera = ticket.getCabecera();        
		String version = cabecera.getVersion();
		String serie = cabecera.getSerie();
		String folio = cabecera.getFolio();
		String fecha = cabecera.getFecha();
		String formaPago = cabecera.getFormaPago();
		String condicionesDePago = cabecera.getCondicionesPago();
		String tipoComprobante = cabecera.getTipoComprobante();
		String metodoPago = cabecera.getMetodoPago();
		String lugarExpedicion = cabecera.getLugarExpedicion();
		
		comprobante.setVersion(version);
		comprobante.setSerie(serie);
		comprobante.setFolio(folio);
		comprobante.setFecha(fecha);
		comprobante.setFormaPago(formaPago);
		comprobante.setCondicionesDePago(condicionesDePago);
		comprobante.setTipoComprobante(tipoComprobante);
		comprobante.setMetodoPago(metodoPago);
		comprobante.setLugarExpedicion(lugarExpedicion);
		
    	return comprobante;
    }
    
    Comprobante obtenerDatosEmisor (Comprobante comprobante, Ticket ticket) {
    	Emisor emisor = new Emisor();
    	String rfc = ticket.getEmisor().getRfc();
    	String nombre = ticket.getEmisor().getNombre();
    	String regimenFiscal = ticket.getEmisor().getRegimenFiscal();
    	
    	emisor.setRfc(rfc);
    	emisor.setNombre(nombre);
    	emisor.setRegimenFiscal(regimenFiscal);
    	
		comprobante.setEmisor(emisor);
    	return comprobante;
    }    
    
    Comprobante obtenerDatosConceptos (Comprobante comprobante, Ticket ticket) {
    	
    	ConceptosTYPE conceptos = new ConceptosTYPE();

    	List<ConceptoTicket> conceptosTicket = ticket.getConceptos();
    	
    	for (int i = 0; i < conceptosTicket.size(); i++) {
    		
    		ConceptoTicket conceptoTicket = conceptosTicket.get(i);
    		
        	Concepto concepto = new Concepto();
        	
        	String claveProdServ = conceptoTicket.getClaveProdServ();
        	String noIdentificacion = conceptoTicket.getNoIdentificacion();
        	String cantidad = conceptoTicket.getCantidad().toString();
        	String claveUnidad = conceptoTicket.getClaveUnidad();
        	String unidad = conceptoTicket.getUnidad();
        	String descripcion = conceptoTicket.getDescripcion();
        	String valorUnitario = conceptoTicket.getValorUnitario().toString();
        	String importe = conceptoTicket.getImporte().toString();
        	String descuento = "0.0";
        	
        	String ordenador = new Integer(i+1).toString();
        	String padre = conceptoTicket.getPadre().toString();
        	String nivel = conceptoTicket.getNivel().toString();
        	
        	concepto.setClaveProdServ(claveProdServ);
        	concepto.setNoIdentificacion(noIdentificacion);
        	concepto.setCantidad(cantidad);
        	concepto.setClaveUnidad(claveUnidad);
        	concepto.setUnidad(unidad);
        	concepto.setDescripcion(descripcion);
        	concepto.setValorUnitario(valorUnitario);
        	concepto.setImporte(importe);
        	concepto.setDescuento(descuento);
        	concepto.setOrdenador(ordenador);
        	concepto.setPadre(padre);
        	concepto.setNivel(nivel);
        	concepto = obtenerImpuestosConcepto(concepto, conceptoTicket.getTraslados());

    		conceptos.getConcepto().add(concepto);
    	}
    	
		comprobante.setConceptos(conceptos);
		
    	return comprobante;
    }    
    
	Concepto obtenerImpuestosConcepto(Concepto concepto, List<TrasladoConcepto> trasladosConcepto) {

		ConceptoImpuestosTYPE impuestos = new ConceptoImpuestosTYPE();

		ConceptoImpuestosTrasladosTYPE traslados = new ConceptoImpuestosTrasladosTYPE();

		for (int i = 0; i < trasladosConcepto.size(); i++) {
			
			ConceptoImpuestosTrasladosTYPE.Traslado traslado = new ConceptoImpuestosTrasladosTYPE.Traslado();

			TrasladoConcepto eitem = trasladosConcepto.get(i);

			String base = eitem.getBase().toString();
			String impuesto = eitem.getImpuesto();
			String tipoFactor = eitem.getTipoFactor();
			String tasaCuota = eitem.getTasaOCuota().toString();
			String importe = eitem.getImporte().toString();
			String ordenador = concepto.getOrdenador();

			traslado.setBase(base);
			traslado.setImpuesto(impuesto);
			traslado.setTipoFactor(tipoFactor);
			traslado.setTasaCuota(tasaCuota);
			traslado.setImporte(importe);
			traslado.setOrdenador(ordenador);

			traslados.getTraslado().add(traslado);
		}

		impuestos.setTraslados(traslados);

		concepto.setImpuestos(impuestos);

		return concepto;
	}
    
	Comprobante obtenerDatosImpuestos(Comprobante comprobante, List<TrasladoConcepto> nImpuestos) {
		
		ImpuestosTYPE impuestos = new ImpuestosTYPE();
		ImpuestosTrasladosTYPE traslados = new ImpuestosTrasladosTYPE();

		for (int i = 0; i < nImpuestos.size(); i++) {
			
			TrasladoConcepto tc = nImpuestos.get(i);
			
			Traslado traslado = new Traslado();
			String impuesto = tc.getImpuesto();
			String tipoFactor = tc.getTipoFactor();
			String tasaCuota = tc.getTasaOCuota().toString();
			String importe = tc.getImporte().toString();
			String ordenador = tc.getOrdenador().toString();

			traslado.setImpuesto(impuesto);
			traslado.setTipoFactor(tipoFactor);
			traslado.setTasaCuota(tasaCuota);
			traslado.setImporte(importe);
			traslado.setOrdenador(ordenador);

			traslados.getTraslado().add(traslado);

			impuestos.setTraslados(traslados);
			
		}
		
		comprobante.setImpuestos(impuestos);

		return comprobante;
	}
	
	Comprobante obtenerDatosTotales (Comprobante comprobante, Ticket ticket) {		
		
		TotalesTicket tt = ticket.getTotales();
		
    	Totales totales = new Totales();
    	String subTotal = tt.getSubtotal();
    	String descuento = tt.getDescuento();
    	String moneda = tt.getMoneda();
    	String tipoCambio = tt.getTipoCambio();
    	String total = tt.getTotal();
    	String importeLetra = tt.getImporteLetra();
    	
    	String totalImpuestosTrasladados = tt.getTotalImpuestosTrasladados();
    	
    	totales.setSubTotal(subTotal);
    	totales.setDescuento(descuento);
    	totales.setMoneda(moneda);
    	totales.setTipoCambio(tipoCambio);
    	totales.setTotal(total);
    	totales.setImporteLetra(importeLetra);
    	totales.setTotalImpuestosTrasladados(totalImpuestosTrasladados);
    	
		comprobante.setTotales(totales);
    	return comprobante;
    }    
    
    Comprobante obtenerDatosControl (Comprobante comprobante, DatosControl ctl) {
    	Control control = new Control();
    	String cfdId = ctl.getIdCfdi();
    	String estatusId = ctl.getIdStatus();
    	String estatusIdImpresion = ctl.getEstatusImpresion();
    	String estatusIdCorreo = ctl.getEstatusCorreo();
    	String estatusIdArchivo = ctl.getEstatusArchivo();
    	String rechazoId = ctl.getIdRechazo();
    	String complementoId = ctl.getIdComplemento();
    	
    	control.setCfdId(cfdId);
    	control.setEstatusId(estatusId);
    	control.setEstatusIdImpresion(estatusIdImpresion);
    	control.setEstatusIdCorreo(estatusIdCorreo);
    	control.setEstatusIdArchivo(estatusIdArchivo);
    	control.setRechazoId(rechazoId);
    	control.setComplementoId(complementoId);
    	
		comprobante.setControl(control );
    	return comprobante;
    }    
    
    public ClienteFacturaConfirmarExpRespTYPE confirmaFactura(String ticket, String codigo, String uuid) throws DataBaseException {
    	ClienteFacturaConfirmarExpRespTYPE response = new ClienteFacturaConfirmarExpRespTYPE();
    	ResponseTYPE respuesta = new ResponseTYPE();
    	
    	QueryBctRepository qry = new QueryBctRepository();
    	ErrorBase res = qry.confirmaFactura(ticket, codigo, uuid);
    	
    	if (res == null) {
			respuesta.setCodigoRespuesta(BigInteger.valueOf(99));
			respuesta.setMensajeRespuesta(Constantes.ERROR_99);						
		} else {
	    	respuesta.setCodigoRespuesta(BigInteger.valueOf(Integer.parseInt(res.getCodigoError())));
	    	respuesta.setMensajeRespuesta(res.getMensajeError());
    	}
    	response.setResponse(respuesta);
    	
    	return response;
    }
    
}
