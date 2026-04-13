package com.sodimac.rebates.service;

import java.util.List;
import com.sodimac.rebates.model.ProgramaPago;

public interface IProgramaPagoService {

	List<ProgramaPago> getAll();

	List<ProgramaPago> getActive();

}
