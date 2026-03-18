package com.sodimac.facturacion.repository.ws;

import java.util.ArrayList;
import java.util.List;

import com.sodimac.facturacion.config.User;

public class UsersWsRepository {

	static List<User> users = new ArrayList<>();
	
	public List<User> getUsers() {
		
        if (users == null || users.isEmpty()) {

    		String sql = "select usuario, contrasena, rol from usuariosWs where activo = 1";
    		@SuppressWarnings("unchecked")
    		List<Object[]> lst = (List<Object[]>) QueryWsRepository.executeGetResultList(sql);

        	if (lst != null && !lst.isEmpty()) {
        		for (Object[] item: lst) {
        			users.add(new User((String) item[0], (String) item[1], (String) item[2]));    			
        		}
        	}		        	
        }
        
        return users;
    }	
}
