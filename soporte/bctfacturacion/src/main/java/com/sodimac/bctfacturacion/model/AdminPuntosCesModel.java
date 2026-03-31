package com.sodimac.bctfacturacion.model;

import java.util.Date;

import lombok.Data;

@Data
public class AdminPuntosCesModel {

	private Integer idAdminPuntoCes;
	private String ticket;
    private String fechaVenta;
    private int puntos;
    private Double montoPuntos;
    private Double montoVenta;
    private int tienda;
    private Date fechaRegistro;
    private Integer tipoTransaccion;
    private int estatus;
    private int estatusContable;
    private Date fechaActualizacion;
	
    private Double montoTotalSinImpuestos;
    private Double montoRedondeo;
    private String id;
    private String tipoTransaccionCes;
    
}
