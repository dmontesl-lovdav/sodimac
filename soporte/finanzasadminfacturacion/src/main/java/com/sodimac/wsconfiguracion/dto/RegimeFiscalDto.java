package com.sodimac.wsconfiguracion.dto;

import java.util.ArrayList;
import java.util.List;

public class RegimeFiscalDto {

	private List<CatRegimenFiscalDto> regimenFiscal = new ArrayList<CatRegimenFiscalDto>();
	
	public RegimeFiscalDto() {}
	
	public RegimeFiscalDto(List<CatRegimenFiscalDto> regimenFiscalDto) {
		this.setRegimenFiscal(regimenFiscalDto);
	}


	public List<CatRegimenFiscalDto> getRegimenFiscal() {
		return regimenFiscal;
	}

	public void setRegimenFiscal(List<CatRegimenFiscalDto> regimenFiscal) {
		this.regimenFiscal = regimenFiscal;
	}
	
	
}
