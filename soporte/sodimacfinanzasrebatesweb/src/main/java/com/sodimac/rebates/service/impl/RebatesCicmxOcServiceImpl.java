package com.sodimac.rebates.service.impl;

import java.text.ParseException;
import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.dto.ExclusionDto;
import com.sodimac.rebates.dto.RebatesCicmxOcDto;
import com.sodimac.rebates.enums.ETipoExclusion;
import com.sodimac.rebates.filter.ExclusionCargaFilter;
import com.sodimac.rebates.mapper.RebatesCicmxOcMapper;
import com.sodimac.rebates.repository.ExclusionRepository;
import com.sodimac.rebates.service.IPeriodoService;
import com.sodimac.rebates.service.IRebatesCicmxOcService;

@Service
public class RebatesCicmxOcServiceImpl implements IRebatesCicmxOcService {

	private static final Integer FILL_RATE = 8;

	@Autowired
	private EntityManager em;
	
	@Autowired
	private IPeriodoService periodoService;
	
	@Autowired
	private ExclusionRepository exclusionRepository;
	
	@SuppressWarnings("unchecked")
	@Override
	public List<RebatesCicmxOcDto> obtenerProveedoresDisponibles(ExclusionCargaFilter filter) {
		List<RebatesCicmxOcDto> listDto = null;
		StringBuilder sbQry = new StringBuilder();
		
		boolean periodoTodos = this.periodoService.isPeriodoTodos(filter.getIdPeriodoCat());
		if (periodoTodos) {
			sbQry.append("SELECT top 100 NUM_PROVEEDOR, NOM_PROVEEDOR\r\n")
			.append("FROM [dbo].[vw_rebate_catalogo]\r\n")
			.append("WHERE NUM_PROVEEDOR not in ( SELECT distinct vw.NumProveedor FROM vw_exclusiones vw WHERE vw.Activo = 1 AND vw.IdCatPeriodo=" + filter.getIdPeriodoCat() + " and vw.idCatTipoRebate="+ filter.getIdCatTipoRebate() + " )\r\n")
			.append("GROUP BY NUM_PROVEEDOR, NOM_PROVEEDOR \r\n")
			.append("ORDER BY NUM_PROVEEDOR");
		} else {
			sbQry.append("SELECT top 100 NUM_PROVEEDOR, NOM_PROVEEDOR\r\n")
			.append("FROM [dbo].[vw_rebate_orden_compra]\r\n")
			.append("WHERE NUM_PROVEEDOR not in ( SELECT distinct vw.NumProveedor FROM vw_exclusiones vw WHERE vw.Activo = 1 AND vw.IdCatPeriodo=" + filter.getIdPeriodoCat() + " and vw.idCatTipoRebate="+ filter.getIdCatTipoRebate() + " )\r\n")
			.append("GROUP BY NUM_PROVEEDOR, NOM_PROVEEDOR \r\n")
			.append("ORDER BY NUM_PROVEEDOR");
		}
			
		List<Object[]> resultList = this.em.createNativeQuery(sbQry.toString()).getResultList();
		listDto = RebatesCicmxOcMapper.convertDtoProveedores(resultList);
		return listDto;
	}
	

	@Override
	public List<RebatesCicmxOcDto> obtenerOrdenesCompraDisponible(ExclusionCargaFilter filter) throws ParseException {
		List<RebatesCicmxOcDto> listDto = null;
//		StringBuilder sbQry = new StringBuilder();
//		sbQry.append("SELECT NUM_PROVEEDOR, NOM_PROVEEDOR, NUM_OC\r\n")
//			.append("FROM [dbo].[vw_rebate_orden_compra]\r\n")
//			.append("WHERE 1=1\r\n");
//			if (filter.getNumProveedor() != null && !filter.getNumProveedor().isEmpty()) {
//				sbQry.append("AND   CAST(NUM_PROVEEDOR AS varchar) = '" + filter.getNumProveedor() + "'\r\n");
//			}
//			//sbQry.append("AND   NUM_OC not in ( SELECT distinct vw.OrdenCompra FROM vw_exclusiones vw WHERE vw.Activo = 1 AND vw.IdCatPeriodo=" + filter.getIdPeriodoCat() + ")\r\n")
//			sbQry.append("AND   NUM_OC not in ( SELECT distinct vw.OrdenCompra FROM vw_exclusiones vw WHERE vw.Activo = 1 and vw.idCatTipoRebate=" + filter.getIdCatTipoRebate() + ")\r\n")
//			.append("GROUP BY NUM_PROVEEDOR, NOM_PROVEEDOR, NUM_OC \r\n")
//			.append("ORDER BY NUM_PROVEEDOR, NUM_OC");

		//List<Object[]> resultList = this.em.createNativeQuery(sbQry.toString()).getResultList();
		
		List<Object[]> resultList = exclusionRepository.getOrdenesCompraDisponibles(filter.getIdUsuario(), filter.getIdPeriodoCat(), filter.getIdCatTipoRebate());
		listDto = RebatesCicmxOcMapper.convertDtoOrdenCompra(resultList);
		return listDto;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<RebatesCicmxOcDto> obtenerFamiliaDisponible(ExclusionCargaFilter filter) {
		List<RebatesCicmxOcDto> listDto = null;
		StringBuilder sbQry = new StringBuilder();
		
		boolean periodoTodos = this.periodoService.isPeriodoTodos(filter.getIdPeriodoCat());
		if (periodoTodos) {
			sbQry.append("SELECT NUM_PROVEEDOR, NOM_PROVEEDOR, FAMILIA, COUNT(1)\r\n")
			.append("FROM [dbo].[vw_rebate_catalogo]\r\n")
			.append("WHERE 1=1\r\n");
			if (filter.getNumProveedor() != null && !filter.getNumProveedor().isEmpty()) {
				sbQry.append("AND   CAST(NUM_PROVEEDOR AS varchar) = '" + filter.getNumProveedor() + "'\r\n");
			}
			if (filter.getOrdenCompra() != null && !filter.getOrdenCompra().isEmpty()) {
				sbQry.append("AND   CAST(NUM_OC AS varchar) = '" + filter.getOrdenCompra() + "'\r\n");
			}
			sbQry.append("AND   FAMILIA not in ( SELECT distinct vw.Clacom FROM vw_exclusiones vw WHERE vw.Activo = 1 AND vw.IdCatPeriodo=" + filter.getIdPeriodoCat() + " and vw.idCatTipoRebate="+ filter.getIdCatTipoRebate() +")\r\n")
			.append("GROUP BY NUM_PROVEEDOR,NOM_PROVEEDOR, FAMILIA\r\n")
			.append("ORDER BY FAMILIA");
		} else {
			sbQry.append("SELECT NUM_PROVEEDOR, NOM_PROVEEDOR, FAMILIA, COUNT(1)\r\n")
			.append("FROM [dbo].[vw_rebate_orden_compra]\r\n")
			.append("WHERE 1=1\r\n");
			if (filter.getNumProveedor() != null && !filter.getNumProveedor().isEmpty()) {
				sbQry.append("AND   CAST(NUM_PROVEEDOR AS varchar) = '" + filter.getNumProveedor() + "'\r\n");
			}
			if (filter.getOrdenCompra() != null && !filter.getOrdenCompra().isEmpty()) {
				sbQry.append("AND   CAST(NUM_OC AS varchar) = '" + filter.getOrdenCompra() + "'\r\n");
			}
			sbQry.append("AND   FAMILIA not in ( SELECT distinct vw.Clacom FROM vw_exclusiones vw WHERE vw.Activo = 1 AND vw.IdCatPeriodo=" + filter.getIdPeriodoCat() + " and vw.idCatTipoRebate="+ filter.getIdCatTipoRebate() +")\r\n")
			.append("GROUP BY NUM_PROVEEDOR,NOM_PROVEEDOR, FAMILIA\r\n")
			.append("ORDER BY FAMILIA");
		}
			
		List<Object[]> resultList = this.em.createNativeQuery(sbQry.toString()).getResultList();
		listDto = RebatesCicmxOcMapper.convertDtoFamilia(resultList);
		return listDto;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<RebatesCicmxOcDto> obtenerFamiliaUnicaDisponible(ExclusionCargaFilter filter) {
		List<RebatesCicmxOcDto> listDto = null;
		StringBuilder sbQry = new StringBuilder();
		boolean periodoTodos = this.periodoService.isPeriodoTodos(filter.getIdPeriodoCat());
		if (periodoTodos) {
			
			sbQry.append("SELECT FAMILIA, 1\r\n")
			.append("FROM [dbo].[vw_rebate_catalogo]\r\n")
			.append("WHERE 1=1\r\n");
			if (filter.getNumProveedor() != null && !filter.getNumProveedor().isEmpty()) {
				sbQry.append("AND   CAST(NUM_PROVEEDOR AS varchar) = '" + filter.getNumProveedor() + "'\r\n");
			}
			if (filter.getOrdenCompra() != null && !filter.getOrdenCompra().isEmpty()) {
				sbQry.append("AND   CAST(NUM_OC AS varchar) = '" + filter.getOrdenCompra() + "'\r\n");
			}
			sbQry.append("AND   FAMILIA not in ( SELECT distinct vw.Clacom FROM vw_exclusiones vw WHERE vw.Activo = 1 AND vw.IdCatPeriodo=" + filter.getIdPeriodoCat() + " and vw.idCatTipoRebate="+ filter.getIdCatTipoRebate() + ")\r\n")
			.append("GROUP BY FAMILIA\r\n")
			.append("ORDER BY FAMILIA");
			
		} else {
			sbQry.append("SELECT FAMILIA, 1\r\n")
			.append("FROM [dbo].[vw_rebate_orden_compra]\r\n")
			.append("WHERE 1=1\r\n");
			if (filter.getNumProveedor() != null && !filter.getNumProveedor().isEmpty()) {
				sbQry.append("AND   CAST(NUM_PROVEEDOR AS varchar) = '" + filter.getNumProveedor() + "'\r\n");
			}
			if (filter.getOrdenCompra() != null && !filter.getOrdenCompra().isEmpty()) {
				sbQry.append("AND   CAST(NUM_OC AS varchar) = '" + filter.getOrdenCompra() + "'\r\n");
			}
			sbQry.append("AND   FAMILIA not in ( SELECT distinct vw.Clacom FROM vw_exclusiones vw WHERE vw.Activo = 1 AND vw.IdCatPeriodo=" + filter.getIdPeriodoCat() + " and vw.idCatTipoRebate="+ filter.getIdCatTipoRebate() + ")\r\n")
			.append("GROUP BY FAMILIA\r\n")
			.append("ORDER BY FAMILIA");
		}
			
		List<Object[]> resultList = this.em.createNativeQuery(sbQry.toString()).getResultList();
		listDto = RebatesCicmxOcMapper.convertDtoFamiliaUnica(resultList);
		return listDto;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<RebatesCicmxOcDto> obtenerSkuDisponible(ExclusionCargaFilter filter) {
		List<RebatesCicmxOcDto> listDto = null;
		StringBuilder sbQry = new StringBuilder();
		
		boolean periodoTodos = this.periodoService.isPeriodoTodos(filter.getIdPeriodoCat());
		if (periodoTodos) {
			sbQry.append("SELECT NUM_OC, NUM_PROVEEDOR, NOM_PROVEEDOR, FAMILIA, SKU, SKU_DESCRIPCION, COUNT(1)\r\n")
			.append("FROM [dbo].[vw_rebate_catalogo]\r\n")
			.append("WHERE 1=1\r\n");
		
			if (filter.getNumProveedor() != null && !filter.getNumProveedor().isEmpty()) {
				sbQry.append("AND   CAST(NUM_PROVEEDOR AS varchar) = '" + filter.getNumProveedor() + "'\r\n");
			}
			if (filter.getOrdenCompra() != null && !filter.getOrdenCompra().isEmpty()) {
				sbQry.append("AND   CAST(NUM_OC AS varchar) = '" + filter.getOrdenCompra() + "'\r\n");
			}
			if (filter.getClacom() != null && !filter.getClacom().isEmpty()) {
				sbQry.append("AND   FAMILIA = '" + filter.getClacom() + "'\r\n");
			}	
				
			sbQry.append("AND   SKU not in ( SELECT distinct vw.Sku FROM vw_exclusiones vw WHERE vw.Activo = 1 AND vw.IdCatPeriodo=" + filter.getIdPeriodoCat() + " and vw.idCatTipoRebate="+ filter.getIdCatTipoRebate() + ")\r\n")
				.append("GROUP BY NUM_OC,NUM_PROVEEDOR, NOM_PROVEEDOR, FAMILIA,SKU,SKU_DESCRIPCION\r\n")
				.append("ORDER BY SKU");
		} else {
			
			sbQry.append("SELECT NUM_OC, NUM_PROVEEDOR, NOM_PROVEEDOR, FAMILIA, SKU, SKU_DESCRIPCION, COUNT(1)\r\n")
			.append("FROM [dbo].[vw_rebate_orden_compra]\r\n")
			.append("WHERE 1=1\r\n");
		
			if (filter.getNumProveedor() != null && !filter.getNumProveedor().isEmpty()) {
				sbQry.append("AND   CAST(NUM_PROVEEDOR AS varchar) = '" + filter.getNumProveedor() + "'\r\n");
			}
			if (filter.getOrdenCompra() != null && !filter.getOrdenCompra().isEmpty()) {
				sbQry.append("AND   CAST(NUM_OC AS varchar) = '" + filter.getOrdenCompra() + "'\r\n");
			}
			if (filter.getClacom() != null && !filter.getClacom().isEmpty()) {
				sbQry.append("AND   FAMILIA = '" + filter.getClacom() + "'\r\n");
			}	
				
			sbQry.append("AND   SKU not in ( SELECT distinct vw.Sku FROM vw_exclusiones vw WHERE vw.Activo = 1 AND vw.IdCatPeriodo=" + filter.getIdPeriodoCat() + " and vw.idCatTipoRebate="+ filter.getIdCatTipoRebate() + ")\r\n")
				.append("GROUP BY NUM_OC,NUM_PROVEEDOR, NOM_PROVEEDOR, FAMILIA,SKU,SKU_DESCRIPCION\r\n")
				.append("ORDER BY SKU");	
		}
			
		List<Object[]> resultList = this.em.createNativeQuery(sbQry.toString()).getResultList();
		listDto = RebatesCicmxOcMapper.convertDtoSku(resultList);
		return listDto;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<RebatesCicmxOcDto> obtenerSkuUnicoDisponible(ExclusionCargaFilter filter) {
		List<RebatesCicmxOcDto> listDto = null;
		StringBuilder sbQry = new StringBuilder();
		
		boolean periodoTodos = this.periodoService.isPeriodoTodos(filter.getIdPeriodoCat());
		if (periodoTodos) {
			sbQry.append("SELECT FAMILIA, SKU, SKU_DESCRIPCION\r\n")
			.append("FROM [dbo].[vw_rebate_catalogo]\r\n")
			.append("WHERE 1=1\r\n");
		
			if (filter.getNumProveedor() != null && !filter.getNumProveedor().isEmpty()) {
				sbQry.append("AND   CAST(NUM_PROVEEDOR AS varchar) = '" + filter.getNumProveedor() + "'\r\n");
			}
			if (filter.getOrdenCompra() != null && !filter.getOrdenCompra().isEmpty()) {
				sbQry.append("AND   CAST(NUM_OC AS varchar) = '" + filter.getOrdenCompra() + "'\r\n");
			}
			if (filter.getClacom() != null && !filter.getClacom().isEmpty()) {
				sbQry.append("AND   FAMILIA = '" + filter.getClacom() + "'\r\n");
			}	
				
			sbQry.append("AND   SKU not in ( SELECT distinct vw.Sku FROM vw_exclusiones vw WHERE vw.Activo = 1 AND vw.IdCatPeriodo=" + filter.getIdPeriodoCat() + " and vw.idCatTipoRebate="+ filter.getIdCatTipoRebate() + ")\r\n")
				.append("GROUP BY FAMILIA, SKU, SKU_DESCRIPCION\r\n")
				.append("ORDER BY SKU");
		} else {
			
			sbQry.append("SELECT FAMILIA, SKU, SKU_DESCRIPCION\r\n")
			.append("FROM [dbo].[vw_rebate_orden_compra]\r\n")
			.append("WHERE 1=1\r\n");
		
			if (filter.getNumProveedor() != null && !filter.getNumProveedor().isEmpty()) {
				sbQry.append("AND   CAST(NUM_PROVEEDOR AS varchar) = '" + filter.getNumProveedor() + "'\r\n");
			}
			if (filter.getOrdenCompra() != null && !filter.getOrdenCompra().isEmpty()) {
				sbQry.append("AND   CAST(NUM_OC AS varchar) = '" + filter.getOrdenCompra() + "'\r\n");
			}
			if (filter.getClacom() != null && !filter.getClacom().isEmpty()) {
				sbQry.append("AND   FAMILIA = '" + filter.getClacom() + "'\r\n");
			}	
				
			sbQry.append("AND   SKU not in ( SELECT distinct vw.Sku FROM vw_exclusiones vw WHERE vw.Activo = 1 AND vw.IdCatPeriodo=" + filter.getIdPeriodoCat() + " and vw.idCatTipoRebate="+ filter.getIdCatTipoRebate() + ")\r\n")
				.append("GROUP BY FAMILIA, SKU, SKU_DESCRIPCION\r\n")
				.append("ORDER BY SKU");
		}
			
		List<Object[]> resultList = this.em.createNativeQuery(sbQry.toString()).getResultList();
		listDto = RebatesCicmxOcMapper.convertDtoSkuUnico(resultList);
		return listDto;
	}

	@Override
	public boolean existeExclusion(String exclusion, Integer idCatPeriodo, Integer idCatTipoExclusion, String numProveedor, Integer idCatTipoRebate) {
		StringBuilder sbQry = new StringBuilder();
		
		if (idCatTipoRebate == 8) { // Fill Rate
			sbQry.append("SELECT COUNT(1)\r\n")
			.append("FROM [dbo].[view_rebate_orden_compra_fill]\r\n")
			.append("WHERE 1=1\r\n");
			
			if (idCatTipoExclusion.intValue() == ETipoExclusion.PROVEEDORES.getId()) {
				sbQry.append("AND   CAST(NUMEROPROVEEDOR AS varchar) = '" + exclusion + "'\r\n");
			}
			if (idCatTipoExclusion.intValue() == ETipoExclusion.ORDEN_COMPRA.getId()) {
				sbQry.append("AND   CAST(NUMEROORDENCOMPRA AS varchar) = '" + exclusion + "'\r\n");
			}
			if (idCatTipoExclusion.intValue() == ETipoExclusion.FAMILIA.getId()) {
				if (numProveedor != null && !numProveedor.isEmpty()) {
					sbQry.append("AND   CAST(NUMEROPROVEEDOR AS varchar) = '" + numProveedor + "'\r\n");
				}
				sbQry.append("AND   FAMILIA = '" + exclusion + "'\r\n");
			}
			if (idCatTipoExclusion.intValue() == ETipoExclusion.SKU.getId()) {
				if (numProveedor != null && !numProveedor.isEmpty()) {
					sbQry.append("AND   CAST(NUMEROPROVEEDOR AS varchar) = '" + numProveedor + "'\r\n");
				}
				sbQry.append("AND   SKU = '" + exclusion + "'\r\n");
			}
			
			
		} else {
			boolean periodoTodos = this.periodoService.isPeriodoTodos(idCatPeriodo);
			if (periodoTodos) {
				sbQry.append("SELECT COUNT(1)\r\n")
				.append("FROM [dbo].[vw_rebate_catalogo]\r\n")
				.append("WHERE 1=1\r\n");
			
				if (idCatTipoExclusion.intValue() == ETipoExclusion.PROVEEDORES.getId()) {
					sbQry.append("AND   CAST(NUM_PROVEEDOR AS varchar) = '" + exclusion + "'\r\n");
				}
				if (idCatTipoExclusion.intValue() == ETipoExclusion.ORDEN_COMPRA.getId()) {
					sbQry.append("AND   CAST(NUM_OC AS varchar) = '" + exclusion + "'\r\n");
				}
				if (idCatTipoExclusion.intValue() == ETipoExclusion.FAMILIA.getId()) {
					if (numProveedor != null && !numProveedor.isEmpty()) {
						sbQry.append("AND   CAST(NUM_PROVEEDOR AS varchar) = '" + numProveedor + "'\r\n");
					}
					sbQry.append("AND   FAMILIA = '" + exclusion + "'\r\n");
				}
				if (idCatTipoExclusion.intValue() == ETipoExclusion.SKU.getId()) {
					if (numProveedor != null && !numProveedor.isEmpty()) {
						sbQry.append("AND   CAST(NUM_PROVEEDOR AS varchar) = '" + numProveedor + "'\r\n");
					}
					sbQry.append("AND   SKU = '" + exclusion + "'\r\n");
				}
			} else {
				sbQry.append("SELECT COUNT(1)\r\n")
				.append("FROM [dbo].[vw_rebate_orden_compra]\r\n")
				.append("WHERE 1=1\r\n");
			
				if (idCatTipoExclusion.intValue() == ETipoExclusion.PROVEEDORES.getId()) {
					sbQry.append("AND   CAST(NUM_PROVEEDOR AS varchar) = '" + exclusion + "'\r\n");
				}
				if (idCatTipoExclusion.intValue() == ETipoExclusion.ORDEN_COMPRA.getId()) {
					sbQry.append("AND   CAST(NUM_OC AS varchar) = '" + exclusion + "'\r\n");
				}
				if (idCatTipoExclusion.intValue() == ETipoExclusion.FAMILIA.getId()) {
					if (numProveedor != null && !numProveedor.isEmpty()) {
						sbQry.append("AND   CAST(NUM_PROVEEDOR AS varchar) = '" + numProveedor + "'\r\n");
					}
					sbQry.append("AND   FAMILIA = '" + exclusion + "'\r\n");
				}
				if (idCatTipoExclusion.intValue() == ETipoExclusion.SKU.getId()) {
					if (numProveedor != null && !numProveedor.isEmpty()) {
						sbQry.append("AND   CAST(NUM_PROVEEDOR AS varchar) = '" + numProveedor + "'\r\n");
					}
					sbQry.append("AND   SKU = '" + exclusion + "'\r\n");
				}
			}
		}
		
		int intValue = ((Number) this.em.createNativeQuery(sbQry.toString()).getSingleResult()).intValue();
		if (intValue > 0) {
			return true;
		}
		return false;
	}


	@Override
	public String obtenerFechaRecepcion(ExclusionDto exclusion) {
		StringBuilder sbQry = new StringBuilder();
//		sbQry.append("SELECT distinct FechaRecepcion\r\n")
//		.append("FROM [dbo].[vw_rebate_orden_compra]\r\n")
//		.append("WHERE 1=1\r\n");
//		sbQry.append("AND   CAST(NUM_OC AS varchar) = '" + exclusion + "'\r\n");
//		sbQry.append("GROUP BY NUM_PROVEEDOR, NOM_PROVEEDOR, NUM_OC, FAMILIA, SKU, SKU_DESCRIPCION, FechaRecepcion");

		String tabla = "vw_rebate_orden_compra";
		String columna = "NUM_OC";
		if (exclusion.getCatTipoRebate().getIdCatTipoRebate().equals(FILL_RATE)) {
			tabla = "view_rebate_orden_compra_fill";
			columna = "NumeroOrdenCompra";
		}
				
		//24-10-08 RMT Antes devolvia mas de un resultado
		sbQry.append("select min(FechaRecepcion) from " + tabla + " \r\n"
				+ "where CAST(" + columna + " AS varchar) = '" + exclusion.getExclusion() + "' \r\n");
		
		String fechaRecepcion = this.em.createNativeQuery(sbQry.toString()).getSingleResult().toString();
		return fechaRecepcion;
	}
	
}
