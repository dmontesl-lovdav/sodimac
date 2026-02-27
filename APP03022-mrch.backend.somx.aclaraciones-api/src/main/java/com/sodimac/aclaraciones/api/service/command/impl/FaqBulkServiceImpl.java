// src/main/java/com/sodimac/aclaraciones/api/service/command/impl/FaqBulkServiceImpl.java
package com.sodimac.aclaraciones.api.service.command.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.sodimac.aclaraciones.api.model.dto.BulkFaqUploadResult;
import com.sodimac.aclaraciones.api.model.entity.Faq;
import com.sodimac.aclaraciones.api.model.entity.FaqCategory;
import com.sodimac.aclaraciones.api.repository.FaqCategoryRepository;
import com.sodimac.aclaraciones.api.repository.FaqRepository;
import com.sodimac.aclaraciones.api.service.faq.command.impl.FaqBulkService; // interfaz
import com.sodimac.aclaraciones.api.util.FaqTemplateBuilder; // plantilla XLSX

/**
 * Importa FAQs exclusivamente desde un archivo **XLSX** con encabezados:
 * question | answer | publicado (✔/✖ o true/false).
 *
 * Además, expone la plantilla de ejemplo en XLSX.
 */
@Service
public class FaqBulkServiceImpl implements FaqBulkService {

    private static final String XLSX_CT = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String GENERIC_CATEGORY_NAME = "General";

    private final FaqRepository faqRepo;
    private final FaqCategoryRepository catRepo;

    public FaqBulkServiceImpl(FaqRepository faqRepo,
            FaqCategoryRepository catRepo) {
        this.faqRepo = faqRepo;
        this.catRepo = catRepo;
    }

    /* ===================================================================== */
    /* Bulk import – SOLO XLSX */
    /* ===================================================================== */
    @Override
    @Transactional
    public BulkFaqUploadResult importXlsx(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Archivo vacío");
        }
        final String name = Objects.toString(file.getOriginalFilename(), "").toLowerCase();
        final String ct = Objects.toString(file.getContentType(), "");
        if (!name.endsWith(".xlsx") || (!XLSX_CT.equals(ct) && !ct.contains("excel"))) {
            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Solo se admite archivo .xlsx");
        }

        FaqCategory generic = findOrCreateGenericCategory();

        // evitar duplicados por pregunta (case-insensitive)
        Set<String> existing = new HashSet<>();
        faqRepo.findAll().forEach(f -> existing.add(f.getQuestion().toLowerCase()));

        List<Faq> toSave = new ArrayList<>();
        List<BulkFaqUploadResult.ErrorRow> errors = new ArrayList<>();
        int inserted = 0, skipped = 0;

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0); // “Plantilla” o primera hoja
            if (sheet == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hoja vacía");
            }

            // Encabezados esperados en fila 0:
            // A: question, B: answer, C: publicado
            final int firstRow = 1; // datos comienzan en la fila 1
            for (int r = firstRow; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null)
                    continue;

                String q = getString(row.getCell(0));
                String a = getString(row.getCell(1));
                String pub = getString(row.getCell(2));

                // saltar filas completamente vacías
                if (isBlank(q) && isBlank(a) && isBlank(pub))
                    continue;

                if (isBlank(q) || isBlank(a)) {
                    errors.add(new BulkFaqUploadResult.ErrorRow(r + 1, "question/answer vacío"));
                    skipped++;
                    continue;
                }
                if (existing.contains(q.toLowerCase())) {
                    errors.add(new BulkFaqUploadResult.ErrorRow(r + 1, "question ya existe"));
                    skipped++;
                    continue;
                }

                boolean active = parsePublished(pub);

                Faq faq = new Faq();
                faq.setQuestion(q.trim());
                faq.setAnswer(a.trim());
                faq.setActive(active);
                faq.setCategory(generic);

                toSave.add(faq);
                existing.add(q.toLowerCase());
                inserted++;
            }
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "XLSX inválido", ex);
        }

        if (!toSave.isEmpty()) {
            faqRepo.saveAll(toSave);
        }
        return new BulkFaqUploadResult(inserted, skipped, errors);
    }

    /* ===================================================================== */
    /* Descarga plantilla – XLSX */
    /* ===================================================================== */
    @Override
    public byte[] getTemplateXlsx() {
        // Genera en memoria el XLSX con hojas "Plantilla" y "Ejemplo"
        return FaqTemplateBuilder.build();
    }

    /* ===================================================================== */
    /* Helpers */
    /* ===================================================================== */
    private FaqCategory findOrCreateGenericCategory() {
        return catRepo.findByNameIgnoreCase(GENERIC_CATEGORY_NAME)
                .orElseGet(() -> {
                    FaqCategory cat = new FaqCategory();
                    cat.setName(GENERIC_CATEGORY_NAME);
                    cat.setDescription("Categoría por defecto para FAQs importadas");
                    cat.setIsActive(true);
                    return catRepo.save(cat);
                });
    }

    private static boolean parsePublished(String v) {
        if (v == null)
            return true; // por defecto, publicado
        String s = v.trim();
        return "✔".equals(s) || "true".equalsIgnoreCase(s) || "1".equals(s) || "v".equalsIgnoreCase(s);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String getString(Cell c) {
        if (c == null)
            return null;
        return switch (c.getCellType()) {
            case STRING -> c.getStringCellValue();
            case BOOLEAN -> Boolean.toString(c.getBooleanCellValue());
            case NUMERIC -> {
                double n = c.getNumericCellValue();
                if (DateUtil.isCellDateFormatted(c)) {
                    // no esperamos fechas aquí; devuélvelo textual
                    yield c.getDateCellValue().toString();
                }
                yield (Math.floor(n) == n) ? Long.toString((long) n) : Double.toString(n);
            }
            case FORMULA -> {
                try {
                    yield c.getStringCellValue();
                } catch (IllegalStateException e) {
                    yield Double.toString(c.getNumericCellValue());
                }
            }
            case BLANK, _NONE, ERROR -> null;
        };
    }
}
