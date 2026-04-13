package com.sodimac.rebates.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.rebates.model.entity.VwRebateOrdenCompraEntity;

@Repository
public interface VwRebateOrdenCompraRepository extends JpaRepository<VwRebateOrdenCompraEntity, Integer> {

	public List<VwRebateOrdenCompraEntity> findByOrdenCompra(Integer ordenCompra);

}
