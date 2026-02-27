package com.sodimac.aclaraciones.api.model.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload para crear una FAQ.
 * Se usa como @ModelAttribute en el endpoint POST /faqs (multipart/form-data).
 *
 * Campos:
 * - categoryId: categoría principal (obligatoria, compatibilidad hacia atrás).
 * - categoryIds: categorías adicionales (opcional, permite varias).
 * - relatedIds: IDs de otras FAQs relacionadas (opcional).
 * - relatedInfoIds: IDs de "Información relacionada" (opcional, permite
 * varias).
 * - aliases: variantes de la pregunta (opcional).
 * - files: adjuntos (opcional, múltiples).
 */
public record CreateFaqRequest(

                /* FK categoría principal */
                @NotNull Long categoryId,

                /* Pregunta / variante principal */
                @NotBlank @Size(max = 512) String question,

                /* Respuesta */
                @NotBlank String answer,

                /* Variantes de la pregunta */
                List<String> aliases,

                /* IDs de FAQs relacionadas (FAQ-FAQ) */
                @ArraySchema(schema = @Schema(type = "integer", format = "int64")) List<Long> relatedIds,

                /* Adjuntos (PDF, imágenes, etc.) — nombre de campo en multipart: "files" */
                @ArraySchema(schema = @Schema(type = "string", format = "binary")) List<MultipartFile> files,

                /* === NUEVOS CAMPOS (opcionales) === */

                /** Categorías adicionales (además de categoryId). */
                @ArraySchema(schema = @Schema(type = "integer", format = "int64")) List<Long> categoryIds,

                /** IDs de "Información relacionada" (entidad externa) */
                @ArraySchema(schema = @Schema(type = "integer", format = "int64")) List<Long> relatedInfoIds) {
}
