package com.sodimac.bctfacturacion.entity.ces;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "ControVentaCes")
public class ControVentaCesEntity {
 
  @Id
  @Column(name = "idControlVentaCes")
  private Integer idControlVentaCes;
  
  @Column(name = "fechaTrx")
  private Date fechaTrx;
  
  @Column(name = "estatus")
  private Integer estatus;
  
  @Column(name = "fechaRegistro")
  private Date fechaRegistro;
  
  
}

