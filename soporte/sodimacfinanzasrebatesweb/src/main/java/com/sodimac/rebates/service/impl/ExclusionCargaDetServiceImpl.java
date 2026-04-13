package com.sodimac.rebates.service.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.dto.ExclusionCargaDetDto;
import com.sodimac.rebates.enums.EEstatus;
import com.sodimac.rebates.enums.ETipoExclusion;
import com.sodimac.rebates.filter.ExclusionCargaFilter;
import com.sodimac.rebates.mapper.ExclusionCargaDetMapper;
import com.sodimac.rebates.model.entity.ExclusionCargaDetEntity;
import com.sodimac.rebates.repository.ExclusionCargaDetRepository;
import com.sodimac.rebates.service.IExclusionCargaDetService;

@Service
public class ExclusionCargaDetServiceImpl implements IExclusionCargaDetService {

	@Autowired
    private EntityManager em;
	
	@Autowired
	private ExclusionCargaDetRepository cargaDetRepository;
	
	@Override
	@Transactional
	public void registraExclusionProveedor(Long pIdCargaExclusion, String pNumProveedor, Integer pIdPeriodo, Integer pIdCatTipoRebate, Integer idUser) {
		this.cargaDetRepository.registraExclusionProveedor(pIdCargaExclusion, pNumProveedor, pIdPeriodo, pIdCatTipoRebate, idUser);
	}
	
	
	@Override
	@Transactional
	public void registraExclusionOrdenCompra(Long pIdCargaExclusion, String pOrdenCompra, Integer pIdPeriodo, Integer pIdCatTipoRebate, Integer idUser) {
		this.cargaDetRepository.registraExclusionOrdenCompra(pIdCargaExclusion, pOrdenCompra, pIdPeriodo, pIdCatTipoRebate, idUser);
	}
	
	@Override
	@Transactional
	public void registraExclusionFamilia(Long pIdCargaExclusion, String pNumProveedor, String pFamilia, Integer pIdPeriodo, Integer pIdCatTipoRebate, Integer idUser) {
		this.cargaDetRepository.registraExclusionFamilia(pIdCargaExclusion, pNumProveedor, pFamilia, pIdPeriodo, pIdCatTipoRebate, idUser);
	}
	
	@Override
	@Transactional
	public void registraExclusionSKU(Long pIdCargaExclusion, String pNumProveedor, String pSku, Integer pIdPeriodo, Integer pIdCatTipoRebate, Integer idUser) {
		this.cargaDetRepository.registraExclusionSKU(pIdCargaExclusion, pNumProveedor, pSku, pIdPeriodo, pIdCatTipoRebate, idUser);
	}
	
	@Override
	@Transactional
	public void registraExclusionProveedorManual(Integer pIdCatPeriodo, Long pIdCargaExclusion, String pNumProveedor) {
		this.cargaDetRepository.registraExclusionProveedorManual(pIdCatPeriodo, pIdCargaExclusion, pNumProveedor);
	}


	@Override
	@Transactional
	public void registraExclusionOrdenCompraManual(Long pIdCargaExclusion, String pNumProveedor, String pOrdenCompra) {
		this.cargaDetRepository.registraExclusionOrdenCompraManual(pIdCargaExclusion, pNumProveedor, pOrdenCompra);
	}


	@Override
	@Transactional
	public void registraExclusionFamiliaManual(Integer pIdCatPeriodo, Long pIdCargaExclusion, String pNumProveedor, String pFamilia) {
		this.cargaDetRepository.registraExclusionFamiliaManual(pIdCatPeriodo, pIdCargaExclusion, pNumProveedor, pFamilia);
	}


	@Override
	@Transactional
	public void registraExclusionSKUManual(Integer pIdCatPeriodo, Long pIdCargaExclusion, String pNumProveedor, String pFamilia,
			String pOrdenCompra, String pSku) {
		this.cargaDetRepository.uspRegistraExclusionSKUManual(pIdCatPeriodo, pIdCargaExclusion, pNumProveedor, pFamilia, pOrdenCompra, pSku);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ExclusionCargaDetDto> obtenerProveedores(ExclusionCargaFilter filter) {
		List<ExclusionCargaDetDto> listDto = null;
		StringBuilder sbQry = new StringBuilder();
			sbQry.append("select excd.NumProveedor, excd.NomProveedor \r\n")
				.append("FROM vw_exclusiones excd\r\n")
				.append("WHERE excd.IdExclusion = " + filter.getIdExclusion() + "\r\n")
				.append("AND   excd.Activo = " + EEstatus.ACTIVO.getId() + " \r\n");
				
			if (filter.getIdExclusionCarga() != null && filter.getIdExclusionCarga().intValue() > 0) {
				sbQry.append("AND   excd.IdExclusionCarga = " + filter.getIdExclusionCarga() + "\r\n");
			}
			sbQry.append("GROUP BY excd.NumProveedor, excd.NomProveedor");
			
		List<Object[]> resultList = this.em.createNativeQuery(sbQry.toString()).getResultList();
		listDto = ExclusionCargaDetMapper.convertDtoProveedores(resultList);
		return listDto;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ExclusionCargaDetDto> obtenerOrdenesCompra(ExclusionCargaFilter filter) {
		List<ExclusionCargaDetDto> listDto = null;
		StringBuilder sbQry = new StringBuilder();
		sbQry.append("select excd.NumProveedor, excd.NomProveedor, excd.OrdenCompra\r\n")
			.append("FROM vw_exclusiones excd\r\n")
			.append("WHERE excd.IdExclusion = " + filter.getIdExclusion() + "\r\n")
			.append("AND   excd.Activo = " + EEstatus.ACTIVO.getId() + " \r\n");
			
		if (filter.getIdExclusionCarga() != null && filter.getIdExclusionCarga().intValue() > 0) {
			sbQry.append("AND   excd.IdExclusionCarga = " + filter.getIdExclusionCarga() + "\r\n");
		}
		if (filter.getNumProveedor() != null && !filter.getNumProveedor().isEmpty()) {
			sbQry.append("AND   excd.NumProveedor = '" + filter.getNumProveedor() + "'\r\n");
		}
		
		sbQry.append("GROUP BY excd.NumProveedor, excd.NomProveedor, excd.OrdenCompra");
			
		List<Object[]> resultList = this.em.createNativeQuery(sbQry.toString()).getResultList();
		listDto = ExclusionCargaDetMapper.convertDtoOrdenCompra(resultList);
		return listDto;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ExclusionCargaDetDto> obtenerFamilias(ExclusionCargaFilter filter) {
		List<ExclusionCargaDetDto> listDto = null;
		StringBuilder sbQry = new StringBuilder();
		sbQry.append("select excd.NumProveedor, excd.OrdenCompra, excd.Clacom\r\n")
			.append("FROM vw_exclusiones excd\r\n")
			.append("WHERE excd.IdExclusion = " + filter.getIdExclusion() + "\r\n")
			.append("AND   excd.Activo = " + EEstatus.ACTIVO.getId() + " \r\n");
			
		if (filter.getIdExclusionCarga() != null && filter.getIdExclusionCarga().intValue() > 0) {
			sbQry.append("AND   excd.IdExclusionCarga = " + filter.getIdExclusionCarga() + "\r\n");
		}
		if (filter.getNumProveedor() != null && !filter.getNumProveedor().isEmpty()) {
			sbQry.append("AND   excd.NumProveedor = '" + filter.getNumProveedor() + "'\r\n");
		}
		if (filter.getOrdenCompra() != null && !filter.getOrdenCompra().isEmpty()) {
			sbQry.append("AND   excd.OrdenCompra = '" + filter.getOrdenCompra() + "'\r\n");
		}
		sbQry.append("GROUP BY excd.NumProveedor, excd.OrdenCompra, excd.Clacom");
			
		List<Object[]> resultList = this.em.createNativeQuery(sbQry.toString()).getResultList();
		listDto = ExclusionCargaDetMapper.convertDtoFamilia(resultList); 
		return listDto;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ExclusionCargaDetDto> obtenerSkus(ExclusionCargaFilter filter) {
		List<ExclusionCargaDetDto> listDto = null;
		StringBuilder sbQry = new StringBuilder();
		sbQry.append("select excd.IdExclusionCargaDet\r\n")
			.append("     , excd.IdExclusionCarga\r\n")
			.append("	 , excd.NumProveedor\r\n")
			.append("	 , excd.NomProveedor\r\n")
			.append("	 , excd.OrdenCompra\r\n")
			.append("	 , excd.Clacom\r\n")
			.append("	 , excd.Sku\r\n")
			.append("	 , excd.SkuDescripcion\r\n")
			.append("	 , excd.Activo\r\n")
			.append("	 , excd.Motivo\r\n")
			.append("	 , excd.CantidadOrdenada\r\n")
			.append("	 , excd.CantidadRecibida\r\n")
			.append("FROM vw_exclusiones excd\r\n")
			.append("WHERE excd.IdExclusion = " + filter.getIdExclusion() + "\r\n")
			.append("AND   excd.Activo = " + EEstatus.ACTIVO.getId() + " \r\n");
		
		if (filter.getIdExclusionCarga() != null && filter.getIdExclusionCarga().intValue() > 0) {
			sbQry.append("AND   excd.IdExclusionCarga = " + filter.getIdExclusionCarga() + "\r\n");
		}
		if (filter.getNumProveedor() != null && !filter.getNumProveedor().isEmpty()) {
			sbQry.append("AND   excd.NumProveedor = '" + filter.getNumProveedor() + "'\r\n");
		}
		if (filter.getOrdenCompra() != null && !filter.getOrdenCompra().isEmpty()) {
			sbQry.append("AND   excd.OrdenCompra = '" + filter.getOrdenCompra() + "'\r\n");
		}
		if (filter.getClacom() != null && !filter.getClacom().isEmpty()) {
			sbQry.append("AND   excd.Clacom = '" + filter.getClacom() + "' \n\r");
		}
		if (filter.getSku() != null && !filter.getSku().isEmpty()) {
			sbQry.append("AND   excd.Sku = '" + filter.getSku() + "' \n\r");
		}
		
		List<Object[]> resultList = this.em.createNativeQuery(sbQry.toString()).getResultList();
		listDto = ExclusionCargaDetMapper.convertDtoSku(resultList);
		return listDto;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ExclusionCargaDetDto> obtenerSkusUnicos(ExclusionCargaFilter filter) {
		List<ExclusionCargaDetDto> listDto = null;
		StringBuilder sbQry = new StringBuilder();
		sbQry.append("select distinct excd.Sku\r\n")
			.append("	 , excd.SkuDescripcion\r\n")
			.append("FROM vw_exclusiones excd\r\n")
			.append("WHERE excd.IdExclusion = " + filter.getIdExclusion() + "\r\n")
			.append("AND   excd.Activo = " + EEstatus.ACTIVO.getId() + " \r\n");
		
		if (filter.getIdExclusionCarga() != null && filter.getIdExclusionCarga().intValue() > 0) {
			sbQry.append("AND   excd.IdExclusionCarga = " + filter.getIdExclusionCarga() + "\r\n");
		}
		if (filter.getNumProveedor() != null && !filter.getNumProveedor().isEmpty()) {
			sbQry.append("AND   excd.NumProveedor = '" + filter.getNumProveedor() + "'\r\n");
		}
		if (filter.getOrdenCompra() != null && !filter.getOrdenCompra().isEmpty()) {
			sbQry.append("AND   excd.OrdenCompra = '" + filter.getOrdenCompra() + "'\r\n");
		}
		if (filter.getClacom() != null && !filter.getClacom().isEmpty()) {
			sbQry.append("AND   excd.Clacom = '" + filter.getClacom() + "'");
		}
			
		List<Object[]> resultList = this.em.createNativeQuery(sbQry.toString()).getResultList();
		listDto = ExclusionCargaDetMapper.convertDtoSkuUnicos(resultList);
		return listDto;
	}


	@Override
	public void borradoLogico(Long idExclusionCargaDet) {
		ExclusionCargaDetEntity entity = this.cargaDetRepository.findByIdExclusionCargaDet(idExclusionCargaDet);
		entity.setActivo(false);
		this.cargaDetRepository.save(entity);
		
	}


	@Override
	public String existeExclusionPeriodo(String exclusion, Integer idCatTipoExclusion, Integer idCatTipoRebate, Integer idCatPeriodo, String numProveedor) {
		StringBuilder sbQry = new StringBuilder();
		sbQry.append("select isnull(Min(excd.Folio),'') \r\n")
			 .append("FROM vw_exclusiones excd\r\n")
			 .append("WHERE excd.Activo = " + EEstatus.ACTIVO.getId() + " \r\n");
		
		if (idCatTipoExclusion.intValue() == ETipoExclusion.PROVEEDORES.getId()) {
			sbQry.append("AND   excd.IdCatPeriodo = " + idCatPeriodo + "\r\n")
				 .append("AND   excd.NumProveedor = '" + exclusion + "'\r\n")
				 .append("AND   excd.IdCatTipoRebate = " + idCatTipoRebate + "\r\n");
			
		}
		if (idCatTipoExclusion.intValue() == ETipoExclusion.ORDEN_COMPRA.getId()) {
			sbQry.append("AND   excd.OrdenCompra = '" + exclusion + "'\r\n")
				 .append("AND   excd.IdCatTipoRebate = " + idCatTipoRebate + "\r\n");
		}
		if (idCatTipoExclusion.intValue() == ETipoExclusion.FAMILIA.getId()) {
			sbQry.append("AND   excd.IdCatPeriodo = " + idCatPeriodo + "\r\n")
				  .append("AND   excd.Clacom = '" + exclusion + "' \n\r")
				  .append("AND   excd.IdCatTipoRebate = " + idCatTipoRebate + "\r\n");
			
			if (numProveedor != null && !numProveedor.isEmpty()) {
				sbQry.append("AND   excd.NumProveedor = '" + numProveedor + "' \n\r");
			}
		}
		if (idCatTipoExclusion.intValue() == ETipoExclusion.SKU.getId()) {
			sbQry.append("AND   excd.IdCatPeriodo = " + idCatPeriodo + "\r\n")
				 .append("AND   excd.Sku = '" + exclusion + "' \n\r")
				 .append("AND   excd.IdCatTipoRebate = " + idCatTipoRebate + "\r\n");
			
			if (numProveedor != null && !numProveedor.isEmpty()) {
				sbQry.append("AND   excd.NumProveedor = '" + numProveedor + "' \n\r");
			}
		}
		
		return this.em.createNativeQuery(sbQry.toString()).getSingleResult().toString();
	}
	
	@Override
	public int obtenerPeriodoCarga(String exclusion, Integer idCatTipoExclusion, Integer idCatPeriodo) {
		StringBuilder sbQry = new StringBuilder();
		sbQry.append("select top 1 excd.IdCatPeriodo \r\n")
			 .append("FROM vw_exclusiones excd\r\n")
			 .append("WHERE excd.Activo = " + EEstatus.ACTIVO.getId() + " \r\n");
		
		if (idCatTipoExclusion.intValue() == ETipoExclusion.PROVEEDORES.getId()) {
			sbQry.append("AND   excd.IdCatPeriodo = " + idCatPeriodo + "\r\n")
				 .append("AND   excd.NumProveedor = '" + exclusion + "'\r\n");
		}
		if (idCatTipoExclusion.intValue() == ETipoExclusion.ORDEN_COMPRA.getId()) {
			sbQry.append("AND   excd.OrdenCompra = '" + exclusion + "'\r\n");
		}
		if (idCatTipoExclusion.intValue() == ETipoExclusion.FAMILIA.getId()) {
			sbQry.append("AND   excd.IdCatPeriodo = " + idCatPeriodo + "\r\n")
				  .append("AND   excd.Clacom = '" + exclusion + "' \n\r");
		}
		if (idCatTipoExclusion.intValue() == ETipoExclusion.SKU.getId()) {
			sbQry.append("AND   excd.IdCatPeriodo = " + idCatPeriodo + "\r\n")
				 .append("AND   excd.Sku = '" + exclusion + "' \n\r");
		}
		
		int idPeriodo = ((Number) this.em.createNativeQuery(sbQry.toString()).getSingleResult()).intValue();
		return idPeriodo;
	}
}
