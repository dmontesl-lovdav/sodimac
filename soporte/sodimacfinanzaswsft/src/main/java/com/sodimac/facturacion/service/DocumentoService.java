package com.sodimac.facturacion.service;

import java.text.ParseException;

public interface DocumentoService {
	public boolean validarExpresionRegular(String documento);
	public String obtenerTicketOrdenCompra(String ordenCompra) throws ParseException;
}
