package com.sodimac.rebates.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.rebates.model.Usuario;
import com.sodimac.rebates.model.entity.CatUsuarioPerfilEntity;
import com.sodimac.rebates.model.entity.CatUsuarioPerfilId;

@Repository
public interface CatUsuarioPerfilRepository extends JpaRepository<CatUsuarioPerfilEntity, CatUsuarioPerfilId> {

	public List<CatUsuarioPerfilEntity> findByUsuario(Usuario usuario);
	
}
