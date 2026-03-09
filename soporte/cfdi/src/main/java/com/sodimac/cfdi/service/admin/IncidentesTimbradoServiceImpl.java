package com.sodimac.cfdi.service.admin;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.cfdi.models.factura.IncidentesTimbradoModel;
import com.sodimac.cfdi.repositoryFactura.IncidentesTimbradoRepository;
import com.sodimac.cfdi.service.SeguridadService;

@Service
@Transactional
public class IncidentesTimbradoServiceImpl implements IncidentesTimbradoService {

	// Índices de las columnas retornadas por el SP uspCFDIClientesTemporal
	private static final int INDEX_TICKET = 0;
	private static final int INDEX_TOTAL = 1;
	private static final int INDEX_RFC = 2;
	private static final int INDEX_RAZON_SOCIAL = 3;
	private static final int INDEX_USO_CFDI = 4;
	private static final int INDEX_EMAIL = 5;
	private static final int INDEX_REGIMEN_FISCAL = 6;
	private static final int INDEX_CODIGO_POSTAL = 7;
	private static final int INDEX_FECHA_CREACION = 8;
	private static final int INDEX_ID_FACTURA_PAC = 9;
	private static final int INDEX_UUID = 10;
	private static final int INDEX_FECHA_TIMBRADO = 11;
	private static final int INDEX_ORIGEN = 12;
	private static final int INDEX_ERROR_LOG = 13;
	private static final int INDEX_INTENTOS = 14;
	private static final int INDEX_NIVEL = 15;
	private static final int INDEX_ATIENDE = 16;

	int rowIndex = 1;

	@Autowired
	private IncidentesTimbradoRepository repository;

	@Autowired
	private SeguridadService seguridadService;

	public List<IncidentesTimbradoModel> findTicket(String ticket, Integer idusuario) {

		List<IncidentesTimbradoModel> parameterList = new ArrayList<IncidentesTimbradoModel>();
		
		repository.findTicket(ticket).forEach(parameter -> {
			IncidentesTimbradoModel param = new IncidentesTimbradoModel();
			param.setTicket(parameter[INDEX_TICKET].toString());
			param.setTotal(Double.parseDouble(parameter[INDEX_TOTAL].toString()));
			param.setRfc(seguridadService.desencriptar(parameter[INDEX_RFC].toString()));
			param.setRazonSocial(seguridadService.desencriptar(parameter[INDEX_RAZON_SOCIAL].toString()));
			param.setUsoCfdi(parameter[INDEX_USO_CFDI].toString());
			param.setEmail(seguridadService.desencriptar(parameter[INDEX_EMAIL].toString()));
			param.setRegimenFiscal(parameter[INDEX_REGIMEN_FISCAL].toString());
			param.setCodigoPostal(seguridadService.desencriptar(parameter[INDEX_CODIGO_POSTAL].toString()));
			param.setFechaCreacion(parameter[INDEX_FECHA_CREACION].toString());

			param.setIdFacturaPac(Integer.parseInt(parameter[INDEX_ID_FACTURA_PAC].toString()));
			param.setUuid(parameter[INDEX_UUID].toString());

			if (parameter[INDEX_FECHA_TIMBRADO] != null) {
				param.setFechaTimbrado(parameter[INDEX_FECHA_TIMBRADO].toString());
			}

			// Columna origen (SODIMAC/DETECNO) - se mapea a facturadoEn
			param.setFacturadoEn(parameter[INDEX_ORIGEN].toString());

			// Columna errorlog - se mapea a mensajeError
			param.setMensajeError(parameter[INDEX_ERROR_LOG].toString());
			param.setIntentos(Integer.parseInt(parameter[INDEX_INTENTOS].toString()));
			param.setNivel(Integer.parseInt(parameter[INDEX_NIVEL].toString()));
			param.setAtiende(parameter[INDEX_ATIENDE].toString());

			parameterList.add(param);
		});
		;
		return parameterList;
	}
	
	public int deleteTicket(String ticket) {
		return repository.deleteTicket(ticket);
	}

	public int deleteIntentos(String ticket) {
		return repository.deleteIntentos(ticket);
	}

}
