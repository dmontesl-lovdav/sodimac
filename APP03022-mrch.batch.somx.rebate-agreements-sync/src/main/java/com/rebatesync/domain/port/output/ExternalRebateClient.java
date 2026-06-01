package com.rebatesync.domain.port.output;

import com.rebatesync.domain.model.Agreement;
import com.rebatesync.domain.model.Contract;

import java.util.List;

public interface ExternalRebateClient {

    /**
     * Fetches contracts from the external service with pagination.
     *
     * @param page the page number (1-indexed)
     * @param pageSize the number of items per page
     * @return List of contracts
     */
    List<Contract> fetchContracts(int page, int pageSize);

    /**
     * Fetches a specific contract by ID.
     *
     * @param contractId the contract ID
     * @return the contract if found
     */
    Contract fetchContractById(String contractId);

    /**
     * Fetches agreements for a specific contract with pagination.
     *
     * @param contractId the contract ID
     * @param page the page number (1-indexed)
     * @param pageSize the number of items per page
     * @return List of agreements
     */
    List<Agreement> fetchAgreements(String contractId, int page, int pageSize);

    /**
     * Fetches a specific agreement by ID.
     *
     * @param contractId the contract ID
     * @param agreementId the agreement ID
     * @return the agreement if found
     */
    Agreement fetchAgreementById(String contractId, String agreementId);

    /**
     * Checks if there are more pages available.
     *
     * @param page the current page number
     * @param pageSize the page size
     * @param totalFetched the total number of items fetched so far
     * @return true if more pages are available, false otherwise
     */
    boolean hasMorePages(int page, int pageSize, int totalFetched);
}
