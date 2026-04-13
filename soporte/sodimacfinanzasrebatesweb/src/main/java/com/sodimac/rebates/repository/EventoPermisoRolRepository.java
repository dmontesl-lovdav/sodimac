package com.sodimac.rebates.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.rebates.model.entity.EventoPermisoRolEntity;
import com.sodimac.rebates.model.entity.EventoPermisoRolEntityPK;

@Repository
public interface EventoPermisoRolRepository extends JpaRepository<EventoPermisoRolEntity, EventoPermisoRolEntityPK> {

	@Query(
		    " Select DISTINCT m " +
			" from EventoPermisoRolEntity m "
			+ " JOIN CatEventoEntity e ON m.eventoPermisoRolEntityPK.idCatEvento = e.idCatEvento "
			+ " JOIN CatRolEntity r ON m.eventoPermisoRolEntityPK.idRol = r.id "
			+ " JOIN CatPermisoEntity p ON m.eventoPermisoRolEntityPK.idPermiso = p.id "
			+ " where m.activo = 1 "
			+ " and   e.activo = 1 "
			+ " and   r.activo = 1 "
			+ " and   p.activo = 1 "
			+ " and   p.id = :pIdPermiso "
			+ " and   m.eventoPermisoRolEntityPK.idRol in (select pr.catPerfilRolPk.perfil "
			+ "           								   from CatUsuarioPerfilEntity up "
			+ "           								   JOIN CatPerfilRolEntity pr on up.catUsuarioPerfilPk.perfil = pr.catPerfilRolPk.perfil "
			+ "           								   where up.catUsuarioPerfilPk.usuario = :pIdUsuario) "
			)
	public List<EventoPermisoRolEntity> findEventosByIdPermiso( @Param("pIdUsuario") Integer pIdUsuario
															  , @Param("pIdPermiso") Integer pIdPermiso);
	
}
