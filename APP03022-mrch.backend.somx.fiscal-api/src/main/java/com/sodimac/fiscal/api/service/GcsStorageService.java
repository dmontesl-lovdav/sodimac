package com.sodimac.fiscal.api.service;

import org.springframework.web.multipart.MultipartFile;

public interface GcsStorageService {
    String uploadPdf(MultipartFile file, String invoiceUuid) throws Exception;
    byte[] downloadPdf(String objectName) throws Exception;
}
