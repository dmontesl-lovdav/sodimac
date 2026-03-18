package com.sodimac.wsconfiguracion.repository.config;

import com.sodimac.wsconfiguracion.entity.config.CatUsoCfdiEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("catUsoCfdiRepositoryConfig")
public interface CatUsoCfdiRepository extends JpaRepository<CatUsoCfdiEntity, Integer> {
   @Query(
      value = "{call uspObtenerUsoCfdi (:pIdVersionCfdi, :pIdTipoPersona, :pRegimenFiscal)}",
      nativeQuery = true
   )
   List<Object[]> getUsoCfdi(@Param("pIdVersionCfdi") Integer pIdVersionCfdi, @Param("pIdTipoPersona") Integer pIdTipoPersona, @Param("pRegimenFiscal") String pRegimenFiscal);

   List<CatUsoCfdiEntity> findByActivo(Boolean activo);
}
