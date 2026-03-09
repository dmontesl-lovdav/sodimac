package com.sodimac.cfdi.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sodimac.cfdi.entity.fiscal.ComplementosEntity;
import com.sodimac.cfdi.entity.fiscal.PagoComplementoEntity;
import com.sodimac.cfdi.entity.fiscal.PagosEntity;
import com.sodimac.cfdi.model.ClientResponseTYPE;
import com.sodimac.cfdi.model.RespuestaClient;
import com.sodimac.cfdi.model.TimbrarComplemento;
import com.sodimac.cfdi.models.ComplementosModel;
import com.sodimac.cfdi.repository.fiscal.ComplementoPagoRepository;
import com.sodimac.cfdi.repository.fiscal.ComplementosRepository;
import com.sodimac.cfdi.util.enums.EComplementoPago;

@Service
public class ComplementosServiceImpl implements ComplementosService {

	private NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
	
	@Autowired
	private ComplementoPagoRepository complementoPagoRepository;
	
	@Autowired
	private ComplementosRepository complementosRepository;
	
	@Autowired
	private SeguridadService seguridadService;
	
	@Autowired
	private CatConfiguracionService catConfiguracionService;
	
	@Autowired
	private WsftApiService wsftApiService;
	
	int nxRow = 0;
	
	@Transactional(isolation = Isolation.READ_UNCOMMITTED)
	@Override
	public List<PagoComplementoEntity> getAllComplementos() {
		
		return complementoPagoRepository.findAll();
	}

	@Transactional(isolation = Isolation.READ_UNCOMMITTED)
	@Override
	public List<ComplementosModel> getComplementosByParams(String fechaInicial, String fechaFinal, String rfcEncriptado, int start, int rowsPerPage, String estatusComplemento, Double pmonto) {

		List<ComplementosModel> list = new ArrayList<ComplementosModel>();
		//fechaInicial = "2022-01-01";
		//estatusComplemento = "CP";
		complementoPagoRepository.getComplementosByParams(fechaInicial, fechaFinal, rfcEncriptado, start, rowsPerPage, estatusComplemento, pmonto).forEach(item -> {
			ComplementosModel itemList = new ComplementosModel();
			
			Integer estatusTimbrado = 0;
			
			String estatus = (item[21] != null) ? ( item[21].toString() ) : null;
			Integer complementoPago = (item[22] != null) ? Integer.valueOf( item[22].toString() ) : 0;
			
			EComplementoPago eComplementoPago = EComplementoPago.getComplementoByEstatus(estatus);
			if (eComplementoPago.equals(EComplementoPago.TIMBRADO)) {
				estatusTimbrado = 1;
			}
			
			itemList.setId(item[0].toString());
			itemList.setNumeroCuenta(item[1].toString());
			itemList.setFechaHoraMovimiento(item[2].toString());
			itemList.setFolioBanco(item[3].toString());
			itemList.setConcepto(item[4].toString());
			itemList.setLeyenda(item[5].toString());
			itemList.setRefInterbancaria(item[6].toString());
			itemList.setFolioCliente(item[7].toString());
			itemList.setTipoDivisa(item[8].toString());
			itemList.setFolioOperacion(item[9].toString());
			itemList.setFormaPago(item[10].toString());
			itemList.setRfc(item[11] != null ? seguridadService.desencriptar(item[11].toString()) : null);
			itemList.setTransaccion(item[12] != null ? item[12].toString() : null);
			itemList.setGranTotal(item[13] != null ? numberFormat.format( Double.valueOf(item[13].toString())) : null);
			itemList.setTotalTransaccion(item[14] != null ? item[14].toString() : null);
			itemList.setSaldoAnterior(item[15] != null ? item[15].toString() : null);
			itemList.setImporte(numberFormat.format( Double.valueOf( item[16].toString()) ));
			itemList.setSaldoPendiente(item[17] != null ? numberFormat.format(Double.valueOf(item[17].toString())) : null);
			itemList.setUuidRelacionado((item[18] != null ? item[18].toString() : "") );
			itemList.setEstatus(eComplementoPago.getEstatus());
			itemList.setDescEstatus(item[20].toString());
			itemList.setComplementoAsignado( complementoPago );
			
			itemList.setEstatusTimbrado(estatusTimbrado);
			list.add(itemList);
			
		});
		
		return list;
	}

	@Override
	@Transactional(transactionManager = "transactionManagerFiscal")
	public String altaComplemento(PagosEntity pago) {
		String mensaje = "";
		try {
			System.out.println("Inicia alta complemento");
			
			PagoComplementoEntity complemento = new PagoComplementoEntity();
			complemento.setNumeroCuenta(pago.getNumeroCuenta());
			complemento.setFechaHoraMovimiento(pago.getFechaHoraMovimiento());
			complemento.setSigno(pago.getSigno());
			complemento.setImporte(pago.getImporte());
			complemento.setFolioBanco(pago.getFolioBanco());
			complemento.setConcepto(pago.getConcepto());
			complemento.setLeyenda(pago.getLeyenda());
			complemento.setRefInterbancaria(pago.getRefInterbancaria());
			complemento.setFolioCliente(pago.getFolioCliente());
			complemento.setTipoDivisa(pago.getTipoDivisa());
			complemento.setFolioOperacion(pago.getFolioOperacion());
			complemento.setFormaPago(pago.getFormaPago());
			complemento.setEstatus("PR");
			complemento.setActivo(true);
			complemento.setFechaCreacion(new java.util.Date());
			complemento.setUsuarioCreacion(1);
			
			complementoPagoRepository.saveAndFlush(complemento);
		} catch (Exception e) {
			e.printStackTrace();
			mensaje = "Ocurrio un problema al intentar registrar el complemento de pago.";
		}
		
		return mensaje;
	}

	@Transactional(isolation = Isolation.READ_UNCOMMITTED)
	@Override
	public boolean getComplementosExcelFechas(String fechaInicial, String fechaFinal, String rfcEncriptado, String nombreArchivo, String estatusComplemento, Double pmonto) {

		String path = catConfiguracionService.findParameterByKey("Mail.PathFile");
		
		Workbook workbook = new XSSFWorkbook();

		Sheet sheet = workbook.createSheet("Complementos");
		sheet.setColumnWidth(0, 5000);
		sheet.setColumnWidth(1, 5000);
		sheet.setColumnWidth(2, 5000);
		sheet.setColumnWidth(3, 5000);
		sheet.setColumnWidth(4, 5000);
		sheet.setColumnWidth(5, 5000);
		sheet.setColumnWidth(6, 5000);
		sheet.setColumnWidth(7, 5000);
		sheet.setColumnWidth(8, 5000);
		sheet.setColumnWidth(9, 5000);
		sheet.setColumnWidth(10, 5000);
		sheet.setColumnWidth(11, 5000);
		sheet.setColumnWidth(12, 5000);
		sheet.setColumnWidth(13, 5000);
		sheet.setColumnWidth(14, 5000);
		sheet.setColumnWidth(15, 5000);
		sheet.setColumnWidth(16, 5000);
		sheet.setColumnWidth(17, 5000);
		sheet.setColumnWidth(18, 5000);
		sheet.setColumnWidth(19, 5000);
		sheet.setColumnWidth(20, 5000);
		sheet.setColumnWidth(21, 10000);
		
		
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

		String[] columns = {"Numero de cuenta","Fecha movimiento","Folio Bancario","Concepto","Leyenda","Referencia Interbancaria","Folio Cliente","Divisa","Folio de operación",
				"Forma de pago","RFC","TX","Saldo Total Factura","Total Transacción","Saldo Anterior","Importe","Saldo Pendiente","UUID Relacionado","Estatus","Desc Estatus"};
		
		for (int i = 0; i< columns.length; i++) {
			Cell headerCell = header.createCell(i);
			headerCell.setCellValue(columns[i]);
			headerCell.setCellStyle(headerStyle);
		}
		
		CellStyle style = workbook.createCellStyle();
		style.setWrapText(true);
		
		CellStyle styleCenter = workbook.createCellStyle();
		styleCenter.setWrapText(true);
		styleCenter.setAlignment(HorizontalAlignment.CENTER);

		nxRow = 0;

		complementoPagoRepository.getComplementosExcelByParams(fechaInicial, fechaFinal, rfcEncriptado, estatusComplemento, pmonto).forEach(item -> {

			nxRow = nxRow + 1;
			Row row = sheet.createRow(nxRow);

			
			Cell cell = row.createCell(0);
			cell.setCellValue(item[1].toString()); // Numero de cuenta
			cell.setCellStyle(style);

			cell = row.createCell(1);
			cell.setCellValue(item[2].toString()); // Fecha movimiento
			cell.setCellStyle(style);
			
			cell = row.createCell(2);
			cell.setCellValue(item[3].toString()); // Folio Bancario
			cell.setCellStyle(styleCenter);

			cell = row.createCell(3);
			cell.setCellValue(item[4].toString()); // Concepto
			cell.setCellStyle(style);
			
			cell = row.createCell(4);
			cell.setCellValue(item[5].toString()); // Leyenda
			cell.setCellStyle(style);

			cell = row.createCell(5);
			cell.setCellValue(item[6].toString()); // Referencia Interbancaria
			cell.setCellStyle(styleCenter);

			cell = row.createCell(6);
			cell.setCellValue(item[7].toString()); // Folio Cliente
			cell.setCellStyle(styleCenter);

			cell = row.createCell(7);
			cell.setCellValue(item[8].toString()); // Divisa
			cell.setCellStyle(styleCenter);

			cell = row.createCell(8);
			cell.setCellValue(item[9].toString()); // Folio de operación
			cell.setCellStyle(styleCenter);

			cell = row.createCell(9);
			cell.setCellValue(item[10].toString()); // Forma de pago
			cell.setCellStyle(style);

			cell = row.createCell(10);
			cell.setCellValue(item[11] != null ? seguridadService.desencriptar(item[11].toString()) : null); // RFC
			cell.setCellStyle(style);
			
			cell = row.createCell(11);
			cell.setCellValue(item[12] != null ? item[12].toString() : null); // TX
			cell.setCellStyle(style);
			
			cell = row.createCell(12);
			cell.setCellValue(item[13] != null ? item[13].toString() : null); // Saldo Total Factura
			cell.setCellStyle(style);
			
			cell = row.createCell(13);
			cell.setCellValue(item[14] != null ? item[14].toString() : null); // Total Transacción
			cell.setCellStyle(style);
			
			cell = row.createCell(14);
			cell.setCellValue(item[15] != null ? item[15].toString() : null); // Saldo Anterior
			cell.setCellStyle(style);
			
			cell = row.createCell(15);
			cell.setCellValue(item[16].toString()); // Importe
			cell.setCellStyle(style);
			
			cell = row.createCell(16);
			cell.setCellValue(item[17] != null ? item[17].toString() : null); // Saldo Pendiente
			cell.setCellStyle(style);
			
			cell = row.createCell(17);
			cell.setCellValue(item[18] != null ? item[18].toString() : null); // UUID Relacionado
			cell.setCellStyle(style);
			
			cell = row.createCell(18);
			cell.setCellValue(item[19].toString()); // Estatus
			cell.setCellStyle(style);

			cell = row.createCell(19);
			cell.setCellValue(item[20].toString()); // Desc Estatus
			cell.setCellStyle(styleCenter);

		});		
			
		File fileLocation = new File(path + nombreArchivo);

		FileOutputStream outputStream;

		try {
			outputStream = new FileOutputStream(fileLocation);
			workbook.write(outputStream);
			workbook.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	@Override
	public String timbrarComplementoPago(Integer idPagoComplemento) {
		
		String result = "";
		Gson gson= new Gson();
        JsonObject clienteJson = new JsonObject();
        	
        ClientResponseTYPE<TimbrarComplemento> resp = wsftApiService.timbrarComplementoCFDI(idPagoComplemento);
    	RespuestaClient respuesta = resp.getRespuesta();
    	if (respuesta != null) {
    		clienteJson.addProperty("TimbrarComplemento", resp.getRespuesta().getCodigo());
    		if (respuesta.getCodigo().equals("1")) {
    			//this.complementoPagoRepository.actualizarEstatusTimbrado(idPagoComplemento);
    			clienteJson.addProperty("STATUS", "OK");
    		} else {
    			clienteJson.addProperty("STATUS", "ERROR");
    			clienteJson.addProperty("descripcion", respuesta.getDescripcion());
    		}
    		
    	} else {
    		clienteJson.addProperty("STATUS", "ERROR");
    	}
    	result = gson.toJson(clienteJson);
		return result;
	}

	@Override
	public ComplementosEntity getComplemento(String idComplemento) {
		return complementosRepository.findByTicket(idComplemento);
	}

	
}
