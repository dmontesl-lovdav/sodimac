package com.sodimac.rebates.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.dto.CalculoRebateMSIDto;
import com.sodimac.rebates.mapper.CalculoRebateMSIMapper;
import com.sodimac.rebates.model.CalculoRebateMSI;
import com.sodimac.rebates.model.CalculoRebateMSI3Entity;

@Service
public class CalculoRebateMSI3Service implements ICalculoRebateMSI3Service {

	private static Logger logger = LoggerFactory.getLogger(RebateOrdenCompraService.class);
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	@Autowired
	private EntityManager em;
	
	@Override
	public List<CalculoRebateMSI3Entity> getCalculoRebateMSI(CalculoRebateMSI calculoRebateMSI) {
		List<CalculoRebateMSI3Entity> list = null;
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CalculoRebateMSI3Entity> cq = cb.createQuery(CalculoRebateMSI3Entity.class);
		Root<CalculoRebateMSI3Entity> oc = cq.from(CalculoRebateMSI3Entity.class);
		List<Predicate> predicates = new ArrayList<>();
		
		long inicio = System.currentTimeMillis();
		
		if (calculoRebateMSI.getFechaIni() != null) {
			logger.info("fechaRecepcion: " + this.sdf.format( calculoRebateMSI.getFechaIni() ));
		}
		if (calculoRebateMSI.getFechaFin() != null) {
			logger.info("fechaRecepcion: " + this.sdf.format( calculoRebateMSI.getFechaFin() ));
		}
		logger.info("numeroProveedor: " + calculoRebateMSI.getProveedor());
		logger.info("ticket: " + calculoRebateMSI.getTicket());
		logger.info("idCatPeriodo: " + calculoRebateMSI.getIdCatPeriodo());
		
		if (calculoRebateMSI.getIdCatPeriodo() != null) {
			predicates.add( cb.equal(oc.get("idPeriodo"), calculoRebateMSI.getIdCatPeriodo() ));
		}
		
		if (calculoRebateMSI.getFechaIni() != null) {
			Predicate onStart = cb.greaterThanOrEqualTo(oc.get("fechaVenta"), calculoRebateMSI.getFechaIni());
			predicates.add( onStart );
		}
		
		if (calculoRebateMSI.getFechaFin() != null ) {
			Predicate onEnd = cb.lessThanOrEqualTo(oc.get("fechaVenta"), calculoRebateMSI.getFechaFin());
			predicates.add( onEnd );
		}
		
		if (calculoRebateMSI.getProveedor() != null && !calculoRebateMSI.getProveedor().isEmpty()) {
			predicates.add( cb.equal(oc.get("numeroProveedor"), calculoRebateMSI.getProveedor() ));
		}
		
		if (calculoRebateMSI.getTicket() != null && !calculoRebateMSI.getTicket().isEmpty()) {
			predicates.add( cb.equal(oc.get("ticketVenta"), calculoRebateMSI.getTicket() ));
		}
		
		cq.where(predicates.toArray(new Predicate[0]));
		if (calculoRebateMSI.getRowsPerPage() > 0) {
			list = em.createQuery(cq).setMaxResults(calculoRebateMSI.getRowsPerPage()).getResultList();
		} else {
			list = em.createQuery(cq).getResultList();
		}
		
		long tempsFinal = System.currentTimeMillis();
		long diferencia = tempsFinal - inicio;
		
		long minutos = TimeUnit.MILLISECONDS.toMinutes(diferencia);
		logger.debug("diferencia: " + diferencia);
		logger.debug("minutos: " + minutos);
		logger.debug("total registros: " + list.size());
		return list;
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public List<CalculoRebateMSIDto> getCalculoRebateMSIView(CalculoRebateMSI calculoRebateMSI) throws ParseException {
		List<CalculoRebateMSIDto> list = null;
		String top = "";
		if (calculoRebateMSI.getRowsPerPage() > 0) {
			top = " TOP " + String.valueOf(calculoRebateMSI.getRowsPerPage());
		} 
		
		//TOP (1000)
		StringBuilder sbQry = new StringBuilder();
		sbQry.append("SELECT " + top + " [idCalculoRebate] \n")	// --1
		.append("      ,[Origen] \n") 					// --2
		.append("      ,[MonedaVenta] \n") 				// --3
		.append("      ,[RFC] \n") 						// --4
		.append("      ,[NumeroProveedor] \n") 			// --5
		.append("      ,[Familia] \n") 					// --6
		.append("      ,[NombreFamilia] \n") 				// --7
		.append("      ,[TicketVenta] \n") 				// --8
		.append("      ,[SucursalVenta] \n") 				// --9
		.append("      ,[FechaVenta] \n") 				// --10
		.append("      ,[Banco] \n") 						// --11
		.append("      ,[NumCuota] \n") 					// --12
		.append("      ,[SKU]  \n") 						// --13
		.append("      ,[DescripcionProducto] \n") 		// --14
		.append("      ,[SubtotalSKU] \n") 				// --15
		.append("      ,[MontoVentaSku] \n") 				// --16
		.append("      ,[TipoAcuerdo] \n") 				// --17
		.append("      ,[MonedaAcuerdo] \n") 				// --18
		.append("      ,[ValorDescuento] \n") 			// --19
		.append("      ,[TipoDescuento] \n") 				// --20
		.append("      ,[MontoRebate] \n") 				// --21
		.append("      ,[IvaRebate] \n") 					// --22
		.append("      ,[MontoTotalRebate] \n") 			// --23
		.append("      ,[ProgramaPago] \n") 				// --24
		.append("      ,[IdPeriodo] \n") 					// --25
		.append("      ,[SubtotalCuenta] \n") 			// --26
		.append("      ,[IVACuenta] \n") 					// --27
		.append("      ,[ProveedorMercancia] \n")	 		// --28
		.append("      ,[TipoDocumentoPoliza] \n") 		// --29
		.append("      ,[CentroCostos] \n") 				// --30
		.append("      ,[CentroBeneficios] \n") 			// --31
		.append("      ,[Sucursal] \n") 					// --32
		.append("      ,[CondicionesPago] \n") 			// --33
		.append("      ,[Exclusion] \n") 					// --34
		.append("      ,[FechaExclusion] \n") 			// --35
		.append("      ,[IdExclusion] \n") 				// --36
		.append(" FROM ( \n");
		
		sbQry.append("SELECT " + top + " [idCalculoRebate] \n")	// --1
				.append("      ,[Origen] \n") 					// --2
				.append("      ,[MonedaVenta] \n") 				// --3
				.append("      ,[RFC] \n") 						// --4
				.append("      ,[NumeroProveedor] \n") 			// --5
				.append("      ,[Familia] \n") 					// --6
				.append("      ,[NombreFamilia] \n") 				// --7
				.append("      ,[TicketVenta] \n") 				// --8
				.append("      ,[SucursalVenta] \n") 				// --9
				.append("      ,CONVERT(NVARCHAR, [FechaVenta], 103) [FechaVenta] \n") 				// --10
				.append("      ,[Banco] \n") 						// --11
				.append("      ,[NumCuota] \n") 					// --12
				.append("      ,[SKU]  \n") 						// --13
				.append("      ,[DescripcionProducto] \n") 		// --14
				.append("      ,[SubtotalSKU] \n") 				// --15
				.append("      ,[MontoVentaSku] \n") 				// --16
				.append("      ,[TipoAcuerdo] \n") 				// --17
				.append("      ,[MonedaAcuerdo] \n") 				// --18
				.append("      ,[ValorDescuento] \n") 			// --19
				.append("      ,[TipoDescuento] \n") 				// --20
				.append("      ,[MontoRebate] \n") 				// --21
				.append("      ,[IvaRebate] \n") 					// --22
				.append("      ,[MontoTotalRebate] \n") 			// --23
				.append("      ,[ProgramaPago] \n") 				// --24
				.append("      ,[IdPeriodo] \n") 					// --25
				.append("      ,[SubtotalCuenta] \n") 			// --26
				.append("      ,[IVACuenta] \n") 					// --27
				.append("      ,[ProveedorMercancia] \n")	 		// --28
				.append("      ,[TipoDocumentoPoliza] \n") 		// --29
				.append("      ,[CentroCostos] \n") 				// --30
				.append("      ,[CentroBeneficios] \n") 			// --31
				.append("      ,[Sucursal] \n") 					// --32
				.append("      ,[CondicionesPago] \n") 			// --33
				.append("      ,[Exclusion] \n") 					// --34
				.append("      ,[FechaExclusion] \n") 			// --35
				.append("      ,[IdExclusion] \n") 				// --36
				.append("  FROM [CalculoRebateMSI3] \n")
				.append("  WHERE 1=1 \n");
				if (calculoRebateMSI.getFechaIni() != null) {
					logger.info("fechaRecepcion: " + this.sdf.format( calculoRebateMSI.getFechaIni() ));
				}
				if (calculoRebateMSI.getFechaFin() != null) {
					logger.info("fechaRecepcion: " + this.sdf.format( calculoRebateMSI.getFechaFin() ));
				}
				logger.info("numeroProveedor: " + calculoRebateMSI.getProveedor());
				logger.info("ticket: " + calculoRebateMSI.getTicket());
				logger.info("idCatPeriodo: " + calculoRebateMSI.getIdCatPeriodo());
				
				if (calculoRebateMSI.getIdCatPeriodo() != null) {
					sbQry.append(" AND IdPeriodo = " + calculoRebateMSI.getIdCatPeriodo() + " \n");
				}
				
				if (calculoRebateMSI.getFechaIni() != null) {
					sbQry.append(" AND fechaVenta >= CONVERT(datetime,'" + this.sdf.format(calculoRebateMSI.getFechaIni()) + "',120) \n");
				}
				
				if (calculoRebateMSI.getFechaFin() != null ) {
					sbQry.append(" AND fechaVenta <= CONVERT(datetime,'" + this.sdf.format(calculoRebateMSI.getFechaFin()) + "',120) \n");
				}
				
				if (calculoRebateMSI.getProveedor() != null && !calculoRebateMSI.getProveedor().isEmpty()) {
					sbQry.append(" AND NumeroProveedor = " + calculoRebateMSI.getProveedor() + " \n");
				}
				
				if (calculoRebateMSI.getTicket() != null && !calculoRebateMSI.getTicket().isEmpty()) {
					sbQry.append(" AND ticketVenta = '" + calculoRebateMSI.getTicket() + "' \n");
				}
		sbQry.append("  UNION ALL  \n")
				.append("SELECT " + top + " [idCalculoRebate] \n")// --1
				.append("      ,[Origen] \n") 					// --2
				.append("      ,[MonedaVenta] \n") 				// --3
				.append("      ,[RFC] \n") 						// --4
				.append("      ,[NumeroProveedor] \n") 			// --5
				.append("      ,[Familia] \n") 					// --6
				.append("      ,[NombreFamilia] \n") 				// --7
				.append("      ,[TicketVenta] \n") 				// --8
				.append("      ,[SucursalVenta] \n") 				// --9
				.append("      ,CONVERT(NVARCHAR, [FechaVenta], 103) [FechaVenta] \n") 				// --10
				.append("      ,[Banco] \n") 						// --11
				.append("      ,[NumCuota] \n") 					// --12
				.append("      ,[SKU]  \n") 						// --13
				.append("      ,[DescripcionProducto] \n") 		// --14
				.append("      ,[SubtotalSKU] \n") 				// --15
				.append("      ,[MontoVentaSku] \n") 				// --16
				.append("      ,[TipoAcuerdo] \n") 				// --17
				.append("      ,[MonedaAcuerdo] \n") 				// --18
				.append("      ,[ValorDescuento] \n") 			// --19
				.append("      ,[TipoDescuento] \n") 				// --20
				.append("      ,[MontoRebate] \n") 				// --21
				.append("      ,[IvaRebate] \n") 					// --22
				.append("      ,[MontoTotalRebate] \n") 			// --23
				.append("      ,[ProgramaPago] \n") 				// --24
				.append("      ,[IdPeriodo] \n") 					// --25
				.append("      ,[SubtotalCuenta] \n") 			// --26
				.append("      ,[IVACuenta] \n") 					// --27
				.append("      ,[ProveedorMercancia] \n")	 		// --28
				.append("      ,[TipoDocumentoPoliza] \n") 		// --29
				.append("      ,[CentroCostos] \n") 				// --30
				.append("      ,[CentroBeneficios] \n") 			// --31
				.append("      ,[Sucursal] \n") 					// --32
				.append("      ,[CondicionesPago] \n") 			// --33
				.append("      ,[Exclusion] \n") 					// --34
				.append("      ,[FechaExclusion] \n") 			// --35
				.append("      ,[IdExclusion] \n") 				// --36
				.append("  FROM [CalculoRebateMSI3Temp]  \n")
				.append(" WHERE 1=1 \n");
		
			if (calculoRebateMSI.getFechaIni() != null) {
				logger.info("fechaRecepcion: " + this.sdf.format( calculoRebateMSI.getFechaIni() ));
			}
			if (calculoRebateMSI.getFechaFin() != null) {
				logger.info("fechaRecepcion: " + this.sdf.format( calculoRebateMSI.getFechaFin() ));
			}
			logger.info("numeroProveedor: " + calculoRebateMSI.getProveedor());
			logger.info("ticket: " + calculoRebateMSI.getTicket());
			logger.info("idCatPeriodo: " + calculoRebateMSI.getIdCatPeriodo());
			
			if (calculoRebateMSI.getIdCatPeriodo() != null) {
				sbQry.append(" AND IdPeriodo = " + calculoRebateMSI.getIdCatPeriodo() + " \n");
			}
			
			if (calculoRebateMSI.getFechaIni() != null) {
				sbQry.append(" AND fechaVenta >= CONVERT(datetime,'" + this.sdf.format(calculoRebateMSI.getFechaIni()) + "',120) \n");
			}
			
			if (calculoRebateMSI.getFechaFin() != null ) {
				sbQry.append(" AND fechaVenta <= CONVERT(datetime,'" + this.sdf.format(calculoRebateMSI.getFechaFin()) + "',120) \n");
			}
			
			if (calculoRebateMSI.getProveedor() != null && !calculoRebateMSI.getProveedor().isEmpty()) {
				sbQry.append(" AND NumeroProveedor = " + calculoRebateMSI.getProveedor() + " \n");
			}
			
			if (calculoRebateMSI.getTicket() != null && !calculoRebateMSI.getTicket().isEmpty()) {
				sbQry.append(" AND ticketVenta = '" + calculoRebateMSI.getTicket() + "' \n");
			}
			
			sbQry.append(") a");
			
			List<Object[]> resultList = this.em.createNativeQuery(sbQry.toString()).getResultList();
			
			list = CalculoRebateMSIMapper.convertToDtos(resultList);
			return list;
	}
}
