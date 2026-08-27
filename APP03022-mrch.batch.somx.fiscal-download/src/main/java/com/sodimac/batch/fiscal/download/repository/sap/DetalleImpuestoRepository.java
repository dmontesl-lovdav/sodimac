package com.sodimac.batch.fiscal.download.repository.sap;

import com.sodimac.batch.fiscal.download.model.entity.sap.DetalleImpuestoEntity;
import com.sodimac.batch.fiscal.download.model.entity.sap.DetalleImpuestoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleImpuestoRepository extends JpaRepository<DetalleImpuestoEntity, DetalleImpuestoId> {
}
