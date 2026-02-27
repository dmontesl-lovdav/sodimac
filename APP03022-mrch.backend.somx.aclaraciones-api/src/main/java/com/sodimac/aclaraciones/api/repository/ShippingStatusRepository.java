package com.sodimac.aclaraciones.api.repository;

import com.sodimac.aclaraciones.api.model.entity.ShippingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShippingStatusRepository extends JpaRepository<ShippingStatus, Integer> {
}
