package com.sodimac.rebates.service;

import java.text.ParseException;
import java.util.List;

import com.sodimac.rebates.dto.CalculoRebateMSIDto;
import com.sodimac.rebates.model.CalculoRebateMSI;
import com.sodimac.rebates.model.CalculoRebateMSIEntity;

public interface ICalculoRebateMSIService {

	public List<CalculoRebateMSIEntity> getCalculoRebateMSI(CalculoRebateMSI calculoRebateMSI);
	
	public List<CalculoRebateMSIDto> getCalculoRebateMSIView(CalculoRebateMSI calculoRebateMSI)
			throws ParseException;
	
}
