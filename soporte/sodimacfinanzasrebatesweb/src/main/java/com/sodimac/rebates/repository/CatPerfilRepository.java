package com.sodimac.rebates.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.rebates.model.entity.CatPerfilEntity;

@Repository
public interface CatPerfilRepository extends JpaRepository<CatPerfilEntity, Integer> {

	@Query(
		    " Select DISTINCT p  " +
			" from CatPerfilEntity p"
			+ " JOIN CatUsuarioPerfilEntity up  ON up.catUsuarioPerfilPk.perfil = p.id " 
			//+ " JOIN UsuariosEntity u ON up.catUsuarioPerfilPk.usuario = u.idUsuario "
			+ " where up.catUsuarioPerfilPk.usuario = :usuario  "
			//+ " where p.id = 1  "
		    + " ORDER BY p.id"
			)
	List<CatPerfilEntity> findPerfilesByUser(@Param("usuario") int usuario);
	
}
