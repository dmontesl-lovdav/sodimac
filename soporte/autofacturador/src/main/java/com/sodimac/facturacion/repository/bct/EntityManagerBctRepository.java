package com.sodimac.facturacion.repository.bct;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.sodimac.facturacion.util.UtilsConexion;

public class EntityManagerBctRepository {

    private static EntityManagerFactory factory;
    private static EntityManager em;
    
    public EntityManager getEntityManager() {
    	
        if (em == null || !em.isOpen()) {
        	
        	factory = UtilsConexion.buildFactory("databaseBct.properties");
        	em = factory.createEntityManager();
        	try {
				Thread.sleep(1*200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        return em;
    }
    
}
