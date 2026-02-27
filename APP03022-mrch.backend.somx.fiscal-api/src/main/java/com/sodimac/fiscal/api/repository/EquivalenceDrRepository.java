package com.sodimac.fiscal.api.repository;

import com.sodimac.fiscal.api.model.entity.EquivalenceDrEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EquivalenceDrRepository extends JpaRepository<EquivalenceDrEntity, UUID> {

}