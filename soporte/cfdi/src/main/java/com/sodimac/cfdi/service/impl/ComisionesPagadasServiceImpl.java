package com.sodimac.cfdi.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.icu.text.SimpleDateFormat;
import com.sodimac.cfdi.models.ComisionesPagadasModel;
import com.sodimac.cfdi.repository.fiscal.ComisionesPagadasRepository;
import com.sodimac.cfdi.service.ComisionesPagadasService;

@Service
public class ComisionesPagadasServiceImpl implements ComisionesPagadasService {
	
	private static Logger logger = LoggerFactory.getLogger(ComisionesPagadasServiceImpl.class);
	
	private static final int ID_COMISIONES_PAGADAS 		= 0;
	private static final int FECHA_TRX            		= 1;
	private static final int TIPO_TRX             		= 2;
	private static final int NUM_TIENDA           		= 3;
	private static final int NUM_TRX              		= 4;
	private static final int NUM_CAJA             		= 5;
	private static final int MONTO_PAGO           		= 6;
	private static final int COD_COMERCIO               = 7;
	private static final int NUM_TARJETA                = 8;
	private static final int NUM_CUOTAS                 = 9;
	private static final int COD_AUTORIZACION           = 10;
	private static final int TIPO_TARJETA               = 11;
	private static final int PASARELA                   = 12;
	private static final int COD_BANCO_EMISOR          	= 13;
	private static final int COD_MARCA_TARJETA         	= 14;
	private static final int COMISION_USO_PORC         	= 15;
	private static final int COMISION_USO_MONTO        	= 16;
	private static final int PORC_DESCTO               	= 17;
	private static final int PORC_SODIMAC_DESCTO       	= 18;
	private static final int PORC_EMISOR_DESCTO        	= 19;
	private static final int SOBRETASA                 	= 20;
	private static final int IND_PROMOCION             	= 21;
	private static final int FACTOR_CAMBIO_MONEDA      	= 22;
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	@Autowired
	private ComisionesPagadasRepository comisionesPagadasRepository;

	@Override
	public List<ComisionesPagadasModel> obtenerComisionesPagadassByParams(String fechaInicial, String fechaFinal,
			String pTicket, int start, int rowsPerPage, Integer tienda) {
		
		List<ComisionesPagadasModel> listModel = new ArrayList<ComisionesPagadasModel>();
		List<Object[]> listObject = this.comisionesPagadasRepository.getComisionesByParams(fechaInicial, fechaFinal, pTicket, start, rowsPerPage, tienda);
		if (listObject != null && listObject.size() > 0) {
			for (Object[] obj : listObject) {
				ComisionesPagadasModel model = this.convertModel(obj);
				listModel.add( model );
			}
		}
		return listModel;
	}
	
	public ComisionesPagadasModel convertModel(Object[] obj) {
		ComisionesPagadasModel model = new ComisionesPagadasModel();
		Integer idComisionesPagadas = this.getIntValue( obj[ID_COMISIONES_PAGADAS] );
		Date fechaTrx = this.getDateValue( obj[FECHA_TRX] );   
		Integer tipoTrx = this.getIntValue( obj[TIPO_TRX] );          
		Integer numTienda = this.getIntValue( obj[NUM_TIENDA] );
		String numTrx = this.getStrValue( obj[NUM_TRX] );
		Integer numCaja = this.getIntValue( obj[NUM_CAJA] );
		Double montoPago = this.getDoubleValue( obj[MONTO_PAGO] );
		String codComercio = this.getStrValue( obj[COD_COMERCIO] );
		String numTarjeta = this.getStrValue( obj[NUM_TARJETA] );
		Integer numCuotas = this.getIntValue( obj[NUM_CUOTAS] );
		String codAutorizacion = this.getStrValue( obj[COD_AUTORIZACION] );
		String tipoTarjeta = this.getStrValue( obj[TIPO_TARJETA] );
		String pasarela = this.getStrValue( obj[PASARELA] );
		Integer codBancoEmisor = this.getIntValue( obj[COD_BANCO_EMISOR] );
		Integer codMarcaTarjeta = this.getIntValue( obj[COD_MARCA_TARJETA] );
		Double comisionUsoPorc = this.getDoubleValue( obj[COMISION_USO_PORC] );
		Double comisionUsoMonto = this.getDoubleValue( obj[COMISION_USO_MONTO] );
		Double porcDescto = this.getDoubleValue( obj[PORC_DESCTO] );
		Double porcSodimacDescto = this.getDoubleValue( obj[PORC_SODIMAC_DESCTO] );
		Double porcEmisorDescto = this.getDoubleValue( obj[PORC_EMISOR_DESCTO] );
		Double sobretasa = this.getDoubleValue( obj[SOBRETASA] );
		String indPromocion = this.getStrValue( obj[IND_PROMOCION] );
		Integer factorCambioMoneda = this.getIntValue( obj[FACTOR_CAMBIO_MONEDA] );
		
		model.setIdComisionesPagadas(idComisionesPagadas);
		model.setFechaTrx(fechaTrx);
		model.setFechaTrxStr( (fechaTrx != null) ? sdf.format(fechaTrx) : "" );
		model.setTipoTrx(tipoTrx);
		model.setNumTienda(numTienda);          
		model.setNumTrx(numTrx);
		model.setNumCaja(numCaja);
		model.setMontoPago(montoPago);
		model.setCodComercio(codComercio);
		model.setNumTarjeta(numTarjeta);
		model.setNumCuotas(numCuotas);
		model.setCodAutorizacion(codAutorizacion);
		model.setTipoTarjeta(tipoTarjeta);
		model.setPasarela(pasarela);
		model.setCodBancoEmisor(codBancoEmisor);
		model.setCodMarcaTarjeta(codMarcaTarjeta);
		model.setComisionUsoPorc(comisionUsoPorc);
		model.setComisionUsoMonto(comisionUsoMonto);
		model.setPorcDescto(porcDescto);
		model.setPorcSodimacDescto(porcSodimacDescto);
		model.setPorcEmisorDescto(porcEmisorDescto);
		model.setSobretasa(sobretasa);
		model.setIndPromocion(indPromocion);
		model.setFactorCambioMoneda(factorCambioMoneda);
		
		return model;
	}
	
	@Override
	public byte[] createExcel(String fechaInicial, String fechaFinal,
			String pTicket, Integer tienda) throws IOException {
		int start = 0;
		int rowsPerPage = 0;
		
		List<ComisionesPagadasModel> listComisiones = this.obtenerComisionesPagadassByParams(fechaInicial, fechaFinal, pTicket, start, rowsPerPage, tienda);
				
		if (listComisiones != null) {
			return this.getXlsx(listComisiones);
		}
		return null;
	}
	
	private byte[] getXlsx(List<ComisionesPagadasModel> listComisiones) {
		try (SXSSFWorkbook workbook = new SXSSFWorkbook(10000)) {
	
			Sheet sheet = workbook.createSheet("comisione");
			sheet.setColumnWidth(0, 5000);
			sheet.setColumnWidth(1, 5000);
			sheet.setColumnWidth(2, 5000);
			sheet.setColumnWidth(3, 10000);
			sheet.setColumnWidth(4, 5000);
			sheet.setColumnWidth(5, 5000);
			sheet.setColumnWidth(6, 10000);
			sheet.setColumnWidth(7, 5000);
			sheet.setColumnWidth(8, 5000);
			sheet.setColumnWidth(9, 10000);
			sheet.setColumnWidth(10, 5000);
			sheet.setColumnWidth(11, 5000);
			sheet.setColumnWidth(12, 10000);
			sheet.setColumnWidth(13, 10000);
			sheet.setColumnWidth(14, 10000);
			sheet.setColumnWidth(15, 10000);
			sheet.setColumnWidth(16, 10000);
			sheet.setColumnWidth(17, 10000);
			sheet.setColumnWidth(18, 10000);
			sheet.setColumnWidth(19, 10000);
			sheet.setColumnWidth(20, 10000);
			sheet.setColumnWidth(21, 10000);
			sheet.setColumnWidth(22, 10000);
			sheet.setColumnWidth(23, 10000);
			
			Row header = sheet.createRow(0);
	
			CellStyle headerStyle = workbook.createCellStyle();
			headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	
			Font font = workbook.createFont();
			font.setFontName("Arial");
			font.setFontHeightInPoints((short) 14);
			font.setBold(true);
			font.setColor(IndexedColors.WHITE.getIndex());	
			headerStyle.setFont(font);
			
			String[] columns ={ 
					"Fecha Trx", 
					"Tipo Trx", 
					"Num Tienda", 
					"Num Trx", 
					"Num Caja", 
					"Monto Pago", 
					"Cod Comercio", 
					"Num Tarjeta", 
					"Num Cuotas", 
					"Cod Autorizaci\u00f3n", 
					"Tipo Tarjeta", 
					"Pasarela", 
					"Cod Banco Emisor", 
					"Cod Marca Tarjeta", 
					"Comisi\u00f3n uso Porc", 
					"Comisi\u00f3n uso Monto", 
					"Porc Descuento", 
					"Porc Sodimac Descuento", 
					"Porc Emisor Descuento", 
					"Sobretasa", 
					"Ind Promoci\u00f3n", 
					"Factor Cambio Moneda" };
			
			for (int i = 0; i< columns.length; i++) {
				Cell headerCell = header.createCell(i);
				headerCell.setCellValue(columns[i]);
				headerCell.setCellStyle(headerStyle);
			}
			
			CellStyle style = workbook.createCellStyle();
			style.setWrapText(true);
			
			CellStyle styleRight = workbook.createCellStyle();
			styleRight.setWrapText(true);
			styleRight.setAlignment(HorizontalAlignment.RIGHT);
	
			int nxRow = 0;
			for (ComisionesPagadasModel model : listComisiones) {
				nxRow = nxRow + 1;
				Row row = sheet.createRow(nxRow);
	
				Cell cell = row.createCell(0);
				cell.setCellValue((model.getFechaTrxStr() != null) ? model.getFechaTrxStr() : "");
				cell.setCellStyle(styleRight);
	
				cell = row.createCell(1);
				cell.setCellValue((model.getTipoTrx() != null) ? model.getTipoTrx() : 0);
				cell.setCellStyle(styleRight);
	
				cell = row.createCell(2);
				cell.setCellValue((model.getNumTienda() != null) ? model.getNumTienda() : 0);
				cell.setCellStyle(styleRight);
	
				cell = row.createCell(3);
				cell.setCellValue((model.getNumTrx() != null) ? model.getNumTrx() : "");
				cell.setCellStyle(style);
				
	
				cell = row.createCell(4);
				cell.setCellValue((model.getNumCaja() != null) ? model.getNumCaja() : 0);
				cell.setCellStyle(styleRight);
	
				cell = row.createCell(5);
				cell.setCellValue((model.getMontoPago() != null) ? model.getMontoPago() : 0.0);
				cell.setCellStyle(styleRight);
	
				cell = row.createCell(6);
				cell.setCellValue((model.getCodComercio() != null) ? model.getCodComercio() : "");
				cell.setCellStyle(style);
	
				cell = row.createCell(7);
				cell.setCellValue((model.getNumTarjeta() != null) ? model.getNumTarjeta() : "" );
				cell.setCellStyle(style);
				
				cell = row.createCell(8);
				cell.setCellValue((model.getNumCuotas() != null) ? model.getNumCuotas() : 0 );
				cell.setCellStyle(styleRight);
				
				cell = row.createCell(9);
				cell.setCellValue((model.getCodAutorizacion() != null) ? model.getCodAutorizacion() : "" );
				cell.setCellStyle(style);
				
				cell = row.createCell(10);
				cell.setCellValue((model.getTipoTarjeta() != null) ? model.getTipoTarjeta() : "" );
				cell.setCellStyle(style);
				
				cell = row.createCell(11);
				cell.setCellValue((model.getPasarela() != null) ? model.getPasarela() : "" );
				cell.setCellStyle(style);
				
				cell = row.createCell(12);
				cell.setCellValue((model.getCodBancoEmisor() != null) ? model.getCodBancoEmisor() : 0 );
				cell.setCellStyle(styleRight);
				
				cell = row.createCell(13);
				cell.setCellValue((model.getCodMarcaTarjeta() != null) ? model.getCodMarcaTarjeta() : 0 );
				cell.setCellStyle(styleRight);
				
				cell = row.createCell(14);
				cell.setCellValue((model.getComisionUsoPorc() != null) ? model.getComisionUsoPorc() : 0 );
				cell.setCellStyle(styleRight);
				
				cell = row.createCell(15);
				cell.setCellValue((model.getComisionUsoMonto() != null) ? model.getComisionUsoMonto() : 0 );
				cell.setCellStyle(styleRight);
				
				cell = row.createCell(16);
				cell.setCellValue((model.getPorcDescto() != null) ? model.getPorcDescto() : 0 );
				cell.setCellStyle(styleRight);
				
				cell = row.createCell(17);
				cell.setCellValue((model.getPorcSodimacDescto() != null) ? model.getPorcSodimacDescto() : 0 );
				cell.setCellStyle(styleRight);
				
				cell = row.createCell(18);
				cell.setCellValue((model.getPorcEmisorDescto() != null) ? model.getPorcEmisorDescto() : 0 );
				cell.setCellStyle(styleRight);
				
				cell = row.createCell(19);
				cell.setCellValue((model.getSobretasa() != null) ? model.getSobretasa() : 0 );
				cell.setCellStyle(styleRight);
				
				cell = row.createCell(20);
				cell.setCellValue((model.getIndPromocion() != null) ? model.getIndPromocion() : "" );
				cell.setCellStyle(style);
				
				cell = row.createCell(21);
				cell.setCellValue((model.getFactorCambioMoneda() != null) ? model.getFactorCambioMoneda() : 0 );
				cell.setCellStyle(styleRight);
				
			}
			
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			try {
				workbook.write(outputStream);
				workbook.close();
				return outputStream.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
				logger.error("exportExcell-comisionese ", ex);
				return null;
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			logger.error("exportExcell-comisionese ", ex);
			return null;
		}
	}

	private Integer getIntValue(Object obj) {
		if (obj != null) {
			return Integer.valueOf( obj.toString() );
		}
		return null;
	}
	
	private String getStrValue(Object obj) {
		if (obj != null) {
			return obj.toString();
		}
		return null;
	}
	
	private Date getDateValue(Object obj) {
		if (obj != null) {
			return (Date) obj;
		}
		return null;
	}
	
	private Double getDoubleValue(Object obj) {
		if (obj != null) {
			return Double.valueOf( obj.toString() );
		}
		return null;
	}
	
	
}
