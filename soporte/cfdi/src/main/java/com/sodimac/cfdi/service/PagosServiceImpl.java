package com.sodimac.cfdi.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import com.sodimac.cfdi.entity.fiscal.PagosEntity;
import com.sodimac.cfdi.models.EstatusPagoModelItem;
import com.sodimac.cfdi.models.PagosModel;
import com.sodimac.cfdi.repository.fiscal.PagosRepository;

@Service
public class PagosServiceImpl implements PagosService {

	private static final int FORMA_PAGO_SPEI = 3;
	
	@Autowired
	private PagosRepository pagosRepository;
	
	@Autowired
	private ComplementosService complementosService;
	
	@Autowired
	private CatConfiguracionService catConfiguracionService;
	
	int nxRow = 0;
	
	@Transactional(isolation = Isolation.READ_UNCOMMITTED)
	@Override
	public List<PagosEntity> getAllPagos() {
		
		return pagosRepository.findAll();
	}
	
	@Transactional(isolation = Isolation.READ_UNCOMMITTED)
	@Override
	public String getStatusPagos(String tipoPago) {
		
		List<EstatusPagoModelItem> list = new ArrayList<EstatusPagoModelItem>();
		
		pagosRepository.findEstatusByTipoPago(tipoPago).forEach(item -> {
			EstatusPagoModelItem itemList = new EstatusPagoModelItem();
			itemList.setIdEstatus(item[0].toString());
			itemList.setDescripcionEstatus(item[1].toString());
			list.add(itemList);			
		});
		Gson gson= new Gson();
		String resultado = gson.toJson(list);
		return resultado;
	}

	@Transactional(isolation = Isolation.READ_UNCOMMITTED)
	@Override
	public List<PagosModel> getPagosByParams(String fechaInicial, String fechaFinal, int start, int rowsPerPage, String estatusPago, Double pmonto) {
		List<PagosModel> list = new ArrayList<PagosModel>();
		//fechaInicial = "2022-08-02";
		//fechaFinal = "2022-08-30";
		pagosRepository.getPagosByParams(fechaInicial, fechaFinal, start, rowsPerPage, estatusPago, pmonto).forEach(item -> {
			PagosModel itemList = new PagosModel();
			String formaPago = item[10].toString();
			itemList.setIdPago(item[0].toString());
			itemList.setNumeroCuenta(item[1].toString());
			itemList.setFechaHoraMovimiento(item[2].toString());
			itemList.setConcepto(item[3].toString());
			itemList.setImporte(item[4].toString());
			itemList.setFolioBanco(item[5].toString());
			itemList.setRefInterbancaria(item[6].toString());
			itemList.setFolioCliente(item[7].toString());
			itemList.setTipoDivisa(item[8].toString());
			itemList.setFolioOperacion(item[9].toString());
			itemList.setFormaPago(formaPago);
			itemList.setSpei( (Integer.valueOf(formaPago).intValue() == FORMA_PAGO_SPEI) );
			itemList.setEstatus(item[11].toString());
			itemList.setDescEstatus(item[12].toString());
			list.add(itemList);
		});
		
		return list;
	}

	
	@Override
	@Transactional(transactionManager = "transactionManagerFiscal")
	public String cambiarEstatusPago(int idPago, String estatusPago) {
		String mensaje = "";
		System.out.println("Inicia servicio");
		try {
			System.out.println("Busca Pago");
			Optional<PagosEntity> optionalPago = pagosRepository.findById(idPago);
			System.out.println("Busca estatus");
			int findEstatus = pagosRepository.findEstatusByIdEstatusPago(estatusPago);
			
			if(optionalPago.isPresent()) {
				if(findEstatus == 1) {
					System.out.println("Actualizando estatus");
					PagosEntity pago = optionalPago.get();
					pago.setEstatus(estatusPago);
					//pago.setFechaModificacion(new java.util.Date());
					pagosRepository.saveAndFlush(pago);		
					System.out.println("Actualizacion OK");
					
					if (estatusPago.trim().equalsIgnoreCase("A")) {
						System.out.println("Guardando en coplemento");
						mensaje = complementosService.altaComplemento(pago);	
					}
				} else {
					mensaje = "El estatus de pago no es valido.";
				}				
			} else {
				mensaje = "No se encontro el pago que se intenta liberar.";
			}
		} catch (Exception e) {
			e.printStackTrace();
			mensaje = "Ocurrio un problema al intentar liberar el pago.";
		}
		System.out.println(mensaje);
		return mensaje;
	}
	
	@Override
	@Transactional(transactionManager = "transactionManagerFiscal")
	public String cambiarFolioCliente(int idPago, String folioCliente) {
		String mensaje = "";
		System.out.println("Inicia servicio");
		try {
			System.out.println("Busca Pago");
			Optional<PagosEntity> optionalPago = pagosRepository.findById(idPago);
			
			if(optionalPago.isPresent()) {
				
				PagosEntity pago = optionalPago.get();
				pago.setFolioCliente(folioCliente);
				pago.setFechaModificacion(new java.util.Date());
				pagosRepository.saveAndFlush(pago);		
				System.out.println("Actualizacion OK");
							
			} else {
				mensaje = "No se encontro el pago que intenta modificar el numero de cuenta.";
			}
		} catch (Exception e) {
			e.printStackTrace();
			mensaje = "Ocurrio un problema al intentar modificar el numero de cuenta";
		}
		System.out.println(mensaje);
		return mensaje;
	}

	@Transactional(isolation = Isolation.READ_UNCOMMITTED)
	@Override
	public boolean getPagosExcelFechas(String fechaInicial, String fechaFinal, String nombreArchivo, String estatusPago, Double pmonto) {

		String path = catConfiguracionService.findParameterByKey("Mail.PathFile");
		
		Workbook workbook = new XSSFWorkbook();

		Sheet sheet = workbook.createSheet("Pagos");
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
		sheet.setColumnWidth(11, 10000);
		
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

		String[] columns = {"Numero de cuenta","Fecha movimiento","Concepto","Importe","Folio Bancario","Referencia Interbancaria","Folio Cliente","Divisa","Folio de operación","Forma de pago","Estatus","Desc Estatus"};
		
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

		pagosRepository.getPagosExcelByParams(fechaInicial, fechaFinal, estatusPago, pmonto).forEach(item -> {

			nxRow = nxRow + 1;
			Row row = sheet.createRow(nxRow);

			Cell cell = row.createCell(0);
			cell.setCellValue(item[1].toString()); // Numero de cuenta
			cell.setCellStyle(style);

			cell = row.createCell(1);
			cell.setCellValue(item[2].toString()); // Fecha movimiento
			cell.setCellStyle(style);

			cell = row.createCell(2);
			cell.setCellValue(item[3].toString()); // Concepto
			cell.setCellStyle(style);

			cell = row.createCell(3);
			cell.setCellValue(item[4].toString()); // Importe
			cell.setCellStyle(style);

			cell = row.createCell(4);
			cell.setCellValue(item[5].toString()); // Folio Bancario
			cell.setCellStyle(styleCenter);
			

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
			cell.setCellValue(item[11].toString()); // Estatus
			cell.setCellStyle(style);

			cell = row.createCell(11);
			cell.setCellValue(item[12].toString()); // Desc Estatus
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

}
