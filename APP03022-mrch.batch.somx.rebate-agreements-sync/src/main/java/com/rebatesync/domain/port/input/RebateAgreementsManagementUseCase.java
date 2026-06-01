package com.rebatesync.domain.port.input;

import com.rebatesync.domain.model.RebateAgreement;
import com.rebatesync.domain.model.SyncResult;
import com.rebatesync.domain.model.SyncSummary;

import java.util.List;
import java.util.Optional;

public interface RebateAgreementsManagementUseCase {

    /**
     * Retrieves all rebate agreements from the database.
     *
     * @return List of all rebate agreements
     */
    List<RebateAgreement> getAllRebateAgreements();

    /**
     * Retrieves a rebate agreement by its composite key.
     *
     * @param supplierNumber the supplier number
     * @param agreementNumber the agreement number
     * @return Optional containing the rebate agreement if found
     */
    Optional<RebateAgreement> getRebateAgreementById(String supplierNumber, String agreementNumber);

    /**
     * Retrieves rebate agreements by supplier number.
     *
     * @param supplierNumber the supplier number
     * @return List of rebate agreements for the supplier
     */
    List<RebateAgreement> getRebateAgreementsBySupplier(String supplierNumber);

    /**
     * Retrieves the last sync result.
     *
     * @return Optional containing the last sync result if available
     */
    Optional<SyncResult> getLastSyncResult();

    /**
     * Retrieves a summary of sync executions.
     *
     * @return SyncSummary containing statistics and recent sync results
     */
    SyncSummary getSyncSummary();

    /**
     * Deletes all rebate agreements from the database.
     */
    void deleteAllRebateAgreements();
}
