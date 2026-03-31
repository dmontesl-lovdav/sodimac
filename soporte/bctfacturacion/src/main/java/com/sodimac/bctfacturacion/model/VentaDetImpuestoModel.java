package com.sodimac.bctfacturacion.model;

import java.util.Date;

import lombok.Data;

@Data
public class VentaDetImpuestoModel {

	private Integer idVentaDetImpuesto;
    private Integer idVentaCab;
    private String ticket;
    private Integer numLinea;
    private Integer ordenImpuesto;
    private String sku;
    private String dvSku;
    private String descripcion;
    private Integer puntos;
    private Double montoPuntos;
    private Double totalLinea;
    private Integer tipoImpuesto;
    private Integer idTasaImpuesto;
    private Double montoImpuesto;
    private Double porcentajeImpuesto;
    private Date fechaRegistro;
    
}
