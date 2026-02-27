package com.sodimac.aclaraciones.api.controller;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sodimac.aclaraciones.api.model.dto.CreateShipmentGuideRequest;
import com.sodimac.aclaraciones.api.model.dto.ShipmentGuideFilter;
import com.sodimac.aclaraciones.api.model.dto.ShipmentGuideView;
import com.sodimac.aclaraciones.api.model.dto.UpdateShipmentGuideRequest;
import com.sodimac.aclaraciones.api.service.shipment.ShipmentGuideQueryService;
import com.sodimac.aclaraciones.api.service.shipment.ShipmentGuideService;
import com.sodimac.aclaraciones.api.service.shipment.ShipmentGuideUpdateService;

/**
 * Controlador único: alta (POST), consulta (GET) y actualización (PATCH)
 * de guías de embarque.
 */
@RestController
@RequestMapping("/shipment-guides")
public class ShipmentGuideController {

    private final ShipmentGuideService createService;
    private final ShipmentGuideQueryService queryService;
    private final ShipmentGuideUpdateService updateService;

    /* ---------- constructor manual (sin Lombok) ---------- */
    public ShipmentGuideController(ShipmentGuideService createService,
            ShipmentGuideQueryService queryService,
            ShipmentGuideUpdateService updateService) {
        this.createService = createService;
        this.queryService = queryService;
        this.updateService = updateService;
    }

    /* ====== POST /shipment-guides (alta) ================================ */
    @PostMapping
    public ResponseEntity<?> create(@Validated @RequestBody CreateShipmentGuideRequest body) {

        try {
            long id = createService.create(body);

            Map<String, Object> payload = Map.of(
                    "id", id,
                    "message", "Shipment guide created");

            return ResponseEntity
                    .created(URI.create("/shipment-guides/" + id))
                    .body(payload);

        } catch (IllegalStateException ex) { // reglas de negocio
            Map<String, Object> error = Map.of(
                    "code", ex.getMessage(),
                    "message", "No se pudieron registrar los datos");
            return ResponseEntity.badRequest().body(error);

        } catch (DataAccessException ex) { // error BD inesperado
            Map<String, Object> error = Map.of(
                    "code", "ERR_DATABASE",
                    "message", "Fallo al acceder a la base de datos");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /* ====== GET /shipment-guides (consulta con filtros) ================= */
    @GetMapping
    public ResponseEntity<List<ShipmentGuideView>> list(
            @ParameterObject ShipmentGuideFilter filter) {

        return ResponseEntity.ok(queryService.find(filter));
    }

    /* ====== PATCH /shipment-guides/{id} (actualización) ================= */
    @PatchMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable long id,
            @Validated @RequestBody UpdateShipmentGuideRequest body,
            @RequestHeader("X-User") String user) {

        try {
            updateService.update(id, body, user);

            Map<String, Object> ok = Map.of(
                    "id", id,
                    "message", "Shipment guide updated");
            return ResponseEntity.ok(ok);

        } catch (NoSuchElementException ex) { // guía no existe
            Map<String, Object> err = Map.of(
                    "code", ex.getMessage(),
                    "message", "Guía no encontrada");
            return ResponseEntity.status(404).body(err);

        } catch (IllegalArgumentException ex) { // validaciones de negocio
            Map<String, Object> err = Map.of(
                    "code", ex.getMessage(),
                    "message", "Datos de actualización inválidos");
            return ResponseEntity.badRequest().body(err);

        } catch (DataAccessException ex) { // fallo BD
            Map<String, Object> err = Map.of(
                    "code", "ERR_DATABASE",
                    "message", "Fallo al acceder a la base de datos");
            return ResponseEntity.internalServerError().body(err);
        }
    }
}
