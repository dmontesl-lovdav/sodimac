package com.sodimac.bctfacturacion.model;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class VentaCabModel {
	private Integer idVentaCab;
	private Long idPuntosCes;
    private String ticket;
    private Date fechaVenta;
    private int tienda;
    private int tipoTransaccion;
    private String tipoTransaccionCes;
    private Double montoTotalPagar;
    private Double montoTotalSinImpuestos;
    private Double montoRedondeo;
    private Date fechaRegistro;        
    private int estatusContable;
    private Date fechaActualizacion;
    private List<VentaDetImpuestoModel> detImpuestos;
}
