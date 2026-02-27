package com.sodimac.catman.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.catman.api.model.entity.SupplierType;

@Repository
public interface SupplierTypeRepository extends JpaRepository<SupplierType, Integer> {

    Optional<SupplierType> findByCode(String code);

    List<SupplierType> findByStatus(Integer status);
}
