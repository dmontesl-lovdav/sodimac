package com.sodimac.rebates.service;

import java.text.ParseException;
import java.util.List;

import com.sodimac.rebates.dto.ExclusionCargaDto;

public interface IExclusionCargaService {
	
	public ExclusionCargaDto getExclusionCargaById(Long idExclusionCarga);
	
	public List<ExclusionCargaDto> getExclusionCarga(Integer idExclusion) throws ParseException;
	
	public List<ExclusionCargaDto> getExclusionCarga(Integer idExclusion, String proveedor) throws ParseException;

	public void guardar(ExclusionCargaDto detDto);
	
	public void guardarJson(ExclusionCargaDto detDto);

	public void borradoLogico(Long idExclusionCarga);

	public List<ExclusionCargaDto> getExclusionCargaFill(Integer idExclusion) throws ParseException;

	public List<ExclusionCargaDto> getExclusionCargaFill(Integer idExclusion, String proveedor) throws ParseException;	
	
}
