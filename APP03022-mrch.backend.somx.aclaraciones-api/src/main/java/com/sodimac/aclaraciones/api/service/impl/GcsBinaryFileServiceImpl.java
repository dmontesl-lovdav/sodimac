package com.sodimac.aclaraciones.api.service.impl;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.sodimac.aclaraciones.api.config.CountryContextHolder;
import com.sodimac.aclaraciones.api.exception.GenericException;
import com.sodimac.aclaraciones.api.service.BinaryFileService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "aclaraciones.storage.type", havingValue = "gcs")
public class GcsBinaryFileServiceImpl implements BinaryFileService {

    private static final Logger log = LoggerFactory.getLogger(GcsBinaryFileServiceImpl.class);

    private final Storage storage = StorageOptions.getDefaultInstance().getService();

    @Value("${aclaraciones.storage.gcs.bucket}")
    private String bucket;

    // Sodimac
    @Value("${GCS_PREFIX_SOMX}")
    private String soMx;
    @Value("${GCS_PREFIX_SOCL}")
    private String soCl;
    @Value("${GCS_PREFIX_SOPE}")
    private String soPe;
    @Value("${GCS_PREFIX_SOCO}")
    private String soCo;
    @Value("${GCS_PREFIX_SOAR}")
    private String soAr;
    @Value("${GCS_PREFIX_SOUY}")
    private String soUy;
    @Value("${GCS_PREFIX_SOBR}")
    private String soBr;

    // Falabella
    @Value("${GCS_PREFIX_FACL}")
    private String faCl;
    @Value("${GCS_PREFIX_FAPE}")
    private String faPe;
    @Value("${GCS_PREFIX_FACO}")
    private String faCo;

    // Tottus
    @Value("${GCS_PREFIX_TOCL}")
    private String toCl;
    @Value("${GCS_PREFIX_TOPE}")
    private String toPe;

    private final Map<String, String> prefixByTenant = new HashMap<>();

    @PostConstruct
    public void init() {
        // build map ONLY (no tenant resolution here)
        prefixByTenant.put("SOMX", soMx);
        prefixByTenant.put("SOCL", soCl);
        prefixByTenant.put("SOPE", soPe);
        prefixByTenant.put("SOCO", soCo);
        prefixByTenant.put("SOAR", soAr);
        prefixByTenant.put("SOUY", soUy);
        prefixByTenant.put("SOBR", soBr);

        prefixByTenant.put("FACL", faCl);
        prefixByTenant.put("FAPE", faPe);
        prefixByTenant.put("FACO", faCo);

        prefixByTenant.put("TOCL", toCl);
        prefixByTenant.put("TOPE", toPe);

        log.info("☁️ GCS prefixes loaded (map initialized)");
    }

    private String resolvePrefix() {
        String commerce = CountryContextHolder.getCommerce();
        String country = CountryContextHolder.getCountry();

        String tenantKey = normalizeTenantKey(commerce, country);
        String resolvedPrefix = prefixByTenant.get(tenantKey);

        if (resolvedPrefix == null || resolvedPrefix.isBlank()) {
            throw new IllegalStateException(
                    "❌ GCS prefix not configured for tenant: " + tenantKey);
        }

        return resolvedPrefix.endsWith("/") ? resolvedPrefix : resolvedPrefix + "/";
    }

    private String normalizeTenantKey(String commerce, String country) {
        if ("SOD".equals(commerce))
            return "SO" + country;
        if ("FAL".equals(commerce))
            return "FA" + country;
        if ("TOT".equals(commerce))
            return "TO" + country;
        return commerce + country;
    }

    @Override
    public String saveBase64Content(String base64Content) throws GenericException {
        try {
            if (base64Content != null && base64Content.startsWith("data:")) {
                int i = base64Content.indexOf(',');
                base64Content = (i > 0) ? base64Content.substring(i + 1) : base64Content;
            }

            byte[] bytes = Base64.getDecoder().decode(base64Content);
            String prefix = resolvePrefix();
            String objectName = prefix + UUID.randomUUID();

            log.debug("📤 Uploading → bucket={} tenantPrefix={} object={} size={}",
                    bucket, prefix, objectName, bytes.length);

            BlobId id = BlobId.of(bucket, objectName);
            BlobInfo info = BlobInfo.newBuilder(id)
                    .setContentType("application/octet-stream")
                    .build();

            storage.create(info, bytes);

            return objectName;

        } catch (Exception ex) {
            log.error("❌ Error uploading file to GCS", ex);
            throw new GenericException(
                    "Error uploading file to Cloud Storage",
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public String retrieveBase64Content(String path) throws GenericException {
        try {
            byte[] raw = storage.readAllBytes(bucket, path);
            return Base64.getEncoder().encodeToString(raw);

        } catch (Exception ex) {
            log.error("❌ Error reading file from GCS → path={}", path, ex);
            throw new GenericException(
                    "File not found: " + path,
                    HttpStatus.NOT_FOUND.value());
        }
    }
}
