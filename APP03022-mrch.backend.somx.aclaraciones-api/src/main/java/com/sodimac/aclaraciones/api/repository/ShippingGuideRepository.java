package com.sodimac.aclaraciones.api.repository;

import com.sodimac.aclaraciones.api.model.entity.ShippingGuide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ShippingGuideRepository
        extends JpaRepository<ShippingGuide, Long>, JpaSpecificationExecutor<ShippingGuide> {
}
