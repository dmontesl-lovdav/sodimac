package com.invoicesync.infrastructure.adapter.persistence;

import com.invoicesync.domain.port.output.SapitoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SapitoRepositoryAdapter implements SapitoRepository {

    private static final Logger log = LoggerFactory.getLogger(SapitoRepositoryAdapter.class);

    private final JdbcTemplate jdbcTemplate;

    public SapitoRepositoryAdapter(@Qualifier("sapitoJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean isPendingToSendToI213(String codigoProveedor, String numeroDocumento, String uuid) {
        String sql = """
            SELECT COUNT(1)
            FROM Envios_Ap
            WHERE CODIGO_PROVEEDOR = ?
              AND NUMERO_DOCUMENTO = ?
              AND NUMERO_UUID = ?
              AND FLAG_ENVIADO IN (0)
            """;

        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class,
                    codigoProveedor, numeroDocumento, uuid);
            log.debug("Pending to send count for proveedor={}, documento={}: {}",
                    codigoProveedor, numeroDocumento, count);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("Error querying SAPITO for pending: {}", e.getMessage());
            throw new RuntimeException("Error querying SAPITO database", e);
        }
    }

    @Override
    public boolean wasSentToI213(String codigoProveedor, String uuid) {
        String sql = """
            SELECT COUNT(1)
            FROM Envios_Ap
            WHERE CODIGO_PROVEEDOR = ?
              AND NUMERO_UUID = ?
              AND FLAG_ENVIADO IN (1)
            """;

        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, codigoProveedor, uuid);
            log.debug("Was sent to i213 for proveedor={}, uuid={}: {}", codigoProveedor, uuid, count);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("Error querying SAPITO for sent: {}", e.getMessage());
            throw new RuntimeException("Error querying SAPITO database", e);
        }
    }

    @Override
    public int getFlagEnviado(String codigoProveedor, String uuid) {
        String sql = """
            SELECT FLAG_ENVIADO
            FROM Envios_Ap
            WHERE CODIGO_PROVEEDOR = ?
              AND NUMERO_UUID = ?
            """;

        try {
            Integer flag = jdbcTemplate.queryForObject(sql, Integer.class, codigoProveedor, uuid);
            log.debug("FLAG_ENVIADO for proveedor={}, uuid={}: {}", codigoProveedor, uuid, flag);
            return flag != null ? flag : -1;
        } catch (Exception e) {
            log.error("Error querying SAPITO for flag: {}", e.getMessage());
            return -1;
        }
    }
}
