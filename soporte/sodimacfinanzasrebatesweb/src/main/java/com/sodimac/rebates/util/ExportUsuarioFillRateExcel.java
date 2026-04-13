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

import com.sodimac.rebates.model.RebateUsuarioFillRateEntity;

public class ExportUsuarioFillRateExcel {

	private static Logger logger = LoggerFactory.getLogger(ExportUsuarioFillRateExcel.class);

	public static ByteArrayInputStream usuarioFillRateListToExcelFile(List<RebateUsuarioFillRateEntity> listUsuarioFillRate) {

//		try (Workbook workbook = new XSSFWorkbook()) {
		try (SXSSFWorkbook workbook = new SXSSFWorkbook(10000)) {
			Sheet sheet = workbook.createSheet("Reporte Usuario FillRate");

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
							      ,"Num Depto"
							      ,"Nombre Depto"
							      ,"Orden Compra"
							      ,"Fecha Emisión"
							      ,"Fecha Recepción"
							      ,"Tipo Acuerdo"
							      ,"Moneda S"
							      ,"FillRate"
							      ,"Valor"
							      ,"Tipo Valor"
							      ,"Programa Pago"
							      ,"Periodo"
							      ,"Numero Semana"
							      ,"Tienda Origen"
							      ,"Tienda Destino"
							      ,"Tipo Orden Compra"
							      ,"SKU"
							      ,"Descripcion Producto"
							      ,"LtEnvio"
							      ,"LtProceso"
							      ,"Dias LeadTime"
							      ,"Dias Totales Reales"
							      ,"Dias Desfase"
							      ,"Cantidad Ordenada"
							      ,"Cantidad Recibida"
							      ,"Faltante Global"							      
							      ,"PorcentajeFRPiezas"
							      ,"Costo Unitario"
							      ,"Monto Ordenado"
							      ,"Monto Recibido"
							      ,"Faltante"
							      ,"PorcentajeFRMonto"
							      ,"Monto Ordenado Total"
//							      ,"Monto Descuento"
							      ,"Semana Anio"
							      ,"Estatus Contrato"
							      ,"LeadTime"
							      ,"Monto Descuento FillRate Sin Impuestos"							      
							      ,"IVA"
							      ,"IEPS"
							      ,"Monto Iva"
							      ,"Monto Ieps"
							      ,"Monto Descuento FillRate"
							      ,"Monto Descuento FillRate Inf"							      
							      ,"Fec Recepcion Inicial"
							      ,"Fecha Ultima Recepcion"
							      ,"Tipo Cambio"
							      ,"Cuenta Global"
							      ,"Exclusion"
							      ,"FechaExclusion"
							      ,"IdExclusion"
							      ,"IdCatPeriodo"
							      ,"DetallePeriodo"
							      ,"Fecha Inicio Periodo"
							      ,"Fecha Fin Periodo"
							   };

			// Creating header
			for (int i = 0; i < columns.length; i++) {

				Cell cell = row.createCell(i);
				cell.setCellValue(columns[i]);
				cell.setCellStyle(headerCellStyle);
				// Tamaño de columnas: cm aprox.
				sheet.setColumnWidth(i, 20 * 256);
			}
			sheet.setColumnWidth(4, 30 * 256);
			sheet.setColumnWidth(6, 30 * 256);
			sheet.setColumnWidth(8, 30 * 256);
			sheet.setColumnWidth(11, 30 * 256);

			CreationHelper createHelper = workbook.getCreationHelper();

			if (listUsuarioFillRate != null && listUsuarioFillRate.size() >= 1) {

				// Creating style rows
				CellStyle rowStyle = workbook.createCellStyle();
				rowStyle.setWrapText(true);

				// Creating style datetime
				CellStyle dateStyle = workbook.createCellStyle();
				dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));

				// Creating data rows for each discount
				for (int i = 0; i < listUsuarioFillRate.size(); i++) {

					Row dataRow = sheet.createRow(i + 1);
					
					int col=0;
					// Cell 0
					Cell cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getOrigen());
					
					// Cell 1
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getMoneda());
					
					// Cell 2
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getRfc());
					
					// Cell 3
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getCodigoProveedor());
					
					// Cell 4
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getProveedor());
					
					// Cell 5
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getGerenteNegocio());
					
					// Cell 6
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getNombreGerente());
					
					// Cell 7
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getNumeroJefeLinea());
					
					// Cell 8
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getNombreJefeLinea());
					
					// Cell 9
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getFamilia());
					
					// Cell 10
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getNombreFamilia());
					
					// Cell 11
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getNumDepto());
					
					// Cell 12
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getNombreDepto());
					
					// Cell 13
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getOrdenCompra());
					
					// Cell 14
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getFechaEmision());
					
					// Cell 15
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getFechaRecepcion());
					cell.setCellStyle(dateStyle);
					
					// Cell 16
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getTipoAcuerdo());
					
					// Cell 17
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getMonedaS());
					
					// Cell 18
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getFillRate());
					
					// Cell 19
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getValor());
					
					// Cell 20
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getTipoValor());
					
					// Cell 21
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getProgramaPago());

					// Cell 22
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getPeriodo());

					// Cell 22
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getNumeroSemana());
					
					// Cell 23
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getTiendaCD());
					
					// Cell 24
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getTienda());
					
					// Cell 25
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getTipoOrdenCompra());
					
					// Cell 26
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getSku());
					
					// Cell 27
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getDescripcionProducto());
					
					// Cell 28
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getLtEnvio());
					
					// Cell 29
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getLtProceso());
										
					// Cell 30
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getDiasLeadTime() );

					// Cell 31
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getDiasTotales());

					// Cell 32
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getDiasDesfase());
					
					// Cell 33
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getCantidadOrdenada());
					
					// Cell 34
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getCantidadRecibida());
					
					// Cell 35
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getFaltanteGlobal());
					
					// Cell 36
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getPorcentajeFRPiezas());
					
					// Cell 37
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getCostoUnitario());
					
					// Cell 38
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getMontoOrdenado());
					
					// Cell 39
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getMontoRecibido());
					
					// Cell 40
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getFaltante());
					
					// Cell 41
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getPorcentajeFRMonto());
					
					// Cell 42
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getMontoOrdenadoTotal());
					
					// Cell 00
//					cell = dataRow.createCell(col++);
//					cell.setCellValue(listUsuarioFillRate.get(i).getMontoDescuento());
					
					// Cell 43
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getSemanaAnio());
					
					// Cell 44
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getEstatusContrato());
					
					// Cell 45
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getLeadTime());
					
					// Cell 46
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getMontoDescuentoFillRateSinImpuestos());
					
					// Cell 47
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getIva());
					
					// Cell 48
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getIeps());
					
					// Cell 49
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getMontoIva());
					
					// Cell 50
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getMontoIeps());
					
					// Cell 51
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getMontoDescuentoFillRate());
					
					// Cell 52
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getMontoDescuentoFillRateInf());
					
					// Cell 53
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getFecRecepcionInicial());
					
					// Cell 54
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getFechaUltimaRecepcion());
					
					// Cell 55
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getTipoCambio());
					
					// Cell 56
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getCuentaGlobal());
					
					// Cell 57
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getExclusion());
					
					// Cell 58
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getFechaExclusion());
					
					// Cell 59
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getIdExclusion() );
					
					// Cell 60
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getIdCatPeriodo() );
					
					// Cell 61
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getDetallePeriodo() );
					
					// Cell 62
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getFechaIni() );
					
					// Cell 63
					cell = dataRow.createCell(col++);
					cell.setCellValue(listUsuarioFillRate.get(i).getFechaFin() );
					
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