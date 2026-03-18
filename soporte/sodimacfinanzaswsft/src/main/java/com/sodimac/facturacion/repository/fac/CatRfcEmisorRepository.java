package com.sodimac.facturacion.repository.fac;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.fac.CatRfcEmisorEntity;

@Repository("catRfcEmisorRepository")
public interface CatRfcEmisorRepository extends JpaRepository<CatRfcEmisorEntity, Integer> {

	CatRfcEmisorEntity findByRfc(String rfc);
}