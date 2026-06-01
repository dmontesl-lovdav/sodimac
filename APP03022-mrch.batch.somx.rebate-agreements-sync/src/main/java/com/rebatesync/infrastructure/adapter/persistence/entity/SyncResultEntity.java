package com.rebatesync.infrastructure.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "CtrlProceCab")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Status", length = 50)
    private String status;

    @Column(name = "StartTime")
    private LocalDateTime startTime;

    @Column(name = "EndTime")
    private LocalDateTime endTime;

    @Column(name = "TotalContractsDownloaded")
    private Integer totalContractsDownloaded;

    @Column(name = "TotalAgreementsDownloaded")
    private Integer totalAgreementsDownloaded;

    @Column(name = "TotalAgreementsLoaded")
    private Integer totalAgreementsLoaded;

    @Column(name = "TotalPages")
    private Integer totalPages;

    @Column(name = "FailedRecords")
    private Integer failedRecords;

    @Column(name = "ErrorMessage", length = 1000)
    private String errorMessage;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
