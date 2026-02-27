package com.sodimac.aclaraciones.api.model.dto;

import java.util.List;

import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload para editar una FAQ (PUT).
 * Permite edición de texto, gestión incremental de adjuntos,
 * categorías adicionales e información relacionada.
 */
public record UpdateFaqRequest(

                /* Categoría principal obligatoria */
                @NotNull Long categoryId,

                /* Pregunta */
                @NotBlank @Size(max = 512) String question,

                /* Respuesta */
                @NotBlank String answer,

                /* Variantes de la pregunta */
                @Nullable List<String> aliases,

                /* IDs de FAQs relacionadas (FAQ-FAQ) */
                @Nullable List<Long> relatedIds,

                /* Adjuntos NUEVOS a agregar (mismo nombre: files) */
                @ArraySchema(schema = @Schema(type = "string", format = "binary")) @Nullable List<MultipartFile> files,

                /**
                 * Adjuntos que se desean CONSERVAR.
                 * Si es null o vacío, se consideran conservados todos los actuales.
                 */
                @Nullable List<Long> keepAttachmentIds,

                /**
                 * Adjuntos que se desean ELIMINAR (soft delete).
                 * Si se envía, se marca isActive=false sin importar keepAttachmentIds.
                 */
                @Nullable List<Long> removeAttachmentIds,

                /* === NUEVOS CAMPOS (opcionales) === */

                /** Categorías adicionales (además de categoryId). */
                @Nullable List<Long> categoryIds,

                /** IDs de "Información relacionada" (entidad externa). */
                @Nullable List<Long> relatedInfoIds) {
}
