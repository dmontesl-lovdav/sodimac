package com.rebatesync.infrastructure.adapter.web;

import com.rebatesync.application.dto.RebateAgreementResponse;
import com.rebatesync.application.mapper.RebateAgreementsApplicationMapper;
import com.rebatesync.domain.model.RebateAgreement;
import com.rebatesync.domain.port.input.RebateAgreementsManagementUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/rebate-agreements")
@RequiredArgsConstructor
public class RebateAgreementController {

    private final RebateAgreementsManagementUseCase managementUseCase;
    private final RebateAgreementsApplicationMapper mapper;

    @GetMapping
    public ResponseEntity<List<RebateAgreementResponse>> getAllRebateAgreements() {
        log.info("GET /api/rebate-agreements - Retrieving all rebate agreements");
        List<RebateAgreement> agreements = managementUseCase.getAllRebateAgreements();
        return ResponseEntity.ok(mapper.toRebateAgreementResponseList(agreements));
    }

    @GetMapping("/{supplierNumber}/{agreementNumber}")
    public ResponseEntity<RebateAgreementResponse> getRebateAgreementById(
            @PathVariable String supplierNumber,
            @PathVariable String agreementNumber) {
        log.info("GET /api/rebate-agreements/{}/{} - Retrieving rebate agreement", supplierNumber, agreementNumber);
        return managementUseCase.getRebateAgreementById(supplierNumber, agreementNumber)
                .map(mapper::toRebateAgreementResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/supplier/{supplierNumber}")
    public ResponseEntity<List<RebateAgreementResponse>> getRebateAgreementsBySupplier(
            @PathVariable String supplierNumber) {
        log.info("GET /api/rebate-agreements/supplier/{} - Retrieving agreements by supplier", supplierNumber);
        List<RebateAgreement> agreements = managementUseCase.getRebateAgreementsBySupplier(supplierNumber);
        return ResponseEntity.ok(mapper.toRebateAgreementResponseList(agreements));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllRebateAgreements() {
        log.info("DELETE /api/rebate-agreements - Deleting all rebate agreements");
        managementUseCase.deleteAllRebateAgreements();
        return ResponseEntity.noContent().build();
    }
}
