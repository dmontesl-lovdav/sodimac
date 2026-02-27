package com.sodimac.aclaraciones.api.service;

import com.sodimac.aclaraciones.api.exception.GenericException;

public interface BinaryFileService {

    /** Descarga el archivo (vía local o GCS) y lo devuelve como cadena Base64 */
    String retrieveBase64Content(String path) throws GenericException;

    /** Sube el archivo (Base64) y devuelve la ruta/objeto a guardar en BD */
    String saveBase64Content(String content) throws GenericException;
}
