package com.sodimac.rebates.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.rebates.model.entity.CatRolEntity;

@Repository
public interface CatRolRepository extends JpaRepository<CatRolEntity, Integer>{
	
	@Query(
		    " Select DISTINCT r " +
			" from CatUsuarioPerfilEntity up"
			+ " JOIN CatPerfilRolEntity pr ON up.catUsuarioPerfilPk.perfil = pr.catPerfilRolPk.perfil "
			+ " JOIN CatRolEntity r ON pr.catPerfilRolPk.rol = r.id "
			+ " where up.catUsuarioPerfilPk.usuario = :usuario  "
			)
	public List<CatRolEntity> findRolesByUser(@Param("usuario") int usuario);
	
	
}
