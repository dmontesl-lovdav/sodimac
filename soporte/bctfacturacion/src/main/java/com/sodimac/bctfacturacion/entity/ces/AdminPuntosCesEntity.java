package com.sodimac.bctfacturacion.entity.ces;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
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
@Table(name = "adminpuntosces")
public class AdminPuntosCesEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAdminPuntoCes")
    private Integer idAdminPuntoCes;
    
    @Column(name = "ticket")
    private String ticket;
    
    @Basic(optional = false)
    @Column(name = "fechaVenta")
    @Temporal(TemporalType.DATE)
    private Date fechaVenta;
    
    @Basic(optional = false)
    @Column(name = "puntos")
    private int puntos;
    
    @Basic(optional = false)
    @Column(name = "montoPuntos")
    private Double montoPuntos;
    
    @Basic(optional = false)
    @Column(name = "montoVenta")
    private Double montoVenta;
    
    @Basic(optional = false)
    @Column(name = "tienda")
    private int tienda;
    
    @Basic(optional = false)
    @Column(name = "fechaRegistro")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaRegistro;
    
    @Basic(optional = false)
    @Column(name = "tipoTransaccion")
    private Integer tipoTransaccion;
    
    @Basic(optional = false)
    @Column(name = "tipoTransaccionCes")
    private String tipoTransaccionCes;
    
    @Basic(optional = false)
    @Column(name = "estatus")
    private int estatus;
    
    @Basic(optional = false)
    @Column(name = "estatusContable")
    private int estatusContable;
    
    @Column(name = "fechaActualizacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaActualizacion;
    
}
