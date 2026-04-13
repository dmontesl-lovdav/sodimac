package com.sodimac.rebates.service;

import com.sodimac.rebates.dto.CatRolDto;
import com.sodimac.rebates.dto.CatTipoRebateDto;
import com.sodimac.rebates.dto.PeriodoDto;
import com.sodimac.rebates.dto.RelPeriodoTipoRebateDto;
import com.sodimac.rebates.enums.EEstatusPeriodo;
import com.sodimac.rebates.model.Generic;
import com.sodimac.rebates.model.Periodo;
import com.sodimac.rebates.model.PeriodoRol;
import com.sodimac.rebates.model.ProgramaPago;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

public interface IPeriodoService {

	PeriodoDto getById(Integer idCatPeriodo);

	List<Periodo> getAll();

	List<Periodo> getActive();
	
	List<Periodo> getActiveActuales() throws ParseException;
	
	List<Periodo> getActiveActualesSinTodos() throws ParseException;	
	
	List<Periodo> getActiveOrderByDesc() throws ParseException;

	List<Periodo> getActiveAndEstatus();
	
	List<Periodo> getPeriodoAbierto() throws ParseException;
	
	List<Periodo> getPeriodoAbiertoSinTodos() throws ParseException;

	List<PeriodoDto> getPeriodoBetweenFechasAndDetallePeriodoLike(Date fechaIni, Date fechaFin, String detallePeriodo);

	List<PeriodoDto> getPeriodoByOptions(Date fechaIni, Date fechaFin, ProgramaPago programaPago, String detallePeriodo);

	Generic getPeriodoEnProceso(Integer id);

	Generic getRequired(Integer id);

	Generic processPeriodo(Integer id, Integer estatusDestino, Integer idUser);

	boolean reprocesarPeriodo(Integer id, Integer idUser);

	boolean deletePeriodo(Integer id);

	void save(PeriodoDto periodo);

	Generic getRequiredProcesarPeriodo(Integer idCatPeriodo);	
	
	void editRelacion(List<RelPeriodoTipoRebateDto> listRelacion);

	public RelPeriodoTipoRebateDto existeRelacion(PeriodoDto periodo, CatTipoRebateDto tipoRebate);	
	
	public int getIdPeriodoTodos();
	
	boolean isPeriodoTodos(Integer idPeriodoCat);
	
	public boolean isPeriodoVigente(PeriodoDto entityPeriodo);
	
	public boolean isFechaRecepcionDentroPeriodo(PeriodoDto entityPeriodo, Date fechaRecepcion);
	
	public List<Periodo> getPeriodosTerminadosAndContabilizados() throws ParseException;
	
	public List<Periodo> getPeriodosSinTodos(List<EEstatusPeriodo> listEstatus) throws ParseException;

	public List<Periodo> getPeriodosTerminados() throws ParseException;

	public void saveOrUpdate(PeriodoDto periodo);
	
	public List<PeriodoRol> getActiveOrderByDesc(List<CatRolDto> roles) throws ParseException;
	
	public List<PeriodoRol> getPeriodoAbierto(List<CatRolDto> roles) throws ParseException;

	public boolean isOrdenCompraDespuesPeriodo(PeriodoDto entityPeriodo, String fechaRecepcionOrdenCompra) throws ParseException;

}
