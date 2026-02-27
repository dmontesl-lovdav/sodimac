package com.sodimac.aclaraciones.api.controller;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sodimac.aclaraciones.api.model.dto.CreatePurchaseOrderRequest;
import com.sodimac.aclaraciones.api.model.dto.UpdatePurchaseOrderRequest;
import com.sodimac.aclaraciones.api.model.dto.filter.PurchaseOrderFilter;
import com.sodimac.aclaraciones.api.model.dto.view.PurchaseOrderView;
import com.sodimac.aclaraciones.api.security.RequireRole;
import com.sodimac.aclaraciones.api.security.Session;
import com.sodimac.aclaraciones.api.service.purshase.PurchaseOrderQueryService;
import com.sodimac.aclaraciones.api.service.purshase.PurchaseOrderService;
import com.sodimac.aclaraciones.api.service.purshase.PurchaseOrderUpdateService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/purchase-orders")
@SecurityRequirement(name = "bearerAuth")
public class PurchaseOrderController {

    private final PurchaseOrderService createService;
    private final PurchaseOrderQueryService queryService;
    private final PurchaseOrderUpdateService updateService;

    public PurchaseOrderController(PurchaseOrderService createService,
            PurchaseOrderQueryService queryService,
            PurchaseOrderUpdateService updateService) {
        this.createService = createService;
        this.queryService = queryService;
        this.updateService = updateService;
    }

    /* ---------- POST ---------- */

    @Operation(operationId = "postPurchaseOrder", summary = "Registra nueva orden de compra", responses = {
            @ApiResponse(responseCode = "201", description = "Orden registrada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @PostMapping
    @RequireRole
    public ResponseEntity<Map<String, Object>> create(
            @Validated @RequestBody CreatePurchaseOrderRequest body,
            @RequestAttribute("session") Session session) {

        long id = createService.create(body);
        return ResponseEntity
                .created(URI.create("/purchase-orders/" + id))
                .body(Map.of("id", id, "message", "Purchase order created"));
    }

    /* ---------- GET ---------- */

    @Operation(operationId = "getPurchaseOrders", summary = "Consulta OC + recepciones")
    @GetMapping
    @RequireRole
    public ResponseEntity<List<PurchaseOrderView>> list(
            @ParameterObject PurchaseOrderFilter filter,
            @RequestAttribute("session") Session session) {

        return ResponseEntity.ok(queryService.find(filter));
    }

    /* ---------- PATCH ---------- */

    @Operation(operationId = "patchPurchaseOrder", summary = "Actualiza estatus de OC / recepciones", responses = {
            @ApiResponse(responseCode = "200", description = "Actualización exitosa", content = @Content(schema = @Schema(implementation = PurchaseOrderView.class)))
    })
    @PatchMapping("/{ordenCompra}")
    @RequireRole
    public ResponseEntity<PurchaseOrderView> patch(
            @PathVariable long ordenCompra,
            @Validated @RequestBody UpdatePurchaseOrderRequest body,
            @RequestAttribute("session") Session session) {

        /* Seguimos enviando 0L hasta que exista un user-id real */
        updateService.update(ordenCompra, body, 0L);

        /* Volvemos a leer la OC ya modificada y la devolvemos al cliente */
        PurchaseOrderFilter filter = new PurchaseOrderFilter(
                ordenCompra, // ordenCompra
                null, null, null, // recepcion, numeroProveedor, estatus
                null, null, null, null // fechaRecDesde, fechaRecHasta, fechaRegDesde, fechaRegHasta
        );

        PurchaseOrderView updated = queryService.find(filter).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Purchase order not found after update"));

        return ResponseEntity.ok(updated);
    }
}
