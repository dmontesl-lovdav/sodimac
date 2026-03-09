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
import com.sodimac.cfdi.models.FacturacionVveeModel;
import com.sodimac.cfdi.repository.fiscal.FacturacionVveeRepository;
import com.sodimac.cfdi.service.FacturacionVveeService;

@Service
public class FacturacionVveeServiceImpl implements FacturacionVveeService {
	
	private static Logger logger = LoggerFactory.getLogger(FacturacionVveeServiceImpl.class);
	
	private static final int ID_FACTURACION_VVEE	= 0;
	private static final int NUM_TRX 				= 1;
	private static final int NUM_DOC_CANAL 			= 2;
	private static final int NRO_GUIA 				= 3;
	private static final int NRO_GUIA_PROV 			= 4;
	private static final int TIPO_FACTURA 			= 5;
	private static final int NOM_OBRA 				= 6;
	private static final int CONTACTO_OBRA 			= 7;
	private static final int EMAIL 					= 8;
	private static final int CFDI 					= 9;
	private static final int RFC 					= 10;
	private static final int ORDEN 					= 11;
	private static final int R_SOCIAL 				= 12;
	private static final int CODIGO_POSTAL 			= 13;
	private static final int REGIMEN_FISCAL 		= 14;

	private static final int FECHA 					= 15;
	private static final int NUM_FACTURA 			= 16;
	private static final int NUM_TICKET 			= 17;
	private static final int NRO_SERIE 				= 18;
	private static final int NRO_FOLIO 				= 19;
	private static final int NUM_TIENDA 			= 20;
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	@Autowired
	private FacturacionVveeRepository facturacionVveeRepository;

	@Override
	public List<FacturacionVveeModel> obtenerFacturacionVveeByParams(String fechaInicial, String fechaFinal,
			String pTicket, int start, int rowsPerPage, Integer tienda) {
		
		List<FacturacionVveeModel> listModel = new ArrayList<FacturacionVveeModel>();
		List<Object[]> listObject = this.facturacionVveeRepository.getFacturacionVveeByParams(fechaInicial, fechaFinal, pTicket, start, rowsPerPage, tienda);
		if (listObject != null && listObject.size() > 0) {
			for (Object[] obj : listObject) {
				FacturacionVveeModel model = this.convertModel(obj);
				listModel.add( model );
			}
		}
		return listModel;
	}
	
	public FacturacionVveeModel convertModel(Object[] obj) {
		FacturacionVveeModel model = new FacturacionVveeModel();
		Integer idFacturacionVvee = this.getIntValue( obj[ID_FACTURACION_VVEE] );
		String numTrx = this.getStrValue( obj[NUM_TRX] );
		Integer numDocCanal = this.getIntValue( obj[NUM_DOC_CANAL] );
		String nroGuia = this.getStrValue( obj[NRO_GUIA] );
		String nroGuiaProv = this.getStrValue( obj[NRO_GUIA_PROV] );
		String tipoFactura = this.getStrValue( obj[TIPO_FACTURA] );
		String nomObra = this.getStrValue( obj[NOM_OBRA] );
		String contactoObra = this.getStrValue( obj[CONTACTO_OBRA] );
		String email = this.getStrValue( obj[EMAIL] );
		String cfdi = this.getStrValue( obj[CFDI] );
		String rfc = this.getStrValue( obj[RFC] );
		String orden = this.getStrValue( obj[ORDEN] );
		String rSocial = this.getStrValue( obj[R_SOCIAL] );
		String codigoPostal = this.getStrValue( obj[CODIGO_POSTAL] );
		String regimenFiscal = this.getStrValue( obj[REGIMEN_FISCAL] );
		Date fecha = this.getDateValue( obj[FECHA] );
		String numFactura = this.getStrValue( obj[NUM_FACTURA] );
		String numTicket = this.getStrValue( obj[NUM_TICKET] );
		String nroSerie = this.getStrValue( obj[NRO_SERIE] );
		Integer nroFolio = this.getIntValue( obj[NRO_FOLIO] );
		Integer numTienda = this.getIntValue( obj[NUM_TIENDA] );
		
		model.setIdFacturacionVvee(idFacturacionVvee);
		model.setNumTrx(numTrx);
		model.setNumDocCanal(numDocCanal);
		model.setNroGuia(nroGuia);
		model.setNroGuiaProv(nroGuiaProv);
		model.setTipoFactura(tipoFactura);
		model.setNomObra(nomObra);
		model.setContactoObra(contactoObra);
		model.setEmail(email);
		model.setCfdi(cfdi);
		model.setRfc(rfc);
		model.setOrden(orden);
		model.setrSocial(rSocial);
		model.setCodigoPostal(codigoPostal);
		model.setRegimenFiscal(regimenFiscal);
		model.setFecha(fecha);
		model.setFechaStr( (fecha != null) ? sdf.format(fecha) : "" );
		model.setNumFactura(numFactura);
		model.setNumTicket(numTicket);
		model.setNroSerie(nroSerie);
		model.setNroFolio(nroFolio);
		model.setNumTienda(numTienda);
		model.setTimbrado( (numFactura != null && !numFactura.isEmpty()) );
		//model.setTimbrado( (nroFolio%2) == 0 );
		
		return model;
	}
	
	@Override
	public byte[] createExcel(String fechaInicial, String fechaFinal,
			String pTicket, Integer tienda) throws IOException {
		int start = 0;
		int rowsPerPage = 0;
		
		List<FacturacionVveeModel> listComisiones = this.obtenerFacturacionVveeByParams(fechaInicial, fechaFinal, pTicket, start, rowsPerPage, tienda);
				
		if (listComisiones != null) {
			return this.getXlsx(listComisiones);
		}
		return null;
	}
	
	private byte[] getXlsx(List<FacturacionVveeModel> listFacturacionVvee) {
		try (SXSSFWorkbook workbook = new SXSSFWorkbook(10000)) {

			Sheet sheet = workbook.createSheet("comisione");
			sheet.setColumnWidth(0, 10000);
			sheet.setColumnWidth(1, 5000);
			sheet.setColumnWidth(2, 5000);
			sheet.setColumnWidth(3, 5000);
			sheet.setColumnWidth(4, 5000);
			sheet.setColumnWidth(5, 5000);
			sheet.setColumnWidth(6, 10000);
			sheet.setColumnWidth(7, 15000);
			sheet.setColumnWidth(8, 10000);
			sheet.setColumnWidth(9, 10000);
			sheet.setColumnWidth(10, 5000);
			sheet.setColumnWidth(11, 20000);
			sheet.setColumnWidth(12, 7000);
			sheet.setColumnWidth(13, 5000);
			sheet.setColumnWidth(14, 5000);
			sheet.setColumnWidth(15, 15000);
			sheet.setColumnWidth(16, 5000);
			sheet.setColumnWidth(17, 5000);
			sheet.setColumnWidth(18, 5000);
			sheet.setColumnWidth(19, 5000);
			
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
					 "Num Trx"
					, "Num Doc Canal"
					, "Nro Guia"
					, "Nro Guia Prov"
					, "Tipo Factura"
					, "Nom Obra"
					, "Contacto Obra"
					, "Email"
					, "Cfdi"
					, "Rfc"
					, "Orden"
					, "R Social"
					, "C\u00f3digo Postal"
					, "Regimen_Fiscal"
					, "Fecha"
					, "Num Factura"
					, "Num Ticket"
					, "Nro Serie"
					, "Nro Folio"
					, "Num Tienda"
			};
			
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
			for (FacturacionVveeModel model : listFacturacionVvee) {
				nxRow = nxRow + 1;
				Row row = sheet.createRow(nxRow);
	
				Cell cell = row.createCell(0);
				cell.setCellValue((model.getNumTrx() != null) ? model.getNumTrx() : "");
				cell.setCellStyle(style);
	
				cell = row.createCell(1);
				cell.setCellValue((model.getNumDocCanal() != null) ? model.getNumDocCanal() : 0);
				cell.setCellStyle(styleRight);
	
				cell = row.createCell(2);
				cell.setCellValue((model.getNroGuia() != null) ? model.getNroGuia() : "");
				cell.setCellStyle(style);
	
				cell = row.createCell(3);
				cell.setCellValue((model.getNroGuiaProv() != null) ? model.getNroGuiaProv() : "");
				cell.setCellStyle(style);
				
	
				cell = row.createCell(4);
				cell.setCellValue((model.getTipoFactura() != null) ? model.getTipoFactura() : "");
				cell.setCellStyle(style);
	
				cell = row.createCell(5);
				cell.setCellValue((model.getNomObra() != null) ? model.getNomObra() : "");
				cell.setCellStyle(style);
	
				cell = row.createCell(6);
				cell.setCellValue((model.getContactoObra() != null) ? model.getContactoObra() : "");
				cell.setCellStyle(style);
	
				cell = row.createCell(7);
				cell.setCellValue((model.getEmail() != null) ? model.getEmail() : "" );
				cell.setCellStyle(style);
				
				cell = row.createCell(8);
				cell.setCellValue((model.getCfdi() != null) ? model.getCfdi() : "" );
				cell.setCellStyle(style);
				
				cell = row.createCell(9);
				cell.setCellValue((model.getRfc() != null) ? model.getRfc() : "" );
				cell.setCellStyle(style);
				
				cell = row.createCell(10);
				cell.setCellValue((model.getOrden() != null) ? model.getOrden() : "" );
				cell.setCellStyle(style);
				
				cell = row.createCell(11);
				cell.setCellValue((model.getrSocial() != null) ? model.getrSocial() : "" );
				cell.setCellStyle(style);
				
				cell = row.createCell(12);
				cell.setCellValue((model.getCodigoPostal() != null) ? model.getCodigoPostal() : "" );
				cell.setCellStyle(style);
				
				cell = row.createCell(13);
				cell.setCellValue((model.getRegimenFiscal() != null) ? model.getRegimenFiscal() : "" );
				cell.setCellStyle(style);
				
				cell = row.createCell(14);
				cell.setCellValue((model.getFechaStr() != null) ? model.getFechaStr() : "" );
				cell.setCellStyle(styleRight);
				
				cell = row.createCell(15);
				cell.setCellValue((model.getNumFactura() != null) ? model.getNumFactura() : "" );
				cell.setCellStyle(style);
				
				cell = row.createCell(16);
				cell.setCellValue((model.getNumTicket() != null) ? model.getNumTicket() : "" );
				cell.setCellStyle(style);
				
				cell = row.createCell(17);
				cell.setCellValue((model.getNroSerie() != null) ? model.getNroSerie() : "" );
				cell.setCellStyle(style);
				
				cell = row.createCell(18);
				cell.setCellValue((model.getNroFolio() != null) ? model.getNroFolio() : 0 );
				cell.setCellStyle(styleRight);
				
				cell = row.createCell(19);
				cell.setCellValue((model.getNumTienda() != null) ? model.getNumTienda() : 0 );
				cell.setCellStyle(styleRight);	
			}
			
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			try {
				workbook.write(outputStream);
				workbook.close();
				return outputStream.toByteArray();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			logger.error("exportExcell-facturacion VVEE ", ex);
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
	
}
