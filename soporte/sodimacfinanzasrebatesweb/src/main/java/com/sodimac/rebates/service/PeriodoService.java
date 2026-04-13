package com.sodimac.rebates.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.dto.CatRolDto;
import com.sodimac.rebates.dto.CatTipoRebateDto;
import com.sodimac.rebates.dto.CatUsuarioPerfilDto;
import com.sodimac.rebates.dto.PeriodoDto;
import com.sodimac.rebates.dto.RelPeriodoTipoRebateDto;
import com.sodimac.rebates.enums.EEstatusPeriodo;
import com.sodimac.rebates.enums.ETipoDocumento;
import com.sodimac.rebates.mapper.PeriodoMapper;
import com.sodimac.rebates.mapper.RelPeriodoTipoRebateMapper;
import com.sodimac.rebates.model.ControlDocumento;
import com.sodimac.rebates.model.Documento;
import com.sodimac.rebates.model.Generic;
import com.sodimac.rebates.model.Periodo;
import com.sodimac.rebates.model.PeriodoRol;
import com.sodimac.rebates.model.ProgramaPago;
import com.sodimac.rebates.model.RelPeriodoTipoRebate;
import com.sodimac.rebates.model.TipoRebate;
import com.sodimac.rebates.model.Usuario;
import com.sodimac.rebates.repository.ControlDocumentoRepository;
import com.sodimac.rebates.repository.DocumentoRepository;
import com.sodimac.rebates.repository.PeriodoRepository;
import com.sodimac.rebates.repository.RelPeriodoTipoRebateRepository;
import com.sodimac.rebates.repository.UsuarioRepository;

@Service
public class PeriodoService implements IPeriodoService {

	private static final String VARIABLE_PERIODO_TODOS = "IdPeriodoComun";
	private static final String VARIABLE_PERIODO_ACTUALES = "FechaPeriodosActuales";
	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	@Autowired
	private EntityManager em; 
	
	@Autowired
	private PeriodoRepository periodoRepo;
	
	@Autowired
	private RelPeriodoTipoRebateRepository relRepository;

	@Autowired
	private ControlDocumentoRepository controlDocumentoRepo;

	@Autowired
	private DocumentoRepository documentoRepo;
	
	@Autowired
	private IConfiguracionService configuracionService;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private ICatUsuarioPerfilService catUsuarioPerfilService;

	@Override
	public PeriodoDto getById(Integer idCatPeriodo) {
		Optional<Periodo> optional = periodoRepo.findById(idCatPeriodo);
		if (optional.isPresent()) {
			Periodo entity = optional.get();
			return PeriodoMapper.convertDtoComplejo(entity);
		}
		return null;
	}

	@Override
	public List<Periodo> getAll() {

		return periodoRepo.findAll();
	}

	@Override
	public List<Periodo> getActive() {
		List<Periodo> listPeriodo = null;
		CriteriaBuilder cb = this.em.getCriteriaBuilder();
		CriteriaQuery<Periodo> cq = cb.createQuery(Periodo.class);
		Root<Periodo> root = cq.from(Periodo.class);
		
		List<Predicate> predicates = new ArrayList<>();
		predicates.add( cb.equal(root.get("activo"), true ));
		
		List<Order> orders = new ArrayList<Order>();
		orders.add(cb.asc(root.get("orden")));
		orders.add(cb.desc(root.get("idCatPeriodo")));
		
        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(orders);
        
        listPeriodo = em.createQuery(cq).getResultList();
        
		return listPeriodo;
	}
	
	@Override
	public List<Periodo> getActiveActuales() throws ParseException {
		List<Periodo> listPeriodo = null;
		int idPeriodoTodos = this.getIdPeriodoTodos();
		String fechaPeriodos = this.configuracionService.getValor(VARIABLE_PERIODO_ACTUALES);
		
		CriteriaBuilder cb = this.em.getCriteriaBuilder();
		CriteriaQuery<Periodo> cq = cb.createQuery(Periodo.class);
		Root<Periodo> root = cq.from(Periodo.class);
		
		List<Predicate> predicates = new ArrayList<>();
		predicates.add( cb.equal(root.get("activo"), true ));
		
		if (fechaPeriodos != null && !fechaPeriodos.isEmpty()) {
			Date fehaPeriodos = dateFormat.parse(fechaPeriodos);
			
			Predicate onStart = cb.greaterThanOrEqualTo(root.get("fechaIni"), fehaPeriodos);
			Predicate todosPredicate = cb.equal(root.get("idCatPeriodo"), idPeriodoTodos);
			
			Predicate predicateOr = cb.or(onStart, todosPredicate);
			predicates.add(predicateOr);
		}
		
		List<Order> orders = new ArrayList<Order>();
		orders.add(cb.asc(root.get("orden")));
		orders.add(cb.desc(root.get("idCatPeriodo")));
		
        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(orders);
        
        listPeriodo = em.createQuery(cq).getResultList();
		return listPeriodo;
	}
	
	@Override
	public List<Periodo> getActiveActualesSinTodos() throws ParseException {
		List<Periodo> listPeriodos = new ArrayList<>();
		List<Periodo> listPeriodosActivos = this.getActiveActuales();
		if (listPeriodosActivos != null) {
			int idPeriodoTodos = this.getIdPeriodoTodos();
			for (Periodo periodo : listPeriodosActivos) {
				if (idPeriodoTodos != periodo.getIdCatPeriodo().intValue()) {
					listPeriodos.add(periodo);
				}
			}
		}
		return listPeriodos;
	}
	
	@Override
	public List<Periodo> getActiveOrderByDesc() throws ParseException {
		List<Periodo> listPeriodo = null;
		int idPeriodoTodos = this.getIdPeriodoTodos();
		CriteriaBuilder cb = this.em.getCriteriaBuilder();
		CriteriaQuery<Periodo> cq = cb.createQuery(Periodo.class);
		Root<Periodo> root = cq.from(Periodo.class);
		
		Predicate pActivo =  cb.equal(root.get("activo"), true );
		
		List<Order> orders = new ArrayList<Order>();
		orders.add(cb.asc(root.get("orden")));
		orders.add(cb.desc(root.get("idCatPeriodo")));
		
		String fechaPeriodos = this.configuracionService.getValor(VARIABLE_PERIODO_ACTUALES);
		Date fehaPeriodos = dateFormat.parse(fechaPeriodos);
			
		Predicate onStart = cb.greaterThanOrEqualTo(root.get("fechaIni"), fehaPeriodos);
		Predicate todosPredicate = cb.equal(root.get("idCatPeriodo"), idPeriodoTodos);
		Predicate predicateOr = cb.or(onStart, todosPredicate);	
		
		Predicate pFinal =  cb.and(pActivo, predicateOr);
		
        cq.where(pFinal);
        cq.orderBy(orders);
        listPeriodo = em.createQuery(cq).getResultList();
		return listPeriodo;
		
		//return periodoRepo.findByActivoOrderByIdCatPeriodoDesc(true);
	}

	@Override
	public List<Periodo> getActiveAndEstatus() {
		List<Periodo> listPeriodo = null;
		CriteriaBuilder cb = this.em.getCriteriaBuilder();
		CriteriaQuery<Periodo> cq = cb.createQuery(Periodo.class);
		Root<Periodo> root = cq.from(Periodo.class);
		
		Predicate pActivo =  cb.equal(root.get("activo"), true );
		Predicate p0 =  cb.equal(root.get("estatus"), EEstatusPeriodo.PENDIENTE_CALCULAR.getId() );
		Predicate p3 =  cb.equal(root.get("estatus"), EEstatusPeriodo.TERMINO_CALCULO.getId() );
		Predicate pIn = cb.or(p0,p3);
		Predicate pFinal =  cb.and(pActivo, pIn);
		
		List<Order> orders = new ArrayList<Order>();
		orders.add(cb.asc(root.get("orden")));
		orders.add(cb.desc(root.get("idCatPeriodo")));
		
        cq.where(pFinal);
        cq.orderBy(orders);
        listPeriodo = em.createQuery(cq).getResultList();
		return listPeriodo;
		//return periodoRepo.findByActivoAndEstatusIn(true, Arrays.asList(0, 3));
	}
	
	@Override
	public List<Periodo> getPeriodoAbierto() throws ParseException { 
		
		List<Periodo> listPeriodo = null;
		int idPeriodoTodos = this.getIdPeriodoTodos();
		
		CriteriaBuilder cb = this.em.getCriteriaBuilder();
		CriteriaQuery<Periodo> cq = cb.createQuery(Periodo.class);
		Root<Periodo> root = cq.from(Periodo.class);
		
		Predicate pActivo =  cb.equal(root.get("activo"), true );
		Predicate p0 =  cb.equal(root.get("estatus"), EEstatusPeriodo.PENDIENTE_CALCULAR.getId() );
		Predicate p1 =  cb.equal(root.get("estatus"), EEstatusPeriodo.SOLICITUD_CALCULO.getId() );
		Predicate p2 =  cb.equal(root.get("estatus"), EEstatusPeriodo.EN_PROCESO_CALCULO.getId() );
		Predicate p3 =  cb.equal(root.get("estatus"), EEstatusPeriodo.TERMINO_CALCULO.getId() );
		
		Predicate pIn = cb.or(p0,p1,p2,p3);
		
		String fechaPeriodos = this.configuracionService.getValor(VARIABLE_PERIODO_ACTUALES);
		Date fehaPeriodos = dateFormat.parse(fechaPeriodos);
			
		Predicate onStart = cb.greaterThanOrEqualTo(root.get("fechaIni"), fehaPeriodos);
		Predicate todosPredicate = cb.equal(root.get("idCatPeriodo"), idPeriodoTodos);
		Predicate predicateOr = cb.or(onStart, todosPredicate);	
		
		Predicate pFinal =  cb.and(pActivo, predicateOr, pIn);
		
		List<Order> orders = new ArrayList<Order>();
		orders.add(cb.asc(root.get("orden")));
		orders.add(cb.desc(root.get("idCatPeriodo")));
		
        cq.where(pFinal);
        cq.orderBy(orders);
        listPeriodo = em.createQuery(cq).getResultList();
		return listPeriodo;
	}
	
	@Override
	public List<Periodo> getPeriodoAbiertoSinTodos() throws ParseException {
		List<Periodo> listPeriodos = new ArrayList<>();
		List<Periodo> listPeriodosAbiertos = this.getPeriodoAbierto();
		if (listPeriodosAbiertos != null) {
			int idPeriodoTodos = this.getIdPeriodoTodos();
			for (Periodo periodo : listPeriodosAbiertos) {
				if (idPeriodoTodos != periodo.getIdCatPeriodo().intValue()) {
					listPeriodos.add(periodo);
				}
			}
		}
		return listPeriodos;
	}

	@Override
	public List<PeriodoDto> getPeriodoBetweenFechasAndDetallePeriodoLike(Date fechaIni, Date fechaFin,
			String detallePeriodo) {
		List<Periodo> entities = this.periodoRepo.findByPeriodoBetweenFechasAndDetallePeriodoLike(fechaIni, fechaFin, detallePeriodo);
		return getNames(PeriodoMapper.convertDtosComplejo(entities));
	}

	@Override
	public List<PeriodoDto> getPeriodoByOptions(Date fechaIni, Date fechaFin, ProgramaPago programaPago,
			String detallePeriodo) {
		List<Periodo> entities = this.periodoRepo.findByPeriodoWithOptions(fechaIni, fechaFin, programaPago, detallePeriodo);
		return getNames(PeriodoMapper.convertDtosComplejo(entities));
	}

	private List<PeriodoDto> getNames(List<PeriodoDto> list) {

		List<Usuario> usuariosList = usuarioRepository.findAll();
		
		list.forEach(l -> {
			if (l.getIdUsuarioCreacion() != null) {
				Usuario item = usuariosList.stream()
						.filter(usu -> usu.getId().equals(l.getIdUsuarioCreacion()))
						.findAny()
						.orElse(null);
				l.setNombreUsuarioCreacion(item.getNombre());				
			}
			if (l.getIdUsuarioModificacion() != null) {
				Usuario item = usuariosList.stream()
						.filter(usu -> usu.getId().equals(l.getIdUsuarioModificacion()))
						.findAny()
						.orElse(null);
				l.setNombreUsuarioModificacion(item.getNombre());				
			}
			if (l.getIdUsuarioModifEstatus() != null) {
				Usuario item = usuariosList.stream()
						.filter(usu -> usu.getId().equals(l.getIdUsuarioModifEstatus()))
						.findAny()
						.orElse(null);
				l.setNombreUsuarioModifEstatus(item.getNombre());				
			}
		});
		
		return list;
	}

	@Override
	public void save(PeriodoDto periodoDto) {

		Periodo periodo = PeriodoMapper.convertEntity(periodoDto);		
		periodoRepo.save(periodo);
		for (RelPeriodoTipoRebate relPeriodo : periodo.getRelPeriodoTipoRebate()) {
			relPeriodo.getPeriodo().setIdCatPeriodo( periodo.getIdCatPeriodo() );
		}
		relRepository.saveAll(periodo.getRelPeriodoTipoRebate());
				
		periodo.setIdPerfil(getIdPerfil(periodo, 0));
		periodoRepo.save(periodo);
		
	}
	
	@Override
	public void saveOrUpdate(PeriodoDto periodoDto) {
		Periodo periodo = PeriodoMapper.convertEntity(periodoDto);
		periodoRepo.save(periodo);
	}

	@Override
	public Generic processPeriodo(Integer id, Integer estatusDestino, Integer idUser) {

		Generic response = new Generic();
		Optional<Periodo> optional = periodoRepo.findById(id);
		Periodo periodo = new Periodo();

		if (optional.isPresent()) {

			periodo = optional.get();

			periodo.setIdUsuarioModifEstatus(idUser);
			periodo.setFechaHoraModifEstatus(new Date(System.currentTimeMillis()));

			if (periodo.getEstatus().intValue() == EEstatusPeriodo.PENDIENTE_CALCULAR.getId() ) {
				
				periodo.setEstatus(estatusDestino);
				periodoRepo.save(periodo);
				// Colocar en estatus 1 documentos que se van a procesar
				List<ControlDocumento> documentoseEnPeriodo = controlDocumentoRepo.findByPeriodoAndActivo(periodo,
						true);
				for (ControlDocumento doc : documentoseEnPeriodo) {

					doc.setIdEstatusArchivo((short) 1);
					controlDocumentoRepo.save(doc);
				}
				response.setTitle("OK");
				response.setMessage("Rebate procesado correctamente");

			}  else if (periodo.getEstatus().intValue() == EEstatusPeriodo.SOLICITUD_CALCULO.getId()) {
				periodo.setEstatus(estatusDestino);
				periodoRepo.save(periodo);

				response.setTitle("OK");
				response.setMessage("Rebate solicitado correctamente");
			} else if (periodo.getEstatus().intValue() == EEstatusPeriodo.TERMINO_CALCULO.getId()) {
				periodo.setEstatus(estatusDestino);

				periodo.setIdPerfil(getIdPerfil(periodo, estatusDestino));
				periodo.setFechaHoraCierre(new Date(System.currentTimeMillis()));
				
				periodoRepo.save(periodo);

				response.setTitle("OK");
				response.setMessage("Rebate terminado correctamente");
			}  else if (periodo.getEstatus().intValue() == EEstatusPeriodo.SOLICITUD_CONTABILIDAD.getId() ) {
				periodo.setEstatus(estatusDestino);
				periodoRepo.save(periodo);
				
				response.setTitle("OK");
				response.setMessage("Rebate en solicitud de contabilizaci\u00f3n");
			} else if (periodo.getEstatus().intValue() == EEstatusPeriodo.AUTORIZACION_CONTABILIDAD.getId() ) {
				periodo.setEstatus(estatusDestino);
				periodoRepo.save(periodo);
				
				response.setTitle("OK");
				response.setMessage("Rebate en proceso de autorizaci\u00f3n");
			} else if (periodo.getEstatus().intValue() == EEstatusPeriodo.REVISION_CONTABILIDAD.getId() ) {
				periodo.setEstatus(estatusDestino);
				periodoRepo.save(periodo);
				
				response.setTitle("OK");
				response.setMessage("Rebate en proceso de revisi\u00f3n");
			} else {

				response.setTitle("Error en Base de Datos");
				response.setMessage("No se pudo acceder a la información");
				response.setTypeMessage(3);
				response.setCode(false);
				return response;
			}

			response.setTypeMessage(1);
			response.setCode(true);
			return response;
		}

		response.setCode(false);
		return response;
	}

	@Override
	public boolean reprocesarPeriodo(Integer id, Integer idUser) {

		Optional<Periodo> optional = periodoRepo.findById(id);
		Periodo periodo = new Periodo();

		if (optional.isPresent()) {

			periodo = optional.get();
			periodo.setEstatus(0); // Cancelado

			periodo.setIdUsuarioModifEstatus(idUser);
			periodo.setFechaHoraModifEstatus(new Date(System.currentTimeMillis()));
			
			periodoRepo.save(periodo);

			// Colocar en estatus 1 documentos que se van a procesar
			/*List<ControlDocumento> documentoseEnPeriodo = controlDocumentoRepo.findByPeriodoAndActivo(periodo, true);
			for (ControlDocumento doc : documentoseEnPeriodo) {

				doc.setIdEstatusArchivo((short) 0);
				doc.setActivo(false);
				controlDocumentoRepo.save(doc);

			}*/

			return true;
		}

		return false;
	}

	@Override
	public boolean deletePeriodo(Integer id) {

		Optional<Periodo> optional = periodoRepo.findById(id);
		Periodo periodo = new Periodo();

		if (optional.isPresent()) {

			periodo = optional.get();
			periodo.setActivo(false); // Borrado lógico
			periodoRepo.save(periodo);

			return true;
		}

		return false;
	}

	@Override
	public Generic getPeriodoEnProceso(Integer id) {

		Generic response = new Generic();
		Optional<Periodo> optional = periodoRepo.findById(id);
		Periodo periodo = new Periodo();

		if (optional.isPresent()) {

			periodo = optional.get();
			List<Periodo> enProceso = periodoRepo.findProceso(periodo.getProgramaPago());

			if (enProceso.size() >= 1) {

				response.setCode(false);
				response.setIdResponse(enProceso.get(0).getIdCatPeriodo());
				return response;
			}

			response.setCode(true);
			response.setIdResponse(id);
			return response;
		}

		response.setCode(false);
		response.setIdResponse(id);
		return response;
	}

	@Override
	public Generic getRequired(Integer id) {
		List<Documento> documentosRequeridos = documentoRepo.findByRequeridoAndActivo(true, true);
		Generic response = this.validateDocuments(id, documentosRequeridos);
		return response;
	}

	@Override
	public Generic getRequiredProcesarPeriodo(Integer idCatPeriodo) {
		List<Documento> documentosRequeridos = documentoRepo.findByRequeridoAndActivo(true, true);
		
		List<Documento> documentosRequeridosFinal = new ArrayList<Documento>(); 
		if (documentosRequeridos != null) {
			for(Documento doc : documentosRequeridos) {
				if ( ! this.isDescartado( doc.getIdDocumento().intValue() )) {
					documentosRequeridosFinal.add( doc );
				}
			}
		}
		Generic response = this.validateDocuments(idCatPeriodo, documentosRequeridosFinal); 
		return response;
	}

	private Generic validateDocuments(Integer id, List<Documento> documentosRequeridos) {
		Generic response = new Generic();
		try {

			ArrayList<Documento> documentosProcesar = new ArrayList<>();
			List<String> requeridos = new ArrayList<>();
			HashSet<Object> seen = new HashSet<>();
			Optional<Periodo> optional = periodoRepo.findById(id);
			Periodo periodo = new Periodo();
			Periodo periodoTodos = new Periodo();
			periodoTodos.setIdCatPeriodo( this.getIdPeriodoTodos() );
			
			if (optional.isPresent()) {

				periodo = optional.get();

				List<ControlDocumento> documentoseEnPeriodo = controlDocumentoRepo.findByPeriodoAndActivo(periodo,true);
				List<ControlDocumento> documentosTodos = controlDocumentoRepo.findByPeriodoAndActivo(periodoTodos,true);
				
				if (documentosTodos != null) {
					for (ControlDocumento todos : documentosTodos) {
						int idDocumento = todos.getDocumento().getIdDocumento().intValue();
						int idControlDocumento = this.getIdPeriodo(documentoseEnPeriodo, idDocumento);
						if (idControlDocumento == 0) { //Solo si no esta en la lista inicial se agrega, para que no contenga periodos repetidos
							documentoseEnPeriodo.add(todos);
						}
					}
				}
				
				for (ControlDocumento doc : documentoseEnPeriodo) {

					Optional<Documento> optionalDoc = documentoRepo.findById(doc.getDocumento().getIdDocumento());
					Documento documento = new Documento();

					if (optionalDoc.isPresent()) {

						documento = optionalDoc.get();
						if (documento.isRequerido()) {

							documentosProcesar.add(documento);
						}

					}

				}

				documentosProcesar.removeIf(d -> !seen.add(d.getIdDocumento()));
				documentosProcesar.sort(Comparator.comparing(Documento::getIdDocumento));
			}

			if (documentosProcesar.size() != documentosRequeridos.size()) {

				// System.out.println("----- Requeridos -----");

				for (Documento req : documentosRequeridos) {

					Integer position = documentosProcesar.indexOf(req);
					/*
					 * System.out.println(req.getIdDocumento() + " - " + req.getNombreDocumento());
					 * System.out.println("Encontrado en: " + position);
					 */

					if (position.equals(-1)) {

						requeridos.add(req.getNombreDocumento());
					}
				}

				String required = "";
				if (requeridos.size() > 1) {

					for (String r : requeridos) {

						required += "</br>" + r;
					}

				} else if (requeridos.size() == 1) {

					required = "</br>" + requeridos.get(0);

				} else {

					response.setCode(true);
					return response;
				}
				response.setTitle("Faltan documentos por publicar, favor de validar con el Área de Finanzas");
				response.setMessage(
						"<p style='text-align:left;' class='px-5'><strong>Requeridos: </strong>" + required + "</p>");
				response.setTypeMessage(2);
				response.setCode(false);

				return response;
			}

		} catch (Exception ex) {

			// TODO: cambiar dinámicamente / añadir a bitacora
			response.setTitle("Error en Base de Datos");
			response.setMessage("No se pudo acceder a la información");
			response.setTypeMessage(3);
			response.setCode(false);
			System.out.println(ex.getMessage());

			return response;
		}

		response.setCode(true);
		return response;
	}
	
	private boolean isDescartado(int id) {
		if (id == ETipoDocumento.AJUSTE_CALCULO_REBATE.getId() ||
				id == ETipoDocumento.AJUSTE_CALCULO_FILL_RATE.getId() ||
				id == ETipoDocumento.DEVOLUCIONES_PROVEEDORES_AP.getId()) {
			return true;
		}
		return false;
	}
	
	private Integer getIdPeriodo(List<ControlDocumento> documentos, int idDocumento) {
		if (documentos != null) {
			for (ControlDocumento documento : documentos) {
				int idDocumentoControl = documento.getDocumento().getIdDocumento().intValue();
				if ( idDocumentoControl == idDocumento) {
					return idDocumentoControl;
				}
			}
		}
		return 0;
	}
	
	@Override
	public void editRelacion(List<RelPeriodoTipoRebateDto> dtos) {
		List<RelPeriodoTipoRebate> listRelacion = RelPeriodoTipoRebateMapper.convertToEntities(dtos);		
		relRepository.saveAll(listRelacion);

		Optional<Periodo> optional = periodoRepo.findById(listRelacion.get(0).getPeriodo().getIdCatPeriodo());
		Periodo periodo = null;
		if (optional.isPresent()) {
			periodo = optional.get();
			periodo.setIdPerfil(getIdPerfil(periodo, 0));
			periodoRepo.save(periodo);
		}
	}

	//rmt 2025-03-25 Asignar perfil
	private Integer getIdPerfil(Periodo periodo, Integer estatusDestino) {
		int idPerfilUser = 0;
		 List<CatUsuarioPerfilDto> perfilesUsuarioModifEstatus = catUsuarioPerfilService.getUsuarioPerfiles(periodo.getIdUsuarioCreacion());
		 if (perfilesUsuarioModifEstatus != null && !perfilesUsuarioModifEstatus.isEmpty() && perfilesUsuarioModifEstatus.get(0).getPerfil() != null) {
			 idPerfilUser = perfilesUsuarioModifEstatus.get(0).getPerfil().getId();
		 }
		
		Integer idPerfil = periodoRepo.getPerfil(periodo.getIdCatPeriodo(), idPerfilUser, periodo.getEstatus().intValue(), estatusDestino);
		if (idPerfil == null) {
			idPerfil = idPerfilUser;
		}

		return idPerfil;
	}

	@Override
	public RelPeriodoTipoRebateDto existeRelacion(PeriodoDto periodo, CatTipoRebateDto tipoRebate) {
		
		Periodo entityPeriodo = new Periodo();
		TipoRebate rebateEntity = new TipoRebate();
		entityPeriodo.setIdCatPeriodo(periodo.getIdCatPeriodo());
		rebateEntity.setIdCatTipoRebate( tipoRebate.getIdCatTipoRebate() );
		
		Optional<RelPeriodoTipoRebate> optional = relRepository.findByPeriodoAndCatTipoRebate(entityPeriodo, rebateEntity);
		if(optional.isPresent()) {
			RelPeriodoTipoRebate relPeriodoTipoRebate = optional.get();
			return RelPeriodoTipoRebateMapper.convertToDto(relPeriodoTipoRebate);
		}
		return null;
	}

	@Override
	public int getIdPeriodoTodos() {
		String valor = this.configuracionService.getValor(VARIABLE_PERIODO_TODOS);
		return Integer.parseInt(valor);
	}
	
	@Override
	public boolean isPeriodoTodos(Integer idPeriodoCat) {
		String valor = this.configuracionService.getValor(VARIABLE_PERIODO_TODOS);
		return (Integer.parseInt(valor) == idPeriodoCat.intValue());
	}

	@Override
	public boolean isPeriodoVigente(PeriodoDto entityPeriodo) {
		Calendar cal = Calendar.getInstance();
		Date fechaIni = entityPeriodo.getFechaIni();
		Date fechaFin = entityPeriodo.getFechaFin();
		
		if (cal.getTime().compareTo( fechaIni ) >= 0 && cal.getTime().compareTo( fechaFin ) <= 0 ) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isFechaRecepcionDentroPeriodo(PeriodoDto entityPeriodo, Date fechaRecepcion) {
		Date fechaIni = entityPeriodo.getFechaIni();
		Date fechaFin = entityPeriodo.getFechaFin();
		
		//2024-11-11 RMT Validar que la recepcion este dentro del periodo 
		if (fechaRecepcion.compareTo( fechaIni ) >= 0 && fechaRecepcion.compareTo( fechaFin ) <= 0 ) {
			return true;
		}		
		return false;
	}

	@Override
	public List<Periodo> getPeriodosTerminadosAndContabilizados() throws ParseException {
		List<Periodo> listPeriodo = null;
		int idPeriodoTodos = this.getIdPeriodoTodos();
		
		CriteriaBuilder cb = this.em.getCriteriaBuilder();
		CriteriaQuery<Periodo> cq = cb.createQuery(Periodo.class);
		Root<Periodo> root = cq.from(Periodo.class);
		
		Predicate pActivo = cb.equal(root.get("activo"), true );
		Predicate p3 =  cb.equal(root.get("estatus"), EEstatusPeriodo.TERMINO_CALCULO.getId() );
		Predicate p4 =  cb.equal(root.get("estatus"), EEstatusPeriodo.SOLICITUD_CONTABILIDAD.getId() );
		Predicate p5 =  cb.equal(root.get("estatus"), EEstatusPeriodo.AUTORIZACION_CONTABILIDAD.getId() );
		Predicate p6 =  cb.equal(root.get("estatus"), EEstatusPeriodo.REVISION_CONTABILIDAD.getId() );
		Predicate p7 =  cb.equal(root.get("estatus"), EEstatusPeriodo.PROCESO_CONTABILIDAD.getId() );
		Predicate p8 =  cb.equal(root.get("estatus"), EEstatusPeriodo.CONTABILIZADO.getId() ); 
		Predicate pIn = cb.or(p3,p4,p5,p6,p7,p8);
		Predicate todosPredicate = cb.notEqual(root.get("idCatPeriodo"), idPeriodoTodos);
		
		String fechaPeriodos = this.configuracionService.getValor(VARIABLE_PERIODO_ACTUALES);
		Date fehaPeriodos = dateFormat.parse(fechaPeriodos);
			
		Predicate onStart = cb.greaterThanOrEqualTo(root.get("fechaIni"), fehaPeriodos);
		Predicate pFinal = cb.and(pActivo,onStart,todosPredicate, pIn);
		
		List<Order> orders = new ArrayList<Order>();
		orders.add(cb.asc(root.get("orden")));
		orders.add(cb.desc(root.get("idCatPeriodo")));
		
        cq.where(pFinal);
        cq.orderBy(orders);
        listPeriodo = em.createQuery(cq).getResultList();
		return listPeriodo;
	}
	
	@Override
	public List<Periodo> getPeriodosSinTodos(List<EEstatusPeriodo> listEstatus) throws ParseException {
		List<Periodo> listPeriodo = null;
		int idPeriodoTodos = this.getIdPeriodoTodos();
		
		CriteriaBuilder cb = this.em.getCriteriaBuilder();
		CriteriaQuery<Periodo> cq = cb.createQuery(Periodo.class);
		Root<Periodo> root = cq.from(Periodo.class);
		
		Predicate pActivo = cb.equal(root.get("activo"), true);
		
		List<Predicate> listPredicate = new ArrayList<>();
		if (listEstatus != null) {
			for (EEstatusPeriodo estatus : listEstatus) {
				Predicate predicate =  cb.equal(root.get("estatus"), estatus.getId() );
				listPredicate.add(predicate);
			}
		}
		
		Predicate[] predicates = new Predicate[listPredicate.size()];
		listPredicate.toArray(predicates);
		
		Predicate pIn = cb.or(predicates);
		Predicate todosPredicate = cb.notEqual(root.get("idCatPeriodo"), idPeriodoTodos);
		
		String fechaPeriodos = this.configuracionService.getValor(VARIABLE_PERIODO_ACTUALES);
		Date fehaPeriodos = dateFormat.parse(fechaPeriodos);
			
		Predicate onStart = cb.greaterThanOrEqualTo(root.get("fechaIni"), fehaPeriodos);
		Predicate pFinal = cb.and(pActivo,onStart,todosPredicate, pIn);
		
		List<Order> orders = new ArrayList<Order>();
		orders.add(cb.asc(root.get("orden")));
		orders.add(cb.desc(root.get("idCatPeriodo")));
		
        cq.where(pFinal);
        cq.orderBy(orders);
        listPeriodo = em.createQuery(cq).getResultList();
		return listPeriodo;
	}
	
	@Override
	public List<Periodo> getPeriodosTerminados() throws ParseException {
		List<Periodo> listPeriodo = null;
		int idPeriodoTodos = this.getIdPeriodoTodos();
		
		CriteriaBuilder cb = this.em.getCriteriaBuilder();
		CriteriaQuery<Periodo> cq = cb.createQuery(Periodo.class);
		Root<Periodo> root = cq.from(Periodo.class);
		
		Predicate pActivo = cb.equal(root.get("activo"), true );
		Predicate p3 =  cb.equal(root.get("estatus"), EEstatusPeriodo.CONTABILIZADO.getId() ); 
		Predicate pIn = cb.or(p3);
		Predicate todosPredicate = cb.notEqual(root.get("idCatPeriodo"), idPeriodoTodos);
		
		String fechaPeriodos = this.configuracionService.getValor(VARIABLE_PERIODO_ACTUALES);
		Date fehaPeriodos = dateFormat.parse(fechaPeriodos);
			
		Predicate onStart = cb.greaterThanOrEqualTo(root.get("fechaIni"), fehaPeriodos);
		Predicate pFinal = cb.and(pActivo,onStart,todosPredicate, pIn);
		
		List<Order> orders = new ArrayList<Order>();
		orders.add(cb.asc(root.get("orden")));
		orders.add(cb.desc(root.get("idCatPeriodo")));
		
        cq.where(pFinal);
        cq.orderBy(orders);
        listPeriodo = em.createQuery(cq).getResultList();
		return listPeriodo;
	}	

	@Override
	public List<PeriodoRol> getActiveOrderByDesc(List<CatRolDto> roles) throws ParseException {
		List<PeriodoRol> listPeriodo = null;
		int idPeriodoTodos = this.getIdPeriodoTodos();
		CriteriaBuilder cb = this.em.getCriteriaBuilder();
		CriteriaQuery<PeriodoRol> cq = cb.createQuery(PeriodoRol.class);
		Root<PeriodoRol> root = cq.from(PeriodoRol.class);
		
		Predicate pActivo =  cb.equal(root.get("activo"), true );
		
		List<Predicate> listPredicate = new ArrayList<>();
		if (roles != null) {
			for (CatRolDto rol : roles) {
				Predicate predicate =  cb.equal(root.get("idCatPerfil"), rol.getId() );
				listPredicate.add(predicate);
			}
		}

		Predicate[] predicates = new Predicate[listPredicate.size()];
		listPredicate.toArray(predicates);
		
		Predicate pIn = cb.or(predicates);

		List<Order> orders = new ArrayList<Order>();
		orders.add(cb.asc(root.get("orden")));
		orders.add(cb.desc(root.get("idCatPeriodo")));
		
		String fechaPeriodos = this.configuracionService.getValor(VARIABLE_PERIODO_ACTUALES);
		Date fehaPeriodos = dateFormat.parse(fechaPeriodos);
			
		Predicate onStart = cb.greaterThanOrEqualTo(root.get("fechaIni"), fehaPeriodos);
		Predicate todosPredicate = cb.equal(root.get("idCatPeriodo"), idPeriodoTodos);
		Predicate predicateOr = cb.or(onStart, todosPredicate);	
		
		Predicate pFinal =  cb.and(pActivo, predicateOr, pIn);
		
        cq.where(pFinal);
        cq.orderBy(orders);
        listPeriodo = em.createQuery(cq).getResultList();
		return listPeriodo;
		
		//return periodoRepo.findByActivoOrderByIdCatPeriodoDesc(true);
	}

	@Override
	public List<PeriodoRol> getPeriodoAbierto(List<CatRolDto> roles) throws ParseException { 
		
		List<PeriodoRol> listPeriodo = null;
		int idPeriodoTodos = this.getIdPeriodoTodos();
		
		CriteriaBuilder cb = this.em.getCriteriaBuilder();
		CriteriaQuery<PeriodoRol> cq = cb.createQuery(PeriodoRol.class);
		Root<PeriodoRol> root = cq.from(PeriodoRol.class);
		
		Predicate pActivo =  cb.equal(root.get("activo"), true );
		Predicate p0 =  cb.equal(root.get("estatus"), EEstatusPeriodo.PENDIENTE_CALCULAR.getId() );
		Predicate p1 =  cb.equal(root.get("estatus"), EEstatusPeriodo.SOLICITUD_CALCULO.getId() );
		Predicate p2 =  cb.equal(root.get("estatus"), EEstatusPeriodo.EN_PROCESO_CALCULO.getId() );
		Predicate p3 =  cb.equal(root.get("estatus"), EEstatusPeriodo.TERMINO_CALCULO.getId() );
		
		Predicate pIn = cb.or(p0,p1,p2,p3);
		
		String fechaPeriodos = this.configuracionService.getValor(VARIABLE_PERIODO_ACTUALES);
		Date fehaPeriodos = dateFormat.parse(fechaPeriodos);
			
		Predicate onStart = cb.greaterThanOrEqualTo(root.get("fechaIni"), fehaPeriodos);
		Predicate todosPredicate = cb.equal(root.get("idCatPeriodo"), idPeriodoTodos);
		Predicate predicateOr = cb.or(onStart, todosPredicate);	
		
		List<Predicate> listPredicate = new ArrayList<>();
		if (roles != null) {
			for (CatRolDto rol : roles) {
				Predicate predicate =  cb.equal(root.get("idCatPerfil"), rol.getId() );
				listPredicate.add(predicate);
			}
		}

		Predicate[] predicates = new Predicate[listPredicate.size()];
		listPredicate.toArray(predicates);
		
		Predicate pIn2 = cb.or(predicates);

		Predicate pFinal =  cb.and(pActivo, predicateOr, pIn, pIn2);
		
		List<Order> orders = new ArrayList<Order>();
		orders.add(cb.asc(root.get("orden")));
		orders.add(cb.desc(root.get("idCatPeriodo")));
		
        cq.where(pFinal);
        cq.orderBy(orders);
        listPeriodo = em.createQuery(cq).getResultList();
		return listPeriodo;
	}
	
	//2024-10-15 RMT Valida una OC a futuro
	@Override
	public boolean isOrdenCompraDespuesPeriodo(PeriodoDto entityPeriodo, String fechaRecepcionOrdenCompra) throws ParseException {
		Date fechaFin = entityPeriodo.getFechaFin();
		
		try {
			Date fechaR = dateFormat.parse(fechaRecepcionOrdenCompra);
			
			 
			if (fechaR.after(fechaFin)) {
				return true;
			}
			
		} catch (ParseException e) {
			throw e;
		}
		
		return false;
	}
	
}
