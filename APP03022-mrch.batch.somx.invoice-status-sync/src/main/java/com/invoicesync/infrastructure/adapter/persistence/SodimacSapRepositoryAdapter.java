package com.invoicesync.infrastructure.adapter.persistence;

import com.invoicesync.domain.port.output.SodimacSapRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SodimacSapRepositoryAdapter implements SodimacSapRepository {

    private static final Logger log = LoggerFactory.getLogger(SodimacSapRepositoryAdapter.class);

    private final JdbcTemplate jdbcTemplate;

    public SodimacSapRepositoryAdapter(@Qualifier("sodimacSapJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean existsInEnviosAp(String codigoProveedor, String numeroDocumento, String uuid) {
        return countByProveedorAndDocumento(codigoProveedor, numeroDocumento, uuid) > 0;
    }

    @Override
    public int countByProveedorAndDocumento(String codigoProveedor, String numeroDocumento, String uuid) {
        String sql = """
            SELECT COUNT(1)
            FROM Envios_Ap
            WHERE CODIGO_PROVEEDOR = ?
              AND NUMERO_DOCUMENTO = ?
              AND NUMERO_UUID = ?
            """;

        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class,
                    codigoProveedor, numeroDocumento, uuid);
            log.debug("Count in Envios_Ap for proveedor={}, documento={}: {}",
                    codigoProveedor, numeroDocumento, count);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("Error querying Envios_Ap: {}", e.getMessage());
            throw new RuntimeException("Error querying SodimacSAP database", e);
        }
    }
}
