package com.sodimac.bctfacturacion.service;

import java.util.List;

import com.sodimac.bctfacturacion.model.DevolucionCabecera;
import com.sodimac.bctfacturacion.model.DevolucionDetalle;
import com.sodimac.bctfacturacion.model.VentasBct;

public interface DevolucionesBctService {
	
	public List<VentasBct> findVentasPorAnio(String pFecha, String pFechaFin);
	
	public Integer getTotalDevolucionesCabecera(String fecha, Integer tienda);

	public Integer getTotalDevolucionDetalle(String pFecha, Integer pTienda);

	public List<DevolucionCabecera> getDevolucionesCabecera(String fecha, Integer tienda);

	public List<DevolucionDetalle> getDevolucionDetalle(String pFecha, Integer pTienda);
}
