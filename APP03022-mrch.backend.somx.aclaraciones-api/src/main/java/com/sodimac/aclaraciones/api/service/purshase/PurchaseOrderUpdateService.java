package com.sodimac.aclaraciones.api.service.purshase;

import com.sodimac.aclaraciones.api.model.dto.UpdatePurchaseOrderRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Modifica **solo** los estatus de la OC y/o sus recepciones.
 * Antes de sobrescribir, registra el cambio en APP.PURCHASE_ORDER_HIST.
 */
@Service
public class PurchaseOrderUpdateService {

    private final NamedParameterJdbcTemplate jdbc;

    public PurchaseOrderUpdateService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /* ----------------------------- API pública ----------------------------- */

    @Transactional
    public void update(long oc,
            UpdatePurchaseOrderRequest rq,
            long userId /* 0 = usuario no identificado */) {

        /* 1) Validar que los estatus existan en catálogo-status */
        requireStatus(rq.estatusOC());
        if (rq.recepciones() != null) {
            rq.recepciones().forEach(r -> requireStatus(r.estatus()));
        }

        /* 2) Obtener estatus actual de la OC */
        Integer oldOcStatus = jdbc.queryForObject("""
                SELECT ESTATUS
                  FROM APP.PURCHASE_ORDER
                 WHERE ORDENCOMPRA = :oc
                """, Map.of("oc", oc), Integer.class);

        if (oldOcStatus == null) {
            throw new IllegalStateException("ERR_OC_NOT_FOUND");
        }

        /* 3) Historial + actualización de la cabecera */
        saveHist(oc, null, oldOcStatus, rq.estatusOC(), userId);

        jdbc.update("""
                UPDATE APP.PURCHASE_ORDER
                   SET ESTATUS = :new
                 WHERE ORDENCOMPRA = :oc
                """, Map.of("new", rq.estatusOC(), "oc", oc));

        /* 4) Recepciones (si vienen en el request) */
        if (rq.recepciones() == null || rq.recepciones().isEmpty()) {
            return;
        }

        for (var rec : rq.recepciones()) {

            Integer oldRecStatus = jdbc.queryForObject("""
                    SELECT ESTATUS
                      FROM APP.RECEPCION
                     WHERE ORDENCOMPRA = :oc
                       AND RECEPCION    = :rec
                    """,
                    Map.of("oc", oc, "rec", rec.recepcion()),
                    Integer.class);

            if (oldRecStatus == null) {
                throw new IllegalStateException("ERR_RECEPCION_NOT_FOUND");
            }

            saveHist(oc, rec.recepcion(), oldRecStatus, rec.estatus(), userId);

            jdbc.update("""
                     UPDATE APP.RECEPCION
                        SET ESTATUS = :new
                      WHERE ORDENCOMPRA = :oc
                        AND RECEPCION   = :rec
                    """,
                    Map.of("new", rec.estatus(),
                            "oc", oc,
                            "rec", rec.recepcion()));
        }
    }

    /* ------------------------------ helpers ------------------------------ */

    /** Verifica en APP.CATALOG_STATUS que el id exista; lanza excepción si no. */
    private void requireStatus(Integer statusId) {
        if (statusId == null) {
            throw new IllegalStateException("ERR_STATUS_NULL");
        }

        Integer cnt = jdbc.queryForObject("""
                 SELECT COUNT(*)
                   FROM APP.CATALOG_STATUS
                  WHERE ID = :id
                """, Map.of("id", statusId), Integer.class);

        if (cnt == null || cnt == 0) {
            throw new IllegalStateException("ERR_STATUS_NOT_FOUND");
        }
    }

    /** Inserta un registro de auditoría. */
    private void saveHist(long oc,
            Long rec,
            int oldSt,
            int newSt,
            long usr) throws DataAccessException {

        jdbc.update("""
                INSERT INTO APP.PURCHASE_ORDER_HIST
                       (ORDENCOMPRA, RECEPCION,
                        ESTATUS_ANT, ESTATUS_NVO,
                        IDUSUARIO,   FECHACAMBIO)
                VALUES (:oc, :rec, :old, :new, :usr, CURRENT_TIMESTAMP)
                """,
                new MapSqlParameterSource()
                        .addValue("oc", oc)
                        .addValue("rec", rec)
                        .addValue("old", oldSt)
                        .addValue("new", newSt)
                        .addValue("usr", usr));
    }
}
