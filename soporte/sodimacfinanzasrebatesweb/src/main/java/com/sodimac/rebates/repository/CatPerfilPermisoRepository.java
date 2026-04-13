package com.sodimac.rebates.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.rebates.model.entity.CatPermisoEntity;
import com.sodimac.rebates.model.entity.CatRolPermisoEntity;
import com.sodimac.rebates.model.entity.CatRolPermisoId;

@Repository
public interface CatPerfilPermisoRepository  extends JpaRepository<CatRolPermisoEntity, CatRolPermisoId>  {
	@Query(
		    " Select DISTINCT m  " +
			" from CatPermisoEntity m"
			+ " JOIN CatRolPermisoEntity rp ON m.id = rp.catRolPermisoPk.permiso "
			+ " JOIN CatRolEntity r ON rp.catRolPermisoPk.rol = r.id "
			+ " JOIN CatPerfilRolEntity pr ON pr.catPerfilRolPk.rol = r.id " 
			+ " JOIN CatPerfilEntity p ON pr.catPerfilRolPk.perfil = p.id " 
			+ " JOIN CatUsuarioPerfilEntity up  ON up.catUsuarioPerfilPk.perfil = p.id " 
			+ " JOIN Usuario u ON up.catUsuarioPerfilPk.usuario = u.id "
			+ " where up.catUsuarioPerfilPk.usuario = :usuario  "
			//+ " where p.id = 1  "
		    + " ORDER BY m.id"
			)
	List<CatPermisoEntity> findPermisosByUser(@Param("usuario") int usuario);
}
