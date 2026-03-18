package com.sodimac.wsconfiguracion.entity.config;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

@Entity
@Table(
   name = "catusoscfdi33"
)
public class CatUsosCfdi33Entity {
   @Id
   @GeneratedValue(
      strategy = GenerationType.IDENTITY
   )
   @Column(
      name = "idUsoCfdi"
   )
   private int idUsoCfdi;
   @Column(
      name = "clave"
   )
   private String clave;
   @Column(
      name = "descripcionUso"
   )
   private String descripcionUso;
   @Column(
      name = "fisica",
      nullable = false,
      columnDefinition = "BIT",
      length = 1
   )
   @Type(
      type = "org.hibernate.type.NumericBooleanType"
   )
   private boolean fisica = false;
   @Column(
      name = "moral",
      nullable = false,
      columnDefinition = "BIT",
      length = 1
   )
   @Type(
      type = "org.hibernate.type.NumericBooleanType"
   )
   private boolean moral = false;
   @Column(
      name = "activo",
      nullable = false,
      columnDefinition = "BIT",
      length = 1
   )
   @Type(
      type = "org.hibernate.type.NumericBooleanType"
   )
   private boolean activo = true;

   public int getIdUsoCfdi() {
      return this.idUsoCfdi;
   }

   public void setIdUsoCfdi(int idUsoCfdi) {
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

   public boolean isFisica() {
      return this.fisica;
   }

   public void setFisica(boolean fisica) {
      this.fisica = fisica;
   }

   public boolean isMoral() {
      return this.moral;
   }

   public void setMoral(boolean moral) {
      this.moral = moral;
   }

   public boolean isActivo() {
      return this.activo;
   }

   public void setActivo(boolean activo) {
      this.activo = activo;
   }

   public String toString() {
      return "CatUsosCfdiEntity [idUsoCfdi=" + this.idUsoCfdi + ", clave=" + this.clave + ", descripcionUso=" + this.descripcionUso + ", fisica=" + this.fisica + ", moral=" + this.moral + ", activo=" + this.activo + "]";
   }
}
