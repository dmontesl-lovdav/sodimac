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

import com.sodimac.rebates.model.RebateOrdenCompraFillEntity;
import com.sodimac.rebates.model.ReporteOrdenCompraFill;

@Service
public class RebateOrdenCompraFillService implements IRebateOrdenCompraFillService {

	private static Logger logger = LoggerFactory.getLogger(RebateOrdenCompraFillService.class);
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	@Autowired
	private EntityManager em;
	
	@Override
	public List<RebateOrdenCompraFillEntity> getOrdenCompraFill(ReporteOrdenCompraFill ordenCompraFill) {
		List<RebateOrdenCompraFillEntity> list = null;
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<RebateOrdenCompraFillEntity> cq = cb.createQuery(RebateOrdenCompraFillEntity.class);
		Root<RebateOrdenCompraFillEntity> oc = cq.from(RebateOrdenCompraFillEntity.class);
		List<Predicate> predicates = new ArrayList<>();
		
		Predicate onStart = cb.greaterThanOrEqualTo(oc.get("fechaRecepcion"), ordenCompraFill.getFechaIni());
		Predicate onEnd = cb.lessThanOrEqualTo(oc.get("fechaRecepcion"), ordenCompraFill.getFechaFin());
		
		predicates.add( onStart );
		predicates.add( onEnd );
		
		long inicio = System.currentTimeMillis();
		
		logger.info("fechaRecepcion: " + this.sdf.format( ordenCompraFill.getFechaIni() ));
		logger.info("fechaRecepcion: " + this.sdf.format( ordenCompraFill.getFechaFin() ));
		logger.info("numeroProveedor: " + ordenCompraFill.getProveedor());
		logger.info("numeroOrdenCompra: " + ordenCompraFill.getNumeroOrdenCompra());
		logger.info("numeroTienda: " + ordenCompraFill.getTienda());
		logger.info("estado: " + ordenCompraFill.getEstado());
		logger.info("tipoOrdenCompra: " + ordenCompraFill.getTipo());
		
		if (ordenCompraFill.getProveedor() != null) {
			predicates.add( cb.equal(oc.get("numeroProveedor"), ordenCompraFill.getProveedor().intValue() ));
		}
		
		if (ordenCompraFill.getNumeroOrdenCompra() != null) {
			predicates.add( cb.equal(oc.get("numeroOrdenCompra"), ordenCompraFill.getNumeroOrdenCompra().intValue() ));
		}
		
		if (ordenCompraFill.getTienda() != null) {
			predicates.add( cb.equal(oc.get("numeroTienda"), ordenCompraFill.getTienda().intValue() ));
		}
		
		if (ordenCompraFill.getEstado() != null && !ordenCompraFill.getEstado().isEmpty()) {
			predicates.add( cb.equal(oc.get("estadoOrdenCompra"), ordenCompraFill.getEstado() ));
		}
		
		if (ordenCompraFill.getTipo() != null && !ordenCompraFill.getTipo().isEmpty()) {
			predicates.add( cb.equal(oc.get("tipoOrdenCompra"), ordenCompraFill.getTipo() ));
		}
		
		cq.where(predicates.toArray(new Predicate[0]));
		if (ordenCompraFill.getRowsPerPage() > 0) {
			list = em.createQuery(cq).setMaxResults(ordenCompraFill.getRowsPerPage()).getResultList();
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
