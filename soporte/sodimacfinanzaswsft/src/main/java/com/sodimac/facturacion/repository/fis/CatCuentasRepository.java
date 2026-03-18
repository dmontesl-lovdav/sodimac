package com.sodimac.facturacion.repository.fis;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.fis.CatCuentasEntity;

@Repository("catCuentasRepository")
public interface CatCuentasRepository extends JpaRepository<CatCuentasEntity, Integer> {

    @Query(value = "select id, cuenta, banco, nombreBanco, rfc, fechaCreacion from catcuentas where cuenta = :cuenta limit 1", nativeQuery = true) 
    CatCuentasEntity findCuentaByCuenta(@Param("cuenta") String cuenta);
	
}
