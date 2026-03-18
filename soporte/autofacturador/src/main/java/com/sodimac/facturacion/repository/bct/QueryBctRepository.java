package com.sodimac.facturacion.repository.bct;

import java.util.ConcurrentModificationException;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sodimac.facturacion.util.enums.EQuery;

public class QueryBctRepository {
	
	private static Logger logger = LoggerFactory.getLogger(QueryBctRepository.class);
	
	public static Object executeGetSingleResult(String sql) {
		return executeGetResult(sql, EQuery.SingleResult);
	}
	
	public static Object executeGetResultList(String sql) {
		return executeGetResult(sql, EQuery.ResultList);
	}
	
	private static Object executeGetResult(String sql, EQuery enumQuery) {

		int contador = 0;
		int reintentos = 7;
		boolean primeraVez = true;
		Object result = null;

		do {
			try {
				if (primeraVez) {
					primeraVez = false;
				} else {
					Thread.sleep(1*200);
				}
				logger.debug("Lee intento: " + contador);
				EntityManager em = new EntityManagerBctRepository().getEntityManager();
				Query q = em.createNativeQuery(sql);
				
				if (enumQuery == EQuery.SingleResult) {
					result = q.getSingleResult();
				} else if (enumQuery == EQuery.ResultList) {
					result = q.getResultList();					
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (NoResultException e) {
				return null;	
			} catch (PersistenceException e) {
				e.printStackTrace();
			} catch (ConcurrentModificationException e) {
				e.printStackTrace();
			} catch (Exception e) {
				logger.error("executeGetResult: ", e);
			}
			contador += 1;

		} while (result == null && contador <= reintentos);
		
		return result;
    }
	
}