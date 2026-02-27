/*───────────────────────────────────────────────────────────
 * src/main/java/com/sodimac/aclaraciones/api/service/command/FaqCategoryBulkService.java
 *───────────────────────────────────────────────────────────*/
package com.sodimac.aclaraciones.api.service.command;

import com.sodimac.aclaraciones.api.model.dto.BulkCategoryUploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FaqCategoryBulkService {

    /** Importación legacy CSV */
    BulkCategoryUploadResult importCsv(MultipartFile file) throws IOException;

    /** Importación moderna XLSX */
    BulkCategoryUploadResult importXlsx(MultipartFile file) throws IOException;

    /** Plantilla CSV legacy (opcional) */
    byte[] getTemplate() throws IOException;

    /**
     * Plantilla XLSX (ahora frontend la maneja, este método puede devolver vacío si
     * no se usa)
     */
    byte[] getTemplateXlsx() throws IOException;
}
