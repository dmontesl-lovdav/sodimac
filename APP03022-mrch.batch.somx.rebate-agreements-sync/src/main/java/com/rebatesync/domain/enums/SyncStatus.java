package com.rebatesync.domain.enums;

public enum SyncStatus {
    SUCCESS("Success"),
    FAILED("Failed"),
    PARTIAL("Partial"),
    IN_PROGRESS("In Progress");

    private final String displayName;

    SyncStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
