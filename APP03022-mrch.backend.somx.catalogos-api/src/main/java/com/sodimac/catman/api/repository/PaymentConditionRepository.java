package com.sodimac.catman.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.catman.api.model.entity.PaymentCondition;

@Repository
public interface PaymentConditionRepository extends JpaRepository<PaymentCondition, Integer> {

    List<PaymentCondition> findByStatus(Integer status);

    List<PaymentCondition> findBySupplierNumber(String supplierNumber);

    List<PaymentCondition> findBySupplierNumberAndStatus(String supplierNumber, Integer status);
}
