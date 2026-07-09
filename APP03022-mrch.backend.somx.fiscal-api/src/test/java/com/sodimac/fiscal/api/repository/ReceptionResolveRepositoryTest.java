package com.sodimac.fiscal.api.repository;

import com.sodimac.fiscal.api.model.entity.ReceptionEntity;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Valida el fix de resolución de recepción (ERR003 "Query did not return a unique result" al
 * publicar NC). reception_number NO es único; se desambigua por número + OC (order_number del
 * addendum). Corre contra el Postgres local (data UAT restaurada, con reception_number duplicados).
 *
 * Datos base (dup reception_number):
 *  - '1' -> {OC 100 = 5205.43 (974582df...)}, {OC 2001 = 33635.02}
 *  - '2' -> {OC 101 = 37995.21 (483ca643...)}, {OC 2002 = 41098.18}
 */
@SpringBootTest
@Disabled("Requiere Postgres local con data UAT (reception_number duplicados). Ejecutar manual "
        + "con -Dtest=ReceptionResolveRepositoryTest; no correr en el pipeline de Sodimac.")
class ReceptionResolveRepositoryTest {

    @Autowired
    private ReceptionRepository receptionRepository;

    @Test
    @DisplayName("numero + OC: devuelve UNA sola recepcion y la correcta (no la de otra OC)")
    void numeroMasOcResuelveUnaCorrecta() {
        List<ReceptionEntity> r1 = receptionRepository.findByReceptionNumberAndOrderNumber("1", "100");
        assertThat(r1).hasSize(1);
        assertThat(r1.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("5205.43"));

        List<ReceptionEntity> r2 = receptionRepository.findByReceptionNumberAndOrderNumber("2", "101");
        assertThat(r2).hasSize(1);
        assertThat(r2.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("37995.21"));
    }

    @Test
    @DisplayName("numero duplicado + OTRA OC: resuelve la otra recepcion (desambigua por OC)")
    void numeroMasOtraOcResuelveOtraRecepcion() {
        List<ReceptionEntity> r = receptionRepository.findByReceptionNumberAndOrderNumber("2", "2002");
        assertThat(r).hasSize(1);
        assertThat(r.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("41098.18"));
    }

    @Test
    @DisplayName("fallback solo por numero: NO lanza NonUniqueResultException aunque haya duplicados")
    void soloNumeroNoRevienta() {
        List<ReceptionEntity> r = receptionRepository.findByReceptionNumberOrdered("1");
        assertThat(r).hasSizeGreaterThanOrEqualTo(2); // '1' esta duplicado; antes esto tronaba
    }
}
