package com.sodimac.rebates.mapper;

import com.sodimac.rebates.dto.RebateProveedorDto;
import com.sodimac.rebates.model.entity.RebateProveedorEntity;

public final class RebateProveedorMapper {

	public static RebateProveedorDto convertDto(RebateProveedorEntity entity) {
		RebateProveedorDto dto = new RebateProveedorDto();
		if (entity != null) {
			dto.setIdRebateProveedor( entity.getIdRebateProveedor() );
			dto.setNumProveedor( entity.getCodigoProveedor() );
			dto.setNomProveedor( entity.getNombreProveedor() );
			dto.setOrigen( entity.getOrigen() );
			dto.setRutv(entity.getRutv() );
			dto.setRfc( entity.getRfc() );
			dto.setCorreo( entity.getCorreo() );
			dto.setRegimenFiscal( entity.getRegimenFiscal() );
			dto.setCodigoPostal( entity.getCodigoPostal() );
		}
		return dto;
	}
}
