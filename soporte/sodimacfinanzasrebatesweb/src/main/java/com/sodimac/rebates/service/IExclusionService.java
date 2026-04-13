package com.sodimac.rebates.service;

import java.text.ParseException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.sodimac.rebates.dto.ExclusionCargaDto;
import com.sodimac.rebates.dto.ExclusionDto;
import com.sodimac.rebates.dto.ExclusionViewDetDto;
import com.sodimac.rebates.dto.PeriodoDto;
import com.sodimac.rebates.filter.ExclusionFilter;
import com.sodimac.rebates.model.DocumentoValidadorModel;

public interface IExclusionService {

	public List<ExclusionDto> getExclusiones(ExclusionFilter filter);
	
	public ExclusionDto getExclusion(Integer idExclusion);
	
	public ExclusionDto getExclusion(Integer idExclusion, String proveedor);
	
	public ExclusionDto getEvidenciaExclusion(Integer idExclusion);	
	
	public List<ExclusionViewDetDto> getExclusionesDet(ExclusionFilter filter);	
	
	public Integer getMaxFolio();
	
	public void guardar(ExclusionDto exclusion, Integer idUsuario);	
	
	public DocumentoValidadorModel<ExclusionCargaDto> leerExcel(MultipartFile multiPart);
	
	public DocumentoValidadorModel<ExclusionCargaDto> leerExcelProveedor(MultipartFile multiPart);	

	public void borradoLogico(Integer idExclusion);
	
	public void inactivar(Integer idExclusion);

	public void autorizar(Integer idExclusion, Integer idUser);
	
	public void rechazar(Integer idExclusion, Integer idUser);

	public String validaExclusion(String numProveedor, String exclusion, Integer idCatTipoExclusion, Integer idCatTipoRebate, Integer idCatPeriodo);
	
	public boolean ordenCompraPertenecePeriodo(ExclusionDto exclusion, PeriodoDto periodo) throws ParseException;

	public boolean ordenCompraDespuesPeriodo(ExclusionDto exclusion, PeriodoDto periodo) throws ParseException;

	public boolean getPerfilAutorizado(List<Integer> perfiles, Integer idTipoExclusion);

	public void modificarComentario(Integer idExclusion, String comentario);

}
