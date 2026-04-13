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

import com.sodimac.rebates.dto.CatUsuarioPerfilDto;
import com.sodimac.rebates.filter.UsuarioFillRateFilter;
import com.sodimac.rebates.model.RebateUsuarioFillRateEntity;

@Service
public class RebateUsuarioFillRateService implements IRebateUsuarioFillRateService {

	private static final int PERFIL_FINANZAS = 1;
	private static Logger logger = LoggerFactory.getLogger(RebateUsuarioFillRateService.class);
	
	@Autowired
	private EntityManager em;

	@Autowired
	private ICatUsuarioPerfilService catUsuarioPerfilService;
	
	@Override
	public List<RebateUsuarioFillRateEntity> getUsuarioFillRate(UsuarioFillRateFilter usuarioFillRate, Integer idUser) {
		List<RebateUsuarioFillRateEntity> list = null;
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<RebateUsuarioFillRateEntity> cq = cb.createQuery(RebateUsuarioFillRateEntity.class);
		Root<RebateUsuarioFillRateEntity> oc = cq.from(RebateUsuarioFillRateEntity.class);
		List<Predicate> predicates = new ArrayList<>();
		
		long inicio = System.currentTimeMillis();
		
		logger.info("Fecha Ini: " + usuarioFillRate.getFechaIni());
		logger.info("Fecha Fin: " + usuarioFillRate.getFechaFin());
		logger.info("Id Periodo: " + usuarioFillRate.getIdPeriodo());
		logger.info("Id Proveedor: " + usuarioFillRate.getIdProveedor());
		logger.info("Tipo Periodo: " + usuarioFillRate.getTipoPeriodo());
		
		if(usuarioFillRate.getFechaIni() != null && usuarioFillRate.getFechaFin() != null) {
			Predicate onStart = cb.greaterThanOrEqualTo(oc.get("fechaRecepcion"), usuarioFillRate.getFechaIni());
			Predicate onEnd = cb.lessThanOrEqualTo(oc.get("fechaRecepcion"), usuarioFillRate.getFechaFin());
			
			predicates.add( onStart );
			predicates.add( onEnd );
		}
		
		if (usuarioFillRate.getIdProveedor() != null && usuarioFillRate.getIdProveedor() != 0) {
			predicates.add( cb.equal(oc.get("codigoProveedor"), usuarioFillRate.getIdProveedor().toString()));
		}
		
		if (usuarioFillRate.getIdPeriodo() != null && usuarioFillRate.getIdPeriodo() != 0) {
			predicates.add( cb.equal(oc.get("idCatPeriodo"), usuarioFillRate.getIdPeriodo().toString()));
		}
		
		if (Strings.isNotBlank(usuarioFillRate.getTipoPeriodo())) {
			predicates.add( cb.equal(oc.get("programaPago"), usuarioFillRate.getTipoPeriodo() ));
		}
		
		int idPerfilUser = 0;
		List<CatUsuarioPerfilDto> perfilesUsuarioModifEstatus = catUsuarioPerfilService.getUsuarioPerfiles(idUser);
		if (perfilesUsuarioModifEstatus != null && !perfilesUsuarioModifEstatus.isEmpty() && perfilesUsuarioModifEstatus.get(0).getPerfil() != null) {
			idPerfilUser = perfilesUsuarioModifEstatus.get(0).getPerfil().getId();
		}
		
		if (idPerfilUser == PERFIL_FINANZAS) {
			predicates.add( cb.equal(oc.get("calculoFinanzas"), 1));
		} else {
			predicates.add( cb.equal(oc.get("calculoLogistica"), 1));
		}
				
		cq.where(predicates.toArray(new Predicate[0]));
		if (usuarioFillRate.getRowsPerPage() > 0) {
			list = em.createQuery(cq).setMaxResults(usuarioFillRate.getRowsPerPage()).getResultList();
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

	@Override
	public List<RebateUsuarioFillRateEntity> getUsuarioFillRateReport() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<RebateUsuarioFillRateEntity> cq = cb.createQuery(RebateUsuarioFillRateEntity.class);
		Root<RebateUsuarioFillRateEntity> oc = cq.from(RebateUsuarioFillRateEntity.class);
		cq.select(oc);
		return em.createQuery(cq).getResultList();
	}

}
