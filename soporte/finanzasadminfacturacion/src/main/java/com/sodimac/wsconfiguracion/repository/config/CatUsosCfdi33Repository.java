package com.sodimac.wsconfiguracion.repository.config;

import com.sodimac.wsconfiguracion.entity.config.CatUsosCfdi33Entity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("catUsosCfdi33Repository")
public interface CatUsosCfdi33Repository extends JpaRepository<CatUsosCfdi33Entity, Integer> {
   List<CatUsosCfdi33Entity> findByFisica(boolean valor);

   List<CatUsosCfdi33Entity> findByMoral(boolean valor);

   CatUsosCfdi33Entity findByClave(String clave);
}
