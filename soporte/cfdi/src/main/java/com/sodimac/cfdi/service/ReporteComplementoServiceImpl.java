package com.sodimac.cfdi.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.cfdi.models.ReporteComplementoModel;
import com.sodimac.cfdi.repository.fiscal.ComplementoPagoRepository;

@Service
public class ReporteComplementoServiceImpl implements ReporteComplementoService {

	private NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	
	@Autowired
	private CatConfiguracionService catConfiguracionService;
	
	@Autowired
	private SeguridadService seguridadService;
	
	@Autowired
	private ComplementoPagoRepository complementoPagoRepository;
	
	int nxRow = 0;
	Integer cTemp = 0;
	String color = "White";
	
	@Override
	public List<ReporteComplementoModel> getReporteComplementosByParams(String fechaInicial, String fechaFinal, String rfc, int start, int rowsPerPage, String ticket, String uuid, String monto) {
		
		List<ReporteComplementoModel> list = new ArrayList<ReporteComplementoModel>();
		
		complementoPagoRepository.getReporteComplementosByParams(fechaInicial, fechaFinal, rfc, start, rowsPerPage, ticket, uuid, monto).forEach(item -> {
			ReporteComplementoModel model = new ReporteComplementoModel();
			
			model.setIdPagoComplementoFolioFactura((Integer) item[0]);
			model.setParcialidad((Integer) item[1]);
			model.setRfc(item[2] != null ? seguridadService.desencriptar((String) item[2]) : "");
			model.setNombreCliente(item[3] != null ? seguridadService.desencriptar((String) item[3]) : "");
			model.setUuidComplemento((String) item[4]);
			model.setSerie((String) item[5]);
			model.setFolio((Integer) item[6]);
			model.setFechaTimbradoComplemento(item[7] != null ? simpleDateFormat.format((Date) item[7]) : null);
			model.setDeposito(numberFormat.format((BigDecimal) item[8]));
			model.setFechaDeposito(item[9] != null ? simpleDateFormat.format((Date) item[9]) : null);
			model.setTicket((String) item[10]);
			model.setUuidFactura((String) item[11]);
			model.setImporteFactura(numberFormat.format((BigDecimal) item[12]));
			model.setFechaTimbradoFactura(item[13] != null ? simpleDateFormat.format((Date) item[13]) : null);
			model.setImporteNotaCredito(item[14] != null ? numberFormat.format((BigDecimal) item[14]) : "");
			model.setImporteNetoFactura(numberFormat.format((BigDecimal) item[15]));
			list.add(model);
			
		});
		
		return list;
	}

	@Transactional(isolation = Isolation.READ_UNCOMMITTED)
	@Override
	public boolean getReporteComplementosExcelFechas(String fechaInicial, String fechaFinal, String rfc, String ticket, String uuid, String monto, String nombreArchivo) {
		
		String path = catConfiguracionService.findParameterByKey("Mail.PathFile");
		
		Workbook workbook = new XSSFWorkbook();

		Sheet sheet = workbook.createSheet("Pago Complementos");
		sheet.setColumnWidth(0, 5000);
		sheet.setColumnWidth(1, 10000);
		sheet.setColumnWidth(2, 10000);
		sheet.setColumnWidth(3, 5000);
		sheet.setColumnWidth(4, 5000);
		sheet.setColumnWidth(5, 5000);
		sheet.setColumnWidth(6, 5000);
		sheet.setColumnWidth(7, 5000);
		sheet.setColumnWidth(8, 6000);
		sheet.setColumnWidth(9, 10000);
		sheet.setColumnWidth(10, 5000);
		sheet.setColumnWidth(11, 5000);
		sheet.setColumnWidth(12, 5000);
		sheet.setColumnWidth(13, 5000);
		sheet.setColumnWidth(14, 5000);
		
		
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

		String[] columns = {"RFC","Nombre Cliente","UUID Complemento","Serie","Folio","Fecha Timbrado Complemento","Depósito","Fecha Depósito","Ticket",
				"UUID Factura"," Importe Fact","Fecha timbrado Factura","Importe Nota Crédito","Importe Neto Factura","Parcialidad"};
		
		for (int i = 0; i< columns.length; i++) {
			Cell headerCell = header.createCell(i);
			headerCell.setCellValue(columns[i]);
			headerCell.setCellStyle(headerStyle);
		}
		
		CellStyle white = workbook.createCellStyle();
		white.setWrapText(true);
		white.setAlignment(HorizontalAlignment.CENTER);
		
		
		CellStyle grey = workbook.createCellStyle();
		grey.setWrapText(true);
		grey.setAlignment(HorizontalAlignment.CENTER);
		grey.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		grey.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		

		nxRow = 0;
		cTemp = 0;
		color = ""; // Para empezar en blanco

		complementoPagoRepository.getReporteComplementosExcelByParams(fechaInicial, fechaFinal, rfc, ticket, uuid, monto).forEach(item -> {

			nxRow = nxRow + 1;
			Row row = sheet.createRow(nxRow);
			
			int cCurrent = (Integer) item[0];
			CellStyle style = null;
			if (cTemp == cCurrent) {
				// Mantenemos Color
				if (color.equals("White")) {
					style = white;
				} else {
					style = grey;
				}
			} else {
				// Cambiamos color
				if (color.equals("White")) {
					color = "";
					style = grey;
				} else {
					style = white;
					color = "White";
				}
			}
			
			cTemp = cCurrent;
			
			Cell cell = row.createCell(0);
			cell.setCellValue(item[2] != null ? seguridadService.desencriptar((String) item[2]) : ""); // RFC
			cell.setCellStyle(style);

			cell = row.createCell(1);
			cell.setCellValue(item[3] != null ? seguridadService.desencriptar((String) item[3]) : ""); // Nombre Cliente
			cell.setCellStyle(style);
			
			cell = row.createCell(2);
			cell.setCellValue(item[4].toString()); // Uuid complemento
			cell.setCellStyle(style);

			cell = row.createCell(3);
			cell.setCellValue(item[5].toString()); // Serie
			cell.setCellStyle(style);
			
			cell = row.createCell(4);
			cell.setCellValue(item[6].toString()); // Folio
			cell.setCellStyle(style);

			cell = row.createCell(5);
			cell.setCellValue(item[7] != null ? simpleDateFormat.format((Date) item[7]) : null); // Fecha Timbrado Complemento
			cell.setCellStyle(style);

			cell = row.createCell(6);
			cell.setCellValue(numberFormat.format((BigDecimal) item[8])); // Deposito
			cell.setCellStyle(style);

			cell = row.createCell(7);
			cell.setCellValue(item[9] != null ? simpleDateFormat.format((Date) item[9]) : null); // Fecha Deposito
			cell.setCellStyle(style);

			cell = row.createCell(8);
			cell.setCellValue(item[10].toString()); // Ticket
			cell.setCellStyle(style);

			cell = row.createCell(9);
			cell.setCellValue(item[11].toString()); // Uuid Factura
			cell.setCellStyle(style);

			cell = row.createCell(10);
			cell.setCellValue(numberFormat.format((BigDecimal) item[12])); // Importe Factura
			cell.setCellStyle(style);
			
			cell = row.createCell(11);
			cell.setCellValue(item[13] != null ? simpleDateFormat.format((Date) item[13]) : null); // Fecha Timbrado Factura
			cell.setCellStyle(style);
			
			cell = row.createCell(12);
			cell.setCellValue(item[14] != null ? numberFormat.format((BigDecimal) item[14]) : ""); // Importe Nota Credito
			cell.setCellStyle(style);
			
			cell = row.createCell(13);
			cell.setCellValue(numberFormat.format((BigDecimal) item[15])); // Importe Neto Factura
			cell.setCellStyle(style);
			
			cell = row.createCell(14);
			cell.setCellValue((Integer) item[1]); // Parcialidad
			cell.setCellStyle(style);

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

}
