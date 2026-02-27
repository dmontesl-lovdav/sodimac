package com.sodimac.catman.api.service;

import com.sodimac.catman.api.model.dto.LayoutValidationResponse;
import org.springframework.web.multipart.MultipartFile;

public interface LayoutValidationService {
    LayoutValidationResponse validateLayout(MultipartFile file, String tipoCatalogoSeleccionado, String nombreCatalogo);
    String getValidationReport(String reportId);
}

