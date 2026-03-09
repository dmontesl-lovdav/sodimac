package com.sodimac.cfdi.repository.fiscal.catalogospdf;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.fiscal.catalogospdf.PacsEntity;

@Repository("pacsRepository")
public interface PacsRepository extends JpaRepository<PacsEntity, Integer> {

	@Query(value = "{call uspObtenerPacDefault ()}", nativeQuery = true)	
	int getIdDefault();

}
