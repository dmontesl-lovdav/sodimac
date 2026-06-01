package com.rebatesync.domain.model;

import com.rebatesync.domain.enums.SyncStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncResult {
    private Long id;
    private SyncStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalContractsDownloaded;
    private Integer totalAgreementsDownloaded;
    private Integer totalAgreementsLoaded;
    private Integer totalPages;
    private Integer failedRecords;
    private String errorMessage;
    private LocalDateTime createdAt;

    public Long getDurationInSeconds() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).getSeconds();
        }
        return null;
    }
}
