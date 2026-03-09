package com.sodimac.cfdi.repository.fiscal;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.sodimac.cfdi.util.UtilsConexion;

public class EntityManagerFiscalRepository {

    private static EntityManagerFactory factory;
    private static EntityManager em;
    
    public EntityManager getEntityManager() {
    	
        if (em == null || !em.isOpen()) {
        	
        	factory = UtilsConexion.buildFactory("databaseFiscal.properties");
        	em = factory.createEntityManager();
        	try {
				Thread.sleep(1*150);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}        	
        }
        
        return em;
    }
    
}
