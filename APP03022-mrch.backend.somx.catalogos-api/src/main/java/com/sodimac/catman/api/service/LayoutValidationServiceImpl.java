package com.sodimac.catman.api.service;

import com.sodimac.catman.api.model.dto.LayoutValidationError;
import com.sodimac.catman.api.model.dto.LayoutValidationResponse;
import com.sodimac.catman.api.repository.CatalogDetailRepository;
import com.sodimac.catman.api.repository.CatalogHeaderRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
public class LayoutValidationServiceImpl implements LayoutValidationService {

    private static final int ERROR_THRESHOLD = 20;
    private static final String[] REQUIRED_HEADERS = {"tipoCatalogo","elemento","valor","fechaInicioVigencia","fechaFinVigencia","idPadre"};
    private static final String[] COL = {"A","B","C","D","E","F","G"};
    private static final int MAX_TIPO = 20, MAX_ELEM = 512, MAX_VAL = 100, MAX_ID = 10, MAX_EXTKEY = 50;
    private static final Pattern EXTKEY_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");
    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final Map<String, String> reportCache = new ConcurrentHashMap<>();
    private final CatalogHeaderRepository headerRepo;
    private final CatalogDetailRepository detailRepo;

    private final Map<String, Integer> elemFirstRow = new HashMap<>();

    public LayoutValidationServiceImpl(CatalogHeaderRepository h, CatalogDetailRepository d) {
        this.headerRepo = h;
        this.detailRepo = d;
    }

    @Override
    public LayoutValidationResponse validateLayout(MultipartFile file, String tipoSel, String nombre) {
        List<LayoutValidationError> errs = new ArrayList<>();
        elemFirstRow.clear();
        int rows = 0;
        try (InputStream is = file.getInputStream()) {
            Workbook wb = WorkbookFactory.create(is);
            Sheet sh = wb.getSheetAt(0);

            for (CellRangeAddress m : sh.getMergedRegions()) {
                int col = Math.min(m.getFirstColumn(), 6);
                errs.add(err(m.getFirstRow() + 1, COL[col],
                        "La celda está combinada con otras celdas. No se permiten celdas combinadas en el archivo."));
            }

            if (!validateHeaders(sh, errs)) return build(errs, rows);

            if (!headerRepo.findByName(nombre).isEmpty()) {
                errs.add(LayoutValidationError.builder().row(0).cell("N/A").column("nombre")
                        .message("El catálogo '" + nombre + "' ya existe en el sistema. No se permite duplicar nombres de catálogos.").build());
            }

            boolean hasColG = hasColumnG(sh);
            Set<String> elems = new HashSet<>();
            int lastData = -1;

            for (int i = sh.getLastRowNum(); i >= 1; i--) {
                Row r = sh.getRow(i);
                if (r != null && !empty(r, hasColG)) { lastData = i; break; }
            }

            int countPrimario = 0, countSecundario = 0;
            for (int i = 1; i <= Math.max(lastData, 0); i++) {
                Row r = sh.getRow(i);
                if (r == null || empty(r, hasColG)) continue;
                String t = csv(r, 0);
                if (t != null && !t.isBlank()) {
                    String tn = t.trim().toUpperCase();
                    if ("PRIMARIO".equals(tn)) countPrimario++;
                    else if ("SECUNDARIO".equals(tn)) countSecundario++;
                }
            }
            String predominantTipo;
            if (countPrimario == 0 && countSecundario == 0) {
                predominantTipo = tipoSel.toUpperCase();
            } else {
                predominantTipo = countPrimario >= countSecundario ? "PRIMARIO" : "SECUNDARIO";
            }

            for (int i = 1; i <= Math.max(lastData, 0); i++) {
                Row r = sh.getRow(i);
                int rn = i + 1;

                if (r == null || empty(r, hasColG)) {
                    if (i < lastData) {
                        errs.add(LayoutValidationError.builder().row(rn).cell("A" + rn).column("fila")
                                .message("La fila está completamente vacía. No se permiten filas vacías entre registros válidos. Las filas vacías solo se permiten al final del archivo.").build());
                    }
                    continue;
                }
                rows++;

                String tipo = csv(r, 0), elem = csv(r, 1), val = csv(r, 2);
                String fi = csvDate(r, 3), ff = csvDate(r, 4), padre = csv(r, 5);

                if (tipo == null || tipo.isBlank())
                    errs.add(err(rn, "A", "El campo 'tipoCatalogo' es obligatorio y no puede estar vacío."));
                if (elem == null || elem.isBlank())
                    errs.add(err(rn, "B", "El campo 'elemento' es obligatorio y no puede estar vacío."));
                if (fi == null || fi.isBlank())
                    errs.add(err(rn, "D", "El campo 'fechaInicioVigencia' es obligatorio y no puede estar vacío."));

                if (tipo != null && !tipo.isBlank()) {
                    String tn = tipo.trim().toUpperCase();
                    if (!tn.equals("PRIMARIO") && !tn.equals("SECUNDARIO"))
                        errs.add(err(rn, "A", "El tipo de catálogo '" + tipo + "' no es válido. Los valores permitidos son: 'primario' o 'secundario'."));
                    else if (!tn.equals(tipoSel.toUpperCase()))
                        errs.add(err(rn, "A", "El tipo de catálogo del archivo ('" + tipo + "') no coincide con el tipo seleccionado en el formulario ('" + tipoSel + "'). Verifique que el archivo corresponda al tipo de catálogo que está creando."));
                    if (tipo.length() > MAX_TIPO)
                        errs.add(err(rn, "A", "El tipo de catálogo excede la longitud máxima permitida de " + MAX_TIPO + " caracteres."));
                }

                if (elem != null && !elem.isBlank()) {
                    if (!elem.equals(elem.trim()))
                        errs.add(err(rn, "B", "El nombre del elemento no puede tener espacios al inicio o al final."));
                    if (hasForbidden(elem, true))
                        errs.add(err(rn, "B", "El valor contiene caracteres no permitidos. Caracteres prohibidos: ! ? , ¡ ¿ : ; ."));
                    if (hasSpecial(elem))
                        errs.add(err(rn, "B", "El valor contiene caracteres especiales no permitidos: @ # % ^ & * ( ) { } < > / ' \""));
                    if (elem.length() > MAX_ELEM)
                        errs.add(err(rn, "B", "El nombre del elemento excede la longitud máxima permitida de " + MAX_ELEM + " caracteres."));
                    String normalizedKey = elem.trim().toLowerCase();
                    if (!elems.add(normalizedKey)) {
                        Integer prevRow = elemFirstRow.get(normalizedKey);
                        errs.add(err(rn, "B", "El elemento '" + elem + "' está duplicado en el archivo. Ya aparece en la fila " + (prevRow != null ? prevRow : "anterior") + ". Cada elemento debe ser único dentro del catálogo."));
                    } else {
                        elemFirstRow.put(normalizedKey, rn);
                    }
                    if (detailRepo.existsByKeyIgnoreCase(elem))
                        errs.add(err(rn, "B", "El elemento '" + elem + "' ya existe en el sistema. No se pueden registrar elementos duplicados."));
                }

                if (val != null && !val.isBlank()) {
                    if (val.contains(" "))
                        errs.add(err(rn, "C", "El valor no puede contener espacios. Los espacios solo están permitidos en la columna 'elemento'."));
                    if (hasForbidden(val, false))
                        errs.add(err(rn, "C", "El valor contiene caracteres no permitidos. Caracteres prohibidos: ! ? , ¡ ¿ : ; ."));
                    if (hasSpecial(val))
                        errs.add(err(rn, "C", "El valor contiene caracteres especiales no permitidos: @ # % ^ & * ( ) { } < > / ' \""));
                    if (val.length() > MAX_VAL)
                        errs.add(err(rn, "C", "El valor excede la longitud máxima permitida de " + MAX_VAL + " caracteres."));
                }

                LocalDate di = parseD(fi), df = parseD(ff);
                if (fi != null && !fi.isBlank() && di == null)
                    errs.add(err(rn, "D", "La fecha '" + fi + "' no tiene un formato válido. Use el formato ISO-8601: yyyy-mm-dd (ejemplo: 2026-01-15)."));
                if (ff != null && !ff.isBlank() && df == null)
                    errs.add(err(rn, "E", "La fecha '" + ff + "' no tiene un formato válido. Use el formato ISO-8601: yyyy-mm-dd (ejemplo: 2026-01-15)."));
                if (di != null && df != null && di.isAfter(df))
                    errs.add(err(rn, "D", "La fecha de inicio de vigencia (" + fi + ") no puede ser posterior a la fecha de fin de vigencia (" + ff + ")."));
                if (df != null && !df.isAfter(LocalDate.now()))
                    errs.add(err(rn, "E", "La fecha de fin de vigencia (" + ff + ") debe ser mayor a la fecha actual (" + LocalDate.now().format(ISO) + ")."));

                if (padre != null && !padre.isBlank()) {
                    try {
                        int pid = Integer.parseInt(padre.trim());
                        if ("PRIMARIO".equalsIgnoreCase(tipoSel)) {
                            errs.add(err(rn, "F", "Los catálogos primarios no deben tener idPadre. El campo debe estar vacío."));
                        } else if ("SECUNDARIO".equalsIgnoreCase(tipoSel)) {
                            var parentOpt = detailRepo.findById(pid);
                            if (parentOpt.isEmpty()) {
                                errs.add(err(rn, "F", "El idPadre '" + pid + "' no existe en el sistema. Verifique que el ID sea correcto."));
                            } else {
                                var parentDetail = parentOpt.get();
                                var parentHeader = parentDetail.getHeader();
                                if (parentHeader != null) {
                                    String parentType = parentHeader.getCatalogType();
                                    if (!"PRIMARIO".equalsIgnoreCase(parentType) && !"HIERARCHICAL".equalsIgnoreCase(parentType)) {
                                        errs.add(err(rn, "F", "El idPadre '" + pid + "' debe corresponder a un elemento de un catálogo de tipo primario o HIERARCHICAL."));
                                    }
                                }
                            }
                        }
                    } catch (NumberFormatException e) {
                        errs.add(err(rn, "F", "El valor '" + padre + "' no es un número entero válido. El campo idPadre debe ser numérico."));
                    }
                    if (padre.length() > MAX_ID)
                        errs.add(err(rn, "F", "El idPadre excede la longitud máxima permitida de " + MAX_ID + " caracteres."));
                }

                if (hasColG) {
                    String extKey = csv(r, 6);
                    if (extKey != null && !extKey.isBlank()) {
                        if (extKey.length() > MAX_EXTKEY)
                            errs.add(err(rn, "G", "El valor de conversión no puede exceder " + MAX_EXTKEY + " caracteres (actual: " + extKey.length() + " caracteres)."));
                        else if (!EXTKEY_PATTERN.matcher(extKey).matches())
                            errs.add(err(rn, "G", "El valor de conversión solo puede contener letras, números, guiones, guiones bajos y puntos."));
                    }
                }
            }
            wb.close();
        } catch (Exception e) {
            errs.add(LayoutValidationError.builder().row(0).cell("N/A").column("archivo")
                    .message("Error al procesar el archivo: " + e.getMessage()).build());
        }
        return build(errs, rows);
    }

    @Override
    public String getValidationReport(String id) { return reportCache.getOrDefault(id, null); }

    private boolean hasColumnG(Sheet sh) {
        Row h = sh.getRow(0);
        if (h == null) return false;
        Cell g = h.getCell(6);
        if (g == null) return false;
        String v = csv(g);
        return v != null && v.trim().equalsIgnoreCase("valorConversion");
    }

    private boolean validateHeaders(Sheet sh, List<LayoutValidationError> e) {
        Row h = sh.getRow(0);
        if (h == null) {
            e.add(err(1, "A", "Falta la fila de encabezados. Las columnas obligatorias son: tipoCatalogo, elemento, valor, fechaInicioVigencia, fechaFinVigencia, idPadre."));
            return false;
        }
        boolean ok = true;
        for (int i = 0; i < 6; i++) {
            Cell c = h.getCell(i);
            String v = c != null ? csv(c) : null;
            String cr = COL[i] + "1";
            if (v == null || v.isBlank()) {
                e.add(LayoutValidationError.builder().row(1).cell(cr).column(REQUIRED_HEADERS[i])
                        .message("Error en la celda " + cr + ": Falta la columna '" + REQUIRED_HEADERS[i] + "'. Las columnas obligatorias son: tipoCatalogo, elemento, valor, fechaInicioVigencia, fechaFinVigencia, idPadre.").build());
                ok = false;
            } else if (!v.trim().equalsIgnoreCase(REQUIRED_HEADERS[i])) {
                e.add(LayoutValidationError.builder().row(1).cell(cr).column(REQUIRED_HEADERS[i])
                        .message("Error en la celda " + cr + ": El nombre de columna '" + v.trim() + "' es incorrecto. Debe ser '" + REQUIRED_HEADERS[i] + "'.").build());
                ok = false;
            }
        }
        Cell g = h.getCell(6);
        String gv = g != null ? csv(g) : null;
        if (gv != null && !gv.isBlank() && !gv.trim().equalsIgnoreCase("valorConversion")) {
            e.add(LayoutValidationError.builder().row(1).cell("G1").column("valorConversion")
                    .message("Error en la celda G1: El nombre de columna '" + gv.trim() + "' es incorrecto. Debe ser 'valorConversion'.").build());
            ok = false;
        }
        return ok;
    }

    private boolean empty(Row r, boolean hasColG) {
        if (r == null) return true;
        int cols = hasColG ? 7 : 6;
        for (int i = 0; i < cols; i++) {
            Cell c = r.getCell(i);
            if (c != null && c.getCellType() != CellType.BLANK) {
                String v = csv(c);
                if (v != null && !v.isBlank()) return false;
            }
        }
        return true;
    }

    private String csv(Row r, int i) { return csv(r.getCell(i)); }

    private String csv(Cell c) {
        if (c == null) return null;
        switch (c.getCellType()) {
            case STRING: return c.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(c)) return c.getLocalDateTimeCellValue().toLocalDate().format(ISO);
                double n = c.getNumericCellValue();
                return n == Math.floor(n) ? String.valueOf((long) n) : String.valueOf(n);
            case BOOLEAN: return String.valueOf(c.getBooleanCellValue());
            default: return null;
        }
    }

    private String csvDate(Row r, int i) {
        Cell c = r.getCell(i);
        if (c != null && c.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(c))
            return c.getLocalDateTimeCellValue().toLocalDate().format(ISO);
        return csv(c);
    }

    private LocalDate parseD(String v) {
        if (v == null || v.isBlank()) return null;
        try { return LocalDate.parse(v.trim(), ISO); }
        catch (DateTimeParseException e) {
            try { return LocalDate.parse(v.trim(), DateTimeFormatter.ofPattern("dd-MM-yyyy")); }
            catch (DateTimeParseException e2) { return null; }
        }
    }

    private boolean hasForbidden(String v, boolean allowSpaces) {
        String p = allowSpaces ? "[!?,¡¿:;.@#%\\^&*(){}\\[\\]<>/'\"\\\\\u0060]" : "[!?,¡¿:;.@#%\\^&*(){}\\[\\]<>/'\"\\\\\u0060 ]";
        return Pattern.compile(p).matcher(v).find();
    }

    private boolean hasSpecial(String v) {
        return Pattern.compile("[@#%\\^&*(){}\\[\\]<>/'\"\\\\\u0060]").matcher(v).find();
    }

    private LayoutValidationError err(int rn, String cl, String desc) {
        String cell = cl + rn;
        return LayoutValidationError.builder().row(rn).cell(cell).column(cl)
                .message("Error en la celda " + cell + ": " + desc).build();
    }

    private LayoutValidationResponse build(List<LayoutValidationError> errs, int rows) {
        String rid = null;
        boolean ra = false;
        if (errs.size() > ERROR_THRESHOLD) {
            rid = UUID.randomUUID().toString();
            ra = true;
            StringBuilder sb = new StringBuilder("=== REPORTE DE ERRORES ===\nTotal: " + errs.size() + "\n\n");
            for (LayoutValidationError e : errs)
                sb.append("Fila:").append(e.getRow()).append(" Celda:").append(e.getCell()).append(" ").append(e.getMessage()).append("\n");
            reportCache.put(rid, sb.toString());
        }
        return LayoutValidationResponse.builder()
                .isValid(errs.isEmpty())
                .errorCount(errs.size())
                .errors(errs)
                .reportAvailable(ra)
                .reportId(rid)
                .rowsProcessed(rows)
                .build();
    }
}
