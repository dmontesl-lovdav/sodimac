package com.sodimac.rebates.service;

import java.text.ParseException;
import java.util.List;

import com.sodimac.rebates.dto.CalculoRebateMSIDto;
import com.sodimac.rebates.model.CalculoRebateMSI;
import com.sodimac.rebates.model.CalculoRebateMSI3Entity;

public interface ICalculoRebateMSI3Service {

	public List<CalculoRebateMSI3Entity> getCalculoRebateMSI(CalculoRebateMSI calculoRebateMSI);

	public List<CalculoRebateMSIDto> getCalculoRebateMSIView(CalculoRebateMSI calculoRebateMSI)
			throws ParseException;
	
}
