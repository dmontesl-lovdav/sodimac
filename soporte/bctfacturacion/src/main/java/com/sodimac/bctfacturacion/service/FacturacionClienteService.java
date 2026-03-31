package com.sodimac.bctfacturacion.service;

import com.sodimac.bctfacturacion.model.FacturaModel;

public interface FacturacionClienteService {

	public void registraFacturacionCliente(FacturaModel factura);

	public Integer existeTicket(String uuid);
}
