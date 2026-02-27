package com.sodimac.aclaraciones.api.service.purshase;

import org.springframework.jdbc.core.namedparam.*;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.sodimac.aclaraciones.api.model.dto.filter.PurchaseOrderFilter;
import com.sodimac.aclaraciones.api.model.dto.view.PurchaseOrderView;
import com.sodimac.aclaraciones.api.model.dto.view.ReceptionView;
import com.sodimac.aclaraciones.api.model.dto.view.SkuView;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Types;
import java.time.LocalDate;
import java.util.*;

@Service
public class PurchaseOrderQueryService {

    private final NamedParameterJdbcTemplate jdbc;

    public PurchaseOrderQueryService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /* ------------------- consulta principal ------------------- */
    public List<PurchaseOrderView> find(PurchaseOrderFilter f) {

        StringBuilder sql = new StringBuilder("""
                    SELECT o.ORDENCOMPRA,
                           o.NUMEROPROVEEDOR,
                           p.NAME                 AS NOMBRE_PROVEEDOR,
                           o.IDORIGEN,
                           o.IMPORTE,
                           o.ESTATUS,
                           o.IDUSUARIO,
                           o.FECHAREGISTRO,

                           r.RECEPCION,
                           r.IDORIGEN   AS R_IDORIGEN,
                           r.IDDESTINO,
                           r.IMPORTE    AS R_IMPORTE,
                           r.ESTATUS    AS R_ESTATUS,
                           r.COMENTARIO,
                           r.FECHARECEPCION,
                           r.IDUSUARIO  AS R_IDUSUARIO,
                           r.FECHAREGISTRO AS R_FECHAREGISTRO,

                           s.SKU,
                           s.DESCRIPCION,
                           s.CANTIDAD,
                           s.COSTOUNITARIO,
                           s.COSTOTOTAL,
                           s.ESTATUS     AS S_ESTATUS

                      FROM  APP.PURCHASE_ORDER     o
                      LEFT  JOIN APP.CATSUPPLIER    p ON p.SUPPLIERNUMBER = o.NUMEROPROVEEDOR
                      LEFT  JOIN APP.RECEPCION      r ON r.ORDENCOMPRA    = o.ORDENCOMPRA
                      LEFT  JOIN APP.RECEPCION_SKU  s ON s.ORDENCOMPRA    = r.ORDENCOMPRA
                                                     AND s.RECEPCION     = r.RECEPCION
                     WHERE 1 = 1
                """);

        MapSqlParameterSource p = new MapSqlParameterSource();
        add(sql, p, " AND o.ORDENCOMPRA       = :oc", "oc", f.ordenCompra());
        add(sql, p, " AND r.RECEPCION         = :rec", "rec", f.recepcion());
        add(sql, p, " AND o.NUMEROPROVEEDOR   = :prov", "prov", f.numeroProveedor());
        add(sql, p, " AND o.ESTATUS           = :st", "st", f.estatus());

        range(sql, p, "r.FECHARECEPCION", "frd", "trd", f.fechaRecDesde(), f.fechaRecHasta());
        range(sql, p, "o.FECHAREGISTRO", "foc", "toc", f.fechaRegDesde(), f.fechaRegHasta());

        sql.append(" ORDER BY o.FECHAREGISTRO DESC, r.RECEPCION");

        List<RowFlat> rows = jdbc.query(sql.toString(), p, mapper());

        /* ------------------- Armado jerárquico ------------------- */
        Map<Long, PurchaseOrderView> mapOc = new LinkedHashMap<>();

        for (RowFlat row : rows) {

            PurchaseOrderView oc = mapOc.computeIfAbsent(
                    row.ordenCompra,
                    k -> new PurchaseOrderView(
                            row.ordenCompra,
                            row.numeroProveedor,
                            row.nombreProveedor,
                            row.idOrigen,
                            row.importeOc,
                            row.estatusOc,
                            row.idUsuarioOc,
                            row.fechaRegOc,
                            new ArrayList<>()));

            // Buscar / crear recepción
            Optional<ReceptionView> maybeRec = oc.recepciones().stream()
                    .filter(r -> Objects.equals(r.recepcion(), row.recepcion))
                    .findFirst();

            ReceptionView rec;
            if (maybeRec.isEmpty()) {
                rec = new ReceptionView(
                        row.recepcion,
                        row.rIdOrigen,
                        row.idDestino,
                        row.importeRec,
                        row.estatusRec,
                        row.comentario,
                        row.fechaRecepcion,
                        row.idUsuarioRec,
                        row.fechaRegRec,
                        new ArrayList<>());
                oc.recepciones().add(rec);
            } else {
                rec = maybeRec.get();
            }

            // Agregar SKU si existe
            if (row.sku != null) {
                rec.skus().add(new SkuView(
                        row.sku,
                        row.descripcion,
                        row.cantidad,
                        row.costoUnitario,
                        row.costoTotal,
                        row.estatusSku));
            }
        }

        return new ArrayList<>(mapOc.values());
    }

    /* ------------------- helpers ------------------- */

    private static void add(StringBuilder sb, MapSqlParameterSource p,
            String clause, String name, Object value) {
        if (value != null) {
            sb.append(clause);
            p.addValue(name, value);
        }
    }

    private static void range(StringBuilder sb, MapSqlParameterSource p,
            String column, String from, String to,
            LocalDate dFrom, LocalDate dTo) {

        if (dFrom != null) {
            sb.append(" AND ").append(column).append(" >= :").append(from);
            p.addValue(from, java.sql.Date.valueOf(dFrom), Types.DATE);
        }
        if (dTo != null) {
            sb.append(" AND ").append(column).append(" <= :").append(to);
            p.addValue(to, java.sql.Date.valueOf(dTo), Types.DATE);
        }
    }

    /* fila plana */
    private record RowFlat(
            Long ordenCompra, Long numeroProveedor, String nombreProveedor, Long idOrigen,
            BigDecimal importeOc, Integer estatusOc, Long idUsuarioOc, LocalDate fechaRegOc,

            Long recepcion, Long rIdOrigen, Long idDestino, BigDecimal importeRec,
            Integer estatusRec, String comentario, LocalDate fechaRecepcion,
            Long idUsuarioRec, LocalDate fechaRegRec,

            String sku, String descripcion, BigDecimal cantidad,
            BigDecimal costoUnitario, BigDecimal costoTotal, Integer estatusSku) {
    }

    private static RowMapper<RowFlat> mapper() {
        return (ResultSet rs, int rowNum) -> new RowFlat(
                rs.getLong("ORDENCOMPRA"),
                rs.getLong("NUMEROPROVEEDOR"),
                rs.getString("NOMBRE_PROVEEDOR"),
                rs.getLong("IDORIGEN"),
                rs.getBigDecimal("IMPORTE"),
                rs.getInt("ESTATUS"),
                rs.getLong("IDUSUARIO"),
                rs.getDate("FECHAREGISTRO").toLocalDate(),

                rs.getLong("RECEPCION"),
                rs.getLong("R_IDORIGEN"),
                rs.getLong("IDDESTINO"),
                rs.getBigDecimal("R_IMPORTE"),
                rs.getInt("R_ESTATUS"),
                rs.getString("COMENTARIO"),
                rs.getDate("FECHARECEPCION") != null ? rs.getDate("FECHARECEPCION").toLocalDate() : null,
                rs.getLong("R_IDUSUARIO"),
                rs.getDate("R_FECHAREGISTRO") != null ? rs.getDate("R_FECHAREGISTRO").toLocalDate() : null,

                rs.getString("SKU"),
                rs.getString("DESCRIPCION"),
                rs.getBigDecimal("CANTIDAD"),
                rs.getBigDecimal("COSTOUNITARIO"),
                rs.getBigDecimal("COSTOTOTAL"),
                rs.getObject("S_ESTATUS", Integer.class));
    }
}
