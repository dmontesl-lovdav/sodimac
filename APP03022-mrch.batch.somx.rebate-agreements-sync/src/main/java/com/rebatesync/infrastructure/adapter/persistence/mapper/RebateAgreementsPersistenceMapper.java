package com.rebatesync.infrastructure.adapter.persistence.mapper;

import com.rebatesync.domain.enums.SyncStatus;
import com.rebatesync.domain.model.*;
import com.rebatesync.infrastructure.adapter.persistence.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RebateAgreementsPersistenceMapper {

    // RebateAgreement mappings
    RebateAgreement toDomain(RebateAgreementEntity entity);

    RebateAgreementEntity toEntity(RebateAgreement domain);

    List<RebateAgreement> toDomainList(List<RebateAgreementEntity> entities);

    List<RebateAgreementEntity> toEntityList(List<RebateAgreement> domains);

    // SyncResult mappings
    @Mapping(target = "status", expression = "java(mapToSyncStatus(entity.getStatus()))")
    SyncResult toDomainSyncResult(SyncResultEntity entity);

    @Mapping(target = "status", expression = "java(domain.getStatus() != null ? domain.getStatus().name() : null)")
    SyncResultEntity toEntitySyncResult(SyncResult domain);

    List<SyncResult> toDomainSyncResultList(List<SyncResultEntity> entities);

    // ProcessStep mappings
    ProcessStep toDomainProcessStep(CtrlProcDetEntity entity);

    CtrlProcDetEntity toEntityProcessStep(ProcessStep domain);

    List<ProcessStep> toDomainProcessStepList(List<CtrlProcDetEntity> entities);

    // ProcessElement mappings
    ProcessElement toDomainProcessElement(CtrlProcesoElementoEntity entity);

    CtrlProcesoElementoEntity toEntityProcessElement(ProcessElement domain);

    List<ProcessElement> toDomainProcessElementList(List<CtrlProcesoElementoEntity> entities);

    List<CtrlProcesoElementoEntity> toEntityProcessElementList(List<ProcessElement> domains);

    // ProcessLog mappings
    ProcessLog toDomainProcessLog(CtrlLogEntity entity);

    CtrlLogEntity toEntityProcessLog(ProcessLog domain);

    List<ProcessLog> toDomainProcessLogList(List<CtrlLogEntity> entities);

    List<CtrlLogEntity> toEntityProcessLogList(List<ProcessLog> domains);

    default SyncStatus mapToSyncStatus(String status) {
        if (status == null) {
            return null;
        }
        try {
            return SyncStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
