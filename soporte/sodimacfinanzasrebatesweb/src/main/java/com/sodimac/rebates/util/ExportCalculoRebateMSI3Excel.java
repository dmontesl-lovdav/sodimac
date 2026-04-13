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

import com.sodimac.rebates.dto.CalculoRebateMSIDto;

public class ExportCalculoRebateMSI3Excel {

	private static Logger logger = LoggerFactory.getLogger(ExportCalculoRebateMSI3Excel.class);

	public static ByteArrayInputStream calculoRebateMSIListToExcelFile(List<CalculoRebateMSIDto> list) {

//		try (Workbook workbook = new XSSFWorkbook()) {
		try (SXSSFWorkbook workbook = new SXSSFWorkbook(10000)) {
			Sheet sheet = workbook.createSheet("Reporte Calculo Rebate MSI3");

			// Tamaño de columnas: cm aprox.
			sheet.setColumnWidth(0, 30 * 150);
			sheet.setColumnWidth(1, 30 * 150);
			sheet.setColumnWidth(2, 30 * 400);
			sheet.setColumnWidth(3, 30 * 200);
			sheet.setColumnWidth(4, 30 * 150);
			sheet.setColumnWidth(5, 30 * 256);
			sheet.setColumnWidth(6, 30 * 256);
			sheet.setColumnWidth(7, 30 * 200);
			sheet.setColumnWidth(8, 30 * 150);
			sheet.setColumnWidth(9, 30 * 256);
			sheet.setColumnWidth(10, 30 * 100);
			sheet.setColumnWidth(11, 30 * 100);
			sheet.setColumnWidth(12, 30 * 350);
			sheet.setColumnWidth(13, 30 * 256);
			sheet.setColumnWidth(14, 30 * 256);
			sheet.setColumnWidth(15, 30 * 150);
			sheet.setColumnWidth(16, 30 * 150);
			sheet.setColumnWidth(17, 30 * 150);
			sheet.setColumnWidth(18, 30 * 150);
			sheet.setColumnWidth(19, 30 * 256);
			sheet.setColumnWidth(20, 30 * 256);
			sheet.setColumnWidth(21, 30 * 256);
			sheet.setColumnWidth(22, 30 * 140);
			sheet.setColumnWidth(23, 30 * 140);
			sheet.setColumnWidth(24, 30 * 200);
			sheet.setColumnWidth(25, 30 * 200);
			sheet.setColumnWidth(26, 30 * 200);
			sheet.setColumnWidth(27, 30 * 120);
			sheet.setColumnWidth(28, 30 * 150);
			sheet.setColumnWidth(29, 30 * 256);
			sheet.setColumnWidth(30, 30 * 150);
			sheet.setColumnWidth(31, 30 * 180);
			sheet.setColumnWidth(32, 30 * 180);
			sheet.setColumnWidth(33, 30 * 180);
			sheet.setColumnWidth(34, 30 * 180);
			

			Row row = sheet.createRow(0);
			CellStyle headerCellStyle = workbook.createCellStyle();
			headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

			Font font = ((SXSSFWorkbook) workbook).createFont();
			headerCellStyle.setFont(font);

			String[] columns = { "Origen"
					, "Moneda Venta"	
					, "RFC"
					, "N\u00famero Proveedor"
					, "Familia"
					, "Nombre Familia"
					, "Ticket Venta"
					, "Sucursal Venta"
					, "Fecha Venta"
					, "Banco"
					, "Num Cuota"
					, "SKU"
					, "Descripci\u00f3n Producto"
					, "Subtotal SKU"
					, "Monto Venta Sku"
					, "Tipo Acuerdo"
					, "Moneda Acuerdo"
					, "Valor Descuento"
					, "Tipo Descuento"
					, "Monto Rebate"
					, "Iva Rebate"
					, "Monto Total Rebate"
					, "Programa Pago"
					, "Id Periodo"
					, "Subtotal Cuenta"
					, "IVA Cuenta"
					, "Proveedor Mercancia"
					, "Tipo Documento P\u00f3liza"
					, "Centro Costos"
					, "Centro Beneficios"
					, "Sucursal"
					, "Condiciones de Pago"
					, "Exclusión"
					, "Fecha Exclusión"
					, "Id Exclusión" };

			// Creating header
			for (int i = 0; i < columns.length; i++) {

				Cell cell = row.createCell(i);
				cell.setCellValue(columns[i]);
				cell.setCellStyle(headerCellStyle);
			}

			CreationHelper createHelper = workbook.getCreationHelper();

			if (list != null && list.size() >= 1) {

				// Creating style rows
				CellStyle rowStyle = workbook.createCellStyle();
				rowStyle.setWrapText(true);

				// Creating style datetime
				CellStyle dateStyle = workbook.createCellStyle();
				dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));

				// Creating data rows for each discount
				for (int i = 0; i < list.size(); i++) {

					Row dataRow = sheet.createRow(i + 1);
					// Cell 0
					Cell cell = dataRow.createCell(0);
					if (list.get(i).getOrigen() != null) {
						cell.setCellValue(getValue(list.get(i).getOrigen()));
					}
					
					// Cell 1
					cell = dataRow.createCell(1);
					if (list.get(i).getMonedaVenta() != null) {
						cell.setCellValue(getValue(list.get(i).getMonedaVenta()));
					}
					
					// Cell 2
					cell = dataRow.createCell(2);
					if (list.get(i).getRfc() != null) {
						cell.setCellValue(getValue(list.get(i).getRfc()));
					}
					
					// Cell 3
					cell = dataRow.createCell(3);
					if (list.get(i).getNumeroProveedor() != null) {
						cell.setCellValue(getValue(list.get(i).getNumeroProveedor()));
					}
					
					// Cell 4
					cell = dataRow.createCell(4);
					if (list.get(i).getFamilia() != null) {
						cell.setCellValue(getValue(list.get(i).getFamilia()));
					}
					
					// Cell 5
					cell = dataRow.createCell(5);
					if (list.get(i).getNombreFamilia() != null) {
						cell.setCellValue(getValue(list.get(i).getNombreFamilia()));
					}
					
					// Cell 6
					cell = dataRow.createCell(6);
					if (list.get(i).getTicketVenta() != null) {
						cell.setCellValue(getValue(list.get(i).getTicketVenta()));
					}
					
					// Cell 7
					cell = dataRow.createCell(7);
					if (list.get(i).getSucursalVenta() != null) {
						cell.setCellValue(getValue(list.get(i).getSucursalVenta()));
					}
					
					// Cell 8
					cell = dataRow.createCell(8);
					if (list.get(i).getFechaVenta() != null) {
						cell.setCellValue(list.get(i).getFechaVenta());
						cell.setCellStyle(dateStyle);
					}
					
					// Cell 9
					cell = dataRow.createCell(9);
					if (list.get(i).getBanco() != null) {
						cell.setCellValue(getValue(list.get(i).getBanco()));
					}
					
					// Cell 10
					cell = dataRow.createCell(10);
					if (list.get(i).getNumCuota() != null) {
						cell.setCellValue(list.get(i).getNumCuota());
					}
					
					// Cell 11
					cell = dataRow.createCell(11);
					if (list.get(i).getSku() != null) {
						cell.setCellValue(list.get(i).getSku());
					}
					
					// Cell 12
					cell = dataRow.createCell(12);
					if (list.get(i).getDescripcionProducto() != null) {
						cell.setCellValue(getValue(list.get(i).getDescripcionProducto()));
					}
					
					// Cell 13
					cell = dataRow.createCell(13);
					if (list.get(i).getSubtotalSku() != null) {
						cell.setCellValue(list.get(i).getSubtotalSku());
					}
					
					// Cell 14
					cell = dataRow.createCell(14);
					if (list.get(i).getMontoVentaSku() != null) {
						cell.setCellValue(list.get(i).getMontoVentaSku());
					}
					
					// Cell 15
					cell = dataRow.createCell(15);
					if (list.get(i).getTipoAcuerdo() != null) {
						cell.setCellValue(getValue(list.get(i).getTipoAcuerdo()));
					}
					
					// Cell 16
					cell = dataRow.createCell(16);
					if (list.get(i).getMonedaAcuerdo() != null) {
						cell.setCellValue(getValue(list.get(i).getMonedaAcuerdo()));
					}
					
					// Cell 17
					cell = dataRow.createCell(17);
					if (list.get(i).getValorDescuento() != null) {
						cell.setCellValue(list.get(i).getValorDescuento());
					}
					
					// Cell 18
					cell = dataRow.createCell(18);
					if (list.get(i).getTipoDescuento() != null) {
						cell.setCellValue(getValue(list.get(i).getTipoDescuento()));
					}
					
					// Cell 19
					cell = dataRow.createCell(19);
					if (list.get(i).getMontoRebate() != null) {
						cell.setCellValue(list.get(i).getMontoRebate());
					}
					
					// Cell 20
					cell = dataRow.createCell(20);
					if (list.get(i).getIvaRebate() != null) {
						cell.setCellValue(list.get(i).getIvaRebate());
					}
					
					// Cell 21
					cell = dataRow.createCell(21);
					if (list.get(i).getMontoTotalRebate() != null) {
						cell.setCellValue(list.get(i).getMontoTotalRebate());
					}
					
					// Cell 22
					cell = dataRow.createCell(22);
					if (list.get(i).getProgramaPago() != null) {
						cell.setCellValue(getValue(list.get(i).getProgramaPago()));
					}
					
					// Cell 23
					cell = dataRow.createCell(23);
					if (list.get(i).getIdPeriodo() != null) {
						cell.setCellValue(list.get(i).getIdPeriodo());
					}
					
					// Cell 24
					cell = dataRow.createCell(24);
					if (list.get(i).getSubtotalCuenta() != null) {
						cell.setCellValue(getValue(list.get(i).getSubtotalCuenta()));
					}
					
					// Cell 25
					cell = dataRow.createCell(25);
					if (list.get(i).getIvaCuenta() != null) {
						cell.setCellValue(getValue(list.get(i).getIvaCuenta()));
					}
					
					// Cell 26
					cell = dataRow.createCell(26);
					if (list.get(i).getProveedorMercancia() != null) {
						cell.setCellValue(getValue(list.get(i).getProveedorMercancia()));
					}
					
					// Cell 27
					cell = dataRow.createCell(27);
					if (list.get(i).getTipoDocumentoPoliza() != null) {
						cell.setCellValue(getValue(list.get(i).getTipoDocumentoPoliza()));
					}
					
					// Cell 28
					cell = dataRow.createCell(28);
					if (list.get(i).getCentroCostos() != null) {
						cell.setCellValue(getValue(list.get(i).getCentroCostos()));
					}
					
					// Cell 29
					cell = dataRow.createCell(29);
					if (list.get(i).getCentroBeneficios() != null) {
						cell.setCellValue(getValue(list.get(i).getCentroBeneficios()));
					}
					
					// Cell 30
					cell = dataRow.createCell(30);
					if (list.get(i).getSucursal() != null) {
						cell.setCellValue(list.get(i).getSucursal());
					}
					
					// Cell 31
					cell = dataRow.createCell(31);
					if (list.get(i).getCondicionesPago() != null) {
						cell.setCellValue(getValue(list.get(i).getCondicionesPago()));
					}
					
					// Cell 32
					cell = dataRow.createCell(32);
					if (list.get(i).getExclusion() != null) {
						cell.setCellValue(list.get(i).getExclusion());
					}
					
					// Cell 33
					cell = dataRow.createCell(33);
					if (list.get(i).getFechaExclusion() != null) {
						cell.setCellValue(list.get(i).getFechaExclusion());
						cell.setCellStyle(dateStyle);
					}
					
					// Cell 34
					cell = dataRow.createCell(34);
					if (list.get(i).getIdExclusion() != null) {
						cell.setCellValue(list.get(i).getIdExclusion());
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
			logger.error("exportExcell-Calculo Rebate ", ex);
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