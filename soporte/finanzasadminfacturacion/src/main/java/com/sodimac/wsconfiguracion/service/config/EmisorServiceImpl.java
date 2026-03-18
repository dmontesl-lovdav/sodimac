package com.sodimac.wsconfiguracion.service.config;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.sodimac.wsconfiguracion.dto.ClientResponseTYPE;
import com.sodimac.wsconfiguracion.dto.ComprobanteDto;
import com.sodimac.wsconfiguracion.dto.ConfDatosEmisorDto;
import com.sodimac.wsconfiguracion.dto.ConfDatosEmisorTiendaDto;
import com.sodimac.wsconfiguracion.dto.SerieFolioTuple;
import com.sodimac.wsconfiguracion.dto.SerieSummaryDTO;
import com.sodimac.wsconfiguracion.entity.config.CatConfiguracionEntity;
import com.sodimac.wsconfiguracion.entity.config.CatTipoComprobanteSodimacEntity;
import com.sodimac.wsconfiguracion.entity.config.ConfDatosEmisorEntity;
import com.sodimac.wsconfiguracion.entity.config.ConfDatosEmisorTiendaEntity;
import com.sodimac.wsconfiguracion.entity.config.ConfFormaMetodoPagoEntity;
import com.sodimac.wsconfiguracion.entity.config.FolioEntity;
import com.sodimac.wsconfiguracion.entity.config.FolioHistorialEntity;
import com.sodimac.wsconfiguracion.entity.config.PacEntity;
import com.sodimac.wsconfiguracion.models.config.EmisorReq;
import com.sodimac.wsconfiguracion.repository.config.CatConfiguracionRepository;
import com.sodimac.wsconfiguracion.repository.config.CatTipoComprobanteSodimacRepository;
import com.sodimac.wsconfiguracion.repository.config.ConfDatosEmisorRepository;
import com.sodimac.wsconfiguracion.repository.config.ConfDatosEmisorTiendaRepository;
import com.sodimac.wsconfiguracion.repository.config.ConfFormaMetodoPagoRepository;
import com.sodimac.wsconfiguracion.repository.config.FolioHistorialRepository;
import com.sodimac.wsconfiguracion.repository.config.FolioRepository;
import com.sodimac.wsconfiguracion.repository.config.PacRepository;
import com.sodimac.wsconfiguracion.service.SeguridadService;
import com.sodimac.wsconfiguracion.util.UtilsApi;
import com.sodimac.wsconfiguracion.util.enums.ECodigo;

@Service
public class EmisorServiceImpl implements EmisorService {

    @Autowired
    private ModelMapper modelMapper;
    
	@Autowired()
	@Qualifier("confDatosEmisorRepositoryConfig")
	private ConfDatosEmisorRepository confDatosEmisorRepository;
	
//	@Autowired()
//	@Qualifier("confDatosEmisorRebRepositoryConfig")
//	private ConfDatosEmisorRebRepository confDatosEmisorRebRepository;
	
	@Autowired
	@Qualifier("ConfDatosEmisorTiendaRepositoryConfig")
	private ConfDatosEmisorTiendaRepository confDatosEmisorTiendaRepository;
	
	@Autowired
	@Qualifier("catTipoComprobanteSodimacRepositoryConfig")
	private CatTipoComprobanteSodimacRepository catTipoComprobanteSodimacRepository;
	
//	@Autowired
//	@Qualifier("ConfDatosEmisorTiendaRebRepositoryConfig")
//	private ConfDatosEmisorTiendaRebRepository confDatosEmisorTiendaRebRepository;
	
	@Autowired
	private SeguridadService seguridadService;
	
	@Autowired
	@Qualifier("catConfiguracionRepositoryConfig")
	private CatConfiguracionRepository catConfiguracionRepository;
	
	@Autowired
	@Qualifier("folioHistorialRepositoryConfig")
	private FolioHistorialRepository folioHistorialRepository;
	
	@Autowired
	@Qualifier("folioRepositoryConfig")
	private FolioRepository folioRepository;
	
	@Autowired
	@Qualifier("pacRepositoryConfig")
	private PacRepository pacRepository;
	
	@Autowired
	@Qualifier("confFormaMetodoPagoRepositoryConfig")
	private ConfFormaMetodoPagoRepository confFormaMetodoPagoRepository;
	
	
	private Object convertirADto(Object obj, Class<?> destinationClass) {
		Object dto = modelMapper.map(obj, destinationClass);
		return dto;
	}
	
	@Override
	public ConfDatosEmisorDto obtenerEmisor(String rfc) throws Exception {
		ConfDatosEmisorEntity entityEmisor = confDatosEmisorRepository.findByRfc(seguridadService.encriptar(rfc));
		ConfDatosEmisorDto confDatosEmisorDto = null;
		if(entityEmisor != null) {
			confDatosEmisorDto =(ConfDatosEmisorDto) convertirADto(entityEmisor, ConfDatosEmisorDto.class);
			confDatosEmisorDto.setRfc(seguridadService.desencriptar(entityEmisor.getRfc()));
			confDatosEmisorDto.setRazonSocial(seguridadService.desencriptar(entityEmisor.getRazonSocial()));
			confDatosEmisorDto.setCodigoPostal(Integer.toString(entityEmisor.getCatCodigoPostalEntity().getCodigopostal()));
		}

		//SOLO DE PRUEBA
		//List<ConfDatosEmisorEntity> test = confDatosEmisorRepository.findAll(); 
//		test.stream().forEach((p)-> {
//			p.setRfc(seguridadService.desencriptar(p.getRfc()));
//			p.setRazonSocial(seguridadService.desencriptar(p.getRazonSocial()));
//			});
		return confDatosEmisorDto;
	}

	@Override
	public ConfDatosEmisorTiendaDto obtenerLugarExpedicion(Integer idTienda) {
		ConfDatosEmisorTiendaEntity tienda = confDatosEmisorTiendaRepository.findByIdTienda(idTienda);
		ConfDatosEmisorTiendaDto ConfDatosEmisorTiendaDto = null;
		if(tienda != null) {
			ConfDatosEmisorTiendaDto = (ConfDatosEmisorTiendaDto) convertirADto(tienda, ConfDatosEmisorTiendaDto.class);
			ConfDatosEmisorTiendaDto.setCodigoPostal(Integer.toString(tienda.getCatCodigoPostalEntity().getCodigopostal()));
		}
		
		return ConfDatosEmisorTiendaDto;
	}

	@Transactional
	@Override
	public ClientResponseTYPE<ComprobanteDto> obtenerEmisorYLugarExpedicion(EmisorReq request) throws Exception {
		ClientResponseTYPE<ComprobanteDto> comprobante = new ClientResponseTYPE<ComprobanteDto>();
		ConfDatosEmisorDto confDatosEmisorDto = obtenerEmisor(request.getRfc());
		ConfDatosEmisorTiendaDto ConfDatosEmisorTiendaDto = obtenerLugarExpedicion (request.getSucursal());
		ConfFormaMetodoPagoEntity confFormaMetodoPagoEntity = obtenerRelacionFormaMetodoPago(request.getTipoDeComprobante(), request.getFormaPago(), request.getVersion());
		ComprobanteDto comprobanteDto = new ComprobanteDto(request.getVersion(), confDatosEmisorDto, ConfDatosEmisorTiendaDto, confFormaMetodoPagoEntity);
		//comprobanteDto.setMetodoPago("PUE");
		comprobanteDto.setTipoDeComprobante(obtieneTipoComproganteSat(request.getTipoDeComprobante()));
		SerieFolioTuple serieFolioTuple = obtieneSerieFolio(request);
		comprobanteDto.setSerie(serieFolioTuple.getSerie());
		comprobanteDto.setFolio(serieFolioTuple.getFolio());

		comprobante.setData(comprobanteDto);
		if (comprobanteDto.getLugarExpedicion()== null || comprobanteDto.getEmisorNode() == null) {

    		UtilsApi.setRespuesta(comprobante.getRespuesta(), ECodigo.EmisorOTiendaNoEcontrado);
    	}
		if(comprobanteDto.getTipoDeComprobante() == null) {
			UtilsApi.setRespuesta(comprobante.getRespuesta(), ECodigo.TipoDeComprobanteNoEncontrado);
		}
		return comprobante;
	}

	private ConfFormaMetodoPagoEntity obtenerRelacionFormaMetodoPago(String tipoDeComprobante, String medioPago, String version) {
		List<ConfFormaMetodoPagoEntity> confFormaMetodoPagoEntity = confFormaMetodoPagoRepository.findByComprobanteMedioVersion(tipoDeComprobante, medioPago, version);
		if (confFormaMetodoPagoEntity.isEmpty())
			return null;
		else
			return confFormaMetodoPagoEntity.get(0);
	}

	private String obtieneTipoComproganteSat(String tipoComprobanteSodimac) throws Exception {
		CatTipoComprobanteSodimacEntity  tipoComprobanteSod = catTipoComprobanteSodimacRepository.findByTipocomprobante(tipoComprobanteSodimac);
		if (tipoComprobanteSod == null) {
			throw new Exception("No existe configuración para el tipo de comprobante solicitado");
		}
		String tipoCompSat = tipoComprobanteSod.getCatTipoComprobanteSatEntity().getTipocomprobante();

		return tipoCompSat;
	}
	
	private SerieFolioTuple obtieneSerieFolio(EmisorReq request) throws Exception {
		SerieSummaryDTO serieSummaryDTO = confDatosEmisorTiendaRepository.findConfigSerieAndTienda(request.getTipoDeComprobante(), request.getSucursal());
		if (serieSummaryDTO == null) {
			throw new Exception("No existe configuración de Serie para la tienda y el tipo de comprobante solicitado");
		}
		
		BigInteger one = BigInteger.valueOf(1);
		BigInteger folio = null;
		FolioEntity folioEntity = folioRepository.findByIdcatserieAndIdconfdatosemisortienda(serieSummaryDTO.getIdcatserie(), serieSummaryDTO.getIdconfdatosemisortienda());
		if (request.getTipoDeOperacion().equals("T") && request.getVersion().equals("4.0")) {

			FolioHistorialEntity folioHistorialEntityNew = new FolioHistorialEntity(serieSummaryDTO.getIdcatserie(), serieSummaryDTO.getIdconfdatosemisortienda(), request.getIdAplicacion(), serieSummaryDTO.getSerie() + serieSummaryDTO.getIdtienda());

			if (folioEntity == null) {
				folio = one;
				folioEntity = new FolioEntity(serieSummaryDTO.getIdcatserie(), serieSummaryDTO.getIdconfdatosemisortienda());
			} else {
				folio = folioEntity.getFolio().add(one);	
			}
			folioEntity.setFolio(folio);
			folioRepository.save(folioEntity);
			folioHistorialEntityNew.setFolio(folio);
			folioHistorialRepository.save(folioHistorialEntityNew);
		} else { // Consulta "C" o Version 3.3
			if (folioEntity == null) {
				folio =  BigInteger.valueOf(0);
			} else {
				folio = folioEntity.getFolio();	
			}
		}
		
		return new SerieFolioTuple(serieSummaryDTO.getSerie() + serieSummaryDTO.getIdtienda(), folio.toString());
	}
//	@Override
//	public ConfDatosEmisorDto obtenerEmisorReb(String rfc) {
//		ConfDatosEmisorRebEntity entityEmisorReb = confDatosEmisorRebRepository.findByRfc(seguridadService.encriptar(rfc));
//		ConfDatosEmisorDto confDatosEmisorDto = null;
//		if (entityEmisorReb != null) {
//			entityEmisorReb.setRfc(seguridadService.desencriptar(entityEmisorReb.getRfc()));
//			entityEmisorReb.setRazonSocial(seguridadService.desencriptar(entityEmisorReb.getRazonSocial()));
//			confDatosEmisorDto =(ConfDatosEmisorDto) convertirADto(entityEmisorReb, ConfDatosEmisorDto.class);
//		}
//
//		return confDatosEmisorDto;
//	}
//
//	@Override
//	public ConfDatosEmisorTiendaDto obtenerLugarExpedicionReb(Integer idTienda) {
//		ConfDatosEmisorTiendaRebEntity tiendaReb = confDatosEmisorTiendaRebRepository.findByIdTienda(idTienda);
//		ConfDatosEmisorTiendaDto ConfDatosEmisorTiendaDto = null;
//		if (tiendaReb != null) {
//			ConfDatosEmisorTiendaDto = (ConfDatosEmisorTiendaDto) convertirADto(tiendaReb, ConfDatosEmisorTiendaDto.class);
//		}
//
//		return ConfDatosEmisorTiendaDto;
//	}
//
//	@Override
//	public EmisorYLugarExpedicionDto obtenerEmisorYLugarExpedicionReb(String rfc, Integer idTienda) {
//		ConfDatosEmisorDto confDatosEmisorDto = obtenerEmisorReb(rfc);
//		ConfDatosEmisorTiendaDto ConfDatosEmisorTiendaDto = obtenerLugarExpedicionReb (idTienda);
//		EmisorYLugarExpedicionDto emisorYLugarExpedicionDto = new EmisorYLugarExpedicionDto (confDatosEmisorDto, ConfDatosEmisorTiendaDto);
//    	if (emisorYLugarExpedicionDto.getConfDatosEmisorDto() == null || emisorYLugarExpedicionDto.getConfDatosEmisorTiendaDto() == null) {
//    		UtilsApi.setRespuesta(emisorYLugarExpedicionDto.getRespuesta(), ECodigo.EmisorOTiendaNoEcontrado);
//    	}
//		return emisorYLugarExpedicionDto;
//	}
	
	@Override
	public List<CatConfiguracionEntity> obtieneConfiguraciones (){
		List<CatConfiguracionEntity> configuracionesList = new ArrayList<CatConfiguracionEntity>();
		configuracionesList = catConfiguracionRepository.findAll();
		return configuracionesList;
		
	}
	
	@Override
	public List<PacEntity> obtienePacs (){
		List<PacEntity> pacsList = new ArrayList<PacEntity>();
		pacsList = pacRepository.findAll();
		return pacsList;
		
	}

}
