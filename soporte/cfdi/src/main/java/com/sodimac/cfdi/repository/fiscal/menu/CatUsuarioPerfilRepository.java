package com.sodimac.cfdi.repository.fiscal.menu;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.fiscal.menu.CatUsuarioPerfilEntity;
import com.sodimac.cfdi.entity.fiscal.menu.CatUsuarioPerfilId;

@Repository
public interface CatUsuarioPerfilRepository extends JpaRepository<CatUsuarioPerfilEntity, CatUsuarioPerfilId>  {
	
}
