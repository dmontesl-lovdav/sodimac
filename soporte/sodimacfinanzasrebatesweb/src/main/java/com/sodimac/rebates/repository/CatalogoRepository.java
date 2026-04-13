package com.sodimac.rebates.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.sodimac.rebates.model.AdminCatalogo;
import com.sodimac.rebates.model.CatalogoId;

public interface CatalogoRepository extends JpaRepository<AdminCatalogo, CatalogoId> {

	List<AdminCatalogo> findByActivo(boolean activo);

	AdminCatalogo findByCatalogoId(CatalogoId catalogoId);

	@Query("SELECT a FROM AdminCatalogo a WHERE a.catalogoId.catalogo.idCatalogo = ?#{#catalogoId.catalogo.idCatalogo} AND a.activo = true")
	List<AdminCatalogo> findByCatalogoSpecific(@Param("catalogoId") CatalogoId catalogoId);

}
