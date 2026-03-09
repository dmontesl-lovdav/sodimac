package com.sodimac.cfdi.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.sodimac.cfdi.models.CanalItem;
import com.sodimac.cfdi.models.TableroControlTimbradoModel;
import com.sodimac.cfdi.models.TableroId;
import com.sodimac.cfdi.models.TiendaItem;
import com.sodimac.cfdi.repository.fiscal.TableroControlTimbradoRepository;

@Service
public class ReportesServiceImpl implements ReportesService{

	@Autowired
	private TableroControlTimbradoRepository tableroControlTimbradoRepository;
	
	@Autowired
	private CatConfiguracionService catConfiguracionService;
	
	@Autowired
	private DescargaService descargaService;
	
	int nxRow = 0;
	int numeroHoja = 0;
	
	private final static String FILE_DETALLE = "Reporte_Relacion_Venta_Facturacion_";
	private final static SimpleDateFormat MI_FORMATO = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
	
	@Override
	public String getTiendas() {
		List<TiendaItem> list = new ArrayList<>();
		tableroControlTimbradoRepository.findTiendas().forEach(item -> {
			TiendaItem itemList = new TiendaItem();
			itemList.setTienda(item[0].toString());
			list.add(itemList);			
		});
		
		Gson gson= new Gson();
		String resultado = gson.toJson(list);
		return resultado;
	}

	@Override
	public String getCanales() {
		List<CanalItem> list = new ArrayList<>();
		tableroControlTimbradoRepository.findCanales().forEach(item -> {
			CanalItem itemList = new CanalItem();
			itemList.setCanal(item[0].toString());
			list.add(itemList);			
		});
		
		Gson gson= new Gson();
		String resultado = gson.toJson(list);
		return resultado;
	}

	@Override
	public List<TableroControlTimbradoModel> getTableroByParams(String fechaInicial, String fechaFinal, int start, int rowsPerPage, String ticket, String canal, String tienda) {
		List<TableroControlTimbradoModel> list = new ArrayList<TableroControlTimbradoModel>();
		//fechaInicial = "2022-08-02";
		//fechaFinal = "2022-08-30";
		tableroControlTimbradoRepository.getTableroByParams(fechaInicial, fechaFinal, start, rowsPerPage, ticket, canal, tienda).forEach(item -> {
			TableroControlTimbradoModel model = new TableroControlTimbradoModel();
			TableroId id = new TableroId();
			id.setFechaTicket((String) item[0]);
			id.setTienda((String) item[1]);
			model.setFechaTicketTienda(id);
			model.setCanal((String) item[2]);
			
			model.setSubtotalPos((String) item[3]);
			model.setNumeroTicketPos((String) item[4]);
			model.setSubtotalFacGlo((String) item[5]);
			model.setNumeroTicketFacGlo((String) item[6]);
			model.setDifSubtotalPosFacGlo((String) item[7]);
			model.setDifTicketPosFacGlo((String) item[8]);
			model.setSubtotalFacCli((String) item[9]);
			model.setNumeroTicketFacClie((String) item[10]);
			model.setSubtotalFacCliNcGlo((String) item[11]);
			model.setNumeroTicketFacCliNcGlo((String) item[12]);
			model.setDifSubtotalFacCliNcGlo((String) item[13]);
			model.setDifTicketFacCliNcGlo((String) item[14]);
			
			model.setSubtotalPosDev((String) item[15]);
			model.setNumeroTicketPosDev((String) item[16]);
			model.setSubtotalNcGlo((String) item[17]);
			model.setNumeroTicketNcGlo((String) item[18]);
			model.setDifSubtotalPosDevNcGlo((String) item[19]);
			model.setDifTicketPosDevNcGlo((String) item[20]);
			model.setSubtotalNcCli((String) item[21]);
			model.setNumeroTicketNcCli((String) item[22]);
			model.setSubtotalFacGloNcCli((String) item[23]);
			model.setNumeroTicketFacGloNcCli((String) item[24]);
			model.setDifSubtotalFacGloNcCli((String) item[25]);
			model.setDifTicketFacGloNcCli((String) item[26]);
			model.setFechaRegistro((String) item[27]);
			
			list.add(model);
		});
		
		return list;
	}

	@Override
	public boolean getTableroByParamsExcel(String fechaInicial, String fechaFinal, String nombreArchivo, String ticket, String canal, String tienda) {
		String path = catConfiguracionService.findParameterByKey("Mail.PathFile");
		
		Workbook workbook = new XSSFWorkbook();

		Sheet sheet = workbook.createSheet("Tablero");
		sheet.setColumnWidth(0, 5000);
		sheet.setColumnWidth(1, 5000);
		sheet.setColumnWidth(2, 5000);
		sheet.setColumnWidth(3, 5000);
		sheet.setColumnWidth(4, 5000);
		sheet.setColumnWidth(5, 5000);
		sheet.setColumnWidth(6, 5000);
		sheet.setColumnWidth(7, 5000);
		sheet.setColumnWidth(8, 5000);
		sheet.setColumnWidth(9, 5000);
		sheet.setColumnWidth(10, 5000);
		sheet.setColumnWidth(11, 5000);
		sheet.setColumnWidth(12, 5000);
		sheet.setColumnWidth(13, 5000);
		sheet.setColumnWidth(14, 5000);
		sheet.setColumnWidth(15, 5000);
		sheet.setColumnWidth(16, 5000);
		sheet.setColumnWidth(17, 5000);
		sheet.setColumnWidth(18, 5000);
		sheet.setColumnWidth(19, 5000);
		sheet.setColumnWidth(20, 5000);
		sheet.setColumnWidth(21, 5000);
		sheet.setColumnWidth(22, 5000);
		sheet.setColumnWidth(23, 5000);
		sheet.setColumnWidth(24, 5000);
		sheet.setColumnWidth(25, 5000);
		sheet.setColumnWidth(26, 5000);
		sheet.setColumnWidth(27, 5000);
		
		Row header = sheet.createRow(0);

		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		XSSFFont font = ((XSSFWorkbook) workbook).createFont();
		font.setFontName("Arial");
		font.setFontHeightInPoints((short) 16);
		font.setBold(true);
		font.setColor(IndexedColors.WHITE.getIndex());	
		headerStyle.setFont(font);

		String[] columns = {"FECHA_TICKET","TIENDA","CANAL","SUBTOTAL_POS","NUMERO_TICKET_POS","SUBTOTAL_FAC_GLO","NUMERO_TICKET_FAC_GLO","DIF_SUBTOTAL_POS_FAC_GLO","DIF_TICKET_POS_FAC_GLO","SUBTOTAL_FAC_CLI","NUMERO_TICKET_FAC_CLIE","SUBTOTAL_FAC_CLI_NC_GLO","NUMERO_TICKET_FAC_CLI_NC_GLO","DIF_SUBTOTAL_FAC_CLI_NC_GLO","DIF_TICKET_FAC_CLI_NC_GLO","SUBTOTAL_POS_DEV","NUMERO_TICKET_POS_DEV","SUBTOTAL_NC_GLO","NUMERO_TICKET_NC_GLO","DIF_SUBTOTAL_POS_DEV_NC_GLO","DIF_TICKET_POS_DEV_NC_GLO","SUBTOTAL_NC_CLI","NUMERO_TICKET_NC_CLI","SUBTOTAL_FAC_GLO_NC_CLI","NUMERO_TICKET_FAC_GLO_NC_CLI","DIF_SUBTOTAL_FAC_GLO_NC_CLI","DIF_TICKET_FAC_GLO_NC_CLI","FECHA_REGISTRO"};
		
		for (int i = 0; i< columns.length; i++) {
			Cell headerCell = header.createCell(i);
			headerCell.setCellValue(columns[i]);
			headerCell.setCellStyle(headerStyle);
		}

		CellStyle styleCenter = workbook.createCellStyle();
		styleCenter.setWrapText(true);
		styleCenter.setAlignment(HorizontalAlignment.CENTER);

		nxRow = 0;
		
		tableroControlTimbradoRepository.getTableoExcelByParams(fechaInicial, fechaFinal, ticket, canal, tienda).forEach(item -> {

			nxRow = nxRow + 1;
			Row row = sheet.createRow(nxRow);

			Cell cell = row.createCell(0);
			cell.setCellValue(item[0].toString()); 
			cell.setCellStyle(styleCenter);

			cell = row.createCell(1);
			cell.setCellValue(item[1].toString()); 
			cell.setCellStyle(styleCenter);

			cell = row.createCell(2);
			cell.setCellValue(item[2].toString()); 
			cell.setCellStyle(styleCenter);

			cell = row.createCell(3);
			cell.setCellValue(item[3].toString()); 
			cell.setCellStyle(styleCenter);

			cell = row.createCell(4);
			cell.setCellValue(item[4].toString()); 
			cell.setCellStyle(styleCenter);
			
			cell = row.createCell(5);
			cell.setCellValue(item[5].toString()); 
			cell.setCellStyle(styleCenter);

			cell = row.createCell(6);
			cell.setCellValue(item[6].toString()); 
			cell.setCellStyle(styleCenter);

			cell = row.createCell(7);
			cell.setCellValue(item[7].toString()); 
			cell.setCellStyle(styleCenter);

			cell = row.createCell(8);
			cell.setCellValue(item[8].toString()); 
			cell.setCellStyle(styleCenter);

			cell = row.createCell(9);
			cell.setCellValue(item[9].toString());
			cell.setCellStyle(styleCenter);

			cell = row.createCell(10);
			cell.setCellValue(item[10].toString());
			cell.setCellStyle(styleCenter);

			cell = row.createCell(11);
			cell.setCellValue(item[11].toString());
			cell.setCellStyle(styleCenter);

			cell = row.createCell(12);
			cell.setCellValue(item[12].toString()); 
			cell.setCellStyle(styleCenter);

			cell = row.createCell(13);
			cell.setCellValue(item[13].toString()); 
			cell.setCellStyle(styleCenter);

			cell = row.createCell(14);
			cell.setCellValue(item[14].toString()); 
			cell.setCellStyle(styleCenter);
			
			cell = row.createCell(15);
			cell.setCellValue(item[15].toString()); 
			cell.setCellStyle(styleCenter);

			cell = row.createCell(16);
			cell.setCellValue(item[16].toString()); 
			cell.setCellStyle(styleCenter);

			cell = row.createCell(17);
			cell.setCellValue(item[17].toString()); 
			cell.setCellStyle(styleCenter);

			cell = row.createCell(18);
			cell.setCellValue(item[18].toString()); 
			cell.setCellStyle(styleCenter);

			cell = row.createCell(19);
			cell.setCellValue(item[19].toString());
			cell.setCellStyle(styleCenter);

			cell = row.createCell(20);
			cell.setCellValue(item[20].toString());
			cell.setCellStyle(styleCenter);

			cell = row.createCell(21);
			cell.setCellValue(item[21].toString());
			cell.setCellStyle(styleCenter);
			
			cell = row.createCell(22);
			cell.setCellValue(item[22].toString()); 
			cell.setCellStyle(styleCenter);

			cell = row.createCell(23);
			cell.setCellValue(item[23].toString()); 
			cell.setCellStyle(styleCenter);

			cell = row.createCell(24);
			cell.setCellValue(item[24].toString()); 
			cell.setCellStyle(styleCenter);
			
			cell = row.createCell(25);
			cell.setCellValue(item[25].toString()); 
			cell.setCellStyle(styleCenter);

			cell = row.createCell(26);
			cell.setCellValue(item[26].toString()); 
			cell.setCellStyle(styleCenter);

			cell = row.createCell(27);
			cell.setCellValue(item[27].toString()); 
			cell.setCellStyle(styleCenter);
		});		
			
		File fileLocation = new File(path + nombreArchivo);

		FileOutputStream outputStream;

		try {
			outputStream = new FileOutputStream(fileLocation);
			workbook.write(outputStream);
			workbook.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean getDetalleByParamsExcel(String fechaInicial, String fechaFinal, String nombreArchivo, String ticket, String canal, String tienda) {
		String path = catConfiguracionService.findParameterByKey("Mail.PathFile");
		
		Workbook workbook = new SXSSFWorkbook(10000);

		numeroHoja = 0;
		
		Sheet sheet = createNewSheet(workbook);
		
		CellStyle styleCenter = workbook.createCellStyle();
		styleCenter.setWrapText(true);
		styleCenter.setAlignment(HorizontalAlignment.CENTER);

		nxRow = 0; 
		
		List<Object []> list = null;
		
		list = tableroControlTimbradoRepository.getDetalleExcelByParams(fechaInicial, fechaFinal, ticket, canal, tienda);
		writeExcelDetalle(list, sheet,styleCenter, workbook);
			
		File fileLocation = new File(path + nombreArchivo);

		FileOutputStream outputStream;

		try {
			outputStream = new FileOutputStream(fileLocation);
			workbook.write(outputStream);
			workbook.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	@Override
	public boolean ejecutarProcesamientoEnSegundoPlano(String idEjecucion, String dateDesdeParse, String dateHastaParse, String ticket, String canal, String tienda) {
		
		Runnable runnable = () -> { 
			try {
				
				String path = catConfiguracionService.findParameterByKey("Mail.PathFile");
				List<String> fileNames = new ArrayList<>();
				
				LocalDate fi = LocalDate.parse(dateDesdeParse);
				LocalDate ff = LocalDate.parse(dateHastaParse);
				Period period = Period.between(fi, ff);
				
				while (true) {
					Date Ahora = new Date();
					
					String nombreArchivo = FILE_DETALLE + "%s_" + MI_FORMATO.format(Ahora).replaceAll(" ","") + ".xlsx";
					nombreArchivo = nombreArchivo.replaceAll("/","");
					nombreArchivo = nombreArchivo.replaceAll(":","");
					
					
					if (Math.abs(period.getMonths()) < 1) {
						System.out.println("Busqueda por fi: " + fi.toString() + " y ff: " + ff.toString());
						nombreArchivo = String.format(nombreArchivo, fi.toString() + "_a_" + ff.toString());
						getDetalleByParamsExcel(fi.toString(), ff.toString(), nombreArchivo, ticket, canal, tienda);
						fileNames.add(path + nombreArchivo);
						break;
					} else {
						String fechaTemp = fi.plusMonths(1).minusDays(1).toString();
						System.out.println("Busqueda por fi: " + fi.toString() + " y ff: " + fechaTemp);
						nombreArchivo = String.format(nombreArchivo, fi.toString() + " a " + fechaTemp);
						getDetalleByParamsExcel(fi.toString(), fechaTemp, nombreArchivo, ticket, canal, tienda);
						fileNames.add(path + nombreArchivo);
					}
					
					fi = fi.plusMonths(1);
					period = Period.between(fi, ff);
					
				}
				
				String archivos = fileNames.stream().map(Object::toString).collect(Collectors.joining(","));
				
				descargaService.updateStatusProceso(idEjecucion, 1, "Listo para descargar", archivos);
			} catch (Exception e) {
				
				e.printStackTrace();
				try {
					descargaService.updateStatusProceso(idEjecucion, 2, "Problema al procesar", null);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
		};
		Thread t = new Thread(runnable);
		t.start();
		
		return true;
	}
	
	
	
	private Sheet createNewSheet(Workbook workbook) {
		numeroHoja += 1;
		Sheet sheet = workbook.createSheet("Detalle_" + numeroHoja);
		sheet.setColumnWidth(0, 6000);
		sheet.setColumnWidth(1, 5000);
		sheet.setColumnWidth(2, 5000);
		sheet.setColumnWidth(3, 4000);
		sheet.setColumnWidth(4, 5000);
		sheet.setColumnWidth(5, 4000);
		sheet.setColumnWidth(6, 5000);
		sheet.setColumnWidth(7, 5000);
		sheet.setColumnWidth(8, 5000);
		sheet.setColumnWidth(9, 5000);
		sheet.setColumnWidth(10, 5000);
		sheet.setColumnWidth(11, 5000);
		sheet.setColumnWidth(12, 5000);
		sheet.setColumnWidth(13, 10000);
		sheet.setColumnWidth(14, 5000);
		sheet.setColumnWidth(15, 5000);
		sheet.setColumnWidth(16, 5000);
		sheet.setColumnWidth(17, 5000);
		sheet.setColumnWidth(18, 10000);
		sheet.setColumnWidth(19, 5000);
		sheet.setColumnWidth(20, 5000);
		sheet.setColumnWidth(21, 5000);
		sheet.setColumnWidth(22, 5000);
		sheet.setColumnWidth(23, 10000);
		sheet.setColumnWidth(24, 5000);
		sheet.setColumnWidth(25, 5000);
		sheet.setColumnWidth(26, 5000);
		sheet.setColumnWidth(27, 5000);
		sheet.setColumnWidth(28, 5000);
		sheet.setColumnWidth(29, 5000);
		sheet.setColumnWidth(30, 5000);
		sheet.setColumnWidth(31, 5000);
		sheet.setColumnWidth(32, 5000);
		sheet.setColumnWidth(33, 5000);
		sheet.setColumnWidth(34, 5000);
		sheet.setColumnWidth(35, 5000);
		sheet.setColumnWidth(36, 5000);
		sheet.setColumnWidth(37, 5000);
		sheet.setColumnWidth(38, 5000);
		sheet.setColumnWidth(39, 5000);
		sheet.setColumnWidth(40, 5000);
		sheet.setColumnWidth(41, 5000);
		sheet.setColumnWidth(42, 10000);
		sheet.setColumnWidth(43, 5000);
		sheet.setColumnWidth(44, 5000);
		sheet.setColumnWidth(45, 5000);
		sheet.setColumnWidth(46, 5000);
		Row header = sheet.createRow(0);

		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		Font font = ((SXSSFWorkbook) workbook).createFont();
		font.setFontName("Arial");
		font.setFontHeightInPoints((short) 14);
		font.setBold(true);
		font.setColor(IndexedColors.WHITE.getIndex());	
		headerStyle.setFont(font);

		String[] columns = {"TICKET","FECHA_TICKET","TIENDA","CAJA","TRANSACCION","TIPO","TOTAL","SUBTOTAL","REDONDEO","TICKET_ORIGEN","FECHA_ENLACE","NUM_DOC_CANAL","CANAL_LINIO","UUID_GLOBAL","FECHA_TIMBRADO_GLOBAL","TOTAL_GLOBAL","SUBTOTAL_GLOBAL","REPETICION_GLOBAL","UUID_CLIENTE","FECHA_TIMBRADO_CLIENTE","TOTAL_CLIENTE","SUBTOTAL_CLIENTE","REPETICION_CLIENTE","UUID_FAC_NC_GLOBAL","FECHA_TIMBRADO_FAC_NC_GLOBAL","TOTAL_FAC_NC_GLOBAL","SUBTOTAL_FAC_NC_GLOBAL","REPETICION_FAC_NC_GLOBAL","UUID_NC_GLOBAL","FECHA_TIMBRADO_NC_GLOBAL","TOTAL_NC_GLOBAL","SUBTOTAL_NC_GLOBAL","REPETICION_NC_GLOBAL","UUID_NC_CLIENTE","FECHA_TIMBRADO_NC_CLIENTE","TOTAL_NC_CLIENTE","SUBTOTAL_NC_CLIENTE","REPETICION_NC_CLIENTE","FACTURA_CLIENTE_ID","FACTURA_CLIENTE_NC_ID","PAC","FACTURA_INHOUSE_ID","UUID_IN_HOUSE","UUID_LINIO","UUID_RELACIONADO_GLOBAL","UUID_RELACIONADO_CLIENTE","FACTURA_ID_RELACIONADA"};
		
		for (int i = 0; i< columns.length; i++) {
			Cell headerCell = header.createCell(i);
			headerCell.setCellValue(columns[i]);
			headerCell.setCellStyle(headerStyle);
		}
		
		return sheet;
	}

	public void writeExcelDetalle(List<Object []> list, Sheet sheet, CellStyle style, Workbook workbook) {
		System.out.println(list.size());
		for (Object[] item : list) {
			nxRow = nxRow + 1;
			Row row = sheet.createRow(nxRow);

			Cell cell = row.createCell(0);
			cell.setCellValue(item[0].toString()); 
			cell.setCellStyle(style);

			cell = row.createCell(1);
			cell.setCellValue(item[1].toString()); 
			cell.setCellStyle(style);

			cell = row.createCell(2);
			cell.setCellValue(item[2].toString()); 
			cell.setCellStyle(style);

			cell = row.createCell(3);
			cell.setCellValue(item[3].toString()); 
			cell.setCellStyle(style);

			cell = row.createCell(4);
			cell.setCellValue(item[4].toString()); 
			cell.setCellStyle(style);
			
			cell = row.createCell(5);
			cell.setCellValue(item[5].toString()); 
			cell.setCellStyle(style);

			cell = row.createCell(6);
			cell.setCellValue(item[6].toString()); 
			cell.setCellStyle(style);

			cell = row.createCell(7);
			cell.setCellValue(item[7].toString()); 
			cell.setCellStyle(style);

			cell = row.createCell(8);
			cell.setCellValue(item[8].toString()); 
			cell.setCellStyle(style);

			cell = row.createCell(9);
			cell.setCellValue(item[9] == null ? "" : item[9].toString());
			cell.setCellStyle(style);

			cell = row.createCell(10);
			cell.setCellValue(item[10].toString());
			cell.setCellStyle(style);

			cell = row.createCell(11);
			cell.setCellValue(item[11] == null ? "" : item[11].toString());
			cell.setCellStyle(style);

			cell = row.createCell(12);
			cell.setCellValue(item[12] == null ? "" : item[12].toString()); 
			cell.setCellStyle(style);

			cell = row.createCell(13);
			cell.setCellValue(item[13].toString()); 
			cell.setCellStyle(style);

			cell = row.createCell(14);
			cell.setCellValue(item[14].toString()); 
			cell.setCellStyle(style);
			
			cell = row.createCell(15);
			cell.setCellValue(item[15].toString()); 
			cell.setCellStyle(style);

			cell = row.createCell(16);
			cell.setCellValue(item[16].toString()); 
			cell.setCellStyle(style);

			cell = row.createCell(17);
			cell.setCellValue(item[17].toString()); 
			cell.setCellStyle(style);

			cell = row.createCell(18);
			cell.setCellValue(item[18].toString()); 
			cell.setCellStyle(style);

			cell = row.createCell(19);
			cell.setCellValue(item[19].toString());
			cell.setCellStyle(style);

			cell = row.createCell(20);
			cell.setCellValue(item[20].toString());
			cell.setCellStyle(style);

			cell = row.createCell(21);
			cell.setCellValue(item[21].toString());
			cell.setCellStyle(style);
			
			cell = row.createCell(22);
			cell.setCellValue(item[22].toString()); 
			cell.setCellStyle(style);

			cell = row.createCell(23);
			cell.setCellValue(item[23].toString()); 
			cell.setCellStyle(style);

			cell = row.createCell(24);
			cell.setCellValue(item[24].toString()); 
			cell.setCellStyle(style);
			
			cell = row.createCell(25);
			cell.setCellValue(item[25].toString()); 
			cell.setCellStyle(style);

			cell = row.createCell(26);
			cell.setCellValue(item[26].toString()); 
			cell.setCellStyle(style);

			cell = row.createCell(27);
			cell.setCellValue(item[27].toString()); 
			cell.setCellStyle(style);
			
			cell = row.createCell(28);
			cell.setCellValue(item[28].toString()); 
			cell.setCellStyle(style);

			cell = row.createCell(29);
			cell.setCellValue(item[29].toString());
			cell.setCellStyle(style);

			cell = row.createCell(30);
			cell.setCellValue(item[30].toString());
			cell.setCellStyle(style);

			cell = row.createCell(31);
			cell.setCellValue(item[31].toString());
			cell.setCellStyle(style);
			
			cell = row.createCell(32);
			cell.setCellValue(item[32].toString()); 
			cell.setCellStyle(style);

			cell = row.createCell(33);
			cell.setCellValue(item[33].toString()); 
			cell.setCellStyle(style);

			cell = row.createCell(34);
			cell.setCellValue(item[34].toString()); 
			cell.setCellStyle(style);
			
			cell = row.createCell(35);
			cell.setCellValue(item[35].toString()); 
			cell.setCellStyle(style);

			cell = row.createCell(36);
			cell.setCellValue(item[36].toString()); 
			cell.setCellStyle(style);

			cell = row.createCell(37);
			cell.setCellValue(item[37].toString()); 
			cell.setCellStyle(style);
			
			cell = row.createCell(38);
			cell.setCellValue(item[38].toString()); 
			cell.setCellStyle(style);

			cell = row.createCell(39);
			cell.setCellValue(item[39].toString());
			cell.setCellStyle(style);

			cell = row.createCell(40);
			cell.setCellValue(item[40].toString());
			cell.setCellStyle(style);

			cell = row.createCell(41);
			cell.setCellValue(item[41].toString());
			cell.setCellStyle(style);
			
			cell = row.createCell(42);
			cell.setCellValue(item[42].toString()); 
			cell.setCellStyle(style);

			cell = row.createCell(43);
			cell.setCellValue(item[43].toString()); 
			cell.setCellStyle(style);

			cell = row.createCell(44);
			cell.setCellValue(item[44].toString()); 
			cell.setCellStyle(style);
			
			cell = row.createCell(45);
			cell.setCellValue(item[45].toString()); 
			cell.setCellStyle(style);

			cell = row.createCell(46);
			cell.setCellValue(item[46].toString()); 
			cell.setCellStyle(style);

			cell.setCellStyle(style);	
			
			if(nxRow % 10000 == 0) {
				System.out.println(nxRow);
			}
			
			if(nxRow % 1000000 == 0) {
				System.out.println("Se crea una hoja nueva");
				sheet = createNewSheet(workbook);
				nxRow = 0;
			}
		}
		
		System.out.println("Escritura OK");
	}

}
