package com.sodimac.fiscal.api.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LogEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "log_id", columnDefinition = "uuid")
    private UUID logId;

    @Column(name = "pac_id")
    private Integer pacId;

    @Column(name = "version_id")
    private Integer versionId;

    @Column(name = "cfdi_uuid", columnDefinition = "uuid")
    private UUID cfdiUuid;

    @Column(name = "operation_type", length = 20, nullable = false)
    private String operationType;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate = LocalDateTime.now();

    @Column(name = "record_start_date")
    private LocalDateTime recordStartDate;

    @Column(name = "record_end_date")
    private LocalDateTime recordEndDate;

    @Column(name = "status_code", length = 10)
    private String statusCode;

    @Column(name = "status_message", columnDefinition = "TEXT")
    private String statusMessage;

    @Column(name = "request_data", columnDefinition = "TEXT")
    private String requestData;

    @Column(name = "response_data", columnDefinition = "TEXT")
    private String responseData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pac_id", insertable = false, updatable = false)
    private PacCatalogEntity pac;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", insertable = false, updatable = false)
    private VersionCatalogEntity version;

 
}