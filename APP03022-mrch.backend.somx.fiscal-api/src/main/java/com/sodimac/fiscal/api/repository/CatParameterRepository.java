package com.sodimac.fiscal.api.repository;

import com.sodimac.fiscal.api.model.entity.CatParameterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CatParameterRepository extends JpaRepository<CatParameterEntity, Integer> {
}
