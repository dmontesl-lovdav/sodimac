package com.sodimac.cfdi.repository.fiscal.menu;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.fiscal.menu.CatMenuEntity;

@Repository
public interface CatMenuRepository extends JpaRepository<CatMenuEntity, Integer>  {


	@Query(
		    " Select DISTINCT m  " +
			" from CatMenuEntity m"
			+ " JOIN CatRolMenuEntity rm ON m.id = rm.catPerfilRolPk.menu "
			+ " JOIN CatRolEntity r ON rm.catPerfilRolPk.rol = r.id "
			+ " JOIN CatPerfilRolEntity pr ON pr.catPerfilRolPk.rol = r.id " 
			+ " JOIN CatPerfilEntity p ON pr.catPerfilRolPk.perfil = p.id " 
			+ " JOIN CatUsuarioPerfilEntity up  ON up.catUsuarioPerfilPk.perfil = p.id " 
			+ " JOIN UsuariosEntity u ON up.catUsuarioPerfilPk.usuario = u.idUsuario "
			+ " where up.catUsuarioPerfilPk.usuario = :usuario  "
			//+ " where p.id = 1  "
		    + " ORDER BY m.id"
			)
	List<CatMenuEntity> findMenuByPerfil(@Param("usuario") int usuario);
	
}
