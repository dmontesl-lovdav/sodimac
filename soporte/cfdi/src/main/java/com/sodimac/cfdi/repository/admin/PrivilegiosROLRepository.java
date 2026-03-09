package com.sodimac.cfdi.repository.admin;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.admin.PrivilegiosPerfilEntity;

@Repository("privilegiosROLRepository")
public interface PrivilegiosROLRepository extends JpaRepository<PrivilegiosPerfilEntity, Integer>{

	@Query(value = "select pr.idperfil,pr.c,pr.r,pr.u,pr.d,pr.admin from sodimacfiscal.privsperfil pr, catusuarioperfil up"
			+ "  where up.idusuario = :idusuario and up.idperfil = pr.idperfil", nativeQuery = true)
	public List<Object[]> getPrivilegiosUsuario(@Param("idusuario")int idusuario);
	
}
