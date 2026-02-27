// src/main/java/com/sodimac/aclaraciones/api/service/faq/command/impl/FaqBulkService.java
package com.sodimac.aclaraciones.api.service.faq.command.impl;

import com.sodimac.aclaraciones.api.model.dto.BulkFaqUploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FaqBulkService {

    /** Importa desde un archivo XLSX y devuelve estadísticas. */
    BulkFaqUploadResult importXlsx(MultipartFile file) throws IOException;

    /** Devuelve la plantilla XLSX para el front. */
    byte[] getTemplateXlsx() throws IOException;
}
