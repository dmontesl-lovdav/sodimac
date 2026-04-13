package com.sodimac.rebates.service;

import java.util.List;

import com.sodimac.rebates.dto.ExclusionCargaDetDto;
import com.sodimac.rebates.filter.ExclusionCargaFilter;

public interface IExclusionCargaDetService {

	public void registraExclusionProveedor(Long pIdCargaExclusion, String pNumProveedor, Integer pIdPeriodo, Integer pIdCatTipoRebate, Integer idUser);

	public void registraExclusionOrdenCompra(Long pIdCargaExclusion, String pOrdenCompra, Integer pIdPeriodo, Integer pIdCatTipoRebate, Integer idUser);

	public void registraExclusionFamilia(Long pIdCargaExclusion, String pNumProveedor, String pFamilia, Integer pIdPeriodo, Integer pIdCatTipoRebate, Integer idUser);

	public void registraExclusionSKU(Long pIdCargaExclusion, String pNumProveedor, String pSku, Integer pIdPeriodo, Integer pIdCatTipoRebate, Integer idUser);
	
	public void registraExclusionProveedorManual(Integer pIdCatPeriodo, Long pIdCargaExclusion, String pNumProveedor);

	public void registraExclusionOrdenCompraManual(Long pIdCargaExclusion, String pNumProveedor, String pOrdenCompra);
	
	public void registraExclusionFamiliaManual(Integer pIdCatPeriodo, Long pIdCargaExclusion, String pNumProveedor, String pFamilia);
	
	public void registraExclusionSKUManual(Integer pIdCatPeriodo, Long pIdCargaExclusion, String pNumProveedor, String pFamilia, String pOrdenCompra, String pSku);

	public List<ExclusionCargaDetDto> obtenerProveedores(ExclusionCargaFilter filter);

	public List<ExclusionCargaDetDto> obtenerOrdenesCompra(ExclusionCargaFilter filter);

	public List<ExclusionCargaDetDto> obtenerFamilias(ExclusionCargaFilter filter);

	public List<ExclusionCargaDetDto> obtenerSkus(ExclusionCargaFilter filter);
	
	public List<ExclusionCargaDetDto> obtenerSkusUnicos(ExclusionCargaFilter filter);
	
	public void borradoLogico(Long idExclusionCargaDet);

	public String existeExclusionPeriodo(String exclusion, Integer idCatTipoExclusion, Integer idCatTipoRebate, Integer idCatPeriodo, String numProveedor);

	public int obtenerPeriodoCarga(String exclusion, Integer idCatTipoExclusion, Integer idCatPeriodo);

		

}
