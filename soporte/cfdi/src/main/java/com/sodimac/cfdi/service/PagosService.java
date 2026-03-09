package com.sodimac.cfdi.service;

import java.util.List;

import com.sodimac.cfdi.entity.fiscal.PagosEntity;
import com.sodimac.cfdi.models.PagosModel;

public interface PagosService {
	
	public List<PagosEntity> getAllPagos();

	public String getStatusPagos(String tipoPago);
	
	public List<PagosModel> getPagosByParams(String fechaInicial, String fechaFinal, int start, int rowsPerPage,String estatusPago, Double pmonto);

	public String cambiarEstatusPago(int idPago, String estatusPago);

	public boolean getPagosExcelFechas(String fechaInicial, String fechaFinal, String nombreArchivo, String estatusPago, Double pmonto);

	public String cambiarFolioCliente(int idPago, String folioCliente);

}
