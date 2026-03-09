package com.sodimac.cfdi.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
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
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sodimac.cfdi.dto.vista.DtoFactura;
import com.sodimac.cfdi.entity.fiscal.FacturasEntity;
import com.sodimac.cfdi.entity.fiscal.FolioFacturaEntity;
import com.sodimac.cfdi.models.FacturaRelacionadaModel;
import com.sodimac.cfdi.models.FolioFacturaModel;
import com.sodimac.cfdi.models.ImpuestoFacturaModel;
import com.sodimac.cfdi.repository.fiscal.FolioFacturaRepository;
import com.sodimac.cfdi.util.enums.EPagoRelacionado;

@Service
public class FolioFacturaServiceImpl implements FolioFacturaService {

	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	private NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
	private DecimalFormat df = new DecimalFormat("000000");
	
	@Autowired
	private FolioFacturaRepository folioFacturaRepository;
	
	@Autowired
	private FacturasService facturasService;
	
	@Autowired
	private SeguridadService seguridadService;
	
	@Override
	public List<FolioFacturaModel> getFoliosFacturaPPDByParams(String fechaInicial, String fechaFinal, Integer pFolioFactura, String rfcEncriptado, int start, int rowsPerPage) {
		List<FolioFacturaModel> listFoliosFactura = new ArrayList<FolioFacturaModel>(0);
		List<Object[]> listFoliosFacturaObj = this.folioFacturaRepository.getFoliosFacturaPPDByParams(fechaInicial, fechaFinal, rfcEncriptado, pFolioFactura, start, rowsPerPage);
		if (listFoliosFacturaObj != null) {
			
			for (Object[] obj : listFoliosFacturaObj) {
				
				Double montoTotalFactura = (obj[6] != null ) ? Double.valueOf(obj[6].toString()) : null;
				Integer estatus = (obj[12] != null ) ? Integer.valueOf(obj[12].toString()) : 1;
				Integer facturaAsignada = (obj[14] != null ) ? Integer.valueOf(obj[14].toString()) : 0;
				String uuid = this.getValue(obj[3]);
				boolean relacionado = false;
				EPagoRelacionado pagoRel = null;
				Double montoTotalNeto = null;
				
				if (facturaAsignada.intValue() > 0) {
					estatus = EPagoRelacionado.ASIGNADO.getIdPagoRelacionado();
				}
				
				FolioFacturaModel folioFac = new FolioFacturaModel();
				folioFac.setDisabled("disabled");
				folioFac.setIdFolioFactura( (obj[0] != null ) ? Integer.valueOf(obj[0].toString()) : null );
				folioFac.setIdFactura( (obj[1] != null ) ? Integer.valueOf(obj[1].toString()) : null );
				folioFac.setRazonSocial( (obj[2] != null ) ? seguridadService.desencriptar(obj[2].toString()) : null);
				folioFac.setUuid( uuid );
				folioFac.setSerie(this.getValue(obj[4]));
				folioFac.setFolio( (obj[5] != null ) ? Integer.valueOf(obj[5].toString()) : null );
				folioFac.setMontoFactura( montoTotalFactura );
				folioFac.setMontoFacturaStr( (montoTotalFactura != null ) ? numberFormat.format(montoTotalFactura) : null );
				folioFac.setFechaRegistro( (obj[8] != null) ? simpleDateFormat.format((Date)obj[8]) : null );
				folioFac.setRfc( (obj[10] != null ) ? seguridadService.desencriptar(obj[10].toString()) : null);
				folioFac.setEstatus( estatus );
				folioFac.setFolioFactura( (obj[13] != null) ? df.format( Integer.valueOf(obj[13].toString())) : null );
				folioFac.setFacturaAsignada( facturaAsignada );
				folioFac.setOrden( (obj[15] != null ) ? Integer.valueOf(obj[15].toString()) : null );
				
				pagoRel = EPagoRelacionado.getPagoRelacionado(estatus.intValue());
				folioFac.setEstatusDescripcion(pagoRel.getDescripcion());
				
				if (pagoRel.equals( EPagoRelacionado.PENDIENTE_ASIGNACION ) || pagoRel.equals( EPagoRelacionado.ASIGNADO )) {
					folioFac.setChecked( true );
				}
				if (!pagoRel.equals( EPagoRelacionado.PENDIENTE_FOLIO )) {
					//folioFac.setDisabled("");
				}
				
				List<FacturaRelacionadaModel> listFacturasRel = this.getMontosUuiRelacionados(uuid);
				if (listFacturasRel != null && listFacturasRel.size() > 0) {
					FacturaRelacionadaModel facturaRelacionada = this.getMontoRealFactura(montoTotalFactura, listFacturasRel);
					if (facturaRelacionada != null) {
						montoTotalNeto = montoTotalFactura - facturaRelacionada.getMonto();
						relacionado = true;
					} else {
						montoTotalNeto = montoTotalFactura;
					}
				} else {
					montoTotalNeto = montoTotalFactura;
				}
				
				folioFac.setRelacionado(relacionado);
				folioFac.setMontoTotalNeto(montoTotalNeto );
				folioFac.setMontoTotalNetoStr( (montoTotalNeto != null ) ? numberFormat.format(montoTotalNeto) : null );
				
				listFoliosFactura.add(folioFac);
			}
		}
		return listFoliosFactura;
	}
	
	@Override
	public List<Object[]> getPagosComplementoFoliosFactura(Integer pIdPagoComplemento, Integer pIdFolioFactura) {
		return this.folioFacturaRepository.getPagosComplementoFoliosFactura(pIdPagoComplemento, pIdFolioFactura);
	}
	
	@Override
	public Double getSumaFoliosFactura(Integer pFolioFactura) {
		return this.folioFacturaRepository.getSumaFoliosFactura(pFolioFactura);
	}
	
	@Override
	public Double getSumaComplementoFoliosFactura(Integer pFolioFactura) {
		return this.folioFacturaRepository.getSumaComplementoFoliosFactura(pFolioFactura);
	}
	
	@Override
	public Integer getMaxFolioFactura() {
		return this.folioFacturaRepository.getMaxFolioFactura();
	}	
	
	@Override
	@Transactional
	public void registrarFolioFactura(List<DtoFactura> listFacturas, Integer folioFactura) {
		
		if (listFacturas != null && listFacturas.size() > 0) {
			Integer idFacturaInit = listFacturas.get(0).getIdFactura();
			this.folioFacturaRepository.registrarFolioFactura(idFacturaInit, folioFactura);
			Integer idFolioFactura = this.folioFacturaRepository.getIdFolioFacturaByFolioFactura(folioFactura);
		
			for (DtoFactura dtoFactura : listFacturas) {
				
				Integer idFactura = dtoFactura.getIdFactura();
			
				FacturasEntity factura = facturasService.getFacturaById(idFactura);
				String xml = seguridadService.desencriptar(factura.getXml());
				Document document = facturasService.obtenerDocumentXml(xml);
				FacturaRelacionadaModel facturaRelacionada = this.getMontoRealFactura(factura.getTotal(), factura.getUuid());
				
				Double montoRefactura = null;
				Double montoFactura = factura.getTotal();
				int notaCredito = 0;
				int orden = (dtoFactura.getOrden() != null) ? dtoFactura.getOrden() : 1;
				if (facturaRelacionada != null) {
					montoRefactura = facturaRelacionada.getMonto();
					notaCredito = 1;
					//orden = 1;
				}
				List<ImpuestoFacturaModel> listImpuesto = this.getImpuestos(document);
			
				this.folioFacturaRepository.registrarFolioFacturaDet(idFolioFactura, idFactura, montoFactura, notaCredito, montoRefactura, orden);
				for (ImpuestoFacturaModel imp : listImpuesto) { 
					BigDecimal importe = new BigDecimal(imp.getBase());
					importe = importe.multiply(new BigDecimal(imp.getTasa()));
					this.folioFacturaRepository.registrarFolioFacturaImpuesto(folioFactura, idFactura, imp.getBase(), imp.getTipoImpuesto(), imp.getTasa(), importe.toString());
				}
			}
		}
	}

	private String getValue(Object obj) {
		if (obj != null) {
			return obj.toString();
		}
		return null;
	}

	@Override
	public void desvincularFolio(Integer idFactura) {
		this.folioFacturaRepository.desvincularFolio(idFactura);
	}
	
	@Override
	public byte[] createExcel(String fechaInicial, String fechaFinal, Integer pFolioFactura, String rfcEncriptado) throws IOException {
		List<FolioFacturaModel> listFolios = this.getFoliosFacturaPPDByParams(fechaInicial, fechaFinal, pFolioFactura, rfcEncriptado, 0, 0);
		if (listFolios != null) {
			return this.getXlsx(listFolios);
		}
		return null;
	}
	
	private byte[] getXlsx(List<FolioFacturaModel> listFolios) {
		Workbook workbook = new XSSFWorkbook();

		Sheet sheet = workbook.createSheet("Folios Pagos");
		sheet.setColumnWidth(0, 5000);
		sheet.setColumnWidth(1, 10000);
		sheet.setColumnWidth(2, 5000);
		sheet.setColumnWidth(3, 10000);
		sheet.setColumnWidth(4, 5000);
		sheet.setColumnWidth(5, 5000);
		sheet.setColumnWidth(6, 5000);
		sheet.setColumnWidth(7, 5000);
		sheet.setColumnWidth(8, 10000);
		sheet.setColumnWidth(9, 10000);
		
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
		
		String[] columns ={ "Folio Factura", "UUID", "RFC", "Razon Social", "Serie", "Folio","Monto","Fecha Timbrado","Pago Relacionado" };
		
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
		for (FolioFacturaModel model : listFolios) {
			nxRow = nxRow + 1;
			Row row = sheet.createRow(nxRow);

			Cell cell = row.createCell(0);
			cell.setCellValue((model.getFolioFactura() != null) ? model.getFolioFactura() : ""); // Folio Factura
			cell.setCellStyle(style);

			cell = row.createCell(1);
			cell.setCellValue((model.getUuid() != null) ? model.getUuid() : ""); // UUID
			cell.setCellStyle(style);

			cell = row.createCell(2);
			cell.setCellValue((model.getRfc() != null) ? model.getRfc() : ""); // RFC
			cell.setCellStyle(style);

			cell = row.createCell(3);
			cell.setCellValue((model.getRazonSocial() != null) ? model.getRazonSocial() : ""); // Razon Social
			cell.setCellStyle(style);

			cell = row.createCell(4);
			cell.setCellValue((model.getSerie() != null) ? model.getSerie() : ""); // Serie
			cell.setCellStyle(style);
			

			cell = row.createCell(5);
			cell.setCellValue((model.getFolio() != null) ? String.valueOf(model.getFolio()) : ""); // Folio
			cell.setCellStyle(styleRight);

			cell = row.createCell(6);
			cell.setCellValue((model.getMontoFacturaStr() != null) ? model.getMontoFacturaStr() : ""); // Monto
			cell.setCellStyle(styleRight);

			cell = row.createCell(7);
			cell.setCellValue((model.getFechaRegistro() != null) ? model.getFechaRegistro() : ""); // Fecha Timbrado
			cell.setCellStyle(style);

			cell = row.createCell(8);
			cell.setCellValue((model.getEstatusDescripcion() != null) ? model.getEstatusDescripcion() : "" ); // Estatus
			cell.setCellStyle(style);
		}
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			workbook.write(outputStream);
			workbook.close();
			return outputStream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    return null;
	}
	
	@Override
	public Optional<FolioFacturaEntity> getIdFolioFactura(Integer idfolioFactura) {
		return this.folioFacturaRepository.findById(idfolioFactura);
	}

	private List<ImpuestoFacturaModel> getImpuestos(Document document) {
		List<ImpuestoFacturaModel> listImpuesto = new ArrayList<ImpuestoFacturaModel>();
		NodeList nodeImpuestos = document.getElementsByTagName("cfdi:Impuestos");
	    for (int i = 0; i < nodeImpuestos.getLength(); i++) {
	        
	        Node currentImpuesto = nodeImpuestos.item(i);
	        Element eElement = (Element) currentImpuesto;
	        boolean TotalImpuestosTrasladados = StringUtils.isEmpty(eElement.getAttribute("TotalImpuestosTrasladados"));
	        if (!TotalImpuestosTrasladados) {
	        	
	        	NodeList listImpuestos = currentImpuesto.getChildNodes();
	    	    for (int j = 0; j < listImpuestos.getLength(); j++) {
	    	        Node nodeTraslados = listImpuestos.item(j);
	    	        
	    	        if (nodeTraslados.getNodeType() == Node.ELEMENT_NODE) {
	    	            NodeList listTraslados = nodeTraslados.getChildNodes();
	    	        	for (int k = 0; k< listTraslados.getLength(); k++) {
	    	        		Node traslado = listTraslados.item(k);
	    	        		if (nodeTraslados.getNodeType() == Node.ELEMENT_NODE) {
			    	            Element eElementTraslado = (Element) traslado;
			    	            
			    	            String base = eElementTraslado.getAttribute("Base");
			    	            String tipoImpuesto = eElementTraslado.getAttribute("Impuesto");
			    	            String tasa = eElementTraslado.getAttribute("TasaOCuota");
			    	            String importe = eElementTraslado.getAttribute("Importe");
								
								ImpuestoFacturaModel imp = new ImpuestoFacturaModel();
								imp.setBase(base);
								imp.setTipoImpuesto(tipoImpuesto);
								imp.setTasa(tasa);
								imp.setImporte(importe);
								
								listImpuesto.add( imp );
	    	        		}
	    	        	}//for (int k = 0; k< listTraslados.getLength(); k++)
	    	        }
	    	        
	    	    }//for (int j = 0; j < listImpuestos.getLength(); j++)
	        }
	    }//for (int i = 0; i < nodeImpuestos.getLength(); i++)
		return listImpuesto;
	}

	@Override
	public void guardar(FolioFacturaEntity entity) {
		this.folioFacturaRepository.saveAndFlush(entity);
	}
	
	@Override
	public List<FacturaRelacionadaModel> getMontoUuiRelacionado(String uuidRelacionado) {
		List<FacturaRelacionadaModel> list = new ArrayList<>();
		List<Object[]> listObject = this.folioFacturaRepository.getMontoUuiRelacionado(uuidRelacionado);
		if (listObject != null && listObject.size() > 0) {
			for (Object[] arrObject : listObject) {
				//sObject[] arrObject = listObject.get(0);
				
				FacturaRelacionadaModel facturaRelacionada = new FacturaRelacionadaModel();
				String uuidRelacionadoRef = (arrObject[2] != null) ? arrObject[2].toString() : null;
				
				facturaRelacionada.setTicket( (arrObject[0] != null) ? arrObject[0].toString() : null );
				facturaRelacionada.setMonto( (arrObject[1] != null) ? Double.valueOf(arrObject[1].toString()) : null );
				facturaRelacionada.setMontoStr( (arrObject[1] != null) ? numberFormat.format( Double.valueOf(arrObject[1].toString())) : null );
				facturaRelacionada.setNotaCredito(true);
				facturaRelacionada.setUuidRelacionado( uuidRelacionadoRef );
				list.add(facturaRelacionada);
			}
		}
		return list;
	}
	
	@Override
	public List<FacturaRelacionadaModel> getMontosUuiRelacionados(String uuidRelacionado) {
		List<FacturaRelacionadaModel> listFacturasRelacionadas = new ArrayList<FacturaRelacionadaModel>();
		int i = 0;
		while(uuidRelacionado != null && i<10){
			List<FacturaRelacionadaModel> facturaRelacionadas = this.getMontoUuiRelacionado(uuidRelacionado);
			if (facturaRelacionadas != null ) {
				
				for (FacturaRelacionadaModel facturaRelacionada : facturaRelacionadas) {
					
					listFacturasRelacionadas.add(facturaRelacionada);
					if( facturaRelacionada.getUuidRelacionado() != null 
							&& !facturaRelacionada.getUuidRelacionado().trim().isEmpty()) {
						uuidRelacionado = facturaRelacionada.getUuidRelacionado().trim();
					}
				}
			} else {
				uuidRelacionado = null;
			}
			i++;
		}
		return listFacturasRelacionadas;
	}
	
	@Override
	public FacturaRelacionadaModel getMontoRealFactura(Double montoFactura, String uuid) {
		List<FacturaRelacionadaModel> listFacturasRel = this.getMontosUuiRelacionados(uuid);
		return this.getMontoRealFactura(montoFactura, listFacturasRel);
	}
	
	@Override
	public FacturaRelacionadaModel getMontoRealFactura(Double montoFactura, List<FacturaRelacionadaModel> listFacturasRel) {
		Double montoRefacturas = 0.0;
		FacturaRelacionadaModel facturaRelacionada = null; 
		if (listFacturasRel != null && listFacturasRel.size() > 0) {
			facturaRelacionada = new FacturaRelacionadaModel();
			facturaRelacionada.setNotaCredito(true);
			for (FacturaRelacionadaModel factura : listFacturasRel) {
				montoRefacturas = montoRefacturas + (factura.getMonto() != null ? factura.getMonto() : 0.0);
			}
			
			facturaRelacionada.setMonto(montoRefacturas);
		}
		return facturaRelacionada;
	}
}
