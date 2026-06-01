package com.rebatesync.domain.port.output;

import com.rebatesync.domain.model.*;

import java.util.List;
import java.util.Optional;

public interface RebateAgreementRepository {

    /**
     * Saves a rebate agreement to the database.
     *
     * @param rebateAgreement the rebate agreement to save
     * @return the saved rebate agreement
     */
    RebateAgreement save(RebateAgreement rebateAgreement);

    /**
     * Saves multiple rebate agreements to the database.
     *
     * @param rebateAgreements the list of rebate agreements to save
     * @return the list of saved rebate agreements
     */
    List<RebateAgreement> saveAll(List<RebateAgreement> rebateAgreements);

    /**
     * Finds a rebate agreement by its composite key (supplier number + agreement number).
     *
     * @param supplierNumber the supplier number
     * @param agreementNumber the agreement number
     * @return Optional containing the rebate agreement if found
     */
    Optional<RebateAgreement> findById(String supplierNumber, String agreementNumber);

    /**
     * Finds all rebate agreements.
     *
     * @return List of all rebate agreements
     */
    List<RebateAgreement> findAll();

    /**
     * Finds rebate agreements by supplier number.
     *
     * @param supplierNumber the supplier number
     * @return List of rebate agreements for the supplier
     */
    List<RebateAgreement> findBySupplierNumber(String supplierNumber);

    /**
     * Deletes all rebate agreements from the database.
     */
    void deleteAll();

    /**
     * Saves a sync result to the database.
     *
     * @param syncResult the sync result to save
     * @return the saved sync result
     */
    SyncResult saveSyncResult(SyncResult syncResult);

    /**
     * Finds the last sync result.
     *
     * @return Optional containing the last sync result if available
     */
    Optional<SyncResult> findLastSyncResult();

    /**
     * Finds all sync results.
     *
     * @return List of all sync results
     */
    List<SyncResult> findAllSyncResults();

    /**
     * Finds the most recent sync results, limited by the specified count.
     *
     * @param limit the maximum number of results to return
     * @return List of recent sync results
     */
    List<SyncResult> findRecentSyncResults(int limit);

    // ===== Métodos para tablas de control =====

    /**
     * Saves a process step to the database.
     *
     * @param processStep the process step to save
     * @return the saved process step with generated ID
     */
    ProcessStep saveProcessStep(ProcessStep processStep);

    /**
     * Updates an existing process step.
     *
     * @param processStep the process step to update
     * @return the updated process step
     */
    ProcessStep updateProcessStep(ProcessStep processStep);

    /**
     * Saves a process element to the database.
     *
     * @param processElement the process element to save
     * @return the saved process element with generated ID
     */
    ProcessElement saveProcessElement(ProcessElement processElement);

    /**
     * Saves multiple process elements in batch.
     *
     * @param processElements the list of process elements to save
     */
    void saveProcessElements(List<ProcessElement> processElements);

    /**
     * Saves a process log to the database.
     *
     * @param processLog the process log to save
     * @return the saved process log with generated ID
     */
    ProcessLog saveProcessLog(ProcessLog processLog);

    /**
     * Saves multiple process logs in batch.
     *
     * @param processLogs the list of process logs to save
     */
    void saveProcessLogs(List<ProcessLog> processLogs);
}
