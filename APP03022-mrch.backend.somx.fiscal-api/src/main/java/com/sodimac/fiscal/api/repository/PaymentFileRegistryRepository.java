package com.sodimac.fiscal.api.repository;

import com.sodimac.fiscal.api.model.entity.PaymentFileRegistryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para el registro de archivos de complementos de pago.
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
@Repository
public interface PaymentFileRegistryRepository extends JpaRepository<PaymentFileRegistryEntity, Long> {

    /**
     * Busca un registro por el UUID del complemento de pago.
     *
     * @param paymentsUuid UUID del complemento de pago
     * @return Optional con el registro si existe
     */
    Optional<PaymentFileRegistryEntity> findByPaymentsUuid(UUID paymentsUuid);

    /**
     * Busca registros por nombre de archivo.
     *
     * @param fileName Nombre del archivo
     * @return Lista de registros
     */
    List<PaymentFileRegistryEntity> findByFileName(String fileName);

    /**
     * Busca registros por estado de procesamiento.
     *
     * @param processingStatus Estado del procesamiento
     * @return Lista de registros
     */
    List<PaymentFileRegistryEntity> findByProcessingStatus(String processingStatus);

    /**
     * Busca registros por proveedor.
     *
     * @param supplierId ID del proveedor
     * @return Lista de registros
     */
    List<PaymentFileRegistryEntity> findBySupplierId(Long supplierId);
}
