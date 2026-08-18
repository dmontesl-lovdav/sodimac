package com.sodimac.fiscal.api.controller;

import com.sodimac.fiscal.api.model.entity.PaymentsEntity;
import com.sodimac.fiscal.api.pdf.PaymentPdfService;
import com.sodimac.fiscal.api.repository.PaymentsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

/**
 * Controlador para generacion de PDF desde XML de complementos de pago.
 *
 * @author Sodimac Tech Team
 * @since 2025-11
 */
@RestController
@RequestMapping("/api/payment/pdf")
@RequiredArgsConstructor
@Tag(name = "Payment PDF Controller", description = "Endpoints para generacion de PDF desde XML de complementos de pago")
public class PaymentPdfController {

    private final PaymentPdfService paymentPdfService;
    private final PaymentsRepository paymentsRepository;

    @Operation(
        summary = "Generar PDF de pago desde archivo XML",
        description = "Convierte un archivo XML de complemento de pago a formato PDF."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "PDF generado exitosamente",
                     content = @Content(mediaType = "application/pdf")),
        @ApiResponse(responseCode = "400", description = "XML invalido"),
        @ApiResponse(responseCode = "500", description = "Error interno al generar el PDF")
    })
    @PostMapping(value = "/from-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> pdfFromFile(
            @Parameter(description = "Archivo XML del complemento de pago", required = true)
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "true: visualizar en navegador, false: descargar")
            @RequestParam(defaultValue = "true") boolean inline) throws Exception {

        String xml = new String(file.getBytes(), StandardCharsets.UTF_8);
        byte[] pdf = paymentPdfService.renderFromXml(xml);
        String filename = Optional.ofNullable(file.getOriginalFilename())
                .orElse("from-file") + ".pdf";
        return buildPdfResponse(pdf, inline, filename);
    }

    @Operation(
        summary = "Generar PDF de pago desde UUID",
        description = "Genera un PDF de complemento de pago a partir del UUID fiscal o del payments_uuid. "
                + "El XML se obtiene de payments.xml_content."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "PDF generado exitosamente",
                     content = @Content(mediaType = "application/pdf")),
        @ApiResponse(responseCode = "404", description = "UUID no encontrado en la base de datos"),
        @ApiResponse(responseCode = "500", description = "Error interno al generar el PDF")
    })
    @GetMapping(value = "/from-uuid/{paymentUuid}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdfFromUuid(
            @Parameter(description = "UUID fiscal o payments_uuid del complemento de pago", required = true)
            @PathVariable UUID paymentUuid,
            @Parameter(description = "true: visualizar en navegador, false: descargar")
            @RequestParam(defaultValue = "true") boolean inline) {

        PaymentsEntity row = paymentsRepository.findByFiscalUuid(paymentUuid)
                .or(() -> paymentsRepository.findById(paymentUuid))
                .orElseThrow(() -> new IllegalArgumentException("No existe el UUID " + paymentUuid));

        String xml = row.getXmlContent();
        if (xml == null || xml.isBlank()) {
            throw new IllegalArgumentException("El complemento no tiene XML disponible: " + paymentUuid);
        }
        byte[] pdf = paymentPdfService.renderFromXml(xml);
        UUID nameUuid = row.getFiscalUuid() != null ? row.getFiscalUuid() : paymentUuid;
        return buildPdfResponse(pdf, inline, "pago_" + nameUuid + ".pdf");
    }

    private static ResponseEntity<byte[]> buildPdfResponse(byte[] pdf, boolean inline, String fileName) {
        ContentDisposition cd = inline
                ? ContentDisposition.inline().filename(fileName).build()
                : ContentDisposition.attachment().filename(fileName).build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .body(pdf);
    }
}
