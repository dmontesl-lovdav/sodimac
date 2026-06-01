package com.rebatesync.infrastructure.adapter.web;

import com.rebatesync.application.dto.SyncResultResponse;
import com.rebatesync.application.dto.SyncSummaryResponse;
import com.rebatesync.application.mapper.RebateAgreementsApplicationMapper;
import com.rebatesync.domain.model.SyncResult;
import com.rebatesync.domain.model.SyncSummary;
import com.rebatesync.domain.port.input.RebateAgreementsManagementUseCase;
import com.rebatesync.domain.port.input.RebateAgreementsSyncUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController {

    private final RebateAgreementsSyncUseCase syncUseCase;
    private final RebateAgreementsManagementUseCase managementUseCase;
    private final RebateAgreementsApplicationMapper mapper;

    /**
     * Ejecuta una sincronización completa de todos los contratos y agreements
     *
     * FLUJO COMPLETO:
     * PASO 1: Descarga todos los contratos con paginación desde API externa
     * PASO 2: Para cada contrato, descarga todos sus agreements con paginación
     * PASO 3: Mapea Contract + Agreement → RebateAgreement
     * PASO 4: Elimina todos los datos existentes e inserta nuevos (Full Sync)
     * PASO 5: Registra el resultado de la sincronización
     */
    @PostMapping("/full")
    public ResponseEntity<SyncResultResponse> executeFullSync() {
        log.info("╔════════════════════════════════════════════════════════════════╗");
        log.info("║  INICIANDO SINCRONIZACIÓN COMPLETA DE REBATE AGREEMENTS       ║");
        log.info("╚════════════════════════════════════════════════════════════════╝");
        log.info("");
        log.info("📥 PASO 1: Descargando contratos desde API externa...");
        log.info("   Endpoint: /rebate-management-cl-sod-api/v1/contracts/MX");
        log.info("   Estrategia: Paginación (page_size=100)");
        log.info("");
        log.info("📥 PASO 2: Descargando agreements por contrato...");
        log.info("   Endpoint: /rebate-management-cl-sod-api/v1/contracts/MX/agreements");
        log.info("");
        log.info("🔄 PASO 3: Mapeando datos Contract + Agreement → RebateAgreement...");
        log.info("");
        log.info("💾 PASO 4: Guardando en base de datos SODIMAC_SAP_DEV...");
        log.info("   Tabla: RebateAcuerdosTemp");
        log.info("   Estrategia: DELETE ALL + INSERT ALL");
        log.info("");

        SyncResult result = syncUseCase.executeFullSync();

        log.info("✅ PASO 5: Resultado registrado");
        log.info("");
        log.info("╔════════════════════════════════════════════════════════════════╗");
        log.info("║              SINCRONIZACIÓN COMPLETADA                         ║");
        log.info("╚════════════════════════════════════════════════════════════════╝");
        log.info("📊 Contratos descargados: {}", result.getTotalContractsDownloaded());
        log.info("📊 Agreements descargados: {}", result.getTotalAgreementsDownloaded());
        log.info("📊 Agreements cargados en BD: {}", result.getTotalAgreementsLoaded());
        log.info("📊 Registros fallidos: {}", result.getFailedRecords());
        log.info("⏱️  Duración: {} segundos", result.getDurationInSeconds());
        log.info("✓ Estado: {}", result.getStatus());
        log.info("");

        return ResponseEntity.ok(mapper.toSyncResultResponse(result));
    }

    @PostMapping("/contract/{contractId}")
    public ResponseEntity<SyncResultResponse> executeSyncByContractId(@PathVariable String contractId) {
        log.info("═══════════════════════════════════════════════════════");
        log.info("SINCRONIZACIÓN POR CONTRATO: {}", contractId);
        log.info("═══════════════════════════════════════════════════════");

        SyncResult result = syncUseCase.executeSyncByContractId(contractId);

        log.info("✓ Sincronización del contrato {} completada", contractId);
        log.info("  Agreements descargados: {}", result.getTotalAgreementsDownloaded());
        log.info("  Agreements cargados: {}", result.getTotalAgreementsLoaded());
        log.info("  Duración: {} segundos", result.getDurationInSeconds());

        return ResponseEntity.ok(mapper.toSyncResultResponse(result));
    }

    @GetMapping("/last-result")
    public ResponseEntity<SyncResultResponse> getLastSyncResult() {
        log.info("GET /api/sync/last-result - Retrieving last sync result");
        return managementUseCase.getLastSyncResult()
                .map(mapper::toSyncResultResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/summary")
    public ResponseEntity<SyncSummaryResponse> getSyncSummary() {
        log.info("GET /api/sync/summary - Retrieving sync summary");
        SyncSummary summary = managementUseCase.getSyncSummary();
        return ResponseEntity.ok(mapper.toSyncSummaryResponse(summary));
    }
}
