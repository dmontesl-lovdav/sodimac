package com.sodimac.bctfacturacion.repository.ces;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.bctfacturacion.entity.ces.AdminPuntosCesEntity;

@Repository
public interface AdminPuntosCesRepository extends JpaRepository<AdminPuntosCesEntity, Integer> {

	public long countByTicketAndTipoTransaccionCesAndEstatus(String ticket, String tipoTransaccionCes, int estatus);
	
	public AdminPuntosCesEntity findByTicketAndTipoTransaccionCesAndEstatus(String ticket, String tipoTransaccionCes, int estatus);
	
	public List<AdminPuntosCesEntity> findByTipoTransaccionCesAndEstatus(String tipoTransaccionCes, int estatus);
	
}
