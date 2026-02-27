// src/main/java/com/sodimac/aclaraciones/api/controller/FaqController.java
package com.sodimac.aclaraciones.api.controller;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.sodimac.aclaraciones.api.model.dto.*;
import com.sodimac.aclaraciones.api.model.dto.filter.FaqFilter;
import com.sodimac.aclaraciones.api.model.dto.view.FaqView;
import com.sodimac.aclaraciones.api.model.entity.FaqAttachment;
import com.sodimac.aclaraciones.api.security.RequireRole;
import com.sodimac.aclaraciones.api.security.Session;
import com.sodimac.aclaraciones.api.service.faq.command.impl.FaqAttachmentService;
import com.sodimac.aclaraciones.api.service.faq.command.impl.FaqBulkService;
import com.sodimac.aclaraciones.api.service.faq.command.impl.FaqCommandService;
import com.sodimac.aclaraciones.api.service.faq.command.impl.FaqQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/faqs")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class FaqController {

        private final FaqQueryService queryService;
        private final FaqCommandService commandService;
        private final FaqBulkService bulkService;
        private final FaqAttachmentService attachmentService;

        public FaqController(FaqQueryService queryService,
                        FaqCommandService commandService,
                        FaqBulkService bulkService,
                        FaqAttachmentService attachmentService) {
                this.queryService = queryService;
                this.commandService = commandService;
                this.bulkService = bulkService;
                this.attachmentService = attachmentService;
        }

        /* ==================================================================== */
        /* GET /faqs – lista / búsqueda */
        /* ==================================================================== */
        @Operation(operationId = "getFaqs", summary = "Busca preguntas frecuentes activas", description = """
                        Filtra por texto libre (`searchTerm`) y/o `categoryId`.
                        Si `popularOnly=true` las ordena por `views DESC`.
                        """)
        @GetMapping
        @RequireRole
        public ResponseEntity<List<FaqView>> list(
                        @RequestParam(required = false) String searchTerm,
                        @RequestParam(required = false) Long categoryId,
                        @RequestParam(required = false) Boolean popularOnly,
                        @RequestParam(defaultValue = "50") int size,
                        @RequestAttribute("session") Session session) {

                String normalizedSearch = (searchTerm != null && !searchTerm.isBlank())
                                ? searchTerm.trim()
                                : null;
                Long normalizedCatId = (categoryId != null && categoryId > 0)
                                ? categoryId
                                : null;

                boolean effectivePopular = Boolean.TRUE.equals(popularOnly) ||
                                (normalizedSearch == null && normalizedCatId == null);
                int effectiveSize = Math.min(Math.max(size, 1), 50);

                FaqFilter filter = new FaqFilter(
                                normalizedSearch,
                                normalizedCatId,
                                effectivePopular,
                                effectiveSize);

                return ResponseEntity.ok()
                                .cacheControl(CacheControl.noCache())
                                .body(queryService.find(filter));
        }

        /* ==================================================================== */
        /* GET /faqs/{id} – Detalle para edición */
        /* ==================================================================== */
        @Operation(operationId = "getFaqById", summary = "Obtiene la FAQ por id (detalle completo)", responses = {
                        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = FaqDetailResponse.class))),
                        @ApiResponse(responseCode = "404", description = "No existe la FAQ", content = @Content(schema = @Schema(implementation = Map.class)))
        })
        @GetMapping("/{id}")
        @RequireRole
        public ResponseEntity<FaqDetailResponse> getOne(
                        @PathVariable Long id,
                        @RequestAttribute("session") Session session) {

                FaqDetailResponse dto = queryService.findDetailById(id);
                return (dto == null) ? ResponseEntity.notFound().build()
                                : ResponseEntity.ok(dto);
        }

        /* ==================================================================== */
        /* POST /faqs — multipart v2-friendly (file1/file2/file3) */
        /* ==================================================================== */
        @Operation(operationId = "createFaq", summary = "Crea una nueva pregunta frecuente", description = """
                        Recibe pregunta(s), respuesta, categoría y (opcional)
                        archivos adjuntos. Devuelve la FAQ recién creada.
                        """, responses = @ApiResponse(responseCode = "201", description = "Creada", content = @Content(schema = @Schema(implementation = FaqResponse.class))))
        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @RequireRole
        public ResponseEntity<FaqResponse> create(
                        // obligatorios
                        @RequestParam("categoryId") Long categoryId,
                        @RequestParam("question") String question,
                        @RequestParam("answer") String answer,

                        // opcionales (listas: repetir el mismo nombre en form-data)
                        @RequestParam(value = "aliases", required = false) List<String> aliases,
                        @RequestParam(value = "relatedIds", required = false) List<Long> relatedIds,
                        @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds,
                        @RequestParam(value = "relatedInfoIds", required = false) List<Long> relatedInfoIds,

                        // archivos como campos separados (OAS2 no soporta array de file)
                        @RequestParam(value = "file1", required = false) MultipartFile file1,
                        @RequestParam(value = "file2", required = false) MultipartFile file2,
                        @RequestParam(value = "file3", required = false) MultipartFile file3,

                        @RequestAttribute("session") Session session) throws IOException {

                List<MultipartFile> files = new ArrayList<>();
                if (Objects.nonNull(file1))
                        files.add(file1);
                if (Objects.nonNull(file2))
                        files.add(file2);
                if (Objects.nonNull(file3))
                        files.add(file3);

                CreateFaqRequest request = new CreateFaqRequest(
                                categoryId,
                                question,
                                answer,
                                aliases,
                                relatedIds,
                                files, // mapeo desde file1..3
                                categoryIds,
                                relatedInfoIds);

                FaqResponse created = commandService.createFaq(request);

                URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                                .path("/{id}")
                                .buildAndExpand(created.id())
                                .toUri();

                return ResponseEntity.created(location).body(created);
        }

        /* ==================================================================== */
        /* PATCH /faqs/{id}/publication */
        /* ==================================================================== */
        @Operation(operationId = "updateFaqPublication", summary = "Publica o despublica una FAQ", responses = {
                        @ApiResponse(responseCode = "200", description = "Estado actualizado", content = @Content(schema = @Schema(implementation = FaqResponse.class))),
                        @ApiResponse(responseCode = "404", description = "FAQ no existe", content = @Content(schema = @Schema(implementation = Map.class)))
        })
        @PatchMapping("/{id}/publication")
        @RequireRole
        public ResponseEntity<FaqResponse> updatePublication(
                        @PathVariable Long id,
                        @RequestBody @Valid UpdateFaqPublicationRequest body,
                        @RequestAttribute("session") Session session) {

                FaqResponse updated = commandService.updatePublication(id, body.published());
                return ResponseEntity.ok(updated);
        }

        /* ==================================================================== */
        /* DELETE /faqs/{id} */
        /* ==================================================================== */
        @Operation(operationId = "deleteFaq", summary = "Elimina permanentemente la FAQ", responses = {
                        @ApiResponse(responseCode = "204", description = "Eliminada"),
                        @ApiResponse(responseCode = "404", description = "FAQ no existe", content = @Content(schema = @Schema(implementation = Map.class)))
        })
        @DeleteMapping("/{id}")
        @RequireRole
        public ResponseEntity<Void> delete(
                        @PathVariable Long id,
                        @RequestAttribute("session") Session session) {

                commandService.deleteFaq(id);
                return ResponseEntity.noContent().build();
        }

        /* ==================================================================== */
        /* Helpers de publicación */
        /* ==================================================================== */
        @Operation(summary = "Publica la FAQ")
        @PostMapping("/{id}/publish")
        @RequireRole
        public ResponseEntity<FaqResponse> publish(
                        @PathVariable Long id,
                        @RequestAttribute("session") Session session) {

                return ResponseEntity.ok(commandService.updatePublication(id, true));
        }

        @Operation(summary = "Despublica la FAQ")
        @PostMapping("/{id}/unpublish")
        @RequireRole
        public ResponseEntity<FaqResponse> unpublish(
                        @PathVariable Long id,
                        @RequestAttribute("session") Session session) {

                return ResponseEntity.ok(commandService.updatePublication(id, false));
        }

        /* ==================================================================== */
        /* PUT /faqs/{id} — multipart v2-friendly (file1/file2/file3) */
        /* ==================================================================== */
        @Operation(operationId = "updateFaq", summary = "Actualiza la pregunta frecuente", responses = {
                        @ApiResponse(responseCode = "200", description = "Actualizada", content = @Content(schema = @Schema(implementation = FaqResponse.class))),
                        @ApiResponse(responseCode = "404", description = "FAQ no existe", content = @Content(schema = @Schema(implementation = Map.class)))
        })
        @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @RequireRole
        public ResponseEntity<FaqResponse> update(
                        @PathVariable Long id,

                        // obligatorios
                        @RequestParam("categoryId") Long categoryId,
                        @RequestParam("question") String question,
                        @RequestParam("answer") String answer,

                        // opcionales (listas)
                        @RequestParam(value = "aliases", required = false) List<String> aliases,
                        @RequestParam(value = "relatedIds", required = false) List<Long> relatedIds,
                        @RequestParam(value = "keepAttachmentIds", required = false) List<Long> keepAttachmentIds,
                        @RequestParam(value = "removeAttachmentIds", required = false) List<Long> removeAttachmentIds,
                        @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds,
                        @RequestParam(value = "relatedInfoIds", required = false) List<Long> relatedInfoIds,

                        // archivos nuevos a agregar
                        @RequestParam(value = "file1", required = false) MultipartFile file1,
                        @RequestParam(value = "file2", required = false) MultipartFile file2,
                        @RequestParam(value = "file3", required = false) MultipartFile file3,

                        @RequestAttribute("session") Session session) throws IOException {

                List<MultipartFile> files = new ArrayList<>();
                if (Objects.nonNull(file1))
                        files.add(file1);
                if (Objects.nonNull(file2))
                        files.add(file2);
                if (Objects.nonNull(file3))
                        files.add(file3);

                UpdateFaqRequest request = new UpdateFaqRequest(
                                categoryId,
                                question,
                                answer,
                                aliases,
                                relatedIds,
                                files, // nuevos adjuntos
                                keepAttachmentIds,
                                removeAttachmentIds,
                                categoryIds,
                                relatedInfoIds);

                FaqResponse updated = commandService.updateFaq(id, request);
                return ResponseEntity.ok(updated);
        }

        /* ==================================================================== */
        /* POST /faqs/bulk-upload – SOLO XLSX */
        /* ==================================================================== */
        @Operation(summary = "Bulk upload FAQs from XLSX", description = "Sube un archivo .xlsx con columnas: question, answer, publicado (✔/✖).", responses = @ApiResponse(responseCode = "201", description = "Resumen de importación", content = @Content(schema = @Schema(implementation = BulkFaqUploadResult.class))))
        @PostMapping(value = "/bulk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @RequireRole
        public ResponseEntity<BulkFaqUploadResult> bulkUpload(
                        @RequestPart("file") MultipartFile file,
                        @RequestAttribute("session") Session session) throws IOException {

                BulkFaqUploadResult res = bulkService.importXlsx(file);
                return ResponseEntity.status(HttpStatus.CREATED).body(res);
        }

        /* ==================================================================== */
        /* GET /faqs/template – descarga plantilla XLSX */
        /* ==================================================================== */
        @Operation(summary = "Download XLSX template for FAQ bulk upload")
        @GetMapping("/template")
        @RequireRole
        public ResponseEntity<byte[]> downloadTemplate(
                        @RequestAttribute("session") Session session) throws IOException {

                byte[] content = bulkService.getTemplateXlsx();
                return ResponseEntity.ok()
                                .contentType(MediaType.parseMediaType(
                                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=\"faq-template.xlsx\"")
                                .contentLength(content.length)
                                .body(content);
        }

        /* ==================================================================== */
        /* GET /faqs/attachments/{id}/download – Descarga adjunto PDF */
        /* ==================================================================== */
        @Operation(summary = "Descarga el archivo adjunto de una FAQ")
        @GetMapping("/attachments/{id}/download")
        @RequireRole
        public ResponseEntity<byte[]> downloadAttachment(
                        @PathVariable Long id,
                        @RequestAttribute("session") Session session) {

                FaqAttachment att = attachmentService.findById(id);

                return ResponseEntity.ok()
                                .contentType(MediaType.parseMediaType(att.getContentType()))
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=\"" + att.getFileName() + "\"")
                                .contentLength(att.getData().length)
                                .body(att.getData());
        }
}
