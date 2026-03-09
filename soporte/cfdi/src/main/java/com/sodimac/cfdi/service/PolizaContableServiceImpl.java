package com.sodimac.cfdi.service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
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

import com.sodimac.cfdi.model.puntosces.CatalogoDto;
import com.sodimac.cfdi.model.puntosces.PolizaContableDto;
import com.sodimac.cfdi.model.puntosces.PolizasFilterDto;
import com.sodimac.cfdi.service.client.PuntosCesClientService;


@Service
public class PolizaContableServiceImpl implements PolizaContableService {
	
	@Autowired
	private PuntosCesClientService puntosCesClientService;
	
	private Map<String, List<CatalogoDto>> catalogos = new HashMap<>();
	
	int rowIndex = 1;
	
	@Override
	public Map<String, List<CatalogoDto>> getCatalogos() {
		
		catalogos.put("documentos", puntosCesClientService.getCatalogo(5));
		catalogos.put("tarjetas", puntosCesClientService.getCatalogo(6));
		catalogos.put("empresas", puntosCesClientService.getCatalogo(7));
		catalogos.put("modulos", puntosCesClientService.getCatalogo(8));
		catalogos.put("monedas", puntosCesClientService.getCatalogo(9));
		catalogos.put("sistemasOrigen", puntosCesClientService.getCatalogo(10));
		catalogos.put("transaccionContable", puntosCesClientService.getCatalogo(11));
		catalogos.put("usos", puntosCesClientService.getCatalogo(12));
		catalogos.put("impuestos", puntosCesClientService.getCatalogo(13));
		catalogos.put("tasas", puntosCesClientService.getCatalogo(14));
		catalogos.put("origenes", puntosCesClientService.getCatalogo(15));
		catalogos.put("transacciones", puntosCesClientService.getCatalogo(16));
		
		catalogos.put("sucursales", puntosCesClientService.getSucursales());
		
		
		return catalogos;
	}
	
	@Override
	public List<PolizaContableDto> findParameters(PolizasFilterDto filterDto) {
		List<PolizaContableDto> result = puntosCesClientService.getPolizasContablesFilter(filterDto); 		
		
		result.stream().forEach(item -> {
			item.setClaseDoc(getElementoDescripcionCatalogo("documentos", item.getClaseDoc()));
			item.setDebitoCredito(getElementoDescripcionCatalogo("tarjetas", item.getDebitoCredito()));
			item.setEmpresa(getElementoDescripcionCatalogo("empresas", item.getEmpresa()));
			item.setIdModulo(getElementoDescripcionCatalogo("modulos", item.getIdModulo()));
			item.setMoneda(getElementoDescripcionCatalogo("monedas", item.getMoneda()));
			item.setSistemaOrigen(getElementoDescripcionCatalogo("sistemasOrigen", item.getSistemaOrigen()));
			item.setTipoTransaccionContable(getElementoDescripcionCatalogo("transaccionContable", item.getTipoTransaccionContable()));
			item.setTipoUso(getElementoDescripcionCatalogo("usos", item.getTipoUso()));
			item.setTipoImpuesto(getElementoDescripcionCatalogo("impuestos", item.getTipoImpuesto()));
			item.setTasaImpuesto(getElementoDescripcionCatalogo("tasas", item.getTasaImpuesto()));
			item.setOrigenEtl(getElementoDescripcionCatalogo("origenes", item.getOrigenEtl()));
			item.setTipoTransaccion(getElementoDescripcionCatalogo("transacciones", item.getTipoTransaccion()));
			
			item.setSucursal(getElementoDescripcionCatalogo("sucursales", item.getSucursal()));
		});
		
		return result;
	}

	@Override
	public String guardarPolizaContable(PolizaContableDto polizaContable, boolean isNewPoliza) {
		try {
		
			if(isNewPoliza) {
				puntosCesClientService.savePolizaContable(polizaContable);
			} else {
				puntosCesClientService.updatePolizaContable(polizaContable);
			}
			
			return null;
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@Override
	public void eliminarPolizaContable(String idConfigContable) {
		puntosCesClientService.deletePolizaContable(Integer.valueOf(idConfigContable));
	}
	
	private String getElementoDescripcionCatalogo(String nombreCatalogo, String param) {
		
		for(CatalogoDto catalogo : catalogos.get(nombreCatalogo)) {
			if (catalogo.getElemento().equals(param)) {
				return catalogo.getDescripcion();
			}
		}
		
		return param;
		
	}

	@Override
	public OutputStream getExcel(PolizasFilterDto filterDto) {
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Polizas Contables");

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

		String[] columns = { "Id","Id Modulo","Empresa","Cabecera","Posicion","Cuenta Contable","Débito / Crédito","Descripción","Moneda",
				"Tipo Cambio","Sistema Origen","Origen ETL","Tipo Uso","Indicador Impuesto","Tipo Transacción","Clase Documento","Sucursal",
				"Impuesto","Tipo Impuesto","Tasa Impuesto","Tipo Transaccion Contable","Estatus","Usuario","Fecha Registro","Fecha Actualización" };

		for (int i = 0; i < columns.length; i++) {
			Cell headerCell = header.createCell(i);
			headerCell.setCellValue(columns[i]);
			headerCell.setCellStyle(headerStyle);
		}

		CellStyle style = workbook.createCellStyle();
		style.setWrapText(true);

		CellStyle styleCenter = workbook.createCellStyle();
		styleCenter.setWrapText(true);
		styleCenter.setAlignment(HorizontalAlignment.CENTER);

		rowIndex = 1;

		findParameters(filterDto).forEach(parametro -> {

			Row row = sheet.createRow(rowIndex);

			Cell cell = row.createCell(0);
			cell.setCellValue(parametro.getIdConfigContable());
			cell.setCellStyle(style);

			cell = row.createCell(1);
			cell.setCellValue(parametro.getIdModulo());
			cell.setCellStyle(style);

			cell = row.createCell(2);
			cell.setCellValue(parametro.getEmpresa());
			cell.setCellStyle(style);

			cell = row.createCell(3);
			cell.setCellValue(parametro.getCabecera());
			cell.setCellStyle(style);
			
			cell = row.createCell(4);
			cell.setCellValue(parametro.getPosicion());
			cell.setCellStyle(style);
			
			cell = row.createCell(5);
			cell.setCellValue(parametro.getCuentaContable());
			cell.setCellStyle(style);
			
			cell = row.createCell(6);
			cell.setCellValue(parametro.getDebitoCredito());
			cell.setCellStyle(style);
			
			cell = row.createCell(7);
			cell.setCellValue(parametro.getDescripcion());
			cell.setCellStyle(style);
			
			cell = row.createCell(8);
			cell.setCellValue(parametro.getMoneda());
			cell.setCellStyle(style);
			
			cell = row.createCell(9);
			cell.setCellValue(parametro.getTipoCambio());
			cell.setCellStyle(style);
			
			cell = row.createCell(10);
			cell.setCellValue(parametro.getSistemaOrigen());
			cell.setCellStyle(style);
			
			cell = row.createCell(11);
			cell.setCellValue(parametro.getOrigenEtl());
			cell.setCellStyle(style);
			
			cell = row.createCell(12);
			cell.setCellValue(parametro.getTipoUso());
			cell.setCellStyle(style);
			
			cell = row.createCell(13);
			cell.setCellValue(parametro.getIndicadorImpuesto());
			cell.setCellStyle(style);
			
			cell = row.createCell(14);
			cell.setCellValue(parametro.getTipoTransaccion());
			cell.setCellStyle(style);
			
			cell = row.createCell(15);
			cell.setCellValue(parametro.getClaseDoc());
			cell.setCellStyle(style);
			
			cell = row.createCell(16);
			cell.setCellValue(parametro.getSucursal());
			cell.setCellStyle(style);
			
			cell = row.createCell(17);
			cell.setCellValue(parametro.getImpuesto());
			cell.setCellStyle(style);
			
			cell = row.createCell(18);
			cell.setCellValue(parametro.getTipoImpuesto());
			cell.setCellStyle(style);
			
			cell = row.createCell(19);
			cell.setCellValue(parametro.getTasaImpuesto());
			cell.setCellStyle(style);
			
			cell = row.createCell(20);
			cell.setCellValue(parametro.getTipoTransaccionContable());
			cell.setCellStyle(style);

			cell = row.createCell(21);
			cell.setCellValue(parametro.getEstatus() == 1 ? "Activo" : "Inactivo");
			cell.setCellStyle(styleCenter);
			
			cell = row.createCell(22);
			cell.setCellValue(parametro.getUsuario());
			cell.setCellStyle(style);
			
			CreationHelper createHelper = workbook.getCreationHelper();
			CellStyle dateCellStyle = workbook.createCellStyle();
			dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy"));

			cell = row.createCell(23);
			cell.setCellValue(parametro.getFechaRegistro()); 
			cell.setCellStyle(dateCellStyle);

			cell = row.createCell(24);
			cell.setCellValue(parametro.getFechaActualizacion()); 
			cell.setCellStyle(dateCellStyle);

			for (int i = 0; i < row.getLastCellNum(); i++) {
				sheet.autoSizeColumn(i);
			}

			rowIndex++;

		});

		ByteArrayOutputStream outputStream = null;

		try {
			outputStream = new ByteArrayOutputStream();
			workbook.write(outputStream);
			workbook.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return outputStream;
	}
	
}
