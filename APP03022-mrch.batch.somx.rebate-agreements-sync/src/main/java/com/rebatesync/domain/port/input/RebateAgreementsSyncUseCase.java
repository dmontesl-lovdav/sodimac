package com.rebatesync.domain.port.input;

import com.rebatesync.domain.model.SyncResult;

public interface RebateAgreementsSyncUseCase {

    /**
     * Executes the full sync process: downloads all contracts and agreements,
     * deletes existing data, and loads new data into the database.
     *
     * @return SyncResult containing details of the sync operation
     */
    SyncResult executeFullSync();

    /**
     * Executes sync for a specific contract ID.
     *
     * @param contractId the contract ID to sync
     * @return SyncResult containing details of the sync operation
     */
    SyncResult executeSyncByContractId(String contractId);
}
