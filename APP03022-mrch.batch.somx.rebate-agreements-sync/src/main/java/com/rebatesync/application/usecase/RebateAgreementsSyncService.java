package com.rebatesync.application.usecase;

import com.rebatesync.domain.enums.SyncStatus;
import com.rebatesync.domain.exception.ExternalServiceException;
import com.rebatesync.domain.exception.SyncException;
import com.rebatesync.domain.model.*;
import com.rebatesync.domain.port.input.RebateAgreementsSyncUseCase;
import com.rebatesync.domain.port.output.ExternalRebateClient;
import com.rebatesync.domain.port.output.NotificationService;
import com.rebatesync.domain.port.output.RebateAgreementRepository;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RebateAgreementsSyncService implements RebateAgreementsSyncUseCase {

    private final ExternalRebateClient externalRebateClient;
    private final RebateAgreementRepository rebateAgreementRepository;
    private final NotificationService notificationService;

    // Contador para elementos procesados
    private int elementSequence = 0;

    @Override
    @Retry(name = "rebateSync", fallbackMethod = "fallbackExecuteFullSync")
    // REMOVIDO @Transactional - La transacción gigante bloqueaba la BD por 15-20 minutos
    // Ahora solo la carga de datos (TRUNCATE + INSERT) está en transacción
    public SyncResult executeFullSync() {
        log.info("Starting full sync process");
        elementSequence = 0;

        SyncResult syncResult = SyncResult.builder()
                .startTime(LocalDateTime.now())
                .status(SyncStatus.IN_PROGRESS)
                .build();

        Long idEjecucion = null;

        try {
            // Save initial sync result to get ID
            SyncResult initialSyncResult = rebateAgreementRepository.saveSyncResult(syncResult);
            idEjecucion = initialSyncResult.getId();
            log.info("✓ Sync process started with ID: {}", idEjecucion);

            // Log inicio del proceso
            saveLog(idEjecucion, "INFO", "INICIO", "Inicio del proceso de sincronización completa");

            // Step 1: Download all contracts with pagination
            // NO EN TRANSACCIÓN - Llamadas a API externa no deben bloquear la BD
            ProcessStep step1 = createProcessStep(idEjecucion, 1, "Descargar contratos desde API externa");
            log.info("Step 1: Downloading contracts from external service");

            List<Contract> allContracts = downloadAllContracts();
            syncResult.setTotalContractsDownloaded(allContracts.size());

            completeProcessStep(step1, allContracts.size());
            log.info("Downloaded {} contracts", allContracts.size());
            saveLog(idEjecucion, "INFO", "EXTRACT",
                String.format("Descargados %d contratos desde API", allContracts.size()));

            // Step 2: Download all agreements for each contract
            // NO EN TRANSACCIÓN - Llamadas a API externa no deben bloquear la BD
            ProcessStep step2 = createProcessStep(idEjecucion, 2, "Descargar agreements de cada contrato");
            log.info("Step 2: Downloading agreements for all contracts");
            List<RebateAgreement> allRebateAgreements = new ArrayList<>();
            List<ProcessElement> processElements = new ArrayList<>();

            for (Contract contract : allContracts) {
                List<Agreement> agreements = downloadAgreementsForContract(contract);
                List<RebateAgreement> rebateAgreements = mapToRebateAgreements(contract, agreements);
                allRebateAgreements.addAll(rebateAgreements);

                // Registrar cada agreement procesado en ctrlProcesoElemento
                for (RebateAgreement ra : rebateAgreements) {
                    ProcessElement element = ProcessElement.builder()
                            .idEjecucion(idEjecucion)
                            .valor(ra.getAgreementNumber())
                            .valorAlterno(ra.getSupplierNumber() + " - " + ra.getBusinessName())
                            .secuencia(++elementSequence)
                            .estatus("PROCESSED")
                            .build();
                    processElements.add(element);
                }
            }

            syncResult.setTotalAgreementsDownloaded(allRebateAgreements.size());
            completeProcessStep(step2, allRebateAgreements.size());

            // Guardar elementos procesados en batch
            rebateAgreementRepository.saveProcessElements(processElements);
            log.info("Downloaded {} total agreements", allRebateAgreements.size());
            saveLog(idEjecucion, "INFO", "EXTRACT",
                String.format("Descargados %d agreements en total", allRebateAgreements.size()));

            // Step 3 & 4: Load data to database
            // ESTA ES LA ÚNICA PARTE EN TRANSACCIÓN - Minimiza el tiempo de bloqueo
            ProcessStep step3 = createProcessStep(idEjecucion, 3, "Eliminar datos existentes");
            ProcessStep step4 = createProcessStep(idEjecucion, 4, "Cargar nuevos datos en base de datos");

            log.info("Step 3 & 4: Loading data to database (transactional)");
            int loadedCount = loadDataToDatabase(allRebateAgreements);

            syncResult.setTotalAgreementsLoaded(loadedCount);
            completeProcessStep(step3, 0);
            completeProcessStep(step4, loadedCount);

            log.info("Loaded {} agreements into database", loadedCount);
            saveLog(idEjecucion, "INFO", "LOAD",
                String.format("Cargados %d agreements en la base de datos", loadedCount));

            // Step 5: Finalize sync result
            syncResult.setEndTime(LocalDateTime.now());
            syncResult.setStatus(SyncStatus.SUCCESS);
            syncResult.setFailedRecords(0);
            syncResult.setId(idEjecucion);

            // Update sync result with final status
            SyncResult finalSyncResult = rebateAgreementRepository.saveSyncResult(syncResult);

            // Send success notification
            notificationService.notifySuccess(finalSyncResult);

            saveLog(idEjecucion, "INFO", "FINALIZACION",
                String.format("Proceso completado exitosamente en %d segundos",
                    finalSyncResult.getDurationInSeconds()));

            log.info("Full sync completed successfully in {} seconds", finalSyncResult.getDurationInSeconds());
            return finalSyncResult;

        } catch (Exception e) {
            log.error("Error during full sync process", e);
            syncResult.setEndTime(LocalDateTime.now());
            syncResult.setStatus(SyncStatus.FAILED);
            syncResult.setErrorMessage(e.getMessage());
            syncResult.setId(idEjecucion);

            SyncResult savedSyncResult = rebateAgreementRepository.saveSyncResult(syncResult);
            notificationService.notifyFailure(savedSyncResult);

            if (idEjecucion != null) {
                saveLog(idEjecucion, "ERROR", "ERROR",
                    "Error en el proceso: " + e.getMessage());
            }

            throw new SyncException("Full sync failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Retry(name = "rebateSync", fallbackMethod = "fallbackExecuteSyncByContractId")
    // REMOVIDO @Transactional - Las llamadas a API externa no deben estar en transacción
    public SyncResult executeSyncByContractId(String contractId) {
        log.info("Starting sync for contract ID: {}", contractId);
        elementSequence = 0;

        SyncResult syncResult = SyncResult.builder()
                .startTime(LocalDateTime.now())
                .status(SyncStatus.IN_PROGRESS)
                .build();

        Long idEjecucion = null;

        try {
            // Save initial sync result to get ID
            SyncResult initialSyncResult = rebateAgreementRepository.saveSyncResult(syncResult);
            idEjecucion = initialSyncResult.getId();
            log.info("✓ Sync process for contract {} started with ID: {}", contractId, idEjecucion);

            saveLog(idEjecucion, "INFO", "INICIO",
                "Inicio de sincronización para contrato: " + contractId);

            // Step 1: Download specific contract
            ProcessStep step1 = createProcessStep(idEjecucion, 1, "Descargar contrato " + contractId);
            Contract contract = externalRebateClient.fetchContractById(contractId);
            syncResult.setTotalContractsDownloaded(1);
            completeProcessStep(step1, 1);

            // Step 2: Download agreements for the contract
            ProcessStep step2 = createProcessStep(idEjecucion, 2, "Descargar agreements del contrato");
            List<Agreement> agreements = downloadAgreementsForContract(contract);
            List<RebateAgreement> rebateAgreements = mapToRebateAgreements(contract, agreements);
            syncResult.setTotalAgreementsDownloaded(rebateAgreements.size());

            // Registrar elementos procesados
            List<ProcessElement> processElements = new ArrayList<>();
            for (RebateAgreement ra : rebateAgreements) {
                ProcessElement element = ProcessElement.builder()
                        .idEjecucion(idEjecucion)
                        .valor(ra.getAgreementNumber())
                        .valorAlterno(ra.getSupplierNumber() + " - " + ra.getBusinessName())
                        .secuencia(++elementSequence)
                        .estatus("PROCESSED")
                        .build();
                processElements.add(element);
            }
            rebateAgreementRepository.saveProcessElements(processElements);
            completeProcessStep(step2, rebateAgreements.size());

            saveLog(idEjecucion, "INFO", "EXTRACT",
                String.format("Descargados %d agreements del contrato %s",
                    rebateAgreements.size(), contractId));

            // Step 3: Save agreements (transactional)
            ProcessStep step3 = createProcessStep(idEjecucion, 3, "Guardar agreements en base de datos");
            int savedCount = saveDataToDatabase(rebateAgreements);
            syncResult.setTotalAgreementsLoaded(savedCount);
            completeProcessStep(step3, savedCount);

            saveLog(idEjecucion, "INFO", "LOAD",
                String.format("Guardados %d agreements en base de datos", savedCount));

            // Finalize
            syncResult.setEndTime(LocalDateTime.now());
            syncResult.setStatus(SyncStatus.SUCCESS);
            syncResult.setFailedRecords(0);
            syncResult.setId(idEjecucion);

            SyncResult savedSyncResult = rebateAgreementRepository.saveSyncResult(syncResult);
            notificationService.notifySuccess(savedSyncResult);

            saveLog(idEjecucion, "INFO", "FINALIZACION",
                String.format("Sincronización de contrato %s completada en %d segundos",
                    contractId, savedSyncResult.getDurationInSeconds()));

            log.info("Sync for contract {} completed successfully", contractId);
            return savedSyncResult;

        } catch (Exception e) {
            log.error("Error during sync for contract {}", contractId, e);
            syncResult.setEndTime(LocalDateTime.now());
            syncResult.setStatus(SyncStatus.FAILED);
            syncResult.setErrorMessage(e.getMessage());
            syncResult.setId(idEjecucion);

            SyncResult savedSyncResult = rebateAgreementRepository.saveSyncResult(syncResult);
            notificationService.notifyFailure(savedSyncResult);

            if (idEjecucion != null) {
                saveLog(idEjecucion, "ERROR", "ERROR",
                    "Error al sincronizar contrato " + contractId + ": " + e.getMessage());
            }

            throw new SyncException("Sync failed for contract " + contractId + ": " + e.getMessage(), e);
        }
    }

    private List<Contract> downloadAllContracts() {
        List<Contract> allContracts = new ArrayList<>();
        int page = 1;
        int pageSize = 100;
        boolean hasMore = true;

        while (hasMore) {
            try {
                log.debug("Fetching contracts page {} with page size {}", page, pageSize);
                List<Contract> contracts = externalRebateClient.fetchContracts(page, pageSize);

                if (contracts == null || contracts.isEmpty()) {
                    hasMore = false;
                } else {
                    allContracts.addAll(contracts);
                    hasMore = contracts.size() >= pageSize;
                    page++;
                }
            } catch (Exception e) {
                log.error("Error fetching contracts at page {}", page, e);
                throw new ExternalServiceException("Failed to fetch contracts at page " + page, e);
            }
        }

        return allContracts;
    }

    private List<Agreement> downloadAgreementsForContract(Contract contract) {
        if (contract.getAgreementIds() == null || contract.getAgreementIds().isEmpty()) {
            log.debug("No agreements found for contract {}", contract.getContractId());
            return new ArrayList<>();
        }

        List<Agreement> allAgreements = new ArrayList<>();
        int page = 1;
        int pageSize = 100;
        boolean hasMore = true;

        while (hasMore) {
            try {
                log.debug("Fetching agreements for contract {} - page {} with page size {}",
                    contract.getContractId(), page, pageSize);

                List<Agreement> agreements = externalRebateClient.fetchAgreements(
                    contract.getContractId(),
                    page,
                    pageSize
                );

                if (agreements == null || agreements.isEmpty()) {
                    hasMore = false;
                } else {
                    allAgreements.addAll(agreements);
                    hasMore = agreements.size() >= pageSize;
                    page++;
                }
            } catch (Exception e) {
                log.error("Failed to fetch agreements for contract {} at page {}",
                    contract.getContractId(), page, e);
                throw new ExternalServiceException(
                    "Failed to fetch agreements for contract " + contract.getContractId(), e);
            }
        }

        log.debug("Downloaded {} agreements for contract {}", allAgreements.size(), contract.getContractId());
        return allAgreements;
    }

    private List<RebateAgreement> mapToRebateAgreements(Contract contract, List<Agreement> agreements) {
        if (agreements == null || agreements.isEmpty()) {
            log.debug("No agreements to map for contract {}", contract.getContractId());
            return new ArrayList<>();
        }

        List<RebateAgreement> rebateAgreements = new ArrayList<>();

        log.debug("Mapping {} agreements for contract {} (Supplier: {}, Name: {})",
            agreements.size(),
            contract.getContractId(),
            contract.getVendorTaxId(),
            contract.getVendorName());

        for (Agreement agreement : agreements) {
            try {
                RebateAgreement rebateAgreement = RebateAgreement.builder()
                        .supplierNumber(contract.getVendorTaxId())
                        .agreementNumber(agreement.getAgreementId())
                        .rfc(null)
                        .businessName(contract.getVendorName())
                        .status(contract.getStatus() != null ? contract.getStatus().name() : null)
                        .family(contract.getProductCategoryId())
                        .commercialClassification(agreement.getProductTypeSelector())
                        .agreementType(agreement.getRebateType() != null ? agreement.getRebateType().name() : null)
                        .currency(agreement.getCurrencyCode())
                        .value(agreement.getContributionAmount() != null ? agreement.getContributionAmount().toString() : null)
                        .valueType(agreement.getContributionType() != null ? agreement.getContributionType().name() : null)
                        .fillRate(agreement.getAgreedFillRate() != null ? agreement.getAgreedFillRate().toString() : null)
                        .paymentProgram(agreement.getPaymentPeriodLabel())
                        .brand(agreement.getBrand())
                        .build();

                rebateAgreements.add(rebateAgreement);

                log.trace("Mapped agreement: Contract={}, Type={}, Value={}, Currency={}",
                    contract.getContractId(),
                    agreement.getRebateType(),
                    agreement.getContributionAmount(),
                    agreement.getCurrencyCode());

            } catch (Exception e) {
                log.error("Error mapping agreement {} for contract {}",
                    agreement.getAgreementId(), contract.getContractId(), e);
            }
        }

        log.debug("Successfully mapped {} rebate agreements for contract {}",
            rebateAgreements.size(), contract.getContractId());

        return rebateAgreements;
    }

    // Fallback methods for Resilience4j
    private SyncResult fallbackExecuteFullSync(Exception e) {
        log.error("Fallback: All retry attempts exhausted for full sync", e);

        SyncResult syncResult = SyncResult.builder()
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .status(SyncStatus.FAILED)
                .errorMessage("All retry attempts exhausted: " + e.getMessage())
                .build();

        SyncResult savedSyncResult = rebateAgreementRepository.saveSyncResult(syncResult);
        notificationService.notifyFailure(savedSyncResult);

        return savedSyncResult;
    }

    private SyncResult fallbackExecuteSyncByContractId(String contractId, Exception e) {
        log.error("Fallback: All retry attempts exhausted for contract sync: {}", contractId, e);

        SyncResult syncResult = SyncResult.builder()
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .status(SyncStatus.FAILED)
                .errorMessage("All retry attempts exhausted for contract " + contractId + ": " + e.getMessage())
                .build();

        SyncResult savedSyncResult = rebateAgreementRepository.saveSyncResult(syncResult);
        notificationService.notifyFailure(savedSyncResult);

        return savedSyncResult;
    }

    /**
     * Carga datos a la base de datos en una transacción corta y rápida.
     * TRUNCATE + INSERT en lotes - Minimiza tiempo de bloqueo de la tabla.
     * Usado para full sync.
     *
     * @param rebateAgreements Lista de agreements a cargar
     * @return Cantidad de registros cargados
     */
    @Transactional
    private int loadDataToDatabase(List<RebateAgreement> rebateAgreements) {
        log.info("Starting transactional data load - TRUNCATE + INSERT");

        // TRUNCATE TABLE - Lock exclusivo pero muy rápido
        log.debug("Executing TRUNCATE TABLE...");
        rebateAgreementRepository.deleteAll();
        log.info("✓ Table truncated");

        // INSERT en lotes de 100 (configurado en Hibernate batch_size)
        log.debug("Inserting {} agreements in batches...", rebateAgreements.size());
        List<RebateAgreement> savedAgreements = rebateAgreementRepository.saveAll(rebateAgreements);
        log.info("✓ Data loaded successfully");

        return savedAgreements.size();
    }

    /**
     * Guarda datos a la base de datos en una transacción corta.
     * Solo INSERT en lotes - Sin TRUNCATE.
     * Usado para sync por contrato específico.
     *
     * @param rebateAgreements Lista de agreements a guardar
     * @return Cantidad de registros guardados
     */
    @Transactional
    private int saveDataToDatabase(List<RebateAgreement> rebateAgreements) {
        log.info("Starting transactional data save - INSERT only");

        // INSERT en lotes de 100 (configurado en Hibernate batch_size)
        log.debug("Inserting {} agreements in batches...", rebateAgreements.size());
        List<RebateAgreement> savedAgreements = rebateAgreementRepository.saveAll(rebateAgreements);
        log.info("✓ Data saved successfully");

        return savedAgreements.size();
    }

    // ===== Métodos auxiliares para tablas de control =====

    /**
     * Crea y guarda un paso del proceso
     */
    private ProcessStep createProcessStep(Long idEjecucion, Integer secuencia, String nombrePaso) {
        ProcessStep step = ProcessStep.builder()
                .idEjecucion(idEjecucion)
                .nombrePaso(nombrePaso)
                .secuencia(secuencia)
                .fechaInicioRegistro(LocalDateTime.now())
                .estatus("IN_PROGRESS")
                .registrosProcesados(0)
                .build();

        ProcessStep savedStep = rebateAgreementRepository.saveProcessStep(step);
        log.debug("✓ Step {} created: {}", secuencia, nombrePaso);
        return savedStep;
    }

    /**
     * Completa un paso del proceso
     */
    private void completeProcessStep(ProcessStep step, Integer registrosProcesados) {
        step.setFechaFinalRegistro(LocalDateTime.now());
        step.setEstatus("SUCCESS");
        step.setRegistrosProcesados(registrosProcesados);
        rebateAgreementRepository.updateProcessStep(step);
        log.debug("✓ Step {} completed with {} records", step.getSecuencia(), registrosProcesados);
    }

    /**
     * Guarda un log del proceso
     */
    private void saveLog(Long idEjecucion, String nivel, String fase, String mensaje) {
        ProcessLog processLog = ProcessLog.builder()
                .idEjecucion(idEjecucion)
                .nombre("Rebate Sync")
                .nivel(nivel)
                .fase(fase)
                .log(mensaje)
                .build();

        rebateAgreementRepository.saveProcessLog(processLog);
    }
}
