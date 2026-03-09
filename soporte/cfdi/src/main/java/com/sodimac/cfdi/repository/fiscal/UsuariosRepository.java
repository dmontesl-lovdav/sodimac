package com.sodimac.cfdi.repository.fiscal;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.fiscal.UsuariosEntity;

@Repository("usuariosRepository")
public interface UsuariosRepository extends JpaRepository<UsuariosEntity, Integer> {

	@Query(value = "{call uspValidarLogin (:usuario, :password)}", nativeQuery = true)	
	UsuariosEntity validarLogin(@Param("usuario") String usuario, @Param("password") String password);
	
	UsuariosEntity findByUsuarioAndPassword(String usuario, String password);

	@Query(value = "{call uspObtenerOpcionesRol (:idRol)}", nativeQuery = true)	
	List<Object[]> obtenerOpcionesRol(@Param("idRol") int idRol);
}
