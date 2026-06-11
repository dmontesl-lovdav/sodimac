package com.rebatesync.application.usecase;

import com.rebatesync.domain.model.RebateAgreement;
import com.rebatesync.domain.port.output.RebateAgreementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Carga transaccional de rebate agreements a BD.
 *
 * Extraído de RebateAgreementsSyncService a un bean propio a propósito:
 * - @Transactional en método PRIVADO no aplica (Spring no intercepta privados) y
 *   además forzaba un proxy CGLIB sobre el servicio, dejando los campos en null
 *   cuando resilience4j invocaba el fallback (NPE en fallbackExecuteFullSync).
 * - Aquí el método es PÚBLICO en un bean separado → la transacción sí aplica y
 *   el servicio de sync queda sin proxy de transacción (fallback funciona).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RebateDataLoadService {

    private final RebateAgreementRepository rebateAgreementRepository;

    /**
     * Full sync: TRUNCATE + INSERT en una transacción corta.
     */
    @Transactional
    public int loadFullSync(List<RebateAgreement> rebateAgreements) {
        log.info("Carga transaccional full-sync - TRUNCATE + INSERT");
        rebateAgreementRepository.deleteAll();
        log.info("✓ Tabla truncada");
        List<RebateAgreement> saved = rebateAgreementRepository.saveAll(rebateAgreements);
        log.info("✓ {} agreements cargados", saved.size());
        return saved.size();
    }

    /**
     * Sync por contrato: solo INSERT en lotes, sin TRUNCATE.
     */
    @Transactional
    public int saveBatch(List<RebateAgreement> rebateAgreements) {
        log.info("Carga transaccional INSERT - {} agreements", rebateAgreements.size());
        return rebateAgreementRepository.saveAll(rebateAgreements).size();
    }
}
