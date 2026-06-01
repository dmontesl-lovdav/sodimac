package com.rebatesync.domain.exception;

public class ContractNotFoundException extends RuntimeException {

    public ContractNotFoundException(String message) {
        super(message);
    }

    public ContractNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ContractNotFoundException forContractId(String contractId) {
        return new ContractNotFoundException("Contract not found with ID: " + contractId);
    }
}
