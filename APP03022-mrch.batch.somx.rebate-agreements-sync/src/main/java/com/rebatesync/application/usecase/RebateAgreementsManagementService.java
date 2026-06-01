package com.rebatesync.application.usecase;

import com.rebatesync.domain.model.RebateAgreement;
import com.rebatesync.domain.model.SyncResult;
import com.rebatesync.domain.model.SyncSummary;
import com.rebatesync.domain.port.input.RebateAgreementsManagementUseCase;
import com.rebatesync.domain.port.output.RebateAgreementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RebateAgreementsManagementService implements RebateAgreementsManagementUseCase {

    private final RebateAgreementRepository rebateAgreementRepository;

    @Override
    public List<RebateAgreement> getAllRebateAgreements() {
        log.info("Retrieving all rebate agreements");
        return rebateAgreementRepository.findAll();
    }

    @Override
    public Optional<RebateAgreement> getRebateAgreementById(String supplierNumber, String agreementNumber) {
        log.info("Retrieving rebate agreement with supplierNumber: {} and agreementNumber: {}", supplierNumber, agreementNumber);
        return rebateAgreementRepository.findById(supplierNumber, agreementNumber);
    }

    @Override
    public List<RebateAgreement> getRebateAgreementsBySupplier(String supplierNumber) {
        log.info("Retrieving rebate agreements for supplier: {}", supplierNumber);
        return rebateAgreementRepository.findBySupplierNumber(supplierNumber);
    }

    @Override
    public Optional<SyncResult> getLastSyncResult() {
        log.info("Retrieving last sync result");
        return rebateAgreementRepository.findLastSyncResult();
    }

    @Override
    public SyncSummary getSyncSummary() {
        log.info("Generating sync summary");

        List<SyncResult> allSyncResults = rebateAgreementRepository.findAllSyncResults();
        List<SyncResult> recentSyncs = rebateAgreementRepository.findRecentSyncResults(10);

        long successCount = allSyncResults.stream()
                .filter(sr -> sr.getStatus().name().equals("SUCCESS"))
                .count();

        long failedCount = allSyncResults.stream()
                .filter(sr -> sr.getStatus().name().equals("FAILED"))
                .count();

        Long averageDuration = (long) allSyncResults.stream()
                .map(SyncResult::getDurationInSeconds)
                .filter(d -> d != null && d > 0)
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        return SyncSummary.builder()
                .executionTime(LocalDateTime.now())
                .totalExecutions(allSyncResults.size())
                .successfulExecutions((int) successCount)
                .failedExecutions((int) failedCount)
                .averageDurationInSeconds(averageDuration)
                .recentSyncs(recentSyncs)
                .build();
    }

    @Override
    public void deleteAllRebateAgreements() {
        log.info("Deleting all rebate agreements");
        rebateAgreementRepository.deleteAll();
        log.info("All rebate agreements deleted successfully");
    }
}
