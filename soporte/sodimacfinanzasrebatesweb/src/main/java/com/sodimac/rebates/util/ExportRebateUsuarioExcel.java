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

import com.sodimac.rebates.model.RebateUsuarioEntity;

public class ExportRebateUsuarioExcel {

	private static Logger logger = LoggerFactory.getLogger(ExportRebateUsuarioExcel.class);

	public static ByteArrayInputStream rebateUsuarioListToExcelFile(List<RebateUsuarioEntity> listRebateUsuario) {

//		try (Workbook workbook = new XSSFWorkbook()) {
		try (SXSSFWorkbook workbook = new SXSSFWorkbook(10000)) {
			Sheet sheet = workbook.createSheet("Reporte Usuario");

			// Tamaño de columnas: cm aprox.
			sheet.setColumnWidth(0, 20 * 256);
			sheet.setColumnWidth(1, 20 * 256);
			sheet.setColumnWidth(2, 20 * 256);
			sheet.setColumnWidth(3, 20 * 256);
			sheet.setColumnWidth(4, 20 * 256);
			sheet.setColumnWidth(5, 20 * 256);
			sheet.setColumnWidth(6, 20 * 256);
			sheet.setColumnWidth(7, 20 * 256);
			sheet.setColumnWidth(8, 20 * 256);
			sheet.setColumnWidth(9, 20 * 256);
			sheet.setColumnWidth(10, 20 * 256);
			sheet.setColumnWidth(11, 20 * 256);
			sheet.setColumnWidth(12, 20 * 256);
			sheet.setColumnWidth(13, 20 * 256);
			sheet.setColumnWidth(14, 20 * 256);
			sheet.setColumnWidth(15, 20 * 256);
			sheet.setColumnWidth(16, 20 * 256);
			sheet.setColumnWidth(17, 20 * 256);
			sheet.setColumnWidth(18, 20 * 256);
			sheet.setColumnWidth(19, 20 * 256);
			sheet.setColumnWidth(20, 20 * 256);
			sheet.setColumnWidth(21, 20 * 256);
			sheet.setColumnWidth(22, 20 * 256);
			sheet.setColumnWidth(23, 20 * 256);
			sheet.setColumnWidth(24, 20 * 256);
			sheet.setColumnWidth(25, 20 * 256);
			sheet.setColumnWidth(26, 20 * 256);
			sheet.setColumnWidth(27, 20 * 256);
			sheet.setColumnWidth(28, 20 * 256);
			sheet.setColumnWidth(29, 20 * 256);
			sheet.setColumnWidth(30, 20 * 256);
			sheet.setColumnWidth(31, 20 * 256);
			sheet.setColumnWidth(32, 20 * 256);
			sheet.setColumnWidth(33, 20 * 256);
			sheet.setColumnWidth(34, 20 * 256);
			sheet.setColumnWidth(35, 20 * 256);
			sheet.setColumnWidth(36, 20 * 256);
			
			

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

			String[] columns = { "Origen"
							      ,"Moneda"
							      ,"RFC"
							      ,"Codigo Proveedor"
							      ,"Proveedor"
							      ,"Gerente Negocio"
							      ,"Nombre Gerente"
							      ,"Numero Jefe Linea"
							      ,"Nombre Jefe Linea"
							      ,"Familia"
							      ,"Nombre Familia"
							      ,"Monto Recibido"
							      ,"Orden Compra"
							      ,"Fecha Emisión"
							      ,"Fecha Recepción"
							      ,"Tipo Acuerdo"
							      ,"Monedas"
							      ,"Valor"
							      ,"Tipo Valor"
							      ,"Monto Rebate"
							      ,"Id Programa Pago"
							      ,"Programa Pago"
							      ,"Tipo Orden Compra"
							      ,"SKU"
							      ,"Descripcion Producto"
							      ,"IVA"
							      ,"IEPS"
							      ,"Monto Iva"
							      ,"Monto Ieps"
							      ,"Monto Descuento"
							      ,"Tipo Cambio"
							      ,"Id Cat Periodo"
							      ,"Id Tipo Rebate"
							      ,"Exclusion"
							      ,"Fecha Exclusion"
							      ,"Id Exclusion"
							      ,"Marca"
							   };

			// Creating header
			for (int i = 0; i < columns.length; i++) {

				Cell cell = row.createCell(i);
				cell.setCellValue(columns[i]);
				cell.setCellStyle(headerCellStyle);
			}

			CreationHelper createHelper = workbook.getCreationHelper();

			if (listRebateUsuario != null && listRebateUsuario.size() >= 1) {

				// Creating style rows
				CellStyle rowStyle = workbook.createCellStyle();
				rowStyle.setWrapText(true);

				// Creating style datetime
				CellStyle dateStyle = workbook.createCellStyle();
				dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));

				// Creating data rows for each discount
				int i = 1;
				for (RebateUsuarioEntity item : listRebateUsuario) {
					
					if(item == null) {
						continue;
					}
					
					Row dataRow = sheet.createRow(i++);
					
					// Cell 0
					Cell cell = dataRow.createCell(0);
					cell.setCellValue( item.getOrigen() );
					
					// Cell 1
					cell = dataRow.createCell(1);
					cell.setCellValue( item.getMoneda() );
					
					// Cell 2
					cell = dataRow.createCell(2);
					cell.setCellValue( item.getRfc() );
					
					// Cell 3
					cell = dataRow.createCell(3);
					cell.setCellValue( item.getCodigoProveedor() );
					
					// Cell 4
					cell = dataRow.createCell(4);
					cell.setCellValue( item.getProveedor() );
					
					// Cell 5
					cell = dataRow.createCell(5);
					cell.setCellValue( item.getGerenteNegocio() );
					
					// Cell 6
					cell = dataRow.createCell(6);
					cell.setCellValue( item.getNombreGerente() );
					
					// Cell 7
					cell = dataRow.createCell(7);
					cell.setCellValue( item.getNumeroJefeLinea() );
					
					// Cell 8
					cell = dataRow.createCell(8);
					cell.setCellValue( item.getNombreJefeLinea() );
					
					// Cell 9
					cell = dataRow.createCell(9);
					cell.setCellValue( item.getFamilia() );
					
					// Cell 10
					cell = dataRow.createCell(10);
					cell.setCellValue(  item.getNombreFamilia() );
					
					// Cell 11
					cell = dataRow.createCell(11);
					cell.setCellValue( item.getMontoRecibido() );
					
					// Cell 12
					cell = dataRow.createCell(12);
					cell.setCellValue( item.getOrdenCompra() );
					
					// Cell 13
					cell = dataRow.createCell(13);
					cell.setCellValue( item.getFechaEmision() );
					
					// Cell 14
					cell = dataRow.createCell(14);
					cell.setCellValue( item.getFechaRecepcion() );
					cell.setCellStyle(dateStyle);
					
					// Cell 15
					cell = dataRow.createCell(15);
					cell.setCellValue( item.getTipoAcuerdo() );
					
					// Cell 16
					cell = dataRow.createCell(16);
					cell.setCellValue( item.getMonedas() );
					
					// Cell 17
					cell = dataRow.createCell(17);
					cell.setCellValue( item.getValor() );
					
					// Cell 18
					cell = dataRow.createCell(18);
					cell.setCellValue( item.getTipoValor() );
					
					// Cell 19
					cell = dataRow.createCell(19);
					cell.setCellValue( item.getMontoRebate() );
					
					// Cell 20
					cell = dataRow.createCell(20);
					cell.setCellValue( item.getIdCatProgramaPago() );

					// Cell 21
					cell = dataRow.createCell(21);
					cell.setCellValue( item.getProgramaPago() );
					
					// Cell 22
					cell = dataRow.createCell(22);
					cell.setCellValue( item.getTipoOrdenCompra() );
					
					// Cell 23
					cell = dataRow.createCell(23);
					cell.setCellValue( item.getSku() );
					
					// Cell 24
					cell = dataRow.createCell(24);
					cell.setCellValue( item.getDescripcionProducto() );
					
					// Cell 25
					cell = dataRow.createCell(25);
					cell.setCellValue( item.getIva() );
					
					// Cell 26
					cell = dataRow.createCell(26);
					cell.setCellValue( item.getIeps() );
					
					// Cell 27
					cell = dataRow.createCell(27);
					cell.setCellValue( item.getMontoIva() );
					
					// Cell 28
					cell = dataRow.createCell(28);
					cell.setCellValue( item.getMontoIeps() );
					
					// Cell 29
					cell = dataRow.createCell(29);
					cell.setCellValue( item.getMontoDescuento() );
					
					// Cell 30
					cell = dataRow.createCell(30);
					cell.setCellValue( item.getTipoCambio() );
					
					// Cell 31
					cell = dataRow.createCell(31);
					cell.setCellValue( item.getPeriodo() );
					
					// Cell 32
					cell = dataRow.createCell(32);
					cell.setCellValue( item.getIdTipoRebate() );
					
					// Cell 33
					cell = dataRow.createCell(33);
					cell.setCellValue( item.getExclusion() );
					
					// Cell 34
					cell = dataRow.createCell(34);
					cell.setCellValue( item.getFechaExclusion() );
					
					// Cell 35
					cell = dataRow.createCell(35);
					cell.setCellValue( item.getIdExclusion() );
					
					// Cell 36
					cell = dataRow.createCell(36);
					cell.setCellValue( item.getMarca() );
					
				}

			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			workbook.write(outputStream);
			return new ByteArrayInputStream(outputStream.toByteArray());

		} catch (Exception ex) {

			ex.printStackTrace();
			logger.error("exportExcell-UsuarioFillRate ", ex);
			return null;
		}
	}
	
}