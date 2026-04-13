package com.sodimac.rebates.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.rebates.model.entity.RebatesCicmxOcEntity;

@Repository
public interface RebatesCicmxOcRepository extends JpaRepository<RebatesCicmxOcEntity, Long> {

}
