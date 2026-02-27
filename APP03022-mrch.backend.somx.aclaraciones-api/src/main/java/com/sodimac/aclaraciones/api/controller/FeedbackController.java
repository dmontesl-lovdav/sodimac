package com.sodimac.aclaraciones.api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sodimac.aclaraciones.api.model.dto.feedback.FeedbackDto;
import com.sodimac.aclaraciones.api.security.RequireRole;
import com.sodimac.aclaraciones.api.security.Session;
import com.sodimac.aclaraciones.api.service.feedback.command.FeedbackCommandService;
import com.sodimac.aclaraciones.api.service.feedback.query.FeedbackQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/feedback")
@SecurityRequirement(name = "bearerAuth")
public class FeedbackController {

    private final FeedbackQueryService queryService;
    private final FeedbackCommandService commandService;

    public FeedbackController(
            FeedbackQueryService queryService,
            FeedbackCommandService commandService) {
        this.queryService = queryService;
        this.commandService = commandService;
    }

    @Operation(summary = "Returns list", responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = FeedbackDto.class))))
    @GetMapping
    @RequireRole
    public ResponseEntity<List<FeedbackDto>> list(
            @RequestParam(name = "size", required = false) Integer size,
            @RequestAttribute("session") Session session) {
        return ResponseEntity.ok(queryService.list(size));
    }

    @Operation(summary = "Get record by id (with answers)")
    @GetMapping("/{id}")
    @RequireRole
    public ResponseEntity<FeedbackDto> getOne(
            @PathVariable Long id,
            @RequestAttribute("session") Session session) {
        return ResponseEntity.ok(queryService.findById(id));
    }

    @Operation(summary = "Create record", responses = @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = FeedbackDto.class))))
    @PostMapping
    @RequireRole
    public ResponseEntity<FeedbackDto> create(
            @RequestBody FeedbackDto body,
            @RequestAttribute("session") Session session) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commandService.create(body, session));
    }

    @Operation(summary = "Update record", responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = FeedbackDto.class))))
    @PutMapping("/{id}")
    @RequireRole
    public ResponseEntity<FeedbackDto> update(
            @PathVariable Long id,
            @RequestBody FeedbackDto body,
            @RequestAttribute("session") Session session) {
        return ResponseEntity.ok(commandService.update(id, body, session));
    }

    @Operation(summary = "Publish record")
    @PostMapping("/{id}/publish")
    @RequireRole
    public ResponseEntity<FeedbackDto> publish(
            @PathVariable Long id,
            @RequestAttribute("session") Session session) {
        return ResponseEntity.ok(commandService.updatePublication(id, true, session));
    }

    @Operation(summary = "Unpublish record")
    @PostMapping("/{id}/unpublish")
    @RequireRole
    public ResponseEntity<FeedbackDto> unpublish(
            @PathVariable Long id,
            @RequestAttribute("session") Session session) {
        return ResponseEntity.ok(commandService.updatePublication(id, false, session));
    }

    @Operation(summary = "Delete permanently", responses = {
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
