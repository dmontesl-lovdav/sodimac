package com.sodimac.fiscal.api.controller;

import com.sodimac.fiscal.api.pdf.InvoicePdfService;
import com.sodimac.fiscal.api.pdf.PdfRenderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

/**
 * Controlador para generacion de PDF desde XML de facturas CFDI 4.0.
 *
 * @author Sodimac Tech Team
 * @since 2025-11
 */
@RestController
@RequestMapping("/api/fiscal/pdf")
@RequiredArgsConstructor
@Tag(name = "PDF Controller", description = "Endpoints para generacion de PDF desde XML de facturas CFDI")
public class PdfController {

    private final PdfRenderService pdfRenderService;
    private final InvoicePdfService invoicePdfService;

    @Operation(
        summary = "Generar PDF desde archivo XML",
        description = "Convierte un archivo XML de factura CFDI 4.0 a formato PDF. Genera QR del SAT y aplica formato visual."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "PDF generado exitosamente",
                     content = @Content(mediaType = "application/pdf")),
        @ApiResponse(responseCode = "400", description = "XML invalido o version no soportada (solo CFDI 4.0)"),
        @ApiResponse(responseCode = "500", description = "Error interno al generar el PDF")
    })
    @PostMapping(value = "/from-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> pdfFromFile(
            @Parameter(description = "Archivo XML de factura CFDI 4.0", required = true)
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "true: visualizar en navegador, false: descargar")
            @RequestParam(defaultValue = "true") boolean inline) throws Exception {

        String xml = new String(file.getBytes(), StandardCharsets.UTF_8);
        byte[] pdf = pdfRenderService.renderFromXml(xml);
        String filename = Optional.ofNullable(file.getOriginalFilename())
                .orElse("from-file") + ".pdf";
        return buildPdfResponse(pdf, inline, filename);
    }

    private ResponseEntity<byte[]> buildPdfResponse(byte[] pdf, boolean inline, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        (inline ? "inline" : "attachment") + "; filename=\"" + filename + "\"")
                .body(pdf);
    }

    @Operation(
        summary = "Generar PDF desde UUID fiscal",
        description = "Genera un PDF de factura a partir del UUID fiscal almacenado en BD. El XML se obtiene del campo xml_content."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "PDF generado exitosamente",
                     content = @Content(mediaType = "application/pdf")),
        @ApiResponse(responseCode = "404", description = "UUID no encontrado en la base de datos"),
        @ApiResponse(responseCode = "500", description = "Error interno al generar el PDF")
    })
    @GetMapping("/from-fiscal-uuid/{invoiceUuid}")
    public ResponseEntity<byte[]> pdfFromFiscalUuid(
            @Parameter(description = "UUID fiscal del TimbreFiscalDigital", required = true)
            @PathVariable UUID invoiceUuid,
            @Parameter(description = "true: visualizar en navegador, false: descargar")
            @RequestParam(defaultValue = "true") boolean inline) {

        byte[] pdfBytes = invoicePdfService.generatePdfFromFiscalUuid(invoiceUuid);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        String dispositionType = inline ? "inline" : "attachment";
        String fileName = "cfdi-" + invoiceUuid + ".pdf";

        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                dispositionType + "; filename=\"" + fileName + "\"");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
