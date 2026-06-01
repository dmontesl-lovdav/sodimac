package com.rebatesync.infrastructure.scheduler;

import com.rebatesync.domain.port.input.RebateAgreementsSyncUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RebateAgreementsSyncScheduler {

    private final RebateAgreementsSyncUseCase syncUseCase;

    /**
     * Scheduled job that runs every day at 03:00 AM (Mexico City time, GMT-6).
     * Cron expression: second minute hour day month day-of-week
     * 0 0 3 * * ? = At 03:00:00 AM every day
     */
    @Scheduled(cron = "${rebate.sync.cron:0 0 3 * * ?}", zone = "America/Mexico_City")
    public void executeScheduledSync() {
        log.info("Starting scheduled rebate agreements sync job");
        try {
            syncUseCase.executeFullSync();
            log.info("Scheduled rebate agreements sync job completed successfully");
        } catch (Exception e) {
            log.error("Scheduled rebate agreements sync job failed", e);
        }
    }
}
