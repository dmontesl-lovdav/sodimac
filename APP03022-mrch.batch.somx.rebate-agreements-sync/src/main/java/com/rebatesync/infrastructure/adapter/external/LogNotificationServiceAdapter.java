package com.rebatesync.infrastructure.adapter.external;

import com.rebatesync.domain.model.SyncResult;
import com.rebatesync.domain.port.output.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogNotificationServiceAdapter implements NotificationService {

    @Override
    public void notifySuccess(SyncResult syncResult) {
        log.info("===================================");
        log.info("SYNC COMPLETED SUCCESSFULLY");
        log.info("===================================");
        log.info("Start Time: {}", syncResult.getStartTime());
        log.info("End Time: {}", syncResult.getEndTime());
        log.info("Duration: {} seconds", syncResult.getDurationInSeconds());
        log.info("Contracts Downloaded: {}", syncResult.getTotalContractsDownloaded());
        log.info("Agreements Downloaded: {}", syncResult.getTotalAgreementsDownloaded());
        log.info("Agreements Loaded: {}", syncResult.getTotalAgreementsLoaded());
        log.info("Failed Records: {}", syncResult.getFailedRecords());
        log.info("===================================");
    }

    @Override
    public void notifyFailure(SyncResult syncResult) {
        log.error("===================================");
        log.error("SYNC FAILED");
        log.error("===================================");
        log.error("Start Time: {}", syncResult.getStartTime());
        log.error("End Time: {}", syncResult.getEndTime());
        log.error("Duration: {} seconds", syncResult.getDurationInSeconds());
        log.error("Contracts Downloaded: {}", syncResult.getTotalContractsDownloaded());
        log.error("Agreements Downloaded: {}", syncResult.getTotalAgreementsDownloaded());
        log.error("Agreements Loaded: {}", syncResult.getTotalAgreementsLoaded());
        log.error("Failed Records: {}", syncResult.getFailedRecords());
        log.error("Error Message: {}", syncResult.getErrorMessage());
        log.error("===================================");
        log.error("ALERT: Support/Operations team should be notified!");
    }

    @Override
    public void notify(String message) {
        log.info("NOTIFICATION: {}", message);
    }
}
