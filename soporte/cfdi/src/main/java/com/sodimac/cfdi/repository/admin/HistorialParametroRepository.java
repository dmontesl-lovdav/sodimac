package com.sodimac.cfdi.repository.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.admin.HistorialParametroEntity;

@Repository("historialParametroRepository")
public interface HistorialParametroRepository extends JpaRepository<HistorialParametroEntity, Integer> {

}
