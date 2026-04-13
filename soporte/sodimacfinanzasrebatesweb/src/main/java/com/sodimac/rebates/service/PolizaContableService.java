package com.sodimac.rebates.service;

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

import com.sodimac.rebates.dto.PolizaContableDto;
import com.sodimac.rebates.dto.PolizaContableReporteDto;
import com.sodimac.rebates.filter.PolizaContableFilter;
import com.sodimac.rebates.mapper.PolizaContableMapper;
import com.sodimac.rebates.mapper.PolizaContableReporteMapper;
import com.sodimac.rebates.model.PolizaContableEntity;
import com.sodimac.rebates.model.PolizaContableReporteEntity;

@Service
public class PolizaContableService implements IPolizaContableService {
	
	private static Logger logger = LoggerFactory.getLogger(PolizaContableService.class);
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	@Autowired
	private EntityManager em;
	
	@Override
	public List<PolizaContableDto> getPolizasContables(PolizaContableFilter filter) {
		List<PolizaContableEntity> list = null;
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<PolizaContableEntity> cq = cb.createQuery(PolizaContableEntity.class);
		Root<PolizaContableEntity> oc = cq.from(PolizaContableEntity.class);
		List<Predicate> predicates = new ArrayList<>();
		
		long inicio = System.currentTimeMillis();
		
		if (filter.getFechaCargaIni() != null) {
			logger.info("fechaRecepcion: " + this.sdf.format( filter.getFechaCargaIni() ));
		}
		if (filter.getFechaCargaFin() != null) {
			logger.info("fechaRecepcion: " + this.sdf.format( filter.getFechaCargaFin() ));
		}
		
		logger.info("idPeriodo: " + filter.getIdPeriodo());
		logger.info("tipoRebate: " + filter.getTipoRebate());
		logger.info("codigoProveedor: " + filter.getIdProveedor());
		
		if (filter.getIdPeriodo() != null) {
			predicates.add( cb.equal(oc.get("idPeriodo"), filter.getIdPeriodo() ));
		}
		
		if (filter.getFechaCargaIni() != null) {
			Predicate onStart = cb.greaterThanOrEqualTo(oc.get("fechaRecepcion"), filter.getFechaCargaIni());
			predicates.add( onStart );
		}
		
		if (filter.getFechaCargaFin() != null ) {
			Predicate onEnd = cb.lessThanOrEqualTo(oc.get("fechaRecepcion"), filter.getFechaCargaFin());
			predicates.add( onEnd );
		}
		
		if (filter.getTipoRebate() != null && filter.getTipoRebate().intValue() > 0 ) {
			predicates.add( cb.equal(oc.get("idTipoRebate"), filter.getTipoRebate() ));
		}
		
		if (filter.getIdProveedor() != null && !filter.getIdProveedor().isEmpty()) {
			predicates.add( cb.equal(oc.get("codigoProveedor"), filter.getIdProveedor() ));
		}
		
		cq.where(predicates.toArray(new Predicate[0]));
		if (filter.getRowsPerPage() > 0) {
			list = em.createQuery(cq).setMaxResults(filter.getRowsPerPage()).getResultList();
		} else {
			list = em.createQuery(cq).getResultList();
		}
		
		List<PolizaContableDto> dtos = PolizaContableMapper.convertToDtos(list);
		
		long tempsFinal = System.currentTimeMillis();
		long diferencia = tempsFinal - inicio;
		
		long minutos = TimeUnit.MILLISECONDS.toMinutes(diferencia);
		logger.debug("diferencia: " + diferencia);
		logger.debug("minutos: " + minutos);
		logger.debug("total registros: " + list.size());
		return dtos;
	}

	@Override
	public List<PolizaContableReporteDto> getReportePolizasContables(PolizaContableFilter filter) {
		List<PolizaContableReporteEntity> list = null;
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<PolizaContableReporteEntity> cq = cb.createQuery(PolizaContableReporteEntity.class);
		Root<PolizaContableReporteEntity> oc = cq.from(PolizaContableReporteEntity.class);
		List<Predicate> predicates = new ArrayList<>();
		
		long inicio = System.currentTimeMillis();
		
		if (filter.getFechaCargaIni() != null) {
			logger.info("fechaRecepcion: " + this.sdf.format( filter.getFechaCargaIni() ));
		}
		if (filter.getFechaCargaFin() != null) {
			logger.info("fechaRecepcion: " + this.sdf.format( filter.getFechaCargaFin() ));
		}
		
		logger.info("idPeriodo: " + filter.getIdPeriodo());
		logger.info("tipoRebate: " + filter.getTipoRebate());
		logger.info("codigoProveedor: " + filter.getIdProveedor());
		
		if (filter.getIdPeriodo() != null) {
			predicates.add( cb.equal(oc.get("idPeriodo"), filter.getIdPeriodo() ));
		}
		
		if (filter.getFechaCargaIni() != null) {
			Predicate onStart = cb.greaterThanOrEqualTo(oc.get("fechaRecepcion"), filter.getFechaCargaIni());
			predicates.add( onStart );
		}
		
		if (filter.getFechaCargaFin() != null ) {
			Predicate onEnd = cb.lessThanOrEqualTo(oc.get("fechaRecepcion"), filter.getFechaCargaFin());
			predicates.add( onEnd );
		}
		
		if (filter.getTipoRebate() != null && filter.getTipoRebate().intValue() > 0 ) {
			predicates.add( cb.equal(oc.get("idTipoRebate"), filter.getTipoRebate()) );
		}
		
		if (filter.getIdProveedor() != null && !filter.getIdProveedor().isEmpty()) {
			predicates.add( cb.equal(oc.get("codigoProveedor"), filter.getIdProveedor() ));
		}
		
		cq.where(predicates.toArray(new Predicate[0]));
		if (filter.getRowsPerPage() > 0) {
			list = em.createQuery(cq).setMaxResults(filter.getRowsPerPage()).getResultList();
		} else {
			list = em.createQuery(cq).getResultList();
		}
		
		List<PolizaContableReporteDto> dtos = PolizaContableReporteMapper.convertToDtos(list);
		
		long tempsFinal = System.currentTimeMillis();
		long diferencia = tempsFinal - inicio;
		
		long minutos = TimeUnit.MILLISECONDS.toMinutes(diferencia);
		logger.debug("diferencia: " + diferencia);
		logger.debug("minutos: " + minutos);
		logger.debug("total registros: " + list.size());
		return dtos;
	}
	
	
}
