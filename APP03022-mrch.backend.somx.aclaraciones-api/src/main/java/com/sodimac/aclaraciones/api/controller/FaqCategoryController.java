// src/main/java/com/sodimac/aclaraciones/api/controller/FaqCategoryController.java
package com.sodimac.aclaraciones.api.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.sodimac.aclaraciones.api.exception.GenericException;
import com.sodimac.aclaraciones.api.model.dto.BulkCategoryUploadResult;
import com.sodimac.aclaraciones.api.model.dto.FaqCategoryDto;
import com.sodimac.aclaraciones.api.model.dto.FaqCategoryResponse;
import com.sodimac.aclaraciones.api.model.dto.UpdateFaqCategoryPublicationRequest;
import com.sodimac.aclaraciones.api.security.RequireRole;
import com.sodimac.aclaraciones.api.security.Session;
import com.sodimac.aclaraciones.api.service.category.FaqCategoryQueryService;
import com.sodimac.aclaraciones.api.service.command.FaqCategoryBulkService;
import com.sodimac.aclaraciones.api.service.command.FaqCategoryCommandService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping({ "/faq-categories", "/faqs-categories" })
@SecurityRequirement(name = "bearerAuth")
public class FaqCategoryController {

    private final FaqCategoryQueryService queryService;
    private final FaqCategoryCommandService commandService;
    private final FaqCategoryBulkService bulkService;

    public FaqCategoryController(
            FaqCategoryQueryService queryService,
            FaqCategoryCommandService commandService,
            FaqCategoryBulkService bulkService) {
        this.queryService = queryService;
        this.commandService = commandService;
        this.bulkService = bulkService;
    }

    /* ======================= LIST ======================= */
    @Operation(operationId = "getFaqCategories", summary = "Returns active FAQ categories", responses = @ApiResponse(responseCode = "200", description = "List of categories", content = @Content(schema = @Schema(implementation = FaqCategoryDto.class))))
    @GetMapping
    @RequireRole
    public ResponseEntity<Page<FaqCategoryDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            @RequestParam(name = "includeIcons", defaultValue = "false") boolean includeIcons,
            @RequestParam(name = "includeIcon", required = false) Boolean includeIconAlias,
            @RequestParam(name = "active", required = false) String active,
            @RequestParam(name = "search", required = false) String search,
            @RequestAttribute("session") Session session) {
        boolean include = includeIcons || Boolean.TRUE.equals(includeIconAlias);

        Sort.Direction direction = dir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

        PageRequest pageable = PageRequest.of(page, size, Sort.by(direction, sort));

        return ResponseEntity.ok(
                queryService.listByActiveAndSearchPaged(active, search, include, pageable));
    }

    /* ======================= GET BY ID ======================= */
    @Operation(operationId = "getFaqCategoryById", summary = "Returns a single FAQ category by id", responses = @ApiResponse(responseCode = "200", description = "Category found", content = @Content(schema = @Schema(implementation = FaqCategoryDto.class))))
    @GetMapping("/{id}")
    @RequireRole
    public ResponseEntity<FaqCategoryDto> getById(
            @PathVariable Long id,
            @RequestParam(name = "includeIcons", required = false, defaultValue = "false") boolean includeIcons,
            @RequestParam(name = "includeIcon", required = false) Boolean includeIconAlias,
            @RequestAttribute("session") Session session) {

        boolean include = includeIcons || Boolean.TRUE.equals(includeIconAlias);
        FaqCategoryDto dto = queryService.findById(id, include);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    /* ======================= CREATE (JSON) ======================= */
    @Operation(operationId = "createFaqCategoryData", summary = "Creates a category with given data", responses = @ApiResponse(responseCode = "200", description = "Created", content = @Content(schema = @Schema(implementation = FaqCategoryResponse.class))))
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @RequireRole
    public ResponseEntity<FaqCategoryResponse> createPublication(
            @RequestBody FaqCategoryDto body,
            @RequestAttribute("session") Session session) throws GenericException {
        return ResponseEntity.ok(commandService.createPublication(body));
    }

    /* ======================= UPDATE (JSON) ======================= */
    @Operation(operationId = "updateFaqCategoryData", summary = "Replaces a category with given data", responses = @ApiResponse(responseCode = "200", description = "Updated", content = @Content(schema = @Schema(implementation = FaqCategoryResponse.class))))
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @RequireRole
    public ResponseEntity<FaqCategoryResponse> updatePublication(
            @PathVariable Long id,
            @RequestBody FaqCategoryDto body,
            @RequestAttribute("session") Session session) throws GenericException {
        return ResponseEntity.ok(commandService.updatePublication(id, body));
    }

    /* ======================= UPDATE (MULTIPART) ======================= */
    @Operation(summary = "Replaces a category with JSON + icon file (multipart)")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireRole
    public ResponseEntity<FaqCategoryResponse> updatePublicationMultipart(
            @PathVariable Long id,
            @RequestPart("data") FaqCategoryDto body,
            @RequestPart(value = "icon", required = false) MultipartFile icon,
            @RequestAttribute("session") Session session) throws GenericException {
        return ResponseEntity.ok(commandService.updatePublication(id, body, icon));
    }

    /* ======================= PUBLISH/UNPUBLISH ======================= */
    @Operation(operationId = "updateFaqCategoryPublication", summary = "Publish / unpublish a category", responses = @ApiResponse(responseCode = "200", description = "Updated", content = @Content(schema = @Schema(implementation = FaqCategoryResponse.class))))
    @PatchMapping("/{id}/publication")
    @RequireRole
    public ResponseEntity<FaqCategoryResponse> updatePublication(
            @PathVariable Long id,
            @RequestBody UpdateFaqCategoryPublicationRequest body,
            @RequestAttribute("session") Session session) {
        return ResponseEntity.ok(commandService.updatePublication(id, body.published()));
    }

    @Operation(summary = "Publish category")
    @PostMapping("/{id}/publish")
    @RequireRole
    public ResponseEntity<FaqCategoryResponse> publish(
            @PathVariable Long id,
            @RequestAttribute("session") Session session) {
        return ResponseEntity.ok(commandService.updatePublication(id, true));
    }

    @Operation(summary = "Un-publish category")
    @PostMapping("/{id}/unpublish")
    @RequireRole
    public ResponseEntity<FaqCategoryResponse> unpublish(
            @PathVariable Long id,
            @RequestAttribute("session") Session session) {
        return ResponseEntity.ok(commandService.updatePublication(id, false));
    }

    /* ======================= DELETE ======================= */
    @Operation(summary = "Delete category")
    @DeleteMapping("/{id}")
    @RequireRole
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id,
            @RequestAttribute("session") Session session) {
        commandService.deleteCategory(id);
        return ResponseEntity.noContent().build(); // 204
    }

    /* ======================= BULK UPLOAD (XLSX) ======================= */
    @Operation(summary = "Bulk upload categories (XLSX)", description = "Columns: name, description, is_active (✔ / true / 1 / v → true; default true). "
            + "Only .xlsx is accepted, same behavior as FAQs.", responses = @ApiResponse(responseCode = "201", description = "Import summary", content = @Content(schema = @Schema(implementation = BulkCategoryUploadResult.class))))
    @PostMapping(value = "/bulk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireRole
    public ResponseEntity<BulkCategoryUploadResult> bulkUpload(
            @RequestPart("file") MultipartFile file,
            @RequestAttribute("session") Session session) throws java.io.IOException {

        // Delega TODA la validación al service (igual que FAQs)
        BulkCategoryUploadResult result = bulkService.importXlsx(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /* ======================= TEMPLATE (XLSX preferido) ======================= */
    @Operation(summary = "Download XLSX template for category bulk upload")
    @GetMapping("/template")
    @RequireRole
    public ResponseEntity<byte[]> downloadTemplate(
            @RequestAttribute("session") Session session) throws java.io.IOException {
        byte[] content = bulkService.getTemplateXlsx();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"categorias-template.xlsx\"")
                .contentLength(content.length)
                .body(content);
    }

    /* (Opcional) Template CSV legacy */
    @Operation(summary = "Download CSV template for legacy bulk upload (optional)")
    @GetMapping("/template.csv")
    @RequireRole
    public ResponseEntity<byte[]> downloadTemplateCsv(
            @RequestAttribute("session") Session session) throws java.io.IOException {
        byte[] content = bulkService.getTemplate(); // CSV
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"categorias-template.csv\"")
                .contentLength(content.length)
                .body(content);
    }
}
