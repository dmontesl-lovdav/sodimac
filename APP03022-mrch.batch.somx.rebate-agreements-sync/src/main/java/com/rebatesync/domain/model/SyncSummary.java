package com.rebatesync.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncSummary {
    private LocalDateTime executionTime;
    private Integer totalExecutions;
    private Integer successfulExecutions;
    private Integer failedExecutions;
    private Long averageDurationInSeconds;
    private List<SyncResult> recentSyncs;
}
