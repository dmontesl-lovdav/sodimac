package com.sodimac.facturacion.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ClienteTicketObtenerExpRespTYPE;
import com.sodimac.facturacion.component.ErrorComponent;
import com.sodimac.facturacion.entity.bct.TicketEntity;
import com.sodimac.facturacion.models.ClientesTemporalModel;
import com.sodimac.facturacion.repository.TicketsRepository;
import com.sodimac.facturacion.repository.bct.EstFacturasRepository;
import com.sodimac.facturacion.repository.bct.TicketRepository;
import com.sodimac.facturacion.service.ws.ObtenerTicketService;
import com.sodimac.facturacion.util.UtilsFechas;
import com.sodimac.facturacion.util.enums.EFacturas;

@Service
public class TicketsServiceImpl implements TicketsService {

	private Logger logger = LoggerFactory.getLogger(TicketsServiceImpl.class);
	
	@Autowired
	private TicketsRepository ticketsRepository;
	@Autowired
	private CatConfiguracionService catConfiguracionService;
	@Autowired
	private CatMensajesService catMensajesService;
	@Autowired
	private FacturasService facturasService;
	@Autowired
	private ErrorComponent errorComponent;
	@Autowired
	private TicketRepository ticketRepository;
	@Autowired
	private EstFacturasRepository estFacturasRepository;
	@Autowired
	private ObtenerTicketService obtenerTicketService;

	final String Ws_NoInformacion = "77";
	final String Ws_Error = "99";
	final String WsConfirm_TicketFacturado = "4";
	final String WsConfirm_TicketEnProceso = "1";
	final String WsObtener_TicketFacturado = "2";
	final String WsObtener_TicketEnProceso = "3";
	private static final String PUNTOS_CES = "102";
	
	@Transactional
	public int getEstatusTicket(String ticket) {
		return ticketsRepository.getEstatusTicket(ticket);
	}
	
	@Transactional
	public String validarTicketExpresionRegular(String ticket) {
		String result = "";

		if (ticket.isEmpty()){
			return result;
		}
		ticket = ticket.trim();
        Pattern pat = Pattern.compile(catConfiguracionService.findParameterByKey("ExpresionRegular.Ticket"));
        Matcher mat = pat.matcher(ticket);
        if (!mat.matches()) {
        	return result;
        }					
		if ((ticket.length()<19 || ticket.length()>20) && ticket.length()!=10){
			return result;
		}
		if (ticket.length()==20 && ticket.substring(0,1).equals("0")) {
			result = ticket.substring(1);
		}
		if (ticket.length()==20 && !ticket.substring(0,1).equals("0")) {
			result = ticket.substring(0, ticket.length()-1);
		}
		if (ticket.length()==10 || ticket.length()==19) {
			result = ticket;
		}
		
		return result;
	}
	
	@Transactional
	public boolean validarMontoExpresionRegular(String monto) {
		boolean result = true;
		
		monto = monto.trim();
		if (monto.isEmpty() || monto.length()> 10) {
			result = false;
		} else {
	        Pattern pat = Pattern.compile(catConfiguracionService.findParameterByKey("ExpresionRegular.Monto"));
	        Matcher mat = pat.matcher(monto);
	        if (!mat.matches()) {
	        	result = false;
	        }					
		}
		return result;
	}
	
	@Transactional
	public String validarTicketWS(String ticket, String monto) throws ParseException {
		ClientesTemporalModel model = new ClientesTemporalModel();
		String mensaje = "";
		
		ticket = ticket.trim();
		monto = monto.trim();
		
		if (ticket.length()==20) {
			ticket = ticket.substring(1);
		}

		model.setTicket(ticket);
		
		logger.info("Validar si el ticket ya esta facturado");
		int existFactura = facturasService.existFactura(ticket);
		if (existFactura==1) {
			return catMensajesService.get(51).getDescripcionMensaje();
		}			

		logger.info("Validar si el ticket ya esta pendiente en el autofacturador");
		int estatusTicket = getEstatusTicket(ticket);
		if (estatusTicket == EFacturas.EnEspera.getValor()) {
			mensaje = catMensajesService.get(52).getDescripcionMensaje();
//			datosArr.clear();
//			datosArr.add(model.getTicket());
//			actividadesModel.registrarActividad(5, datosArr, "GuardarTicket");
			return mensaje;
		}
		
		//Validar fecha del ticket en Bct
		logger.info("Validar si el ticket esta dentro del rango de fecha valido");
		TicketEntity ticketBctHdrVal = ticketRepository.findByTicket(ticket);
		boolean ticketNC = false;
		if (ticketBctHdrVal == null) {
			return "OK";
		} else {
			if (ticketBctHdrVal.getTipoTicket() == 9 || ticketBctHdrVal.getTipoTicket() == 10) {
				//9 = Devolucion normal y 10 devolucion libre
				ticketNC = true;
			}
		}
		int diasPermitidos = Integer.parseInt(catConfiguracionService.findParameterByKey("Aplicacion.DiasPermitidosFacturar")) * -1;
		String fechaActual = UtilsFechas.getLocalDate().toString() + " 00:00:00";
		Date fechaActualDate = UtilsFechas.convertirDate(fechaActual, "yyyy-MM-dd HH:mm:ss");
		Date fechalimite = DateUtils.addDays(fechaActualDate, diasPermitidos);
		if (ticketBctHdrVal.getFecha().before(fechalimite)) {
			mensaje = catMensajesService.get(124).getDescripcionMensaje();
			return mensaje;
		}
		
		try {
//			pacDefault = pacsService.getIdDefault();
//			if (pacDefault == 0) {
//				actividadesModel.registrarActividad(7, null, "GuardarTicket");
//				return catMensajesService.get(54).getDescripcionMensaje();
//			}
//			model.setPac(pacDefault);
			
			//model.setIdEstatusFactura(EFacturas.ConsultaTicket.getValor());
			//facturasService.insertarLogFacturacion(model);

			
			//validar si el ticket esta en proceso en bct
			Boolean validarEnProceso = Boolean.parseBoolean(catConfiguracionService.findParameterByKey("WebService.Sodimac.ValidarEnProceso"));
			if (validarEnProceso) {
				String ticketEnEstatusEnProceso = estFacturasRepository.ValidateStatusPendingByTicket(model.getTicket());
				if (ticketEnEstatusEnProceso != null && !ticketEnEstatusEnProceso.equals("")) {
					return catMensajesService.get(51).getDescripcionMensaje();
				}
			}
			
			
			logger.info("Validar si el monto del ticket esta dentro del rango valido");
			
			ClienteTicketObtenerExpRespTYPE resultTicketOld = obtenerTicketService.getTicket(model.getTicket());
			String formaPago = resultTicketOld.getComprobante().getFormaPago();
			
			if (!formaPago.equals(PUNTOS_CES)) {
				BigDecimal bMontoObtener = new BigDecimal(validarImporteVacio(resultTicketOld.getComprobante().getTotales().getTotal()));
				BigDecimal bMonto = new BigDecimal(monto);
				BigDecimal bDiferencia = bMontoObtener.subtract(bMonto).abs();
				
				String rangoValido = this.catConfiguracionService.findParameterByKey("Aplicacion.ToleranciaTicket");
				BigDecimal bRangoValido = new BigDecimal(rangoValido);
				
				boolean excedeRango = ( bDiferencia.compareTo(bRangoValido) > 0 );
				if (  excedeRango ) {
					return catMensajesService.get(55).getDescripcionMensaje();
				}				
			}
			
			
			if (ticketNC) {
				logger.info("Validar si el ticket de devolucion tiene habilitado el timbrado libre");
				Boolean deshabilitarTimbradoLibre = Boolean.parseBoolean(catConfiguracionService.findParameterByKey("DeshabilitarTimbradoLibre"));
				if (deshabilitarTimbradoLibre && ticketBctHdrVal.getTipoTicket() == 10) {
					return catMensajesService.get(115).getDescripcionMensaje();
				}
				String uuidRelacionado = estFacturasRepository.findUuidByTicket(ticketBctHdrVal.getOriginal());
				if (uuidRelacionado == null || uuidRelacionado.equals("")) {
					return catMensajesService.get(114).getDescripcionMensaje().replace("{ticket}", model.getTicket());
				}
				
				return "OKNC";
			}
			logger.info("Fin de la validacion del ticket");

		} catch (Exception e) {
			errorComponent.setTicket(model.getTicket());
			errorComponent.setRfc(model.getRfc());
			errorComponent.setPagina("validarTicketWS");
			errorComponent.guardarLog(e, model);
			e.printStackTrace();
		}
		
		if (ticketNC) {
			return "OKNC";
		}
		return "OK";
	}

	@Override
	public TicketEntity getTicket(String ticket) {
		return ticketRepository.findByTicket(ticket);
	}
	
	private String validarImporteVacio (String importe) {
		
		if (importe == null || importe.isEmpty()) {
			importe = "0";
		}
		
		return importe;
	}
	
}
