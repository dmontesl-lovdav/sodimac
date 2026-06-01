package com.rebatesync.domain.enums;

public enum ContributionType {
    PERCENTAGE("Percentage"),
    FIXED_AMOUNT("Fixed Amount"),
    TIERED("Tiered");

    private final String displayName;

    ContributionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
