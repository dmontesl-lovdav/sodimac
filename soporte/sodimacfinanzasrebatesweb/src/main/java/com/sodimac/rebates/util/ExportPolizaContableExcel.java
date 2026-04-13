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

import com.sodimac.rebates.dto.PolizaContableReporteDto;

public class ExportPolizaContableExcel {

	private static Logger logger = LoggerFactory.getLogger(ExportPolizaContableExcel.class);

	public static ByteArrayInputStream polizaContableListToExcelFile(List<PolizaContableReporteDto> listPolizaContable) {

//		try (Workbook workbook = new XSSFWorkbook()) {
		try (SXSSFWorkbook workbook = new SXSSFWorkbook(10000)) {
			Sheet sheet = workbook.createSheet("Detalle Poliza Contable");

			// Tamaño de columnas: cm aprox.
			sheet.setColumnWidth(0, 45 * 256);
			sheet.setColumnWidth(1, 15 * 256);
			sheet.setColumnWidth(2, 20 * 256);
			sheet.setColumnWidth(3, 20 * 256);
			sheet.setColumnWidth(4, 30 * 256);
			sheet.setColumnWidth(5, 15 * 256);
			sheet.setColumnWidth(6, 15 * 256);
			sheet.setColumnWidth(7, 17 * 256);
			sheet.setColumnWidth(8, 20 * 256);
			sheet.setColumnWidth(9, 20 * 256);
			sheet.setColumnWidth(10, 20 * 256);
			sheet.setColumnWidth(11, 20 * 256);
			sheet.setColumnWidth(12, 20 * 256);
			sheet.setColumnWidth(13, 15 * 256);
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

			// keep 100 rows in memory, exceeding rows will be
			// flushed to diskSheet sh = workbook.createSheet();

			Row row = sheet.createRow(0);
			CellStyle headerCellStyle = workbook.createCellStyle();
			// headerCellStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
			// headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

			Font font = ((SXSSFWorkbook) workbook).createFont();
//			XSSFFont font = ((XSSFWorkbook) workbook).createFont();
			// font.setFontName("Arial");
			// font.setFontHeightInPoints((short) 12);
			font.setBold(true);
			// font.setColor(IndexedColors.WHITE.getIndex());
			headerCellStyle.setFont(font);

			String[] columns = { "Id", "Empresa", "Fecha Documento", "Referencia Documento", "Número Documento",
					"Moneda", "Tipo Cambio", "Debito/Credito", "Cuenta Contable", "Código Proveedor", "Monto Calculado",
					"Importe", "Monto Contabilizado", "Sucursal", "Condicion Pago", "Fecha Vencimiento", "Bloqueo Pago",
					"Sistema Origen", "Fecha Envio", "Fecha Contable", "Clase Documento", "Número Referencia",
					"Centro Costo", "Centro Beneficio", "Número UUID", "Flag Enviado", "Fecha Recepción",
					"Tipo Documento", "Origen Etl", "Id Periodo", "Fecha Inicio Periodo", "Fecha Final Periodo",
					"Id TipoRebate", "Tipo Rebate", "Timbrado" };

			// Creating header
			for (int i = 0; i < columns.length; i++) {

				Cell cell = row.createCell(i);
				cell.setCellValue(columns[i]);
				cell.setCellStyle(headerCellStyle);
			}

			CreationHelper createHelper = workbook.getCreationHelper();

			if (listPolizaContable != null && listPolizaContable.size() >= 1) {

				// Creating style rows
				CellStyle rowStyle = workbook.createCellStyle();
				rowStyle.setWrapText(true);

				// Creating style datetime
				CellStyle dateStyle = workbook.createCellStyle();
				dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));

				CellStyle currencyStyle = workbook.createCellStyle();

//				HSSFDataFormat format = createHelper.createDataFormat(};/
//				currencyStyle.setDataFormat((short)8); ////8 = "($#,##0.00_);[Red]($#,##0.00)"
//				currencyStyle.setDataFormat(createHelper.createDataFormat().getFormat(HSSFDataFormat.getBuiltinFormat((short) 8)));
				currencyStyle.setDataFormat(createHelper.createDataFormat().getFormat("$#,##0.00"));

				// Creating data rows for each discount
				for (int i = 0; i < listPolizaContable.size(); i++) {

					Row dataRow = sheet.createRow(i + 1);
					// Cell 0
					Cell cell = dataRow.createCell(0);
					cell.setCellValue(listPolizaContable.get(i).getId());

					// Cell 1
					cell = dataRow.createCell(1);
					cell.setCellValue(listPolizaContable.get(i).getEmpresa());

					// Cell 2
					cell = dataRow.createCell(2);
					cell.setCellValue(listPolizaContable.get(i).getFechaDocumento());
					cell.setCellStyle(dateStyle);

					// Cell 3
					cell = dataRow.createCell(3);
					cell.setCellValue(listPolizaContable.get(i).getReferenciaDocumento());

					// Cell 4
					cell = dataRow.createCell(4);
					cell.setCellValue(listPolizaContable.get(i).getNumeroDocumento());

					// Cell 5
					cell = dataRow.createCell(5);
					cell.setCellValue(listPolizaContable.get(i).getMoneda());

					// Cell 6
					cell = dataRow.createCell(6);
					cell.setCellValue(listPolizaContable.get(i).getTipoCambio());

					// Cell 7
					cell = dataRow.createCell(7);
					cell.setCellValue(listPolizaContable.get(i).getDebitoCredito());

					// Cell 8
					cell = dataRow.createCell(8);
					cell.setCellValue(listPolizaContable.get(i).getCuentaContable());

					// Cell 9
					cell = dataRow.createCell(9);
					cell.setCellValue(listPolizaContable.get(i).getCodigoProveedor());

					// Cell 10
					cell = dataRow.createCell(10);
					cell.setCellValue(listPolizaContable.get(i).getMontoCalculado() != null ? listPolizaContable.get(i).getMontoCalculado() : 0);
					cell.setCellStyle(currencyStyle);

					// Cell 11
					cell = dataRow.createCell(11);
					cell.setCellValue(listPolizaContable.get(i).getImporte());
					cell.setCellStyle(currencyStyle);
					
					// Cell 12
					cell = dataRow.createCell(12);
					cell.setCellValue(listPolizaContable.get(i).getMontoContabilizado());
					cell.setCellStyle(currencyStyle);

					// Cell 13
					cell = dataRow.createCell(13);
					cell.setCellValue(listPolizaContable.get(i).getSucursal());

					// Cell 14
					cell = dataRow.createCell(14);
					cell.setCellValue(listPolizaContable.get(i).getCondicionPago());
					
					// Cell 15
					cell = dataRow.createCell(15);
					cell.setCellValue(listPolizaContable.get(i).getFechaVencimiento());
					cell.setCellStyle(dateStyle);

					// Cell 16
					cell = dataRow.createCell(16);
					cell.setCellValue(listPolizaContable.get(i).getBloqueoPago());

					// Cell 17
					cell = dataRow.createCell(17);
					cell.setCellValue(listPolizaContable.get(i).getSistemaOrigen());

					// Cell 18
					cell = dataRow.createCell(18);
					cell.setCellValue(listPolizaContable.get(i).getFechaEnvio());
					cell.setCellStyle(dateStyle);

					// Cell 19
					cell = dataRow.createCell(19);
					cell.setCellValue(listPolizaContable.get(i).getFechaContable());
					cell.setCellStyle(dateStyle);

					// Cell 20
					cell = dataRow.createCell(20);
					cell.setCellValue(listPolizaContable.get(i).getClaseDocumento());

					// Cell 21
					cell = dataRow.createCell(21);
					cell.setCellValue(listPolizaContable.get(i).getNumeroReferencia());

					// Cell 22
					cell = dataRow.createCell(22);
					cell.setCellValue(listPolizaContable.get(i).getCentroCosto());

					// Cell 23
					cell = dataRow.createCell(23);
					cell.setCellValue(listPolizaContable.get(i).getCentroBeneficio());

					// Cell 24
					cell = dataRow.createCell(24);
					cell.setCellValue(listPolizaContable.get(i).getNumeroUuid());

					// Cell 25
					cell = dataRow.createCell(25);
					cell.setCellValue(listPolizaContable.get(i).getFlagEnviado());
					
					// Cell 26
					cell = dataRow.createCell(26);
					cell.setCellValue(listPolizaContable.get(i).getFechaRecepcion());
					cell.setCellStyle(dateStyle);

					// Cell 27
					cell = dataRow.createCell(27);
					cell.setCellValue(listPolizaContable.get(i).getTipoDocumento());

					// Cell 28
					cell = dataRow.createCell(28);
					cell.setCellValue(listPolizaContable.get(i).getOrigenEtl());

					// Cell 29
					cell = dataRow.createCell(29);
					cell.setCellValue(listPolizaContable.get(i).getIdPeriodo());

					// Cell 30
					cell = dataRow.createCell(30);
					cell.setCellValue(listPolizaContable.get(i).getFechaInicioPeriodo());
					cell.setCellStyle(dateStyle);

					// Cell 31
					cell = dataRow.createCell(31);
					cell.setCellValue(listPolizaContable.get(i).getFechaFinPeriodo());
					cell.setCellStyle(dateStyle);

					// Cell 32
					cell = dataRow.createCell(32);
					if (listPolizaContable.get(i).getIdTipoRebate() != null) {
						cell.setCellValue(listPolizaContable.get(i).getIdTipoRebate());
					}

					// Cell 33
					cell = dataRow.createCell(33);
					if (listPolizaContable.get(i).getTipoRebate() != null) {
						cell.setCellValue(listPolizaContable.get(i).getTipoRebate());
					}

					// Cell 34
					cell = dataRow.createCell(34);
					cell.setCellValue(listPolizaContable.get(i).getTimbrado());
				}

			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			workbook.write(outputStream);
			return new ByteArrayInputStream(outputStream.toByteArray());

		} catch (Exception ex) {

			ex.printStackTrace();
			logger.error("exportExcell-PolizaContable ", ex);
			return null;
		}
	}

}