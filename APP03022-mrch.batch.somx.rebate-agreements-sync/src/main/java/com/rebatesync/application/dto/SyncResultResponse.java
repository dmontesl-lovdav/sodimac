package com.rebatesync.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncResultResponse {
    private Long id;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationInSeconds;
    private Integer totalContractsDownloaded;
    private Integer totalAgreementsDownloaded;
    private Integer totalAgreementsLoaded;
    private Integer totalPages;
    private Integer failedRecords;
    private String errorMessage;
    private LocalDateTime createdAt;
}
