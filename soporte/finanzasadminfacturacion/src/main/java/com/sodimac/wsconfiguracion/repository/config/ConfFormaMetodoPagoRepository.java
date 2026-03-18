package com.sodimac.wsconfiguracion.repository.config;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.wsconfiguracion.entity.config.ConfFormaMetodoPagoEntity;

@Repository("confFormaMetodoPagoRepositoryConfig")
public interface ConfFormaMetodoPagoRepository extends JpaRepository<ConfFormaMetodoPagoEntity, Integer>{

	@Query("SELECT a FROM ConfFormaMetodoPagoEntity a WHERE a.catTipoComprobanteSodimacEntity.tipocomprobante = :tipoComprobante AND a.catMedioPagoEntity.idMedioPago = :medioPago AND a.versionEntity.version = :version")
	List<ConfFormaMetodoPagoEntity> findByComprobanteMedioVersion(@Param("tipoComprobante") String tipoComprobante, @Param("medioPago") String medioPago, @Param("version") String version);
}
