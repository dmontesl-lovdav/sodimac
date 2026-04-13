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

import com.sodimac.rebates.dto.ExclusionDto;
import com.sodimac.rebates.dto.ExclusionViewDetDto;

public class ExportExclusionExcel {

	private static Logger logger = LoggerFactory.getLogger(ExportExclusionExcel.class);

	public static ByteArrayInputStream createExcel(List<ExclusionDto> list, List<ExclusionViewDetDto> listDet) {

//		try (Workbook workbook = new XSSFWorkbook()) {
		try (SXSSFWorkbook workbook = new SXSSFWorkbook(10000)) {
			
			hojaExclusion(workbook, list);
			hojaExclusionDet(workbook, listDet);
			
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			workbook.write(outputStream);
			return new ByteArrayInputStream(outputStream.toByteArray());

		} catch (Exception ex) {

			ex.printStackTrace();
			logger.error("exportExcell-Exclusiones", ex);
			return null;
		}
	}
	
	private static void hojaExclusion(SXSSFWorkbook workbook, List<ExclusionDto> list) {
		Sheet sheet = workbook.createSheet("Exclusiones");

		// Tamaño de columnas: cm aprox.
		sheet.setColumnWidth(0, 30 * 150);
		sheet.setColumnWidth(1, 30 * 150);
		sheet.setColumnWidth(2, 30 * 400);
		sheet.setColumnWidth(3, 30 * 200);
		sheet.setColumnWidth(4, 30 * 150);
		sheet.setColumnWidth(5, 30 * 256);
		sheet.setColumnWidth(6, 30 * 200);
		sheet.setColumnWidth(7, 30 * 150);
		sheet.setColumnWidth(8, 30 * 256);
		sheet.setColumnWidth(9, 30 * 100);
		sheet.setColumnWidth(10, 30 * 100);
		sheet.setColumnWidth(11, 30 * 350);

		Row row = sheet.createRow(0);
		CellStyle headerCellStyle = workbook.createCellStyle();
		headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

		Font font = ((SXSSFWorkbook) workbook).createFont();
		headerCellStyle.setFont(font);

		String[] columns = { "Id"
				, "Descripci\u00f3n"	
				, "Tipo Rebate"
				, "Tipo Exclusi\u00f3n"
				, "Estatus Exclusi\u00f3n"
				, "Periodo"
				, "Folio"
				, "Usuario Solicitud"
				, "Usuario Autorizaci\u00f3n"
				, "Fecha Solicitud"
				, "Fecha Autorizaci\u00f3n"
				, "Contabilizado"
				};

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
			dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));

			// Creating data rows for each discount
			for (int i = 0; i < list.size(); i++) {

				Row dataRow = sheet.createRow(i + 1);
				// Cell 0
				Cell cell = dataRow.createCell(0);
				if (list.get(i).getIdExclusion() != null) {
					cell.setCellValue(list.get(i).getIdExclusion());
				}
				
				// Cell 1
				cell = dataRow.createCell(1);
				if (list.get(i).getComentario() != null) {
					cell.setCellValue(getValue(list.get(i).getComentario()));
				}
				
				// Cell 2
				cell = dataRow.createCell(2);
				if (list.get(i).getCatTipoRebate() != null) {
					cell.setCellValue(getValue(list.get(i).getCatTipoRebate().getTipoRebate()));
				}
				
				// Cell 3
				cell = dataRow.createCell(3);
				if (list.get(i).getCatTipoExclusion() != null) {
					cell.setCellValue(getValue(list.get(i).getCatTipoExclusion().getDescripcion()));
				}
				
				// Cell 4
				cell = dataRow.createCell(4);
				if (list.get(i).getCatEstatusExclusion() != null) {
					cell.setCellValue(getValue(list.get(i).getCatEstatusExclusion().getDescripcion()));
				}
				
				// Cell 5
				cell = dataRow.createCell(5);
				if (list.get(i).getPeriodo() != null) {
					cell.setCellValue(getValue(list.get(i).getPeriodo().getDetallePeriodo()));
				}
				
				// Cell 6
				cell = dataRow.createCell(6);
				if (list.get(i).getFolio() != null) {
					cell.setCellValue(getValue(list.get(i).getFolio()));
				}
				
				// Cell 7
				cell = dataRow.createCell(7);
				if (list.get(i).getUsuarioSolicitud() != null) {
					cell.setCellValue(list.get(i).getUsuarioSolicitud().getNombreCompleto());
				}
				
				// Cell 8
				cell = dataRow.createCell(8);
				if (list.get(i).getUsuarioAutorizacion() != null) {
					cell.setCellValue(getValue(list.get(i).getUsuarioAutorizacion().getNombreCompleto()));
				}
				
				// Cell 9
				cell = dataRow.createCell(9);
				if (list.get(i).getFechaHoraSolicitud() != null) {
					cell.setCellValue(list.get(i).getFechaHoraSolicitud());
					cell.setCellStyle(dateStyle);
				}
				
				// Cell 10
				cell = dataRow.createCell(10);
				if (list.get(i).getFechaHoraAutorizacion() != null) {
					cell.setCellValue(list.get(i).getFechaHoraAutorizacion());
					cell.setCellStyle(dateStyle);
				}
				
				// Cell 11
				cell = dataRow.createCell(11);
				if (list.get(i).getStrContabilizado() != null) {
					cell.setCellValue(getValue(list.get(i).getStrContabilizado()));
				}
			}
		}
	}
	
	private static void hojaExclusionDet(SXSSFWorkbook workbook, List<ExclusionViewDetDto> listDet) {
		Sheet sheet = workbook.createSheet("Exclusiones Detalle");

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

		Row row = sheet.createRow(0);
		CellStyle headerCellStyle = workbook.createCellStyle();
		headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

		Font font = ((SXSSFWorkbook) workbook).createFont();
		headerCellStyle.setFont(font);

		String[] columns = { "IdExclusion"
				, "Descripci\u00f3n"	
				, "Tipo Rebate"
				, "Tipo Exclusi\u00f3n"
				, "Folio"
				, "Motivo"
				, "NumProveedor"
				, "NomProveedor"
				, "OrdenCompra"
				, "Familia"
				, "Sku"
				, "Sku Descripci\u00f3n"
				, "Periodo Vigente"
				, "Acuerdo Comercial"
				};

		// Creating header
		for (int i = 0; i < columns.length; i++) {

			Cell cell = row.createCell(i);
			cell.setCellValue(columns[i]);
			cell.setCellStyle(headerCellStyle);
		}

		if (listDet != null && listDet.size() >= 1) {

			// Creating style rows
			CellStyle rowStyle = workbook.createCellStyle();
			rowStyle.setWrapText(true);
			
			// Creating data rows for each discount
			for (int i = 0; i < listDet.size(); i++) {

				Row dataRow = sheet.createRow(i + 1);
				// Cell 0
				Cell cell = dataRow.createCell(0);
				if (listDet.get(i).getIdExclusion() != null) {
					cell.setCellValue(listDet.get(i).getIdExclusion());
				}
				
				// Cell 1
				cell = dataRow.createCell(1);
				if (listDet.get(i).getComentario() != null) {
					cell.setCellValue(getValue(listDet.get(i).getComentario()));
				}
				
				// Cell 2
				cell = dataRow.createCell(2);
				if (listDet.get(i).getDescripcionRebate() != null) {
					cell.setCellValue(getValue(listDet.get(i).getDescripcionRebate()));
				}
				
				// Cell 3
				cell = dataRow.createCell(3);
				if (listDet.get(i).getDescripcionExclusion() != null) {
					cell.setCellValue(getValue(listDet.get(i).getDescripcionExclusion()));
				}
				
				// Cell 4
				cell = dataRow.createCell(4);
				if (listDet.get(i).getFolio() != null) {
					cell.setCellValue(getValue(listDet.get(i).getFolio()));
				}
				
				// Cell 5
				cell = dataRow.createCell(5);
				if (listDet.get(i).getMotivo() != null) {
					cell.setCellValue(getValue(listDet.get(i).getMotivo()));
				}
				
				// Cell 6
				cell = dataRow.createCell(6);
				if (listDet.get(i).getNumProveedor() != null) {
					cell.setCellValue(getValue(listDet.get(i).getNumProveedor()));
				}
				
				// Cell 7
				cell = dataRow.createCell(7);
				if (listDet.get(i).getNomProveedor() != null) {
					cell.setCellValue(getValue(listDet.get(i).getNomProveedor()));
				}
				
				// Cell 8
				cell = dataRow.createCell(8);
				if (listDet.get(i).getOrdenCompra() != null) {
					cell.setCellValue(listDet.get(i).getOrdenCompra());
				}
				
				// Cell 9
				cell = dataRow.createCell(9);
				if (listDet.get(i).getClacom() != null) {
					cell.setCellValue(getValue(listDet.get(i).getClacom()));
				}
				
				// Cell 10
				cell = dataRow.createCell(10);
				if (listDet.get(i).getSku() != null) {
					cell.setCellValue(listDet.get(i).getSku());
				}
				
				// Cell 11
				cell = dataRow.createCell(11);
				if (listDet.get(i).getSkuDescripcion() != null) {
					cell.setCellValue(listDet.get(i).getSkuDescripcion());
				}
				// Cell 12
				cell = dataRow.createCell(12);
				if (listDet.get(i).getPeriodoVigente() != null) {
					String periodoVigente = "No";
					if (listDet.get(i).getPeriodoVigente()==1) periodoVigente = "Si";
					cell.setCellValue(periodoVigente);
				}
				// Cell 13
				cell = dataRow.createCell(13);
				String tieneAcuerdo = "No";
				if (listDet.get(i).isTieneAcuerdo()) tieneAcuerdo = "Si";
				cell.setCellValue(tieneAcuerdo);
				
			}
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