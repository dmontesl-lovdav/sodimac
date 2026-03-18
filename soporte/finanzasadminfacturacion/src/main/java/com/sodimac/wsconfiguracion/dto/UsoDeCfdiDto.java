package com.sodimac.wsconfiguracion.dto;

public class UsoDeCfdiDto {
   private int idUsoCfdi;
   private String clave;
   private String descripcionUso;
   private int activo;

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

   public int getActivo() {
      return this.activo;
   }

   public void setActivo(int activo) {
      this.activo = activo;
   }
}
