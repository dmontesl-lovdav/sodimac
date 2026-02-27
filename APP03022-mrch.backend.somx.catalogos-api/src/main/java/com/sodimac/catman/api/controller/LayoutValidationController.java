package com.sodimac.catman.api.controller;

import com.sodimac.catman.api.model.dto.LayoutValidationResponse;
import com.sodimac.catman.api.service.LayoutValidationService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/catalogos")
public class LayoutValidationController {

    private final LayoutValidationService svc;
    public LayoutValidationController(LayoutValidationService svc) { this.svc = svc; }

    @PostMapping(value = "/validate-layout", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LayoutValidationResponse> validate(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tipoCatalogoSeleccionado") String tipo,
            @RequestParam("nombreCatalogo") String nombre) {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(svc.validateLayout(file, tipo, nombre));
    }

    @GetMapping("/validation-reports/{reportId}")
    public ResponseEntity<byte[]> report(@PathVariable String reportId) {
        String r = svc.getValidationReport(reportId);
        if (r == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_errores.txt")
                .body(r.getBytes());
    }
}

