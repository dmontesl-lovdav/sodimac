/*
 * Archivo: FsBinaryFileServiceImpl.java
 * Servicio local: guarda y lee archivos en disco temporal
 */
package com.sodimac.aclaraciones.api.service.impl;

import com.sodimac.aclaraciones.api.exception.GenericException;
import com.sodimac.aclaraciones.api.service.BinaryFileService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Base64;

@Component
@ConditionalOnProperty(name = "aclaraciones.storage.type", havingValue = "fs", matchIfMissing = true // ← si la prop no
                                                                                                     // existe, usa FS
)
public class FsBinaryFileServiceImpl implements BinaryFileService {

    @Override
    public String retrieveBase64Content(String path) {

        try (FileInputStream fis = new FileInputStream(path)) {
            return Base64.getEncoder().encodeToString(fis.readAllBytes());
        } catch (Exception e) {
            // SUPPRESSING EXCEPTION
            return null;
        }
    }

    @Override
    public String saveBase64Content(String base64Content) throws GenericException {
        try {
            File file = File.createTempFile("aclaraciones", ".attachment");

            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(Base64.getDecoder().decode(base64Content));
                fos.flush();
            }

            return file.getAbsolutePath();
        } catch (Exception e) {
            throw new GenericException(
                    "Unable to create temp file",
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}
