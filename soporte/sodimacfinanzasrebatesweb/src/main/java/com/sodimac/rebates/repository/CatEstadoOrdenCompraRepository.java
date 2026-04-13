package com.sodimac.rebates.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.rebates.model.CatEstadoOrdenCompraEntity;

@Repository
public interface CatEstadoOrdenCompraRepository extends JpaRepository<CatEstadoOrdenCompraEntity, Integer> {

	public List<CatEstadoOrdenCompraEntity> findByEstatus(int estatus);

}
