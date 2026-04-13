package com.sodimac.rebates.service;

import java.util.Date;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.sodimac.rebates.model.ControlDocumento;
import com.sodimac.rebates.model.Documento;
import com.sodimac.rebates.model.Generic;
import com.sodimac.rebates.model.Periodo;

public interface IControlDocumentoService {

	List<ControlDocumento> getAll();

	List<ControlDocumento> getActive();

	List<ControlDocumento> getCargas(Documento documento, Periodo periodo, boolean activo);

	List<ControlDocumento> getActiveAndStatusDistinticContabilizado();

	List<ControlDocumento> getActiveQeryWithDates(Date fechaCargaIni, Date fechaCargaFin, String nombreArchivo,
			String idDocumento, String idPeriodo);

	List<ControlDocumento> getActiveQeryWithOutDates(String nombreArchivo, String idDocumento, String idPeriodo);

	void save(ControlDocumento controlDocumento);

	boolean logicDelete(Integer id);
	
	public ControlDocumento getControlDocumento(Integer id);
	
	Generic readCsvEnviosAp(MultipartFile multiPart, Integer user);

	public boolean existeDocumento(Documento documento, Periodo periodo);
	
	public boolean isSftpHabilitado();	

}
