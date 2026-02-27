/*────────────────────────────────────────────────────────────
 * src/main/java/com/sodimac/aclaraciones/api/service/command/impl/FaqCategoryBulkServiceImpl.java
 *────────────────────────────────────────────────────────────*/
package com.sodimac.aclaraciones.api.service.command.impl;

import com.sodimac.aclaraciones.api.model.dto.BulkCategoryUploadResult;
import com.sodimac.aclaraciones.api.model.entity.FaqCategory;
import com.sodimac.aclaraciones.api.repository.FaqCategoryRepository;
import com.sodimac.aclaraciones.api.service.command.FaqCategoryBulkService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class FaqCategoryBulkServiceImpl implements FaqCategoryBulkService {

    private final FaqCategoryRepository repo;

    public FaqCategoryBulkServiceImpl(FaqCategoryRepository repo) {
        this.repo = repo;
    }

    private static final String HEADER_NAME = "name";
    private static final String HEADER_DESCRIPTION = "description";
    private static final String HEADER_ACTIVE = "is_active";
    private static final String XLSX_CT = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /* ================= CSV legacy ================= */
    @Override
    @Transactional
    public BulkCategoryUploadResult importCsv(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        Set<String> existing = new HashSet<>();
        repo.findAll().forEach(c -> existing.add(c.getName().toLowerCase()));

        int inserted = 0, skipped = 0;
        List<BulkCategoryUploadResult.ErrorRow> errors = new ArrayList<>();
        List<FaqCategory> toSave = new ArrayList<>();

        CSVFormat format = CSVFormat.Builder.create()
                .setHeader(HEADER_NAME, HEADER_DESCRIPTION, HEADER_ACTIVE)
                .setSkipHeaderRecord(true)
                .setDelimiter("\t")
                .setTrim(true)
                .setIgnoreEmptyLines(true)
                .build();

        try (BufferedReader rd = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
                CSVParser parser = new CSVParser(rd, format)) {

            int line = 0;
            for (CSVRecord rec : parser) {
                line++;
                String name = rec.get(HEADER_NAME).trim();
                String description = rec.get(HEADER_DESCRIPTION).trim();
                String actRaw = rec.get(HEADER_ACTIVE).trim();

                if (name.isBlank() && description.isBlank() && actRaw.isBlank())
                    continue;

                if (name.isBlank()) {
                    errors.add(new BulkCategoryUploadResult.ErrorRow(line, "name is empty"));
                    skipped++;
                    continue;
                }
                if (existing.contains(name.toLowerCase())) {
                    errors.add(new BulkCategoryUploadResult.ErrorRow(line, "name already exists"));
                    skipped++;
                    continue;
                }

                boolean isActive = parseActive(actRaw);

                FaqCategory cat = new FaqCategory();
                cat.setName(name);
                cat.setDescription(description);
                cat.setIsActive(isActive);

                toSave.add(cat);
                existing.add(name.toLowerCase());
                inserted++;
            }
        }

        if (!toSave.isEmpty())
            repo.saveAll(toSave);
        return new BulkCategoryUploadResult(inserted, skipped, errors);
    }

    /* ================= XLSX moderno (alineado a FAQs) ================= */
    @Override
    @Transactional
    public BulkCategoryUploadResult importXlsx(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Archivo vacío");
        }

        final String name = Objects.toString(file.getOriginalFilename(), "").toLowerCase();
        final String ct = Objects.toString(file.getContentType(), "");
        if (!name.endsWith(".xlsx") || (!XLSX_CT.equals(ct) && !ct.contains("excel"))) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Solo se admite archivo .xlsx");
        }

        Set<String> existing = new HashSet<>();
        repo.findAll().forEach(c -> existing.add(c.getName().toLowerCase()));

        int inserted = 0, skipped = 0;
        List<BulkCategoryUploadResult.ErrorRow> errors = new ArrayList<>();
        List<FaqCategory> toSave = new ArrayList<>();

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hoja vacía");
            }

            for (int r = 1; r <= sheet.getLastRowNum(); r++) { // row 0 = headers
                Row row = sheet.getRow(r);
                if (row == null)
                    continue;

                String n = getString(row.getCell(0));
                String d = getString(row.getCell(1));
                String a = getString(row.getCell(2));

                // skip fully empty rows (consistent with FAQs)
                if (isBlank(n) && isBlank(d) && isBlank(a))
                    continue;

                if (isBlank(n)) {
                    errors.add(new BulkCategoryUploadResult.ErrorRow(r + 1, "name vacío"));
                    skipped++;
                    continue;
                }
                if (existing.contains(n.toLowerCase())) {
                    errors.add(new BulkCategoryUploadResult.ErrorRow(r + 1, "name ya existe"));
                    skipped++;
                    continue;
                }

                boolean active = parseActive(a);

                FaqCategory cat = new FaqCategory();
                cat.setName(n.trim());
                cat.setDescription(d != null ? d.trim() : null);
                cat.setIsActive(active);

                toSave.add(cat);
                existing.add(n.toLowerCase());
                inserted++;
            }
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "XLSX inválido", ex);
        }

        if (!toSave.isEmpty())
            repo.saveAll(toSave);
        return new BulkCategoryUploadResult(inserted, skipped, errors);
    }

    /* ================= Templates ================= */
    @Override
    public byte[] getTemplate() throws IOException {
        return StreamUtils.copyToByteArray(
                FaqCategoryBulkServiceImpl.class.getResourceAsStream("categorias-template.csv"));
    }

    @Override
    public byte[] getTemplateXlsx() {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet("Plantilla");
            Row h = sh.createRow(0);
            h.createCell(0).setCellValue(HEADER_NAME);
            h.createCell(1).setCellValue(HEADER_DESCRIPTION);
            h.createCell(2).setCellValue(HEADER_ACTIVE);

            Row ex = sh.createRow(1);
            ex.createCell(0).setCellValue("General");
            ex.createCell(1).setCellValue("Categoría por defecto");
            ex.createCell(2).setCellValue("✔");

            for (int i = 0; i < 3; i++)
                sh.autoSizeColumn(i);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            wb.write(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo generar plantilla", e);
        }
    }

    /* ================= Helpers ================= */
    private static boolean parseActive(String v) {
        if (v == null)
            return true; // default active
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
