package com.sodimac.bctfacturacion.entity.ces;

import java.io.Serializable;
import java.util.Date;
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
@Table(name = "ventadetimpuesto")
public class VentaDetImpuestoEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
   @Column(name = "idVentaDetImpuesto")
    private Integer idVentaDetImpuesto;
    
    @Column(name = "idVentaCab")
    private Integer idVentaCab;
    
    @Column(name = "ticket")
    private String ticket;
    
    @Column(name = "numLinea")
    private Integer numLinea;
    
    @Column(name = "ordenImpuesto")
    private Integer ordenImpuesto;
    
    @Column(name = "sku")
    private String sku;
    
    @Column(name = "dvSku")
    private String dvSku;
    
    @Column(name = "descripcion")
    private String descripcion;
    
    @Column(name = "puntos")
    private Integer puntos;
    
    @Column(name = "montoPuntos")
    private Double montoPuntos;

    @Column(name = "totalLinea")
    private Double totalLinea;
    
    @Column(name = "tipoImpuesto")
    private Integer tipoImpuesto;
    
    @Column(name = "idTasaImpuesto")
    private Integer idTasaImpuesto;
    
    @Column(name = "montoImpuesto")
    private Double montoImpuesto;
    
    @Column(name = "porcentajeImpuesto")
    private Double porcentajeImpuesto;
    
    @Column(name = "fechaRegistro")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaRegistro;

}
