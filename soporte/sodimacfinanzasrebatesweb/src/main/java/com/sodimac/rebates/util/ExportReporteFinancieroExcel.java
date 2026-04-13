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

import com.sodimac.rebates.dto.ReporteFinancieroDto;

public class ExportReporteFinancieroExcel {

	private static Logger logger = LoggerFactory.getLogger(ExportReporteFinancieroExcel.class);

	public static ByteArrayInputStream reporteFinancieroListToExcelFile(List<ReporteFinancieroDto> listReporteFinanciero) {

//		try (Workbook workbook = new XSSFWorkbook()) {
		try (SXSSFWorkbook workbook = new SXSSFWorkbook(10000)) {
			Sheet sheet = workbook.createSheet("Reporte Financiero");

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
			sheet.setColumnWidth(37, 20 * 256);
			sheet.setColumnWidth(38, 20 * 256);
			sheet.setColumnWidth(39, 20 * 256);
			sheet.setColumnWidth(40, 20 * 256);
			sheet.setColumnWidth(41, 20 * 256);
			sheet.setColumnWidth(42, 20 * 256);
			sheet.setColumnWidth(43, 20 * 256);
			sheet.setColumnWidth(44, 20 * 256);
			sheet.setColumnWidth(45, 20 * 256);

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

			String[] columns = { "Id Registro"
								,"Sociedad"
								,"Fecha Documento"
								,"Fecha Contabilización"
								,"Tipo Documento"
								,"Referencia Fact"
								,"Referencia Ejercicio"
								,"Referencia Posicion"
								,"No. Contrato"
								,"Periodo"
								,"Referencia"
								,"Texto Cabecera"
								,"Moneda"
								,"Fecha Conversion"
								,"Clave Contabilizacion"
								,"Cuenta"
								,"Indicador CME"
								,"Clase Movimiento"
								,"Importe"
								,"Importe Impuestos"
								,"Calcular Impuestos"
								,"Indicador Impuestos"
								,"Centro Beneficios"
								,"Centro Coste"
								,"Orden"
								,"Elemento PEP"
								,"Segmento"
								,"Condicion Pago"
								,"Fecha Base"
								,"Metodo Pago"
								,"Bloqueo Pago"
								,"Articulo"
								,"Cantidad"
								,"Unidad Medida"
								,"Asignacion"
								,"Texto"
								,"Referencia1"
								,"Referencia2"
								,"Referencia3"
								,"Tipo Rebate"
								,"Fecha Valor"
								,"Tipo Cambio"
								,"IdCatPeriodo"
								,"Programa Pago"
								,"Numero Proveedor"
								,"Nombre Proveedor"
							   };

			// Creating header
			for (int i = 0; i < columns.length; i++) {

				Cell cell = row.createCell(i);
				cell.setCellValue(columns[i]);
				cell.setCellStyle(headerCellStyle);
			}

			CreationHelper createHelper = workbook.getCreationHelper();

			if (listReporteFinanciero != null && listReporteFinanciero.size() >= 1) {

				// Creating style rows
				CellStyle rowStyle = workbook.createCellStyle();
				rowStyle.setWrapText(true);

				// Creating style datetime
				CellStyle dateStyle = workbook.createCellStyle();
				dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));

				// Creating data rows for each discount
				int i = 1;
				for (ReporteFinancieroDto item : listReporteFinanciero) {
					
					if(item == null) {
						continue;
					}
					
					Row dataRow = sheet.createRow(i++);
					
					// Cell 0
					Cell cell = dataRow.createCell(0);
					cell.setCellValue( getValue(item.getIdRegistro()));
					
					// Cell 1
					cell = dataRow.createCell(1);
					cell.setCellValue( getValue(item.getSociedad()) );
					
					// Cell 2
					cell = dataRow.createCell(2);
					cell.setCellValue( getValue(item.getFechaDocumento()) );
					
					// Cell 3
					cell = dataRow.createCell(3);
					cell.setCellValue( item.getFechaContabilizacion() );
					cell.setCellStyle(dateStyle);
					
					// Cell 4
					cell = dataRow.createCell(4);
					cell.setCellValue( getValue(item.getTipoDocumento()) );
					
					// Cell 5
					cell = dataRow.createCell(5);
					cell.setCellValue( getValue(item.getReferenciaFact()) );
					
					// Cell 6
					cell = dataRow.createCell(6);
					cell.setCellValue( getValue(item.getReferenciaEjercicio()) );
					
					// Cell 7
					cell = dataRow.createCell(7);
					cell.setCellValue( getValue(item.getReferenciaPosicion()) );
					
					// Cell 8
					cell = dataRow.createCell(8);
					cell.setCellValue( getValue(item.getNoContrato()) );
					
					// Cell 9
					cell = dataRow.createCell(9);
					cell.setCellValue( getValue(item.getPeriodo()) );
					
					// Cell 10
					cell = dataRow.createCell(10);
					cell.setCellValue( getValue(item.getReferencia()) );
					
					// Cell 11
					cell = dataRow.createCell(11);
					cell.setCellValue( getValue( item.getTextoCabecera()) );
					
					// Cell 12
					cell = dataRow.createCell(12);
					cell.setCellValue( getValue(item.getMoneda()) );
					
					// Cell 13
					cell = dataRow.createCell(13);
					cell.setCellValue( item.getFechaConversion() );
					
					// Cell 14
					cell = dataRow.createCell(14);
					cell.setCellValue( getValue(item.getClaveContabilizacion()) );
					
					// Cell 15
					cell = dataRow.createCell(15);
					cell.setCellValue( getValue(item.getCuenta()) );
					
					// Cell 16
					cell = dataRow.createCell(16);
					cell.setCellValue( getValue(item.getIndicadorCME()) );
					
					// Cell 17
					cell = dataRow.createCell(17);
					cell.setCellValue( getValue(item.getClaseMovimiento()) );
					
					// Cell 18
					cell = dataRow.createCell(18);
					cell.setCellValue( item.getImporte() );
					
					// Cell 19
					cell = dataRow.createCell(19);
					cell.setCellValue( getValue(item.getImporteImpuestos()) );
					
					// Cell 20
					cell = dataRow.createCell(20);
					cell.setCellValue( getValue(item.getCalcularImpuestos()) );
					
					// Cell 21
					cell = dataRow.createCell(21);
					cell.setCellValue( getValue(item.getIndicadorImpuestos()) );

					// Cell 22
					cell = dataRow.createCell(22);
					cell.setCellValue( getValue(item.getCentroBeneficios()) );
					
					// Cell 23
					cell = dataRow.createCell(23);
					cell.setCellValue( getValue(item.getCentroCoste()) );
					
					// Cell 24
					cell = dataRow.createCell(24);
					cell.setCellValue( getValue(item.getOrden()) );
					
					// Cell 25
					cell = dataRow.createCell(25);
					cell.setCellValue( getValue(item.getElementoPEP()) );
					
					// Cell 26
					cell = dataRow.createCell(26);
					cell.setCellValue( getValue(item.getSegmento()) );
					
					// Cell 27
					cell = dataRow.createCell(27);
					cell.setCellValue( getValue(item.getCondicionPago()) );
					
					// Cell 28
					cell = dataRow.createCell(28);
					cell.setCellValue( getValue(item.getFechaBase()) );
					
					// Cell 29
					cell = dataRow.createCell(29);
					cell.setCellValue( getValue(item.getMetodoPago()) );
					
					// Cell 30
					cell = dataRow.createCell(30);
					cell.setCellValue( getValue(item.getBloqueoPago()) );
					
					// Cell 31
					cell = dataRow.createCell(31);
					cell.setCellValue( getValue(item.getArticulo()) );
					
					// Cell 32
					cell = dataRow.createCell(32);
					cell.setCellValue( getValue(item.getCantidad()) );
					
					// Cell 33
					cell = dataRow.createCell(33);
					cell.setCellValue( getValue(item.getUnidadMedida()) );
					
					// Cell 34
					cell = dataRow.createCell(34);
					cell.setCellValue( getValue(item.getAsignacion()) );
					
					// Cell 35
					cell = dataRow.createCell(35);
					cell.setCellValue( getValue(item.getTexto()) );
					
					// Cell 36
					cell = dataRow.createCell(36);
					cell.setCellValue( getValue(item.getReferencia1()) );
					
					// Cell 37
					cell = dataRow.createCell(37);
					cell.setCellValue( getValue(item.getReferencia2()) );
					
					// Cell 38
					cell = dataRow.createCell(38);
					cell.setCellValue( item.getReferencia3() );
					
					// Cell 39
					cell = dataRow.createCell(39);
					cell.setCellValue( getValue(item.getRebate()) );
					
					// Cell 40
					cell = dataRow.createCell(40);
					cell.setCellValue( getValue(item.getFechaValor()) );
					
					// Cell 41
					cell = dataRow.createCell(41);
					cell.setCellValue( item.getTipoCambio() );
					
					// Cell 42
					cell = dataRow.createCell(42);
					cell.setCellValue( item.getIdCatPeriodo() );
					
					// Cell 43
					cell = dataRow.createCell(43);
					cell.setCellValue( getValue(item.getProgramaPago()) );
					
					// Cell 44
					cell = dataRow.createCell(44);
					cell.setCellValue( item.getNumeroProveedor() );
					
					// Cell 45
					cell = dataRow.createCell(45);
					cell.setCellValue( getValue(item.getProveedor()) );
					
				}

			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			workbook.write(outputStream);
			return new ByteArrayInputStream(outputStream.toByteArray());

		} catch (Exception ex) {

			ex.printStackTrace();
			logger.error("exportExcell-ReporteFinanciero ", ex);
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