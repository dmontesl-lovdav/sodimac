package com.invoicesync.infrastructure.adapter.persistence;

import com.invoicesync.domain.port.output.I213Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Adapter para consultas a la base de datos i213 mediante Stored Procedures.
 *
 * Conexión: 10.138.153.10:1433 / AdmIF213ProdDB / SodimacETLUSR
 *
 * STORED PROCEDURES:
 * - i123_Valida_Documento_AP: Valida contabilización (Escenario 4: 9→10/13)
 * - i213_Valida_Documento_Pagado_AP: Valida pago (Escenario 5: 10→11)
 *
 * NOTA: El SP de contabilización tiene nombre "i123" (typo en sistema origen).
 *
 * Actualizado: 2026-02-25
 */
@Repository
public class I213RepositoryAdapter implements I213Repository {

    private static final Logger log = LoggerFactory.getLogger(I213RepositoryAdapter.class);

    private final JdbcTemplate jdbcTemplate;

    public I213RepositoryAdapter(@Qualifier("i213JdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        log.info("i213 Repository initialized - Using SPs: i123_Valida_Documento_AP, i213_Valida_Documento_Pagado_AP");
    }

    /**
     * Valida si un documento está contabilizado en i213.
     * Ejecuta SP: i123_Valida_Documento_AP
     *
     * @param idProveedor ID del proveedor
     * @param numeroDocumento Número del documento
     * @return 1=contabilizado, 0=rechazado, -1=error/no encontrado
     */
    @Override
    public int validateDocumentoAP(String idProveedor, String numeroDocumento) {
        log.debug("Executing i123_Valida_Documento_AP for proveedor={}, documento={}",
                idProveedor, numeroDocumento);

        try {
            String sql = "{call i123_Valida_Documento_AP(?, ?)}";

            return jdbcTemplate.query(sql, rs -> {
                if (rs.next()) {
                    int code = rs.getInt("CODE");
                    String msg = rs.getString("MSG");
                    log.debug("i123_Valida_Documento_AP result: CODE={}, MSG={}", code, msg);
                    return code;
                }
                log.warn("i123_Valida_Documento_AP returned no rows");
                return -1;
            }, idProveedor, numeroDocumento);

        } catch (Exception e) {
            log.error("Error executing i123_Valida_Documento_AP: {}", e.getMessage());
            throw new RuntimeException("Error executing stored procedure i123_Valida_Documento_AP", e);
        }
    }

    /**
     * Valida si un documento ha sido pagado en i213.
     * Ejecuta SP: i213_Valida_Documento_Pagado_AP
     *
     * @param idProveedor ID del proveedor
     * @param numeroDocumento Número del documento
     * @return 1=pagado, 0=no pagado, -1=error/no encontrado
     */
    @Override
    public int validateDocumentoPagado(String idProveedor, String numeroDocumento) {
        log.debug("Executing i213_Valida_Documento_Pagado_AP for proveedor={}, documento={}",
                idProveedor, numeroDocumento);

        try {
            String sql = "{call i213_Valida_Documento_Pagado_AP(?, ?)}";

            return jdbcTemplate.query(sql, rs -> {
                if (rs.next()) {
                    int code = rs.getInt("CODE");
                    String msg = rs.getString("MSG");
                    String refBank = rs.getString("REF_BANK");
                    log.debug("i213_Valida_Documento_Pagado_AP result: CODE={}, MSG={}, REF_BANK={}", code, msg, refBank);
                    return code;
                }
                log.warn("i213_Valida_Documento_Pagado_AP returned no rows");
                return 0;
            }, idProveedor, numeroDocumento);

        } catch (Exception e) {
            log.error("Error executing i213_Valida_Documento_Pagado_AP: {}", e.getMessage());
            throw new RuntimeException("Error executing stored procedure i213_Valida_Documento_Pagado_AP", e);
        }
    }
}
