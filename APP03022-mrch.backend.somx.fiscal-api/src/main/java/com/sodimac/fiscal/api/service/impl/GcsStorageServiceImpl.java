package com.sodimac.fiscal.api.service.impl;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.sodimac.fiscal.api.service.GcsStorageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class GcsStorageServiceImpl implements GcsStorageService {

    @Value("${fiscal.storage.gcs.bucket:}")
    private String bucket;

    @Value("${fiscal.storage.gcs.prefix:fiscal}")
    private String prefix;

    private Storage storage;

    @PostConstruct
    public void init() {
        if (bucket != null && !bucket.isBlank()) {
            this.storage = StorageOptions.getDefaultInstance().getService();
            log.info("GCS storage inicializado. Bucket={} Prefix={}", bucket, prefix);
        } else {
            log.warn("GCS no configurado (fiscal.storage.gcs.bucket vacío) — uploads de PDF deshabilitados");
        }
    }

    @Override
    public String uploadPdf(MultipartFile file, String invoiceUuid) throws Exception {
        if (storage == null) {
            throw new IllegalStateException("GCS no configurado — fiscal.storage.gcs.bucket es obligatorio para subir PDFs");
        }
        String normalizedPrefix = prefix.replaceAll("^/+", "").replaceAll("/+$", "");
        String objectName = (normalizedPrefix.isBlank() ? "" : normalizedPrefix + "/") + invoiceUuid + ".pdf";

        BlobId blobId = BlobId.of(bucket, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("application/pdf")
                .build();

        Blob blob = storage.create(blobInfo, file.getBytes());
        String storageId = blob.getName();
        log.debug("PDF subido a GCS. bucket={} storageId={} size={}", bucket, storageId, file.getSize());
        return storageId;
    }

    @Override
    public byte[] downloadPdf(String storageId) throws Exception {
        if (storage == null) {
            throw new IllegalStateException("GCS no configurado — fiscal.storage.gcs.bucket es obligatorio para descargar PDFs");
        }
        byte[] bytes = storage.readAllBytes(bucket, storageId);
        log.debug("PDF descargado de GCS. bucket={} storageId={} size={}", bucket, storageId, bytes.length);
        return bytes;
    }
}
