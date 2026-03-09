package com.sodimac.cfdi.service;

import java.io.IOException;
import java.util.List;

import com.sodimac.cfdi.models.ComisionesPagadasModel;

public interface ComisionesPagadasService {

	public List<ComisionesPagadasModel> obtenerComisionesPagadassByParams(String fechaInicial, String fechaFinal, String pTicket, int start, int rowsPerPage, Integer tienda);

	public byte[] createExcel(String fechaInicial, String fechaFinal, String pTicket, Integer tienda) throws IOException;
	
}
