package com.sodimac.facturacion.service;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.sodimac.facturacion.entity.bct.TicketDetalleEntity;
import com.sodimac.facturacion.repository.bct.TicketDetalleRepository;
import com.sodimac.facturacion.util.UtilsFechas;

@Service
@EnableTransactionManagement
public class DocumentoServiceImpl implements DocumentoService {

	@Autowired
	private ConfiguracionWsService configuracionWsService;
	@Autowired
	private TicketDetalleRepository ticketDetalleRepository;

	public boolean validarExpresionRegular(String documento) {
		boolean result = true;
		
		int longitudTicket = Integer.parseInt(configuracionWsService.findParameterByKey("Ticket.Longitud"));
		int longitudOC = Integer.parseInt(configuracionWsService.findParameterByKey("OrdenCompra.Longitud"));;
		int longitudMascara = Integer.parseInt(configuracionWsService.findParameterByKey("Mascara.Longitud"));;
		int longitudMinimaDAD = Integer.parseInt(configuracionWsService.findParameterByKey("DAD.Longitud.Minima"));;
		int longitudDAD = Integer.parseInt(configuracionWsService.findParameterByKey("DAD.Longitud"));;
		int longitudLipto = Integer.parseInt(configuracionWsService.findParameterByKey("Lipto.Longitud"));;
		int longitudArrto = Integer.parseInt(configuracionWsService.findParameterByKey("Arrendamiento.Longitud"));;
		int longitudMaxima = 0;
		
		if (longitudOC > longitudMascara) {
			longitudMaxima = longitudOC;
		} else {
			longitudMaxima = longitudMascara;
		}
		if (longitudDAD > longitudMaxima) longitudMaxima = longitudDAD;
		
		documento = documento.trim();
		
		if (documento.isEmpty() || (documento.length() == 1 || documento.length() > longitudTicket)) {
			result = false;
		} else {
	        Pattern pat = Pattern.compile(configuracionWsService.findParameterByKey("ExpresionRegular.Documento.Caracteres"));
	        Matcher mat = pat.matcher(documento);
	        if (!mat.matches()) {
	        	result = false;
	        }
			if (documento.contains("L") && documento.length()>longitudLipto) {
				result = false;			
			}
			if (documento.contains("A") && documento.length()>longitudArrto) {
				result = false;			
			}
			
			if (!documento.contains("L") && !documento.contains("A") 
					&& (documento.length() < longitudMinimaDAD || (documento.length()!=longitudTicket && documento.length() > longitudMaxima))) {
				result = false;
			}
	        
		}
		return result;
		
	}
	
	public String obtenerTicketOrdenCompra(String ordenCompra) throws ParseException {
		String ticket = "";
		int periodoMeses = Integer.parseInt(configuracionWsService.findParameterByKey("Request.OrdenCompra.PeriodoMeses")) * -1;
		String canalesValidos = configuracionWsService.findParameterByKey("Request.OrdenCompra.Canal");
		List<String> canales = Arrays.asList(canalesValidos.split(",")) ;
		
		Date hoy = new Date();
		String fecha = UtilsFechas.formatear(hoy, "yyyy-MM-dd");
		String fechaPeriodo = UtilsFechas.sumarMeses(fecha, "yyyy-MM-dd", periodoMeses, "dd-MM-yy");
		List<TicketDetalleEntity> detalleList = ticketDetalleRepository.findByDocumento(ordenCompra, fechaPeriodo);
		
		if (detalleList != null && !detalleList.isEmpty()) {
			for (TicketDetalleEntity det: detalleList) {
				String canal = det.getNumeroDocumento().toString().substring(0, 2);
				if (canales.contains(canal)) {
					ticket = det.getTicket();
					break;					
				}
			}
		} 
		return ticket;
	}
	
}
