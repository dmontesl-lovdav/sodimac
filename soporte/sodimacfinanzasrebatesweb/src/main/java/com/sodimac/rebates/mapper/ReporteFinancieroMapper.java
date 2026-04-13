package com.sodimac.rebates.mapper;

import java.util.ArrayList;
import java.util.List;

import com.sodimac.rebates.dto.ReporteFinancieroDto;
import com.sodimac.rebates.model.ReporteFinancieroEntity;

public final class ReporteFinancieroMapper {
	
	private ReporteFinancieroMapper() {
		super();
	}

	public static ReporteFinancieroDto convertToDto(ReporteFinancieroEntity entity) {
		ReporteFinancieroDto dto = new ReporteFinancieroDto();
		dto.setRowNumber( entity.getRowNumber() );
		dto.setIdRegistro( entity.getIdRegistro() );
		dto.setSociedad( entity.getSociedad() );
		dto.setFechaDocumento( entity.getFechaDocumento() );
		dto.setFechaContabilizacion( entity.getFechaContabilizacion() ); 
		dto.setTipoDocumento( entity.getTipoDocumento() );
		dto.setReferenciaFact( entity.getReferenciaFact() );
		dto.setReferenciaEjercicio( entity.getReferenciaEjercicio() );
		dto.setReferenciaPosicion( entity.getReferenciaPosicion() );
		dto.setNoContrato( entity.getNoContrato() );
		dto.setPeriodo( entity.getPeriodo() );
		dto.setReferencia( entity.getReferencia() );
		dto.setTextoCabecera( entity.getTextoCabecera() );
		dto.setMoneda( entity.getMoneda() );
		dto.setFechaConversion( entity.getFechaConversion() );
		dto.setClaveContabilizacion( entity.getClaveContabilizacion() );
		dto.setCuenta( entity.getCuenta() );
		dto.setIndicadorCME( entity.getIndicadorCME() );
		dto.setClaseMovimiento( entity.getClaseMovimiento() );
		dto.setImporte( entity.getImporte() );
		dto.setImporteImpuestos( entity.getImporteImpuestos() );
		dto.setCalcularImpuestos( entity.getCalcularImpuestos() );
		dto.setIndicadorImpuestos( entity.getIndicadorImpuestos() );
		dto.setCentroBeneficios( entity.getCentroBeneficios() );
		dto.setCentroCoste( entity.getCentroCoste() );
		dto.setOrden( entity.getOrden() );
		dto.setElementoPEP( entity.getElementoPEP() );
		dto.setSegmento( entity.getSegmento() );
		dto.setCondicionPago( entity.getCondicionPago() );
		dto.setFechaBase( entity.getFechaBase() );
		dto.setMetodoPago( entity.getMetodoPago() );
		dto.setBloqueoPago( entity.getBloqueoPago() );
		dto.setArticulo( entity.getArticulo() );
		dto.setCantidad( entity.getCantidad() );
		dto.setUnidadMedida( entity.getUnidadMedida() );
		dto.setAsignacion( entity.getUnidadMedida() );
		dto.setTexto( entity.getTexto() );
		dto.setReferencia1( entity.getReferencia1() );
		dto.setReferencia2( entity.getReferencia2() );
		dto.setReferencia3( entity.getReferencia3() );
		dto.setFechaValor( entity.getFechaValor() );
		dto.setTipoCambio( entity.getTipoCambio() );
		dto.setIdCatPeriodo( entity.getIdCatPeriodo() );
		dto.setProgramaPago( entity.getProgramaPago() );
		dto.setRebate( entity.getRebate() );
		dto.setNumeroProveedor( entity.getNumeroProveedor() );
		dto.setProveedor( entity.getProveedor() );
		return dto;
	}
	
	public static ReporteFinancieroEntity convertToEntity(ReporteFinancieroDto dto) {
		ReporteFinancieroEntity entity = new ReporteFinancieroEntity();
		entity.setRowNumber( dto.getRowNumber() );
		entity.setIdRegistro( dto.getIdRegistro() );
		entity.setSociedad( dto.getSociedad() );
		entity.setFechaDocumento( dto.getFechaDocumento() );
		entity.setFechaContabilizacion( dto.getFechaContabilizacion() );
		entity.setTipoDocumento( dto.getTipoDocumento() );
		entity.setReferenciaFact( dto.getReferenciaFact() );
		entity.setReferenciaEjercicio( dto.getReferenciaEjercicio() );
		entity.setReferenciaPosicion( dto.getReferenciaPosicion() );
		entity.setNoContrato( dto.getNoContrato() );
		entity.setPeriodo( dto.getPeriodo() );
		entity.setReferencia( dto.getReferencia() );
		entity.setTextoCabecera( dto.getTextoCabecera() );
		entity.setMoneda( dto.getMoneda() );
		entity.setFechaConversion( dto.getFechaConversion() );
		entity.setClaveContabilizacion( dto.getClaveContabilizacion() );
		entity.setCuenta( dto.getCuenta() );
		entity.setIndicadorCME( dto.getIndicadorCME() );
		entity.setClaseMovimiento( dto.getClaseMovimiento() );
		entity.setImporte( dto.getImporte() );
		entity.setImporteImpuestos( dto.getImporteImpuestos() );
		entity.setCalcularImpuestos( dto.getCalcularImpuestos() );
		entity.setIndicadorImpuestos( dto.getIndicadorImpuestos() );
		entity.setCentroBeneficios( dto.getCentroBeneficios() );
		entity.setCentroCoste( dto.getCentroCoste() );
		entity.setOrden( dto.getOrden() );
		entity.setElementoPEP( dto.getElementoPEP() );
		entity.setSegmento( dto.getSegmento() );
		entity.setCondicionPago( dto.getCondicionPago() );
		entity.setFechaBase( dto.getFechaBase() );
		entity.setMetodoPago( dto.getMetodoPago() );
		entity.setBloqueoPago( dto.getBloqueoPago() );
		entity.setArticulo( dto.getArticulo() );
		entity.setCantidad( dto.getCantidad() );
		entity.setUnidadMedida( dto.getUnidadMedida() );
		entity.setAsignacion( dto.getUnidadMedida() );
		entity.setTexto( dto.getTexto() );
		entity.setReferencia1( dto.getReferencia1() );
		entity.setReferencia2( dto.getReferencia2() );
		entity.setReferencia3( dto.getReferencia3() );
		entity.setFechaValor( dto.getFechaValor() );
		entity.setTipoCambio( dto.getTipoCambio() );
		entity.setIdCatPeriodo( dto.getIdCatPeriodo() );
		entity.setProgramaPago( dto.getProgramaPago() );
		entity.setRebate( dto.getRebate() );
		entity.setNumeroProveedor( dto.getNumeroProveedor() );
		entity.setProveedor( dto.getProveedor() );
		return entity;
	}
	
	public static List<ReporteFinancieroDto> convertToDtos(List<ReporteFinancieroEntity> entities) {
		List<ReporteFinancieroDto> dtos = new ArrayList<>();
		if (entities != null) {
			for (ReporteFinancieroEntity entity : entities) {
				dtos.add( convertToDto(entity) );
			}
		}
		return dtos;
	}
}
