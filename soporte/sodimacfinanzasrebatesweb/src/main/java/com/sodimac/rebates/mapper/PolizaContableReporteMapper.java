package com.sodimac.rebates.mapper;

import java.util.ArrayList;
import java.util.List;

import com.sodimac.rebates.dto.PolizaContableReporteDto;
import com.sodimac.rebates.model.PolizaContableReporteEntity;

public final class PolizaContableReporteMapper {

	private PolizaContableReporteMapper() {
		super();
	}
	
	public static PolizaContableReporteDto convertToDto(PolizaContableReporteEntity entity) {
		PolizaContableReporteDto dto = new PolizaContableReporteDto();
		dto.setId( entity.getId() );
		dto.setEmpresa( entity.getEmpresa() );
		dto.setFechaDocumento( entity.getFechaDocumento() );
		dto.setReferenciaDocumento( entity.getReferenciaDocumento() );
		dto.setNumeroDocumento( entity.getNumeroDocumento() );
		dto.setMoneda( entity.getMoneda() );
		dto.setTipoCambio( entity.getTipoCambio() );
		dto.setDebitoCredito( entity.getDebitoCredito() );
		dto.setCuentaContable( entity.getCuentaContable() );
		dto.setCodigoProveedor( entity.getCodigoProveedor() );
		dto.setImporte( entity.getImporte() );
		dto.setSucursal( entity.getSucursal() );
		dto.setCondicionPago( entity.getCondicionPago() );
		dto.setFechaVencimiento( entity.getFechaVencimiento() );
		dto.setBloqueoPago( entity.getBloqueoPago() );
		dto.setSistemaOrigen( entity.getSistemaOrigen() );
		dto.setFechaEnvio( entity.getFechaEnvio() );
		dto.setFechaContable( entity.getFechaContable() );
		dto.setClaseDocumento( entity.getClaseDocumento() );
		dto.setNumeroReferencia( entity.getNumeroReferencia() );
		dto.setCentroCosto( entity.getCentroCosto() );
		dto.setCentroBeneficio( entity.getCentroBeneficio() );
		dto.setNumeroUuid( entity.getNumeroUuid() );
		dto.setFlagEnviado( entity.getFlagEnviado() );
		dto.setFechaRecepcion( entity.getFechaRecepcion() );
		dto.setTipoDocumento( entity.getTipoDocumento() );
		dto.setOrigenEtl( entity.getOrigenEtl() );
		dto.setIdPeriodo( entity.getIdPeriodo() );
		dto.setFechaInicioPeriodo( entity.getFechaInicioPeriodo() );
		dto.setFechaFinPeriodo( entity.getFechaFinPeriodo() );
		dto.setIdTipoRebate( entity.getIdTipoRebate() );
		dto.setTipoRebate( entity.getTipoRebate() );
		dto.setTimbrado( entity.getTimbrado() );
		dto.setMontoCalculado( entity.getMontoCalculado() );
		dto.setMontoContabilizado( entity.getMontoContabilizado() );
		return dto;
	}
	
	public static List<PolizaContableReporteDto> convertToDtos(List<PolizaContableReporteEntity> entities) {
		List<PolizaContableReporteDto> dtos = new ArrayList<>();
		if (entities != null) {
			for (PolizaContableReporteEntity entity : entities) {
				dtos.add( convertToDto(entity) );
			}
		}
		return dtos;
	}
}
