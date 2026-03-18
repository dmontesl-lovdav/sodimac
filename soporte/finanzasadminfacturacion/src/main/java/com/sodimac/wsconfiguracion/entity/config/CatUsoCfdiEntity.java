package com.sodimac.wsconfiguracion.entity.config;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(
   name = "catusoscfdi"
)
public class CatUsoCfdiEntity {
   @Id
   @GeneratedValue(
      strategy = GenerationType.IDENTITY
   )
   @Column(
      name = "idUsoCfdi"
   )
   private Integer idUsoCfdi;
   @Column
   private String clave;
   @Column
   private String descripcionUso;
   @Column
   private Boolean activo;

   public Integer getIdUsoCfdi() {
      return this.idUsoCfdi;
   }

   public void setIdUsoCfdi(Integer idUsoCfdi) {
      this.idUsoCfdi = idUsoCfdi;
   }

   public String getClave() {
      return this.clave;
   }

   public void setClave(String clave) {
      this.clave = clave;
   }

   public String getDescripcionUso() {
      return this.descripcionUso;
   }

   public void setDescripcionUso(String descripcionUso) {
      this.descripcionUso = descripcionUso;
   }

   public Boolean getActivo() {
      return this.activo;
   }

   public void setActivo(Boolean activo) {
      this.activo = activo;
   }
}
