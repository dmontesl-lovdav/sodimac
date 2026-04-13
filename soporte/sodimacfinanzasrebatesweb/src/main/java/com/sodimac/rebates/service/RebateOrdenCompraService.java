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

import com.sodimac.rebates.model.RebateOrdenCompraEntity;
import com.sodimac.rebates.model.ReporteOrdenCompra;

@Service
public class RebateOrdenCompraService implements IRebateOrdenCompraService {

	private static Logger logger = LoggerFactory.getLogger(RebateOrdenCompraService.class);
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	@Autowired
	private EntityManager em;
	
	@Override
	public List<RebateOrdenCompraEntity> getOrdenCompra(ReporteOrdenCompra ordenCompra) {
		List<RebateOrdenCompraEntity> list = null;
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<RebateOrdenCompraEntity> cq = cb.createQuery(RebateOrdenCompraEntity.class);
		Root<RebateOrdenCompraEntity> oc = cq.from(RebateOrdenCompraEntity.class);
		List<Predicate> predicates = new ArrayList<>();
		
		Predicate onStart = cb.greaterThanOrEqualTo(oc.get("fechaRecepcion"), ordenCompra.getFechaIni());
		Predicate onEnd = cb.lessThanOrEqualTo(oc.get("fechaRecepcion"), ordenCompra.getFechaFin());
		
		predicates.add( onStart );
		predicates.add( onEnd );
		
		long inicio = System.currentTimeMillis();
		
		logger.info("fechaRecepcion: " + this.sdf.format( ordenCompra.getFechaIni() ));
		logger.info("fechaRecepcion: " + this.sdf.format( ordenCompra.getFechaFin() ));
		logger.info("numeroProveedor: " + ordenCompra.getProveedor());
		logger.info("numeroOrdenCompra: " + ordenCompra.getNumeroOrdenCompra());
		logger.info("numeroTienda: " + ordenCompra.getTienda());
		logger.info("estado: " + ordenCompra.getEstado());
		logger.info("tipoOrdenCompra: " + ordenCompra.getTipo());
		
		if (ordenCompra.getProveedor() != null) {
			predicates.add( cb.equal(oc.get("numeroProveedor"), ordenCompra.getProveedor().intValue() ));
		}
		
		if (ordenCompra.getNumeroOrdenCompra() != null) {
			predicates.add( cb.equal(oc.get("numeroOrdenCompra"), ordenCompra.getNumeroOrdenCompra().intValue() ));
		}
		
		if (ordenCompra.getTienda() != null) {
			predicates.add( cb.equal(oc.get("numeroTienda"), ordenCompra.getTienda().intValue() ));
		}
		
		if (ordenCompra.getEstado() != null && !ordenCompra.getEstado().isEmpty()) {
			predicates.add( cb.equal(oc.get("estadoOrdenCompra"), ordenCompra.getEstado() ));
		}
		
		if (ordenCompra.getTipo() != null && !ordenCompra.getTipo().isEmpty()) {
			predicates.add( cb.equal(oc.get("tipoOrdenCompra"), ordenCompra.getTipo() ));
		}
		
		cq.where(predicates.toArray(new Predicate[0]));
		if (ordenCompra.getRowsPerPage() > 0) {
			list = em.createQuery(cq).setMaxResults(ordenCompra.getRowsPerPage()).getResultList();
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
