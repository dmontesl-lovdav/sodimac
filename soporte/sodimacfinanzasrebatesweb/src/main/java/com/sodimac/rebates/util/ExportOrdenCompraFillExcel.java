package com.sodimac.rebates.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sodimac.rebates.model.RebateOrdenCompraFillEntity;

public class ExportOrdenCompraFillExcel {

	private static Logger logger = LoggerFactory.getLogger(ExportOrdenCompraFillExcel.class);

	public static ByteArrayInputStream ordenCompraFillListToExcelFile(List<RebateOrdenCompraFillEntity> listOrdenCompraFill) {

//		try (Workbook workbook = new XSSFWorkbook()) {
		try (SXSSFWorkbook workbook = new SXSSFWorkbook(10000)) {
			Sheet sheet = workbook.createSheet("Reporte orden compra fill");

			// Tamaño de columnas: cm aprox.
			sheet.setColumnWidth(0, 30 * 256);
			sheet.setColumnWidth(1, 30 * 256);
			sheet.setColumnWidth(2, 30 * 256);
			sheet.setColumnWidth(3, 30 * 256);
			sheet.setColumnWidth(4, 30 * 256);
			sheet.setColumnWidth(5, 30 * 256);
			sheet.setColumnWidth(6, 30 * 256);
			sheet.setColumnWidth(7, 30 * 256);
			sheet.setColumnWidth(8, 30 * 256);
			sheet.setColumnWidth(9, 30 * 256);
			sheet.setColumnWidth(10, 30 * 256);
			sheet.setColumnWidth(11, 30 * 256);
			sheet.setColumnWidth(12, 30 * 256);
			sheet.setColumnWidth(13, 30 * 256);
			sheet.setColumnWidth(14, 30 * 256);
			sheet.setColumnWidth(15, 30 * 256);
			sheet.setColumnWidth(16, 30 * 256);
			sheet.setColumnWidth(17, 30 * 256);
			sheet.setColumnWidth(18, 30 * 256);
			sheet.setColumnWidth(19, 30 * 256);
			

			// keep 100 rows in memory, exceeding rows will be
			// flushed to diskSheet sh = workbook.createSheet();

			Row row = sheet.createRow(0);
			CellStyle headerCellStyle = workbook.createCellStyle();
			//headerCellStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
			//headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

			Font font = ((SXSSFWorkbook) workbook).createFont();
//			XSSFFont font = ((XSSFWorkbook) workbook).createFont();
			//font.setFontName("Arial");
			//font.setFontHeightInPoints((short) 12);
			font.setBold(true);
			//font.setColor(IndexedColors.WHITE.getIndex());
			headerCellStyle.setFont(font);

			String[] columns = { 
							   "SKU"
							   , "Usuario Recepción"
							   , "Número Tienda"
							   , "Número de Proveedor"
							   , "Número Orden de Compra"
							   , "Moneda Orden de Compra"
							   , "Estado Orden de Compra"
							   , "Tipo Orden de Compra"
							   , "Fecha de Emisión"
							   , "Fecha de Recibo Esperada"
							   , "Fecha de Cancelación"
							   , "Cantidad Ordenada"
							   , "Costo Total Ordenado"
							   , "Fecha de Recepción"
							   , "Cantidad Recibida"
							   , "Total Recibido"
							   , "Numero Tienda CD"
							   , "Fecha Recepcion Inicial"
							   , "Fecha Ultima Recepción"
							   , "Fecha Registro"
							   };

			// Creating header
			for (int i = 0; i < columns.length; i++) {

				Cell cell = row.createCell(i);
				cell.setCellValue(columns[i]);
				cell.setCellStyle(headerCellStyle);
			}

			CreationHelper createHelper = workbook.getCreationHelper();

			if (listOrdenCompraFill != null && listOrdenCompraFill.size() >= 1) {

				// Creating style rows
				CellStyle rowStyle = workbook.createCellStyle();
				rowStyle.setWrapText(true);

				// Creating style datetime
				CellStyle dateStyle = workbook.createCellStyle();
				dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));

				// Creating data rows for each discount
				for (int i = 0; i < listOrdenCompraFill.size(); i++) {

					Row dataRow = sheet.createRow(i + 1);
					// Cell 0
					Cell cell = dataRow.createCell(0);
					cell.setCellValue(getValue(listOrdenCompraFill.get(i).getId().getSku()));
					
					// Cell 1
					cell = dataRow.createCell(1);
					cell.setCellValue( getValue(listOrdenCompraFill.get(i).getUsuarioRecepcion() ));
					
					// Cell 2
					cell = dataRow.createCell(2);
					if (listOrdenCompraFill.get(i).getId().getNumeroTienda() != null) {
						cell.setCellValue(listOrdenCompraFill.get(i).getId().getNumeroTienda());
					}
					
					// Cell 3
					cell = dataRow.createCell(3);
					if (listOrdenCompraFill.get(i).getId().getNumeroProveedor() != null) {
						cell.setCellValue(listOrdenCompraFill.get(i).getId().getNumeroProveedor());
					}
					
					// Cell 4
					cell = dataRow.createCell(4);
					if (listOrdenCompraFill.get(i).getId().getNumeroOrdenCompra() != null) {
						cell.setCellValue( listOrdenCompraFill.get(i).getId().getNumeroOrdenCompra());
					}
					
					// Cell 5
					cell = dataRow.createCell(5);
					cell.setCellValue( getValue(listOrdenCompraFill.get(i).getMonedaOrdenCompra() ));
					
					// Cell 6
					cell = dataRow.createCell(6);
					cell.setCellValue( getValue(listOrdenCompraFill.get(i).getEstadoOrdenCompra()) );
					
					// Cell 7
					cell = dataRow.createCell(7);
					cell.setCellValue( getValue(listOrdenCompraFill.get(i).getTipoOrdenCompra()) );
					
					// Cell 8
					cell = dataRow.createCell(8);
					if (listOrdenCompraFill.get(i).getFechaEmision() != null) {
						cell.setCellValue(listOrdenCompraFill.get(i).getFechaEmision());
						cell.setCellStyle(dateStyle);
					}
					
					// Cell 9
					cell = dataRow.createCell(9);
					if (listOrdenCompraFill.get(i).getFechaReciboEsperada() != null) {
						cell.setCellValue(listOrdenCompraFill.get(i).getFechaReciboEsperada());
						cell.setCellStyle(dateStyle);
					}
					
					// Cell 10
					cell = dataRow.createCell(10);
					if (listOrdenCompraFill.get(i).getFechaCancelacion() != null) {
						cell.setCellValue(listOrdenCompraFill.get(i).getFechaCancelacion());
						cell.setCellStyle(dateStyle);
					}
					
					// Cell 11
					cell = dataRow.createCell(11);
					if (listOrdenCompraFill.get(i).getCantidadOrdenada() != null) {
						cell.setCellValue(listOrdenCompraFill.get(i).getCantidadOrdenada());
					}
					
					// Cell 12
					cell = dataRow.createCell(12);
					if (listOrdenCompraFill.get(i).getCostoTotalOrdenado() != null) {
						cell.setCellValue( listOrdenCompraFill.get(i).getCostoTotalOrdenado());
					}
										
					// Cell 13
					cell = dataRow.createCell(13);
					if (listOrdenCompraFill.get(i).getFechaRecepcion() != null) {
						cell.setCellValue(listOrdenCompraFill.get(i).getFechaRecepcion());
						cell.setCellStyle(dateStyle);
					}
					
					// Cell 14
					cell = dataRow.createCell(14);
					if (listOrdenCompraFill.get(i).getCantidadRecibida() != null) {
						cell.setCellValue(listOrdenCompraFill.get(i).getCantidadRecibida());
					}
					
					// Cell 15
					cell = dataRow.createCell(15);
					if (listOrdenCompraFill.get(i).getTotalRecibido() != null) {
						cell.setCellValue(listOrdenCompraFill.get(i).getTotalRecibido());
					}
					
					// Cell 16
					cell = dataRow.createCell(16);
					cell.setCellValue(listOrdenCompraFill.get(i).getNumeroTiendaCd());
					
					// Cell 17
					cell = dataRow.createCell(17);
					if (listOrdenCompraFill.get(i).getFechaRecepcionInicial() != null) {
						cell.setCellValue(listOrdenCompraFill.get(i).getFechaRecepcionInicial());
						cell.setCellStyle(dateStyle);
					}
					
					// Cell 18
					cell = dataRow.createCell(18);
					if (listOrdenCompraFill.get(i).getFechaUltimaRecepcion() != null) {
						cell.setCellValue(listOrdenCompraFill.get(i).getFechaUltimaRecepcion());
						cell.setCellStyle(dateStyle);
					}
					
					// Cell 19
					cell = dataRow.createCell(19);
					if (listOrdenCompraFill.get(i).getFechaRegistro() != null) {
						cell.setCellValue(listOrdenCompraFill.get(i).getFechaRegistro());
						cell.setCellStyle(dateStyle);
					}
				}

				// Making size of column auto resize to fit with data
//				for (int i = 0; i < columns.length; i++) {
//
//					sheet.autoSizeColumn(i);
//
//				}

			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			workbook.write(outputStream);
			return new ByteArrayInputStream(outputStream.toByteArray());

		} catch (Exception ex) {

			ex.printStackTrace();
			logger.error("exportExcell-OrdenCompraFill ", ex);
			return null;
		}
	}
	
	private static String getValue(String value) {
		if (value == null) {
			return "";
		} else {
			return value;
		}
	}
}