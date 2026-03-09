package com.sodimac.cfdi.repositoryFactura;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entityFactura.CatMensajesEntity;

@Repository("incidentesTimbradoRepository")
public interface IncidentesTimbradoRepository extends JpaRepository<CatMensajesEntity, Integer> {

	@Query(value = "{call uspCFDIClientesTemporal (:ticket)}", nativeQuery = true)
	public List<Object[]> findTicket(@Param("ticket") String ticket);

	@Query(value = "{call uspCFDIEliminarTicket (:ticket, 0)}", nativeQuery = true)
	public int deleteTicket(@Param("ticket") String ticket);

	@Query(value = "{call uspCFDIEliminarTicket (:ticket, 1)}", nativeQuery = true)
	public int deleteIntentos(@Param("ticket") String ticket);
}
