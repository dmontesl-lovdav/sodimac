package com.sodimac.rebates.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.rebates.model.CatTipoOrdenCompraEntity;

@Repository
public interface CatTipoOrdenCompraRepository extends JpaRepository<CatTipoOrdenCompraEntity, Integer> {

	public List<CatTipoOrdenCompraEntity> findByEstatusOrderByDescripcion(int estatus);

}
