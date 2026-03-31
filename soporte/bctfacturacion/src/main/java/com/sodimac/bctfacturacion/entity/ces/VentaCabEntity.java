package com.sodimac.bctfacturacion.entity.ces;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

/**
 *
 * @author david.montes
 */
@Data
@Entity
@Table(name = "ventacab")
public class VentaCabEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @Column(name = "idVentaCab")
    private Integer idVentaCab;
    
    @Column(name = "idPuntosCes")
    private Long idPuntosCes;
    
    @Column(name = "ticket")
    private String ticket;
    
    @Column(name = "fechaVenta")
    @Temporal(TemporalType.DATE)
    private Date fechaVenta;
    
    @Column(name = "tienda")
    private int tienda;
    
    @Column(name = "tipoTransaccion")
    private int tipoTransaccion;
    
    @Column(name = "tipoTransaccionCes")
    private String tipoTransaccionCes;
    
    @Column(name = "montoTotalPagar")
    private Double montoTotalPagar;
    
    @Column(name = "montoTotalSinImpuestos")
    private Double montoTotalSinImpuestos;
    
    @Column(name = "montoRedondeo")
    private Double montoRedondeo;
    
    @Column(name = "fechaRegistro")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaRegistro;        
    
    @Basic(optional = false)
    @Column(name = "estatusContable")
    private int estatusContable;
    
    @Column(name = "fechaActualizacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaActualizacion;        
}
