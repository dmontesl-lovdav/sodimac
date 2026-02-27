package com.sodimac.fiscal.api.repository;

import com.sodimac.fiscal.api.model.entity.PacCatalogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PacCatalogRepository extends JpaRepository<PacCatalogEntity, Long> {

}