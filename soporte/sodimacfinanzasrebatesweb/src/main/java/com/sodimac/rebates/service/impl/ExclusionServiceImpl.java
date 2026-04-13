package com.sodimac.rebates.service.impl;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.apache.logging.log4j.util.Strings;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.monitorjbl.xlsx.StreamingReader;
import com.sodimac.rebates.dto.ExclusionCargaDto;
import com.sodimac.rebates.dto.ExclusionDto;
import com.sodimac.rebates.dto.ExclusionViewDetDto;
import com.sodimac.rebates.dto.PeriodoDto;
import com.sodimac.rebates.enums.EEstatusExclusion;
import com.sodimac.rebates.enums.EMensaje;
import com.sodimac.rebates.enums.ETipoExclusion;
import com.sodimac.rebates.filter.ExclusionFilter;
import com.sodimac.rebates.mapper.ExclusionMapper;
import com.sodimac.rebates.mapper.ExclusionViewDetMapper;
import com.sodimac.rebates.model.DocumentoValidadorModel;
import com.sodimac.rebates.model.entity.CatEstatusExclusionEntity;
import com.sodimac.rebates.model.entity.ExclusionEntity;
import com.sodimac.rebates.repository.ExclusionRepository;
import com.sodimac.rebates.service.ICatMensajeService;
import com.sodimac.rebates.service.IExclusionCargaDetService;
import com.sodimac.rebates.service.IExclusionCargaService;
import com.sodimac.rebates.service.IExclusionService;
import com.sodimac.rebates.service.IPeriodoService;
import com.sodimac.rebates.service.IRebatesCicmxOcService;
import com.sodimac.rebates.util.MemoryUtil;

@Service
public class ExclusionServiceImpl implements IExclusionService {

	private static Logger logger = LoggerFactory.getLogger(ExclusionServiceImpl.class);
	private DecimalFormat dfCarga = new DecimalFormat("#########");
	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	static final Integer FILL_RATE = 8;
	
	@Autowired
    private EntityManager em;
	
	@Autowired
	private ExclusionRepository exclusionRepository;
	
	@Autowired
	private IExclusionCargaService exclusionCargaService;
	
	@Autowired
	private IExclusionCargaDetService exclusionCargaDetService;
	
	@Autowired
	private ICatMensajeService catMensajeService;
	
	@Autowired
	private IRebatesCicmxOcService iRebatesCicmxOcService;
	
	@Autowired
	private IPeriodoService servicePeriodo;
	
	@Override
	public List<ExclusionDto> getExclusiones(ExclusionFilter filter) {
		List<ExclusionEntity> list = exclusionRepository.getExclusiones(getValue(filter.getIdUsuario()), 
																		getValue(filter.getFolio()), 
																		getValue(filter.getComentario()), 
																		getValue(filter.getIdPeriodo()), 
																		getValue(filter.getIdTipoExclusion()), 
																		getValue(filter.getNumProveedor()), 
																		getValue(filter.getOrdenCompra()));		
		List<ExclusionDto> listDtos = ExclusionMapper.convertDtos(list);
		
		List<Integer> listaUsuarios = listDtos.stream().distinct().map(x -> x.getUsuarioSolicitud().getId()).collect(Collectors.toList());
		listaUsuarios.forEach(x -> {
			Integer soy = exclusionRepository.getJefeComprador(filter.getIdUsuario(), x);
			if (soy != null) listDtos.stream().filter(y -> y.getUsuarioSolicitud().getId() == x).forEach(y ->{y.setSoyHijo(1);});
			});

		return listDtos;
	}
	
	private String getValue(String param) {
		if(Strings.isBlank(param)) {
			return null;
		} else {
			return param;
		}
	}
	
	private Integer getValue(Integer param) {
		if(param == null || param.intValue() == 0 ) {
			return null;
		} else {
			return param;
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<ExclusionViewDetDto> getExclusionesDet(ExclusionFilter filter) {
		List<ExclusionViewDetDto> listDto = null;
		StringBuilder sbQry = new StringBuilder();
		sbQry.append("SELECT [IdCatPeriodo] \r\n")				//-->	0
			 .append("      ,[DetallePeriodo] \r\n")			//-->	1
			 .append("      ,[PeriodoFechaIni] \r\n")			//-->	2
			 .append("      ,[PeriodoFechaFin] \r\n")			//-->	3
			 .append("      ,[IdExclusion] \r\n")				//-->	4
			 .append("      ,[IdCatTipoRebate] \r\n")			//-->	5
			 .append("      ,[DescripcionRebate] \r\n")			//-->	6
			 .append("      ,[IdCatTipoExclusion] \r\n")		//-->	7
			 .append("      ,[DescripcionExclusion] \r\n")		//-->	8
			 .append("      ,[IdCatEstatusExclusion] \r\n")		//-->	9
			 .append("      ,[Folio] \r\n")						//-->	10
			 .append("      ,[Contabilizado] \r\n")				//-->	11
			 .append("      ,[Comentario] \r\n")				//-->	12
			 .append("      ,[IdExclusionCarga] \r\n")			//-->	13
			 .append("      ,[IdExclusionCargaDet] \r\n")		//-->	14
			 .append("      ,[Motivo] \r\n")					//-->	15
			 .append("      ,[NumProveedor] \r\n")				//-->	16
			 .append("      ,[NomProveedor] \r\n")				//-->	17
			 .append("      ,[OrdenCompra] \r\n")				//-->	18
			 .append("      ,[Clacom] \r\n")					//-->	19
			 .append("      ,[Sku] \r\n")						//-->	20
			 .append("      ,[SkuDescripcion] \r\n")			//-->	21
			 .append("      ,[Activo] \r\n")					//-->	22
			 .append("      ,[PeriodoVigente] \r\n")			//-->	23
			 .append("      ,[TieneAcuerdo] \r\n")		  		//-->	24
			 .append("  FROM [vw_exclusiones] \r\n")
			 .append("  WHERE 1=1 \r\n");
		
		if (filter.getFolio() != null && !filter.getFolio().isEmpty()) {
			sbQry.append("  AND   Folio like '%" + filter.getFolio() + "%' \r\n");
		}
		if (filter.getNumProveedor() != null && !filter.getNumProveedor().isEmpty()) {
			sbQry.append("  AND   NumProveedor = '" +  filter.getNumProveedor() + "' \r\n");
		}
		if (filter.getComentario() != null && !filter.getComentario().isEmpty()) {
			sbQry.append("  AND   Comentario like '%" +  filter.getComentario() + "%' \r\n");
		}
		if (filter.getIdPeriodo() != null && filter.getIdPeriodo().intValue() > 0) {
			sbQry.append("  AND   IdCatPeriodo = " + filter.getIdPeriodo() + " \r\n");
		}
		if (filter.getIdTipoExclusion() != null && filter.getIdTipoExclusion().intValue() > 0) {
			sbQry.append("  AND   IdCatTipoExclusion = " + filter.getIdTipoExclusion() + " \r\n");
		}
		sbQry.append("  ORDER BY IdExclusion desc, IdExclusionCarga, IdExclusionCargaDet");
		List<Object[]> resultList = this.em.createNativeQuery(sbQry.toString()).getResultList();
		listDto = ExclusionViewDetMapper.convertDtos(resultList);
		return listDto;
	}
	
	@Override
	public ExclusionDto getExclusion(Integer idExclusion) {
		ExclusionEntity entity = this.exclusionRepository.findByIdExclusion(idExclusion);
		ExclusionDto dto = ExclusionMapper.convertDto(entity);
		List<ExclusionCargaDto> listDetalle = null;
		try {
			if (entity.getCatTipoRebate().getIdCatTipoRebate().equals(FILL_RATE)) {
				listDetalle = this.exclusionCargaService.getExclusionCargaFill(idExclusion);
			} else {
				listDetalle = this.exclusionCargaService.getExclusionCarga(idExclusion);	
			}
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dto.setListExclusiones(listDetalle);
		return dto;
	}	
	
	@Override
	public ExclusionDto getExclusion(Integer idExclusion, String proveedor) {
		ExclusionEntity entity = this.exclusionRepository.findByIdExclusion(idExclusion);
		ExclusionDto dto = ExclusionMapper.convertDto(entity);
		List<ExclusionCargaDto> listDetalle = null;
		try {
			if (proveedor == null || proveedor.isEmpty()) {
				if (entity.getCatTipoRebate().getIdCatTipoRebate().equals(FILL_RATE)) {
					listDetalle = this.exclusionCargaService.getExclusionCargaFill(idExclusion);
				} else {
					listDetalle = this.exclusionCargaService.getExclusionCarga(idExclusion);	
				}

			} else {
				if (entity.getCatTipoRebate().getIdCatTipoRebate().equals(FILL_RATE)) {
					listDetalle = this.exclusionCargaService.getExclusionCargaFill(idExclusion, proveedor);
				} else {
					listDetalle = this.exclusionCargaService.getExclusionCarga(idExclusion, proveedor);
				}
				
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		dto.setListExclusiones(listDetalle);
		return dto;
	}	

	@Override
	public ExclusionDto getEvidenciaExclusion(Integer idExclusion) {
		ExclusionEntity entity = this.exclusionRepository.findByIdExclusion(idExclusion);
		ExclusionDto dto = ExclusionMapper.convertDtoImagen(entity);
		return dto;
	}
	
	@Override
	public Integer getMaxFolio() {
		return this.exclusionRepository.getMaxFolio();
	}
	
	@Override
	@Transactional
	public void guardar(ExclusionDto exclusion, Integer idUsuario) {
		
		ExclusionEntity entity = ExclusionMapper.convertEntity(exclusion);
		this.exclusionRepository.save(entity);
		Integer idExclusion = entity.getIdExclusion();
		
		exclusion.setIdExclusion(idExclusion);
		logger.info("Exclusion guardada: " + idExclusion);
		List<ExclusionCargaDto> listExclusiones = exclusion.getListExclusiones();
		
		//Carga por pantalla
		if (exclusion.getExclusion() != null && !exclusion.getExclusion().isEmpty()) {
			 String exclusionCarga = exclusion.getExclusion();
			 ExclusionCargaDto cargaDto = new ExclusionCargaDto();
			 cargaDto.setCarga(exclusionCarga);
			 cargaDto.setMotivo(exclusion.getComentario());
			 cargaDto.setNumProveedor( exclusion.getNumeroProveedor() );
			 this.guardarExclusion(exclusion, cargaDto, idUsuario);
		}
		
		//Carga por plantilla
		if (listExclusiones != null) {
			for (ExclusionCargaDto cargaDto : listExclusiones) {
				this.guardarExclusion(exclusion, cargaDto, idUsuario);
			}
		}
	}
	
	private void guardarExclusion(ExclusionDto exclusion, ExclusionCargaDto cargaDto, Integer idUsuario) {
		Integer idExclusion = exclusion.getIdExclusion();
		Integer idPeriodo = exclusion.getPeriodo().getIdCatPeriodo();
		Integer idCatTipoRebate =  exclusion.getCatTipoRebate().getIdCatTipoRebate();
		
		cargaDto.setIdExclusion( idExclusion );
		cargaDto.setActivo(true);
		cargaDto.setFechaRegistro(new Date());
		this.exclusionCargaService.guardar(cargaDto); 
		
		Long idExclusionCarga = cargaDto.getIdExclusionCarga();
		if(exclusion.getCatTipoExclusion().getIdCatTipoExclusion() == ETipoExclusion.PROVEEDORES.getId()) {
			this.exclusionCargaDetService.registraExclusionProveedor(idExclusionCarga, cargaDto.getCarga(), idPeriodo, idCatTipoRebate, idUsuario);
		}
		else if(exclusion.getCatTipoExclusion().getIdCatTipoExclusion() == ETipoExclusion.ORDEN_COMPRA.getId()) {
			this.exclusionCargaDetService.registraExclusionOrdenCompra(idExclusionCarga, cargaDto.getCarga(), idPeriodo, idCatTipoRebate, idUsuario);
		}
		else if(exclusion.getCatTipoExclusion().getIdCatTipoExclusion() == ETipoExclusion.FAMILIA.getId()) {
			this.exclusionCargaDetService.registraExclusionFamilia(idExclusionCarga, cargaDto.getNumProveedor(), cargaDto.getCarga(), idPeriodo, idCatTipoRebate, idUsuario);
		} 
		else if(exclusion.getCatTipoExclusion().getIdCatTipoExclusion() == ETipoExclusion.SKU.getId()) {
			this.exclusionCargaDetService.registraExclusionSKU(idExclusionCarga, cargaDto.getNumProveedor(), cargaDto.getCarga(), idPeriodo, idCatTipoRebate, idUsuario);
		}
	}
	
	@Override
	@Transactional
	public void borradoLogico(Integer idExclusion) {
		ExclusionEntity entity = this.exclusionRepository.findByIdExclusion(idExclusion);
		entity.setActivo(false);
		this.exclusionRepository.save(entity);
	}
	
	@Override
	@Transactional
	public void inactivar(Integer idExclusion) {
		ExclusionEntity entity = this.exclusionRepository.findByIdExclusion(idExclusion);
		CatEstatusExclusionEntity catEstatus = new CatEstatusExclusionEntity();
		catEstatus.setIdCatEstatusExclusion( EEstatusExclusion.INACTIVA.getIdEstatus() );
		entity.setCatEstatusExclusion(catEstatus);
		this.exclusionRepository.save(entity);
	}
	
	@Override
	@Transactional
	public void autorizar(Integer idExclusion, Integer idUser) {
		this.exclusionRepository.autorizarExclusion(idExclusion, idUser);
	}
	
	@Override
	@Transactional
	public void rechazar(Integer idExclusion, Integer idUser) {
		this.exclusionRepository.rechazarExclusion(idExclusion, idUser);
	}
	
	@Override
	@SuppressWarnings({ "deprecation", "incomplete-switch" })
	public DocumentoValidadorModel<ExclusionCargaDto> leerExcelProveedor(MultipartFile multiPart) {
		DocumentoValidadorModel<ExclusionCargaDto> response = new DocumentoValidadorModel<ExclusionCargaDto>();
		response.setStatus("OK");
		logger.info("Comienza procesamiento de archivo EXCEL");
		
		String nameFile = multiPart.getOriginalFilename();
		System.out.println(nameFile);
		logger.info(nameFile);
		Workbook workbook = null;
		
		try {
			logger.info("Creando objeto: Workbook workbook = new XSSFWorkbook(multiPart.getInputStream());");
			MemoryUtil.showMemoryStats();
			//Workbook workbook = new XSSFWorkbook(multiPart.getInputStream());
			workbook = StreamingReader.builder().rowCacheSize(100).bufferSize(8192).open(multiPart.getInputStream());
			logger.info("Crear objeto: " + workbook);
			MemoryUtil.showMemoryStats();
			
			Integer filaComienza = 0;
			Sheet sheet = workbook.getSheetAt(0);
			Row rowHeader = null;	
			
			for(Row r : sheet) {
				if (r.getRowNum() == filaComienza.intValue()) {
					rowHeader = r;
					logger.info( ":" + rowHeader.getRowNum());
					break;
				}
			}
		
			int numRows = sheet.getLastRowNum();
			logger.info("Total de registros:" + numRows);

            // Validamos que el archivo no este vacio
            if (numRows == 0) {
            	logger.info("El archivo esta vacio");
                System.out.println("El archivo esta vacio");
                
                String mensaje = this.catMensajeService.getMensaje(EMensaje.ARCHIVO_VACIO);
                response.setStatus("ERROR");
        		response.setMessage(mensaje);
        		return response;
            }
            if (numRows < 1) { // Que tenga más de dos filas
            	logger.info("Falta informacion");
            	System.out.println("Falta informacion");
            	
            	String mensaje = this.catMensajeService.getMensaje(EMensaje.INFORMACION_INCOMPLETA);
            	response.setStatus("ERROR");
        		response.setMessage(mensaje);
        		return response;
            }
            
            
            String[] headerConfig = {"Proveedor","Tipo","Motivo"};
            String[] headerExcel = new String[rowHeader.getLastCellNum()];
            int x=0;
            if (rowHeader != null) {
	        	for (Cell cell : rowHeader) {
	        		switch (cell.getCellTypeEnum()) {
	                    case STRING: headerExcel[x] = cell.getStringCellValue(); break;
	                    case NUMERIC: break;
	                    case BOOLEAN: break;
	                    case FORMULA: break;
	        		}
	        		x ++;
	        	}
            }
            
            logger.info("Validando cabecera");
            // Validamos el header
            if (!validHeader(headerExcel, headerConfig)) {
            	logger.info("La cabecera es inválida");
            	System.out.println("La cabecera es inválida");
            	
            	String mensaje = this.catMensajeService.getMensaje(EMensaje.CABECERA_INCORRECTA);
            	response.setStatus("ERROR");
        		response.setMessage(mensaje);
        		return response;
            } else {
            	System.out.println("Header OK");	
            }
            
            Double bytes = multiPart.getSize() * 1.0;
            Double kilobytes = (bytes / 1024);
            Double megabytes = (kilobytes / 1024);
            
            response.setTotalRegistros( numRows );
            response.setPeso( megabytes ) ;
            
            List<ExclusionCargaDto> listDetExclusion = new ArrayList<>();
            for (Row row : sheet) {
            	logger.info("row:" + row.getRowNum());
            	if (row.getRowNum() > filaComienza) {
	            	ExclusionCargaDto exclusionDet = new ExclusionCargaDto();
	            	
	            	String proveedor = null;
	            	String carga = null;
	            	
	            	for (Cell cell : row) {
	                	int columnIndex = cell.getColumnIndex();
	                	CellType cellTypeEnum = cell.getCellTypeEnum(); 
	                	switch (columnIndex) {
	                    case 0:
	                    	if(cellTypeEnum == CellType.STRING) {
	                    		proveedor = cell.getStringCellValue();
	                    	} else if(cellTypeEnum == CellType.NUMERIC) {
	                    		Double numericCellValue = cell.getNumericCellValue();
	                    		if (numericCellValue != null) {
	                    			proveedor = dfCarga.format(numericCellValue);
	                    		}
	                    	}
	                        break;
	                    case 1:
	                    	if(cellTypeEnum == CellType.STRING) {
	                    		carga = cell.getStringCellValue();
	                    	} else if(cellTypeEnum == CellType.NUMERIC) {
	                    		Double numericCellValue = cell.getNumericCellValue();
	                    		if (numericCellValue != null) {
	                    			carga = dfCarga.format(numericCellValue);
	                    		}
	                    	}
	                        break;
	                    case 2:
	                    	exclusionDet.setMotivo( cell.getStringCellValue() );
	                        break;
	                	}
	                }
	                
	            	exclusionDet.setNumProveedor(proveedor);
	                exclusionDet.setCarga( carga );
	                listDetExclusion.add(exclusionDet);
	                
            	}//if (row.getRowNum() > filaComienza)
            }//for (Row row : sheet)
            
            response.setRegistros(listDetExclusion);
            
		} catch (Exception e) {
            e.printStackTrace();
            logger.error("Error inesperado", e);
            
            String mensaje = this.catMensajeService.getMensaje(EMensaje.ERROR_PROCESAR_ARCHIVO);
            response.setStatus("ERROR");
    		response.setMessage(mensaje);
    		return response;
        }   finally {
        	if (workbook != null) {
        		try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
		return response;
	}
	
	@Override
	@SuppressWarnings({ "deprecation", "incomplete-switch" })
	public DocumentoValidadorModel<ExclusionCargaDto> leerExcel(MultipartFile multiPart) {
		DocumentoValidadorModel<ExclusionCargaDto> response = new DocumentoValidadorModel<ExclusionCargaDto>();
		response.setStatus("OK");
		logger.info("Comienza procesamiento de archivo EXCEL");
		
		String nameFile = multiPart.getOriginalFilename();
		System.out.println(nameFile);
		logger.info(nameFile);
		Workbook workbook = null;
		
		try {
			logger.info("Creando objeto: Workbook workbook = new XSSFWorkbook(multiPart.getInputStream());");
			MemoryUtil.showMemoryStats();
			//Workbook workbook = new XSSFWorkbook(multiPart.getInputStream());
			workbook = StreamingReader.builder().rowCacheSize(100).bufferSize(8192).open(multiPart.getInputStream());
			logger.info("Crear objeto: " + workbook);
			MemoryUtil.showMemoryStats();
			
			Integer filaComienza = 0;
			Sheet sheet = workbook.getSheetAt(0);
			Row rowHeader = null;	
			
			for(Row r : sheet) {
				if (r.getRowNum() == filaComienza.intValue()) {
					rowHeader = r;
					logger.info( ":" + rowHeader.getRowNum());
					break;
				}
			}
		
			int numRows = sheet.getLastRowNum();
			logger.info("Total de registros:" + numRows);

            // Validamos que el archivo no este vacio
            if (numRows == 0) {
            	logger.info("El archivo esta vacio");
                System.out.println("El archivo esta vacio");
                
                String mensaje = this.catMensajeService.getMensaje(EMensaje.ARCHIVO_VACIO);
                response.setStatus("ERROR");
        		response.setMessage(mensaje);
        		return response;
            }
            if (numRows < 1) { // Que tenga más de dos filas
            	logger.info("Falta informacion");
            	System.out.println("Falta informacion");
            	
            	String mensaje = this.catMensajeService.getMensaje(EMensaje.INFORMACION_INCOMPLETA);
            	response.setStatus("ERROR");
        		response.setMessage(mensaje);
        		return response;
            }
            
            
            String[] headerConfig = {"Tipo","Motivo"};
            String[] headerExcel = new String[rowHeader.getLastCellNum()];
            int x=0;
            if (rowHeader != null) {
	        	for (Cell cell : rowHeader) {
	        		switch (cell.getCellTypeEnum()) {
	                    case STRING: headerExcel[x] = cell.getStringCellValue(); break;
	                    case NUMERIC: break;
	                    case BOOLEAN: break;
	                    case FORMULA: break;
	        		}
	        		x ++;
	        	}
            }
            
            logger.info("Validando cabecera");
            // Validamos el header
            if (!validHeader(headerExcel, headerConfig)) {
            	logger.info("La cabecera es inválida");
            	System.out.println("La cabecera es inválida");
            	
            	String mensaje = this.catMensajeService.getMensaje(EMensaje.CABECERA_INCORRECTA);
            	response.setStatus("ERROR");
        		response.setMessage(mensaje);
        		return response;
            } else {
            	System.out.println("Header OK");	
            }
            
            Double bytes = multiPart.getSize() * 1.0;
            Double kilobytes = (bytes / 1024);
            Double megabytes = (kilobytes / 1024);
            
            response.setTotalRegistros( numRows );
            response.setPeso( megabytes ) ;
            
            List<ExclusionCargaDto> listDetExclusion = new ArrayList<>();
            for (Row row : sheet) {
            	logger.info("row:" + row.getRowNum());
            	if (row.getRowNum() > filaComienza) {
	            	ExclusionCargaDto exclusionDet = new ExclusionCargaDto();
	            	String carga = null;
	            	for (Cell cell : row) {
	                	int columnIndex = cell.getColumnIndex();
	                	CellType cellTypeEnum = cell.getCellTypeEnum(); 
	                	switch (columnIndex) {
	                    case 0:
	                    	if(cellTypeEnum == CellType.STRING) {
	                    		carga = cell.getStringCellValue();
	                    	} else if(cellTypeEnum == CellType.NUMERIC) {
	                    		Double numericCellValue = cell.getNumericCellValue();
	                    		if (numericCellValue != null) {
	                    			carga = dfCarga.format(numericCellValue);
	                    		}
	                    	}
	                        break;
	                    case 1:
	                    	exclusionDet.setMotivo( cell.getStringCellValue() );
	                        break;
	                    }
	                }
	                
	                exclusionDet.setCarga( carga );
	                listDetExclusion.add(exclusionDet);
	                
            	}//if (row.getRowNum() > filaComienza)
            }//for (Row row : sheet)
            
            response.setRegistros(listDetExclusion);
            
		} catch (Exception e) {
            e.printStackTrace();
            logger.error("Error inesperado", e);
            
            String mensaje = this.catMensajeService.getMensaje(EMensaje.ERROR_PROCESAR_ARCHIVO);
            response.setStatus("ERROR");
    		response.setMessage(mensaje);
    		return response;
        }   finally {
        	if (workbook != null) {
        		try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
		return response;
	}
	
	private boolean validHeader(String[] headerExcel, String[] headerConfig) {
		return (headerExcel.length == headerConfig.length);
	}	

	@Override
	public String validaExclusion(String numProveedor, String exclusion, Integer idCatTipoExclusion, Integer idCatTipoRebate, Integer idCatPeriodo) {
		String validaExclusion = "OK";
		if (exclusion != null && !exclusion.isEmpty()) {
			boolean existeExclusion = this.iRebatesCicmxOcService.existeExclusion(exclusion.trim()
					, idCatPeriodo
					, idCatTipoExclusion
					, numProveedor
					, idCatTipoRebate);
			
			if (!existeExclusion) {
				return "La exclusi\u00f3n [" + exclusion + "] no existe";
			}
			
			String existeExclusionPeriodo = this.exclusionCargaDetService.existeExclusionPeriodo(exclusion.trim()
					, idCatTipoExclusion
					, idCatTipoRebate
					, idCatPeriodo
					, numProveedor);
			
			if (!existeExclusionPeriodo.isEmpty()) {
				int idPeriodoCarga = this.exclusionCargaDetService.obtenerPeriodoCarga(exclusion.trim(), idCatTipoExclusion, idCatPeriodo);
				PeriodoDto periodo = this.servicePeriodo.getById(idPeriodoCarga);
				return "La exclusi\u00f3n [" + exclusion + "] ya se dio de alta en el folio [" + existeExclusionPeriodo + "] al periodo " + idPeriodoCarga + " " + periodo.getDetallePeriodo();
			}	
		}
		 
		return validaExclusion;
	}

	@Override
	public boolean ordenCompraPertenecePeriodo(ExclusionDto exclusion, PeriodoDto periodo) throws ParseException {
		String fechaRecepcion = this.iRebatesCicmxOcService.obtenerFechaRecepcion(exclusion);
		
		Date fechaR = dateFormat.parse(fechaRecepcion);
		return this.servicePeriodo.isFechaRecepcionDentroPeriodo(periodo, fechaR);
	}

	@Override
	public boolean ordenCompraDespuesPeriodo(ExclusionDto exclusion, PeriodoDto periodo) throws ParseException {
		String fechaRecepcion = this.iRebatesCicmxOcService.obtenerFechaRecepcion(exclusion);
		
		return this.servicePeriodo.isOrdenCompraDespuesPeriodo(periodo, fechaRecepcion);
	}

	@Override
	public boolean getPerfilAutorizado(List<Integer> perfiles, Integer idTipoExclusion) {
		
		for (Integer p: perfiles) {
			Integer idperfilAutorizado = this.exclusionRepository.getperfilAutorizado(p, idTipoExclusion);
			if (idperfilAutorizado != null) return true;			
		}
		
		return false;		
	}	

	@Override
	@Transactional
	public void modificarComentario(Integer idExclusion, String comentario) {
		ExclusionEntity entity = this.exclusionRepository.findByIdExclusion(idExclusion);
		entity.setComentario(comentario);
		this.exclusionRepository.save(entity);
	}

}
