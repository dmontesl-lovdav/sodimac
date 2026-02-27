package com.sodimac.aclaraciones.api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sodimac.aclaraciones.api.model.dto.RelatedInformationDto;
import com.sodimac.aclaraciones.api.security.RequireRole;
import com.sodimac.aclaraciones.api.security.Session;
import com.sodimac.aclaraciones.api.service.relatedInformation.command.RelatedInformationCommandService;
import com.sodimac.aclaraciones.api.service.relatedInformation.query.RelatedInformationQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/related-information")
@SecurityRequirement(name = "bearerAuth")
public class RelatedInformationController {

    private final RelatedInformationQueryService queryService;
    private final RelatedInformationCommandService commandService;

    public RelatedInformationController(
            RelatedInformationQueryService queryService,
            RelatedInformationCommandService commandService) {
        this.queryService = queryService;
        this.commandService = commandService;
    }

    /* ==================================================================== */
    /* GET /related-information (filtrado por query params) */
    /* ==================================================================== */
    @Operation(summary = "Returns list", responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = RelatedInformationDto.class))))
    @GetMapping
    @RequireRole
    public ResponseEntity<List<RelatedInformationDto>> list(
            @RequestParam(name = "id", required = false) Long id,
            @RequestParam(name = "businessUnit", required = false) Integer businessUnitId,
            @RequestParam(name = "country", required = false) Integer countryId,
            @RequestParam(name = "size", required = false) Integer size,
            @RequestAttribute("session") Session session) {

        if (id != null) {
            return ResponseEntity.ok(List.of(queryService.findById(id)));
        }

        return ResponseEntity.ok(
                queryService.listFiltered(businessUnitId, countryId, size));
    }

    /* ==================================================================== */
    /* GET BY id /related-information/{id} */
    /* ==================================================================== */
    @GetMapping("/{id}")
    @RequireRole
    @Operation(summary = "Get record (with image)")
    public ResponseEntity<RelatedInformationDto> getOne(
            @PathVariable Long id,
            @RequestAttribute("session") Session session) {

        return ResponseEntity.ok(queryService.findById(id));
    }

    /* ==================================================================== */
    /* POST /related-information */
    /* ==================================================================== */
    @Operation(summary = "Create record", responses = @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = RelatedInformationDto.class))))
    @PostMapping
    @RequireRole
    public ResponseEntity<RelatedInformationDto> create(
            @RequestBody RelatedInformationDto body,
            @RequestAttribute("session") Session session) {

        return ResponseEntity.status(HttpStatus.CREATED).body(commandService.create(body));
    }

    /* ==================================================================== */
    /* PUT /related-information/{id} */
    /* ==================================================================== */
    @Operation(summary = "Update record", responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = RelatedInformationDto.class))))
    @PutMapping("/{id}")
    @RequireRole
    public ResponseEntity<RelatedInformationDto> update(
            @PathVariable Long id,
            @RequestBody RelatedInformationDto body,
            @RequestAttribute("session") Session session) {

        return ResponseEntity.ok(commandService.update(id, body));
    }

    /* ==================================================================== */
    /* POST /related-information/{id}/publish / unpublish */
    /* ==================================================================== */
    @Operation(summary = "Publish record")
    @PostMapping("/{id}/publish")
    @RequireRole
    public ResponseEntity<RelatedInformationDto> publish(
            @PathVariable Long id,
            @RequestAttribute("session") Session session) {

        return ResponseEntity.ok(commandService.updatePublication(id, true));
    }

    @Operation(summary = "Un-publish record")
    @PostMapping("/{id}/unpublish")
    @RequireRole
    public ResponseEntity<RelatedInformationDto> unpublish(
            @PathVariable Long id,
            @RequestAttribute("session") Session session) {

        return ResponseEntity.ok(commandService.updatePublication(id, false));
    }

    /* ==================================================================== */
    /* DELETE /related-information/{id} */
    /* ==================================================================== */
    @Operation(summary = "Delete record permanently", responses = {
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @DeleteMapping("/{id}")
    @RequireRole
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestAttribute("session") Session session) {

        commandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
