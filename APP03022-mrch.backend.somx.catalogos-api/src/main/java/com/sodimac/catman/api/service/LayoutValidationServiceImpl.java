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
    private static final String[] EXPECTED_HEADERS = {"tipoCatalogo","elemento","valor","fechaInicioVigencia","fechaFinVigencia","idPadre"};
    private static final String[] COL = {"A","B","C","D","E","F"};
    private static final int MAX_TIPO=20, MAX_ELEM=64, MAX_VAL=100, MAX_ID=10;
    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final Map<String, String> reportCache = new ConcurrentHashMap<>();
    private final CatalogHeaderRepository headerRepo;
    private final CatalogDetailRepository detailRepo;

    public LayoutValidationServiceImpl(CatalogHeaderRepository h, CatalogDetailRepository d) { this.headerRepo=h; this.detailRepo=d; }

    @Override
    public LayoutValidationResponse validateLayout(MultipartFile file, String tipoSel, String nombre) {
        List<LayoutValidationError> errs = new ArrayList<>();
        int rows = 0;
        try (InputStream is = file.getInputStream()) {
            Workbook wb = WorkbookFactory.create(is);
            Sheet sh = wb.getSheetAt(0);
            for (CellRangeAddress m : sh.getMergedRegions())
                errs.add(err(m.getFirstRow()+1, COL[Math.min(m.getFirstColumn(),5)], "merged", "Celdas combinadas no permitidas."));
            if (!validateHeaders(sh, errs)) return build(errs, rows);
            if (headerRepo.findByName(nombre).isPresent())
                errs.add(LayoutValidationError.builder().row(0).cell("N/A").column("nombre").message("Ya existe catálogo: "+nombre).build());
            String firstTipo = null; Set<String> elems = new HashSet<>(); int lastData = -1;
            for (int i = sh.getLastRowNum(); i >= 1; i--) { Row r = sh.getRow(i); if (r!=null && !empty(r)) { lastData=i; break; } }
            for (int i = 1; i <= Math.max(lastData, 0); i++) {
                Row r = sh.getRow(i); int rn = i+1;
                if (r==null||empty(r)) { if(i<lastData) errs.add(err(rn,"A","fila","Fila vacía intermedia.")); continue; }
                rows++;
                String tipo=csv(r,0), elem=csv(r,1), val=csv(r,2), fi=csvDate(r,3), ff=csvDate(r,4), padre=csv(r,5);
                if(tipo==null||tipo.isBlank()) errs.add(err(rn,"A","tipoCatalogo","Campo obligatorio vacío."));
                if(elem==null||elem.isBlank()) errs.add(err(rn,"B","elemento","Campo obligatorio vacío."));
                if(fi==null||fi.isBlank()) errs.add(err(rn,"D","fechaInicioVigencia","Campo obligatorio vacío."));
                if(tipo!=null&&!tipo.isBlank()) {
                    String tn=tipo.trim().toUpperCase();
                    if(!tn.equals("PRIMARIO")&&!tn.equals("SECUNDARIO")) errs.add(err(rn,"A","tipoCatalogo","Solo 'primario' o 'secundario'."));
                    if(firstTipo==null) firstTipo=tn; else if(!tn.equals(firstTipo)) errs.add(err(rn,"A","tipoCatalogo","Tipo inconsistente."));
                    if(!tn.equals(tipoSel.toUpperCase())) errs.add(err(rn,"A","tipoCatalogo","No coincide con formulario."));
                    if(tipo.length()>MAX_TIPO) errs.add(err(rn,"A","tipoCatalogo","Excede longitud máxima."));
                }
                if(elem!=null&&!elem.isBlank()) {
                    if(elem.contains(" ")) errs.add(err(rn,"B","elemento","No puede contener espacios."));
                    if(hasForbidden(elem,false)) errs.add(err(rn,"B","elemento","Caracteres no permitidos (!?,¡¿:;.@#%^&*(){}[]<>/'\"\\ )."));
                    if(elem.length()>MAX_ELEM) errs.add(err(rn,"B","elemento","Excede longitud máxima de "+MAX_ELEM+" caracteres."));
                    if(!elems.add(elem.toLowerCase())) errs.add(err(rn,"B","elemento","Duplicado en archivo."));
                    // CA-31: Validar que el elemento no exista previamente en el sistema
                    if(detailRepo.existsByKeyIgnoreCase(elem)) errs.add(err(rn,"B","elemento","El elemento '"+elem+"' ya existe en el sistema."));
                }
                if(val!=null&&!val.isBlank()) {
                    if(hasForbidden(val,true)) errs.add(err(rn,"C","valor","Caracteres no permitidos."));
                    if(val.length()>MAX_VAL) errs.add(err(rn,"C","valor","Excede longitud máxima."));
                }
                LocalDate di=parseD(fi), df=parseD(ff);
                if(fi!=null&&!fi.isBlank()&&di==null) errs.add(err(rn,"D","fechaInicioVigencia","Fecha inválida (yyyy-MM-dd)."));
                if(ff!=null&&!ff.isBlank()&&df==null) errs.add(err(rn,"E","fechaFinVigencia","Fecha inválida (yyyy-MM-dd)."));
                if(di!=null&&df!=null&&di.isAfter(df)) errs.add(err(rn,"D","fechaInicioVigencia","Inicio posterior a fin."));
                if(df!=null&&!df.isAfter(LocalDate.now())) errs.add(err(rn,"E","fechaFinVigencia","Fin debe ser mayor a fecha actual."));
                if(padre!=null&&!padre.isBlank()) {
                    try { int pid=Integer.parseInt(padre.trim());
                        if("PRIMARIO".equals(firstTipo)) errs.add(err(rn,"F","idPadre","Primario no debe tener idPadre."));
                        else if("SECUNDARIO".equals(firstTipo)&&!detailRepo.existsById(pid)) errs.add(err(rn,"F","idPadre","idPadre "+pid+" no existe."));
                    } catch(NumberFormatException e) { errs.add(err(rn,"F","idPadre","Debe ser número entero.")); }
                    if(padre.length()>MAX_ID) errs.add(err(rn,"F","idPadre","Excede longitud máxima."));
                }
            }
            wb.close();
        } catch(Exception e) { errs.add(LayoutValidationError.builder().row(0).cell("N/A").column("archivo").message("Error: "+e.getMessage()).build()); }
        return build(errs, rows);
    }

    @Override public String getValidationReport(String id) { return reportCache.getOrDefault(id, null); }

    private boolean validateHeaders(Sheet sh, List<LayoutValidationError> e) {
        Row h = sh.getRow(0); if(h==null){e.add(err(1,"A","encabezados","Sin encabezados.")); return false;}
        boolean ok=true;
        for(int i=0;i<6;i++){Cell c=h.getCell(i);String v=c!=null?csv(c):null;String cr=COL[i]+"1";
            if(v==null||!v.trim().equalsIgnoreCase(EXPECTED_HEADERS[i])){e.add(LayoutValidationError.builder().row(1).cell(cr).column(EXPECTED_HEADERS[i]).message("Se esperaba '"+EXPECTED_HEADERS[i]+"' en "+cr).build());ok=false;}}
        return ok;
    }
    private boolean empty(Row r){if(r==null)return true;for(int i=0;i<6;i++){Cell c=r.getCell(i);if(c!=null&&c.getCellType()!=CellType.BLANK){String v=csv(c);if(v!=null&&!v.isBlank())return false;}}return true;}
    private String csv(Row r,int i){return csv(r.getCell(i));}
    private String csv(Cell c){if(c==null)return null;switch(c.getCellType()){case STRING:return c.getStringCellValue().trim();case NUMERIC:if(DateUtil.isCellDateFormatted(c))return c.getLocalDateTimeCellValue().toLocalDate().format(ISO);double n=c.getNumericCellValue();return n==Math.floor(n)?String.valueOf((long)n):String.valueOf(n);case BOOLEAN:return String.valueOf(c.getBooleanCellValue());default:return null;}}
    private String csvDate(Row r,int i){Cell c=r.getCell(i);if(c!=null&&c.getCellType()==CellType.NUMERIC&&DateUtil.isCellDateFormatted(c))return c.getLocalDateTimeCellValue().toLocalDate().format(ISO);return csv(c);}
    private LocalDate parseD(String v){if(v==null||v.isBlank())return null;try{return LocalDate.parse(v.trim(),ISO);}catch(DateTimeParseException e){try{return LocalDate.parse(v.trim(),DateTimeFormatter.ofPattern("dd-MM-yyyy"));}catch(DateTimeParseException e2){return null;}}}
    private boolean hasForbidden(String v,boolean allowSpaces){String p=allowSpaces?"[!?,¡¿:;.@#%\\^&*(){}\\[\\]<>/'\"\\\\\u0060]":"[!?,¡¿:;.@#%\\^&*(){}\\[\\]<>/'\"\\\\\u0060 ]";return Pattern.compile(p).matcher(v).find();}
    private LayoutValidationError err(int rn,String cl,String col,String desc){String cell=cl+rn;return LayoutValidationError.builder().row(rn).cell(cell).column(col).message("Error en celda "+cell+": "+desc).build();}
    private LayoutValidationResponse build(List<LayoutValidationError> errs,int rows){
        String rid=null;boolean ra=false;
        if(errs.size()>ERROR_THRESHOLD){rid=UUID.randomUUID().toString();ra=true;StringBuilder sb=new StringBuilder("=== REPORTE DE ERRORES ===\nTotal: "+errs.size()+"\n\n");
            for(LayoutValidationError e:errs)sb.append("Fila:").append(e.getRow()).append(" Celda:").append(e.getCell()).append(" ").append(e.getMessage()).append("\n");reportCache.put(rid,sb.toString());}
        return LayoutValidationResponse.builder().isValid(errs.isEmpty()).errorCount(errs.size()).errors(errs.size()>ERROR_THRESHOLD?errs.subList(0,ERROR_THRESHOLD):errs).reportAvailable(ra).reportId(rid).rowsProcessed(rows).build();
    }
}

