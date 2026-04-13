package com.sodimac.rebates.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.filter.RebateUsuarioFilter;
import com.sodimac.rebates.model.RebateUsuarioEntity;

@Service
public class RebateUsuarioService implements IRebateUsuarioService {

	private static Logger logger = LoggerFactory.getLogger(RebateUsuarioService.class);
	
	@Autowired
	private EntityManager em;
	
	@Override
	public List<RebateUsuarioEntity> getRebateUsuario(RebateUsuarioFilter rebateUsuario) {
		List<RebateUsuarioEntity> list = null;
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<RebateUsuarioEntity> cq = cb.createQuery(RebateUsuarioEntity.class);
		Root<RebateUsuarioEntity> oc = cq.from(RebateUsuarioEntity.class);
		List<Predicate> predicates = new ArrayList<>();
		
		long inicio = System.currentTimeMillis();
		
		logger.info("Fecha Ini: " + rebateUsuario.getFechaIni());
		logger.info("Fecha Fin: " + rebateUsuario.getFechaFin());
		logger.info("Id Periodo: " + rebateUsuario.getIdPeriodo());
		logger.info("Id Proveedor: " + rebateUsuario.getIdProveedor());
		logger.info("Tipo Periodo: " + rebateUsuario.getTipoPeriodo());
		logger.info("Tipo Rebate: " + rebateUsuario.getTipoRebate());
		
		if(rebateUsuario.getFechaIni() != null && rebateUsuario.getFechaFin() != null) {
			Predicate onStart = cb.greaterThanOrEqualTo(oc.get("fechaRecepcion"), rebateUsuario.getFechaIni());
			Predicate onEnd = cb.lessThanOrEqualTo(oc.get("fechaRecepcion"), rebateUsuario.getFechaFin());
			
			predicates.add( onStart );
			predicates.add( onEnd );
		}
		
		if (rebateUsuario.getIdProveedor() != null && rebateUsuario.getIdProveedor() != 0) {
			predicates.add( cb.equal(oc.get("codigoProveedor"), rebateUsuario.getIdProveedor().toString()));
		}
		
		if (rebateUsuario.getIdPeriodo() != null && rebateUsuario.getIdPeriodo() != 0) {
			predicates.add( cb.equal(oc.get("periodo"), rebateUsuario.getIdPeriodo().toString()));
		}
		
		if (Strings.isNotBlank(rebateUsuario.getTipoPeriodo())) {
			predicates.add( cb.equal(oc.get("programaPago"), rebateUsuario.getTipoPeriodo() ));
		}
		
		if (rebateUsuario.getTipoRebate() != null && rebateUsuario.getTipoRebate() != 0) {
			predicates.add( cb.equal(oc.get("idTipoRebate"), rebateUsuario.getTipoRebate() ));
		}		
		
		cq.where(predicates.toArray(new Predicate[0]));
		if (rebateUsuario.getRowsPerPage() > 0) {
			list = em.createQuery(cq).setMaxResults(rebateUsuario.getRowsPerPage()).getResultList();
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

}
