package com.sodimac.bctfacturacion.service;

import java.text.ParseException;
import java.util.List;

import com.sodimac.bctfacturacion.model.VentaDetImpuestoModel;

public interface IVentaDetImpuestoService {
	
	public Integer obtnerIdVentaDetImpuesto();

	public boolean existeVentaDetImpuesto(String ticket, int linea);
	
	public List<VentaDetImpuestoModel> getVentasDet(String ticket); 
	
	public void guardar(List<VentaDetImpuestoModel> dtos) throws ParseException;

	public void guardar(VentaDetImpuestoModel ventaDetImp) throws ParseException;
	
}
