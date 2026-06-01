package com.rebatesync.infrastructure.adapter.persistence;

import com.rebatesync.domain.model.*;
import com.rebatesync.domain.port.output.RebateAgreementRepository;
import com.rebatesync.infrastructure.adapter.persistence.entity.*;
import com.rebatesync.infrastructure.adapter.persistence.mapper.RebateAgreementsPersistenceMapper;
import com.rebatesync.infrastructure.adapter.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RebateAgreementRepositoryAdapter implements RebateAgreementRepository {

    private final JpaRebateAgreementRepository jpaRebateAgreementRepository;
    private final JpaSyncResultRepository jpaSyncResultRepository;
    private final JpaCtrlProcDetRepository jpaCtrlProcDetRepository;
    private final JpaCtrlProcesoElementoRepository jpaCtrlProcesoElementoRepository;
    private final JpaCtrlLogRepository jpaCtrlLogRepository;
    private final RebateAgreementsPersistenceMapper mapper;

    @Qualifier("batchJdbcTemplate")
    private final JdbcTemplate batchJdbcTemplate;

    private final JdbcTemplate jdbcTemplate; // Para operaciones en SODIMAC_REBATES_DEV

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public RebateAgreement save(RebateAgreement rebateAgreement) {
        log.debug("Saving rebate agreement: {}", rebateAgreement);
        RebateAgreementEntity entity = mapper.toEntity(rebateAgreement);
        RebateAgreementEntity savedEntity = jpaRebateAgreementRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public List<RebateAgreement> saveAll(List<RebateAgreement> rebateAgreements) {
        log.debug("Saving {} rebate agreements", rebateAgreements.size());
        List<RebateAgreementEntity> entities = mapper.toEntityList(rebateAgreements);
        jpaRebateAgreementRepository.saveAll(entities);
        // Return original list instead of mapping back to avoid unnecessary SELECT query
        return rebateAgreements;
    }

    @Override
    public Optional<RebateAgreement> findById(String supplierNumber, String agreementNumber) {
        log.debug("Finding rebate agreement by supplierNumber: {} and agreementNumber: {}", supplierNumber, agreementNumber);
        RebateAgreementEntityId id = new RebateAgreementEntityId(supplierNumber, agreementNumber);
        return jpaRebateAgreementRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<RebateAgreement> findAll() {
        log.debug("Finding all rebate agreements");
        List<RebateAgreementEntity> entities = jpaRebateAgreementRepository.findAll();
        return mapper.toDomainList(entities);
    }

    @Override
    public List<RebateAgreement> findBySupplierNumber(String supplierNumber) {
        log.debug("Finding rebate agreements by supplier number: {}", supplierNumber);
        List<RebateAgreementEntity> entities = jpaRebateAgreementRepository.findBySupplierNumber(supplierNumber);
        return mapper.toDomainList(entities);
    }

    @Override
    public void deleteAll() {
        log.debug("Truncating all rebate agreements (optimized)");
        try {
            // TRUNCATE es mucho más rápido que DELETE - no registra cada fila en el log
            // y no activa triggers. Es seguro aquí porque no hay FK hacia esta tabla.
            jdbcTemplate.execute("TRUNCATE TABLE RebateAcuerdosTemp");
            log.info("✓ Table truncated successfully - much faster than DELETE");
        } catch (Exception e) {
            // Si TRUNCATE falla (ej: tabla no existe), usar JPA delete
            log.warn("TRUNCATE failed (table may not exist yet), using JPA deleteAll: {}", e.getMessage());
            jpaRebateAgreementRepository.deleteAllInBatch();
            log.info("✓ Records deleted using JPA");
        }
    }

    @Override
    public SyncResult saveSyncResult(SyncResult syncResult) {
        log.debug("Saving sync result to SODIMAC_BATCH_DEV: {}", syncResult);

        String sql = "INSERT INTO dbo.ctrlProcesoCab " +
                "(id_proceso, registros_origen, registros_destino, fecha_inicio, fecha_final, estatus, mensaje_error) " +
                "VALUES (1, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        batchJdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            // 1. registros_origen -> totalContractsDownloaded
            if (syncResult.getTotalContractsDownloaded() != null) {
                ps.setInt(1, syncResult.getTotalContractsDownloaded());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }

            // 2. registros_destino -> totalAgreementsLoaded
            if (syncResult.getTotalAgreementsLoaded() != null) {
                ps.setInt(2, syncResult.getTotalAgreementsLoaded());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }

            // 3. fecha_inicio -> startTime
            ps.setTimestamp(3, syncResult.getStartTime() != null ? Timestamp.valueOf(syncResult.getStartTime()) : null);

            // 4. fecha_final -> endTime
            ps.setTimestamp(4, syncResult.getEndTime() != null ? Timestamp.valueOf(syncResult.getEndTime()) : null);

            // 5. estatus -> status
            ps.setString(5, syncResult.getStatus() != null ? syncResult.getStatus().name() : null);

            // 6. mensaje_error -> errorMessage
            ps.setString(6, syncResult.getErrorMessage());

            return ps;
        }, keyHolder);

        // Get generated ID
        Long generatedId = keyHolder.getKey().longValue();
        log.info("✓ SyncResult guardado exitosamente en SODIMAC_BATCH_DEV con ID: {}", generatedId);

        // Return the syncResult with the generated ID
        return SyncResult.builder()
                .id(generatedId)
                .status(syncResult.getStatus())
                .startTime(syncResult.getStartTime())
                .endTime(syncResult.getEndTime())
                .totalContractsDownloaded(syncResult.getTotalContractsDownloaded())
                .totalAgreementsDownloaded(syncResult.getTotalAgreementsDownloaded())
                .totalAgreementsLoaded(syncResult.getTotalAgreementsLoaded())
                .totalPages(syncResult.getTotalPages())
                .failedRecords(syncResult.getFailedRecords())
                .errorMessage(syncResult.getErrorMessage())
                .build();
    }

    @Override
    public Optional<SyncResult> findLastSyncResult() {
        log.debug("Finding last sync result");
        return jpaSyncResultRepository.findLatest()
                .map(mapper::toDomainSyncResult);
    }

    @Override
    public List<SyncResult> findAllSyncResults() {
        log.debug("Finding all sync results");
        List<SyncResultEntity> entities = jpaSyncResultRepository.findAll();
        return mapper.toDomainSyncResultList(entities);
    }

    @Override
    public List<SyncResult> findRecentSyncResults(int limit) {
        log.debug("Finding {} recent sync results", limit);
        List<SyncResultEntity> entities = jpaSyncResultRepository.findAllByOrderByCreatedAtDesc(
                PageRequest.of(0, limit)
        );
        return mapper.toDomainSyncResultList(entities);
    }

    // ===== Implementación de métodos de control =====

    @Override
    @Transactional
    public ProcessStep saveProcessStep(ProcessStep processStep) {
        log.debug("Saving process step: {}", processStep.getNombrePaso());
        CtrlProcDetEntity entity = mapper.toEntityProcessStep(processStep);
        CtrlProcDetEntity savedEntity = jpaCtrlProcDetRepository.save(entity);
        // Flush inmediato para evitar conflictos con batch processing
        entityManager.flush();
        log.debug("✓ Process step saved with ID: {}", savedEntity.getIdFlujo());
        return mapper.toDomainProcessStep(savedEntity);
    }

    @Override
    @Transactional
    public ProcessStep updateProcessStep(ProcessStep processStep) {
        log.debug("Updating process step: {}", processStep.getIdFlujo());
        CtrlProcDetEntity entity = mapper.toEntityProcessStep(processStep);
        CtrlProcDetEntity updatedEntity = jpaCtrlProcDetRepository.save(entity);
        // Flush inmediato para evitar conflictos con batch processing
        entityManager.flush();
        log.debug("✓ Process step updated: {}", updatedEntity.getIdFlujo());
        return mapper.toDomainProcessStep(updatedEntity);
    }

    @Override
    @Transactional
    public ProcessElement saveProcessElement(ProcessElement processElement) {
        log.debug("Saving process element: {}", processElement.getValor());
        CtrlProcesoElementoEntity entity = mapper.toEntityProcessElement(processElement);
        CtrlProcesoElementoEntity savedEntity = jpaCtrlProcesoElementoRepository.save(entity);
        entityManager.flush();
        return mapper.toDomainProcessElement(savedEntity);
    }

    @Override
    @Transactional
    public void saveProcessElements(List<ProcessElement> processElements) {
        if (processElements == null || processElements.isEmpty()) {
            return;
        }
        log.debug("Saving {} process elements in batch", processElements.size());
        List<CtrlProcesoElementoEntity> entities = mapper.toEntityProcessElementList(processElements);
        jpaCtrlProcesoElementoRepository.saveAll(entities);
        // Flush después de guardar elementos de control en batch
        entityManager.flush();
        log.debug("✓ {} process elements saved", processElements.size());
    }

    @Override
    @Transactional
    public ProcessLog saveProcessLog(ProcessLog processLog) {
        log.debug("Saving process log: {} - {}", processLog.getNivel(), processLog.getNombre());
        CtrlLogEntity entity = mapper.toEntityProcessLog(processLog);
        CtrlLogEntity savedEntity = jpaCtrlLogRepository.save(entity);
        entityManager.flush();
        return mapper.toDomainProcessLog(savedEntity);
    }

    @Override
    @Transactional
    public void saveProcessLogs(List<ProcessLog> processLogs) {
        if (processLogs == null || processLogs.isEmpty()) {
            return;
        }
        log.debug("Saving {} process logs in batch", processLogs.size());
        List<CtrlLogEntity> entities = mapper.toEntityProcessLogList(processLogs);
        jpaCtrlLogRepository.saveAll(entities);
        // Flush después de guardar logs de control en batch
        entityManager.flush();
        log.debug("✓ {} process logs saved", processLogs.size());
    }
}
