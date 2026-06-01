package com.rebatesync.domain.enums;

public enum AgreementType {
    VOLUME_DISCOUNT("Volume Discount"),
    SEASONAL_REBATE("Seasonal Rebate"),
    PROMOTIONAL_REBATE("Promotional Rebate"),
    FIXED_AMOUNT("Fixed Amount"),
    PERCENTAGE_BASED("Percentage Based");

    private final String displayName;

    AgreementType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
