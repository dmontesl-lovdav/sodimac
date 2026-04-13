package com.sodimac.rebates.service.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.dto.ExclusionCargaDetDto;
import com.sodimac.rebates.dto.ExclusionCargaDto;
import com.sodimac.rebates.dto.ExclusionDto;
import com.sodimac.rebates.enums.EEstatus;
import com.sodimac.rebates.enums.ETipoExclusion;
import com.sodimac.rebates.mapper.ExclusionCargaDetMapper;
import com.sodimac.rebates.mapper.ExclusionCargaMapper;
import com.sodimac.rebates.model.entity.ExclusionCargaEntity;
import com.sodimac.rebates.repository.ExclusionCargaRepository;
import com.sodimac.rebates.service.IExclusionCargaDetService;
import com.sodimac.rebates.service.IExclusionCargaService;
import com.sodimac.rebates.service.IExclusionService;

@Service
public class ExclusionCargaServiceImpl implements IExclusionCargaService {

	private Logger logger = LoggerFactory.getLogger(ExclusionCargaServiceImpl.class);
	
	@Autowired
    private EntityManager em;
	
	@Autowired
	private ExclusionCargaRepository exclusionDetRepository;
	
	@Autowired
	private IExclusionService exclusionService;
	
	@Autowired
	private IExclusionCargaDetService exclusionCargaDetService;
	
	@Override
	public ExclusionCargaDto getExclusionCargaById(Long idExclusionCarga) {
		ExclusionCargaEntity entity = this.exclusionDetRepository.findByIdExclusionCarga(idExclusionCarga);
		ExclusionCargaDto dto = ExclusionCargaMapper.convertDto(entity);
		return dto;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ExclusionCargaDto> getExclusionCarga(Integer idExclusion) throws ParseException {
		List<ExclusionCargaDto> listDtos = new ArrayList<>();
		
		StringBuilder sbQry = new StringBuilder();
		sbQry.append("SELECT a.IdExclusionCarga\r\n")
			 .append("     , a.IdExclusion\r\n")
			 .append("	   , a.Carga\r\n")
			 .append("	   , a.Motivo\r\n")
			 .append("	   , b.NumProveedor\r\n")
			 .append("	   , b.NomProveedor\r\n")
			 .append("	   , b.PeriodoVigente\r\n")
			 .append("	   , min(c.FechaRecepcion)\r\n")
			 .append("	   , b.TieneAcuerdo\r\n")
			 .append("FROM [ExclusionCarga] a\r\n")
			 .append("   join [ExclusionCargaDet] b on (a.IdExclusionCarga = b.IdExclusionCarga)\r\n")
			 .append("   left join [RebateOrdenCompra] c on (c.NumeroOrdenCompra = a.Carga)\r\n")
			 .append("where a.Activo = " + EEstatus.ACTIVO.getId() + " \r\n")
			 .append("and   a.IdExclusion = " + idExclusion + "\r\n")
			 .append("group by a.IdExclusionCarga, a.IdExclusion, a.Carga, a.Motivo, b.NumProveedor, b.NomProveedor, b.PeriodoVigente, b.TieneAcuerdo");
		
		List<Object[]> resultList = this.em.createNativeQuery(sbQry.toString()).getResultList();
		listDtos = ExclusionCargaMapper.convertDtosObj(resultList);
		return listDtos;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ExclusionCargaDto> getExclusionCargaFill(Integer idExclusion) throws ParseException {
		List<ExclusionCargaDto> listDtos = new ArrayList<>();
		
		StringBuilder sbQry = new StringBuilder();
		sbQry.append("SELECT a.IdExclusionCarga\r\n")
			 .append("     , a.IdExclusion\r\n")
			 .append("	   , a.Carga\r\n")
			 .append("	   , a.Motivo\r\n")
			 .append("	   , b.NumProveedor\r\n")
			 .append("	   , b.NomProveedor\r\n")
			 .append("	   , b.PeriodoVigente\r\n")
			 .append("	   , min(c.FechaRecepcion)\r\n")
			 .append("	   , b.TieneAcuerdo\r\n")
			 .append("FROM [ExclusionCarga] a\r\n")
			 .append("   join [ExclusionCargaDet] b on (a.IdExclusionCarga = b.IdExclusionCarga)\r\n")
			 .append("   left join [RebateOrdenCompraFill] c on (c.NumeroOrdenCompra = a.Carga)\r\n")
			 .append("where a.Activo = " + EEstatus.ACTIVO.getId() + " \r\n")
			 .append("and   a.IdExclusion = " + idExclusion + "\r\n")
			 .append("group by a.IdExclusionCarga, a.IdExclusion, a.Carga, a.Motivo, b.NumProveedor, b.NomProveedor, b.PeriodoVigente, b.TieneAcuerdo");
		
		List<Object[]> resultList = this.em.createNativeQuery(sbQry.toString()).getResultList();
		listDtos = ExclusionCargaMapper.convertDtosObj(resultList);
		return listDtos;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ExclusionCargaDto> getExclusionCarga(Integer idExclusion, String proveedor) throws ParseException {
		List<ExclusionCargaDto> listDtos = new ArrayList<>();
		
		StringBuilder sbQry = new StringBuilder();
		sbQry.append("SELECT a.IdExclusionCarga\r\n")
			 .append("     , a.IdExclusion\r\n")
			 .append("	   , a.Carga\r\n")
			 .append("	   , a.Motivo\r\n")
			 .append("	   , b.NumProveedor\r\n")
			 .append("	   , b.NomProveedor\r\n")
			 .append("	   , b.PeriodoVigente\r\n")
			 .append("	   , min(c.FechaRecepcion)\r\n")
			 .append("	   , b.TieneAcuerdo\r\n")
			 .append("FROM [ExclusionCarga] a\r\n")
			 .append("   join [ExclusionCargaDet] b on (a.IdExclusionCarga = b.IdExclusionCarga)\r\n")
			 .append("   left join [RebateOrdenCompra] c on (c.NumeroOrdenCompra = a.Carga)\r\n")
			 .append("where a.Activo = " + EEstatus.ACTIVO.getId() + " \r\n")
			 .append("and   a.IdExclusion = " + idExclusion + "\r\n")
			 .append("and   b.NumProveedor = '" + proveedor + "'\r\n")
			 .append("group by a.IdExclusionCarga, a.IdExclusion, a.Carga, a.Motivo, b.NumProveedor, b.NomProveedor, b.PeriodoVigente, b.TieneAcuerdo");
		
		List<Object[]> resultList = this.em.createNativeQuery(sbQry.toString()).getResultList();
		listDtos = ExclusionCargaMapper.convertDtosObj(resultList);
		return listDtos;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ExclusionCargaDto> getExclusionCargaFill(Integer idExclusion, String proveedor) throws ParseException {
		List<ExclusionCargaDto> listDtos = new ArrayList<>();
		
		StringBuilder sbQry = new StringBuilder();
		sbQry.append("SELECT a.IdExclusionCarga\r\n")
			 .append("     , a.IdExclusion\r\n")
			 .append("	   , a.Carga\r\n")
			 .append("	   , a.Motivo\r\n")
			 .append("	   , b.NumProveedor\r\n")
			 .append("	   , b.NomProveedor\r\n")
			 .append("	   , b.PeriodoVigente\r\n")
			 .append("	   , min(c.FechaRecepcion)\r\n")
			 .append("	   , b.TieneAcuerdo\r\n")
			 .append("FROM [ExclusionCarga] a\r\n")
			 .append("   join [ExclusionCargaDet] b on (a.IdExclusionCarga = b.IdExclusionCarga)\r\n")
			 .append("   left join [RebateOrdenCompraFill] c on (c.NumeroOrdenCompra = a.Carga)\r\n")
			 .append("where a.Activo = " + EEstatus.ACTIVO.getId() + " \r\n")
			 .append("and   a.IdExclusion = " + idExclusion + "\r\n")
			 .append("and   b.NumProveedor = '" + proveedor + "'\r\n")
			 .append("group by a.IdExclusionCarga, a.IdExclusion, a.Carga, a.Motivo, b.NumProveedor, b.NomProveedor, b.PeriodoVigente, b.TieneAcuerdo");
		
		List<Object[]> resultList = this.em.createNativeQuery(sbQry.toString()).getResultList();
		listDtos = ExclusionCargaMapper.convertDtosObj(resultList);
		return listDtos;
	}

	@Override
	@Transactional
	public void guardar(ExclusionCargaDto detDto) {
		ExclusionCargaEntity entity = ExclusionCargaMapper.convertEntity(detDto);
		this.exclusionDetRepository.save(entity);
		detDto.setIdExclusionCarga( entity.getIdExclusionCarga() );
	}

	@Override
	@Transactional
	public void guardarJson(ExclusionCargaDto detDto) {
		logger.info(detDto.getJsonId());
		ExclusionDto exclusion = this.exclusionService.getExclusion(detDto.getIdExclusion());
		List<ExclusionCargaDetDto> listExclusionCarga = ExclusionCargaDetMapper.convertJson(detDto.getJsonId());
		if (listExclusionCarga != null) {
			for (ExclusionCargaDetDto cargaDet : listExclusionCarga) {
				
				ExclusionCargaEntity entity = ExclusionCargaMapper.convertEntity(detDto);
				
				Integer idCatPeriodo = exclusion.getPeriodo().getIdCatPeriodo();
				int idCatTipoExclusion = exclusion.getCatTipoExclusion().getIdCatTipoExclusion().intValue();
				if (idCatTipoExclusion == ETipoExclusion.PROVEEDORES.getId()) {
					
					entity.setCarga(cargaDet.getNumProveedor());
					this.exclusionDetRepository.save(entity);
					Long idExclusionCarga = entity.getIdExclusionCarga();
					
					this.exclusionCargaDetService.registraExclusionProveedorManual(idCatPeriodo, idExclusionCarga
							, cargaDet.getNumProveedor());
					
				} else if (idCatTipoExclusion == ETipoExclusion.ORDEN_COMPRA.getId()) {
					
					entity.setCarga(cargaDet.getOrdenCompra());
					this.exclusionDetRepository.save(entity);
					Long idExclusionCarga = entity.getIdExclusionCarga();
					
					this.exclusionCargaDetService.registraExclusionOrdenCompraManual(idExclusionCarga
							, cargaDet.getNumProveedor(), cargaDet.getOrdenCompra());
					
				} else if (idCatTipoExclusion == ETipoExclusion.FAMILIA.getId()) {
					
					entity.setCarga(cargaDet.getClacom());
					this.exclusionDetRepository.save(entity);
					Long idExclusionCarga = entity.getIdExclusionCarga();
					
					this.exclusionCargaDetService.registraExclusionFamiliaManual(idCatPeriodo, idExclusionCarga
							, cargaDet.getNumProveedor(), cargaDet.getClacom());
	
				} else if (idCatTipoExclusion == ETipoExclusion.SKU.getId()) {
					entity.setCarga(cargaDet.getSku());
					this.exclusionDetRepository.save(entity);
					Long idExclusionCarga = entity.getIdExclusionCarga();
					
					this.exclusionCargaDetService.registraExclusionSKUManual(idCatPeriodo, idExclusionCarga
							, cargaDet.getNumProveedor()
							, cargaDet.getClacom()
							, cargaDet.getOrdenCompra()
							, cargaDet.getSku());
				}
			}
		}
	}

	@Override
	public void borradoLogico(Long idExclusionCarga) {
		ExclusionCargaEntity entity = this.exclusionDetRepository.findByIdExclusionCarga(idExclusionCarga);
		entity.setActivo(false);
		this.exclusionDetRepository.save(entity);
	}	

}
