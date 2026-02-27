package com.sodimac.aclaraciones.api.service.purshase;

import com.sodimac.aclaraciones.api.model.dto.*;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PurchaseOrderService {

    private final NamedParameterJdbcTemplate jdbc;

    public PurchaseOrderService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public long create(CreatePurchaseOrderRequest req) {
        try {
            /* ───── Validaciones previas ───── */
            if (req.recepciones() == null || req.recepciones().isEmpty()) {
                throw new IllegalStateException("ERR_FIELD_RECEPCIONES");
            }

            BigDecimal totalRecepciones = BigDecimal.ZERO;

            /* ───── 1. Insertar cabecera de la OC ───── */
            String sqlOrder = """
                        INSERT INTO APP.PURCHASE_ORDER
                        (ORDENCOMPRA, NUMEROPROVEEDOR, IDORIGEN, IMPORTE, ESTATUS, IDUSUARIO, FECHAREGISTRO)
                        VALUES (:ordenCompra, :numeroProveedor, :idOrigen, :importe, :estatus, :idUsuario, :fechaRegistro)
                    """;

            Map<String, Object> orderParams = new HashMap<>();
            orderParams.put("ordenCompra", req.ordenCompra());
            orderParams.put("numeroProveedor", req.numeroProveedor());
            orderParams.put("idOrigen", req.idOrigen());
            orderParams.put("importe", req.importe());
            orderParams.put("estatus", req.estatus());
            orderParams.put("idUsuario", req.idUsuario());
            orderParams.put("fechaRegistro", Date.valueOf(req.fechaRegistro())); // ← conversión

            jdbc.update(sqlOrder, new MapSqlParameterSource(orderParams));

            /* ───── 2. Insertar recepciones + SKUs ───── */
            for (CreateReceptionRequest r : req.recepciones()) {

                BigDecimal totalSku = BigDecimal.ZERO;
                for (CreateSkuRequest sku : r.skus()) {
                    totalSku = totalSku.add(sku.costoTotal());
                }

                if (r.importe().compareTo(totalSku) != 0) {
                    throw new IllegalStateException("ERR_RECEPCION_TOTAL_MISMATCH");
                }

                totalRecepciones = totalRecepciones.add(r.importe());

                /* ── 2.1 Recepción ── */
                String sqlReception = """
                            INSERT INTO APP.RECEPCION
                            (ORDENCOMPRA, RECEPCION, IDORIGEN, IDDESTINO, IMPORTE, ESTATUS, COMENTARIO,
                             FECHARECEPCION, IDUSUARIO, FECHAREGISTRO)
                            VALUES (:ordenCompra, :recepcion, :idOrigen, :idDestino, :importe, :estatus, :comentario,
                             :fechaRecepcion, :idUsuario, :fechaRegistro)
                        """;

                Map<String, Object> rParams = new HashMap<>();
                rParams.put("ordenCompra", req.ordenCompra());
                rParams.put("recepcion", r.recepcion());
                rParams.put("idOrigen", r.idOrigen());
                rParams.put("idDestino", r.idDestino());
                rParams.put("importe", r.importe());
                rParams.put("estatus", r.estatus());
                rParams.put("comentario", r.comentario());
                rParams.put("fechaRecepcion", Date.valueOf(r.fechaRecepcion()));
                rParams.put("idUsuario", r.idUsuario());
                rParams.put("fechaRegistro", Date.valueOf(r.fechaRegistro()));

                jdbc.update(sqlReception, new MapSqlParameterSource(rParams));

                /* ── 2.2 SKUs de la recepción ── */
                String sqlSku = """
                            INSERT INTO APP.RECEPCION_SKU
                            (ORDENCOMPRA, RECEPCION, SKU, DESCRIPCION, CANTIDAD, COSTOUNITARIO,
                             COSTOTOTAL, ESTATUS, IDUSUARIOACTUALIZACION, FECHAACTUALIZACION)
                            VALUES (:ordenCompra, :recepcion, :sku, :descripcion, :cantidad, :costoUnitario,
                             :costoTotal, :estatus, :idUsuarioActualizacion, :fechaActualizacion)
                        """;

                for (CreateSkuRequest sku : r.skus()) {
                    Map<String, Object> skuParams = new HashMap<>();
                    skuParams.put("ordenCompra", req.ordenCompra());
                    skuParams.put("recepcion", r.recepcion());
                    skuParams.put("sku", sku.sku());
                    skuParams.put("descripcion", sku.descripcion());
                    skuParams.put("cantidad", sku.cantidad());
                    skuParams.put("costoUnitario", sku.costoUnitario());
                    skuParams.put("costoTotal", sku.costoTotal());
                    skuParams.put("estatus", sku.estatus());
                    skuParams.put("idUsuarioActualizacion", sku.idUsuarioActualizacion());
                    skuParams.put("fechaActualizacion",
                            sku.fechaActualizacion() != null ? Date.valueOf(sku.fechaActualizacion()) : null);

                    jdbc.update(sqlSku, new MapSqlParameterSource(skuParams));
                }
            }

            /* ───── 3. Validar totales ───── */
            if (totalRecepciones.compareTo(req.importe()) > 0) {
                throw new IllegalStateException("ERR_TOTAL_RECEPCIONES_EXCEEDS_OC");
            }

            return req.ordenCompra();

        } catch (DataAccessException ex) {
            // Log temporal para depurar (puedes migrar a logger después)
            ex.printStackTrace();
            String root = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();
            throw new IllegalStateException("DB_ERROR|" + root, ex);
        }
    }
}
