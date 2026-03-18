package com.sodimac.facturacion.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ClienteTicketObtenerExpRespTYPE;
import com.sodimac.facturacion.component.ActividadesComponent;
import com.sodimac.facturacion.component.ErrorComponent;
import com.sodimac.facturacion.entity.bct.TicketEntity;
import com.sodimac.facturacion.models.ClientesTemporalModel;
import com.sodimac.facturacion.repository.bct.EstFacturasRepository;
import com.sodimac.facturacion.repository.bct.TicketRepository;
import com.sodimac.facturacion.repository.fac.TicketsRepository;
import com.sodimac.facturacion.service.catalogospdf.PacsService;
import com.sodimac.facturacion.service.ws.ObtenerTicketService;
import com.sodimac.facturacion.util.enums.EFacturas;

@Service
public class TicketsServiceImpl implements TicketsService {

	@Autowired
	private TicketsRepository ticketsRepository;
	@Autowired
	private ConfiguracionFacturacionService configFacService;
	@Autowired
	private CatMensajesService catMensajesService;
	@Autowired
	private FacturasService facturasService;
	@Autowired
	private ActividadesComponent actividadesModel;
	@Autowired
	private ObtenerTicketService obtenerTicketService;
	@Autowired
	private ErrorComponent errorComponent;
	@Autowired
	private PacsService pacsService;
	@Autowired
	private TicketRepository ticketRepository;
	@Autowired
	private EstFacturasRepository estFacturasRepository;
	@Autowired
	private TicketsBctService ticketsBctService;

	final String Ws_NoInformacion = "77";
	final String Ws_Error = "99";
	final String Ws_Error2 = "100";
	final String WsConfirm_TicketFacturado = "4";
	final String WsConfirm_TicketEnProceso = "1";
	final String WsObtener_TicketFacturado = "2";
	final String WsObtener_TicketEnProceso = "3";
			
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
        Pattern pat = Pattern.compile(configFacService.getConfig().get("ExpresionRegular.Ticket"));
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
		int longitudMonto = Integer.parseInt(configFacService.getConfig().get("Request.Timbrado.Monto.longitud"));
		
		monto = monto.trim();
		if (monto.isEmpty() || monto.length() > longitudMonto) {
			result = false;
		} else {
	        Pattern pat = Pattern.compile(configFacService.getConfig().get("ExpresionRegular.Monto"));
	        Matcher mat = pat.matcher(monto);
	        if (!mat.matches()) {
	        	result = false;
	        }					
		}
		return result;
	}
	
	@Transactional
	public String validarTicketWS(String ticket, String monto) {
		int pacDefault = 0;
		ClientesTemporalModel model = new ClientesTemporalModel();
		String mensaje = "";
		List<String> datosArr = new ArrayList <String>();
		
		ticket = ticket.trim();
		monto = monto.trim();
		
		if (ticket.length()==20) {
			ticket = ticket.substring(1);
		}

		model.setTicket(ticket);
		
		//ClienteFacturaConfirmarExpRespTYPE resultTicket = null;
		ClienteTicketObtenerExpRespTYPE obtenerTicket = null;
		
		try {
			pacDefault = pacsService.getIdDefault();
			if (pacDefault == 0) {
				actividadesModel.registrarActividad(7, null, "GuardarTicket");
				return catMensajesService.get(54).getDescripcionMensaje();
			}
			model.setPac(pacDefault);
			
			int estatusTicket = getEstatusTicket(ticket);
			
			int existFactura = facturasService.existFactura(ticket);
			if (existFactura==1) {
				mensaje = catMensajesService.get(51).getDescripcionMensaje();
				return mensaje;
			}
			
			if (estatusTicket == EFacturas.EnEspera.getValor()) {
				mensaje = catMensajesService.get(52).getDescripcionMensaje();
				datosArr.clear();
				datosArr.add(model.getTicket());
				actividadesModel.registrarActividad(5, datosArr, "GuardarTicket");
				return mensaje;
			}

			facturasService.actualizarEstatusLog(model, EFacturas.ConsultaTicket.getValor());

			int contador = 0;
			int reintentosWs = Integer.parseInt(configFacService.getConfig().get("WebService.ObtenerTicket.Reintentos"));
			
			do {
				try {
					Thread.sleep(1*100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
						
				//resultTicket = confirmarService.confirmar(ticket, comprobanteFiscal, codigo);
				obtenerTicket = obtenerTicketService.getTicket(ticket);
				//obtenerTicket.getComprobante().getTotales().getTotal();
				
	
				
				contador += 1;

			} while ((obtenerTicket == null 
				    //|| resultTicket.getResponse().getCodigoRespuesta().toString().equals(Ws_Error)
				    )
					&& contador <= reintentosWs);		
			
			if (obtenerTicket == null) {
				errorComponent.setTicket(model.getTicket());
				errorComponent.setRfc(model.getRfc());
				errorComponent.setPagina("validarTicketWS");
				errorComponent.guardarLog("El servicio de obtener el ticket no esta disponible", model);
			} else {
				if (obtenerTicket.getRespuesta().getCodigo().toString().equals(Ws_Error)) {
					errorComponent.setTicket(model.getTicket());
					errorComponent.setRfc(model.getRfc());
					errorComponent.setPagina("validarTicketWS");
					errorComponent.guardarLog(obtenerTicket, model);
				} else {
					if (obtenerTicket.getRespuesta().getCodigo().toString().equals(Ws_NoInformacion)) {
						datosArr.clear();
						datosArr.add(model.getTicket());
						actividadesModel.registrarActividad(3, datosArr, "GuardarTicket");
					}
					
					Boolean validarEnProceso = Boolean.parseBoolean(configFacService.getConfig().get("WebService.Sodimac.ValidarEnProceso"));
					if (obtenerTicket.getRespuesta().getCodigo().toString().equals(WsObtener_TicketFacturado) 
					 || (validarEnProceso && obtenerTicket.getRespuesta().getCodigo().toString().equals(WsObtener_TicketEnProceso)) 
					 ) {
						if (obtenerTicket.getRespuesta().getCodigo().toString().equals(WsObtener_TicketEnProceso)) {
							datosArr.clear();
							datosArr.add(model.getTicket());
							actividadesModel.registrarActividad(5, datosArr, "GuardarTicket");
						}
						errorComponent.setTicket(model.getTicket());
						errorComponent.setRfc(model.getRfc());
						errorComponent.setPagina("validarTicketWS");
						errorComponent.guardarLog(obtenerTicket, model);

						mensaje = catMensajesService.get(51).getDescripcionMensaje();
						return mensaje;
					}
					if (!obtenerTicket.getRespuesta().getCodigo().toString().equals(Ws_NoInformacion)) {
						datosArr.clear();
						datosArr.add(model.getTicket());
						actividadesModel.registrarActividad(2, datosArr, "GuardarTicket");
						String montoObtener = obtenerTicket.getComprobante().getTotales().getTotal().toString().trim();
						Float monto1 = Float.valueOf(montoObtener);
						Float monto2 = Float.valueOf(monto);

						if (Float.compare(monto1,monto2) != 0) {
							return catMensajesService.get(55).getDescripcionMensaje();
						}
						
						String tipoComprobante = obtenerTicket.getComprobante().getTipoComprobante();
						if (tipoComprobante.equals("E")) {
							TicketEntity ticketBctHdr = ticketsBctService.findByTicket(ticket);
							if (ticketBctHdr != null) {
								Boolean deshabilitarTimbradoLibre = Boolean.parseBoolean(configFacService.getConfig().get("DeshabilitarTimbradoLibre"));
								if (deshabilitarTimbradoLibre && ticketBctHdr.getTipoTicket() == 10) {
									return catMensajesService.get(115).getDescripcionMensaje();
								}
								String uuidRelacionado = estFacturasRepository.findUuidByTicket(ticketBctHdr.getOriginal());
								if (uuidRelacionado == null || uuidRelacionado.equals("")) {
									return catMensajesService.get(114).getDescripcionMensaje();
								}
							}
							return "OKNC";
						}
					}
					
				}
			}

		} catch (Exception e) {
			errorComponent.setTicket(model.getTicket());
			errorComponent.setRfc(model.getRfc());
			errorComponent.setPagina("validarTicketWS");
			errorComponent.guardarLog(e, model);
			e.printStackTrace();
		}
		
		return "OK";
	}
	
	@Transactional
	public ClienteTicketObtenerExpRespTYPE getResultTicketWS(ClientesTemporalModel model) throws NumberFormatException {
		
		ClienteTicketObtenerExpRespTYPE resultTicket = null;
		
		int contador = 0;
		int reintentosWs = Integer.parseInt(configFacService.getConfig().get("WebService.ObtenerTicket.Reintentos"));
		
		do {
			try {
				Thread.sleep(1*100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			errorComponent.setRfc(model.getRfc());
			resultTicket = obtenerTicketService.getTicket(model.getTicket());
			contador += 1;

		} while ((resultTicket == null 
			    || resultTicket.getRespuesta().getCodigo().equals(Ws_Error))
				&& contador <= reintentosWs);		
		
		if (resultTicket == null) {
			errorComponent.setTicket(model.getTicket());
			errorComponent.setRfc(model.getRfc());
			errorComponent.setPagina("getResultTicketWS");
			errorComponent.guardarLog("El servicio de recuperacion del ticket no esta disponible", model);
		} else if (resultTicket.getRespuesta().getCodigo().equals(Ws_NoInformacion)
		        || resultTicket.getRespuesta().getCodigo().equals(Ws_Error)) {
				errorComponent.setTicket(model.getTicket());
				errorComponent.setRfc(model.getRfc());
				errorComponent.setPagina("getResultTicketWS");
				errorComponent.guardarLog(resultTicket, model);
		} else {
			Boolean validarEnProceso = Boolean.parseBoolean(configFacService.getConfig().get("WebService.Sodimac.ValidarEnProceso"));
			if (resultTicket.getRespuesta().getCodigo().equals(WsObtener_TicketFacturado)
			 || (validarEnProceso && resultTicket.getRespuesta().getCodigo().equals(WsObtener_TicketEnProceso))
			 ) {
				errorComponent.setTicket(model.getTicket());
				errorComponent.setRfc(model.getRfc());
				errorComponent.setPagina("getResultTicketWS");
				errorComponent.guardarLog(resultTicket, model);
			}			
		}
		
		return resultTicket;
	}
	
	@Transactional
	public int eliminarTicket(String ticket) {
		return ticketsRepository.eliminarTicket(ticket);
	}

	// Valida si el ticket ya esta facturado o bien esta en proceso
	public int validarTicket(String ticket) {
		
		String uuid = estFacturasRepository.findUuidByTicket(ticket);
		if (uuid != null && !uuid.trim().isEmpty()) {
			return 130;
		}
		
		int existEnProceso = ticketsRepository.existTicketEnProceso(ticket);
		if (existEnProceso == 1) {
			return 131;
		}
		
		return 300;
	}

}
