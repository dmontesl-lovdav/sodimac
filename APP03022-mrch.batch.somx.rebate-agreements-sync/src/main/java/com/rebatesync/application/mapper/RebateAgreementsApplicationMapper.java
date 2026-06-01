package com.rebatesync.application.mapper;

import com.rebatesync.application.dto.*;
import com.rebatesync.domain.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RebateAgreementsApplicationMapper {

    // Contract mappings
    ContractResponse toContractResponse(Contract contract);

    List<ContractResponse> toContractResponseList(List<Contract> contracts);

    // Agreement mappings
    @Mapping(target = "rebateType", expression = "java(agreement.getRebateType() != null ? agreement.getRebateType().name() : null)")
    @Mapping(target = "contributionType", expression = "java(agreement.getContributionType() != null ? agreement.getContributionType().name() : null)")
    AgreementResponse toAgreementResponse(Agreement agreement);

    List<AgreementResponse> toAgreementResponseList(List<Agreement> agreements);

    // RebateAgreement mappings
    RebateAgreementResponse toRebateAgreementResponse(RebateAgreement rebateAgreement);

    List<RebateAgreementResponse> toRebateAgreementResponseList(List<RebateAgreement> rebateAgreements);

    // SyncResult mappings
    @Mapping(target = "status", expression = "java(syncResult.getStatus() != null ? syncResult.getStatus().name() : null)")
    @Mapping(target = "durationInSeconds", source = "durationInSeconds")
    SyncResultResponse toSyncResultResponse(SyncResult syncResult);

    List<SyncResultResponse> toSyncResultResponseList(List<SyncResult> syncResults);

    // SyncSummary mappings
    SyncSummaryResponse toSyncSummaryResponse(SyncSummary syncSummary);
}
