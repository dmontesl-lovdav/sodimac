package com.sodimac.wsconfiguracion.dto;

import java.time.LocalDate;

public class ConfDatosEmisorTiendaDtoVM {
   private Integer id;
   private Integer idConfDatosEmisor;
   private String emisor;
   private Integer idTienda;
   private String descripcion;
   private String calle;
   private String noExterior;
   private String noInterior;
   private String colonia;
   private String localidad;
   private String referencia;
   private String municipio;
   private String estado;
   private Integer idCatCodigoPostal;
   private Integer idCatTipoTienda;
   private String tipoTienda;
   private Boolean activo;
   private LocalDate fechaInicio;

   public ConfDatosEmisorTiendaDtoVM() {
   }

   public ConfDatosEmisorTiendaDtoVM(Integer id, Integer idConfDatosEmisor, String emisor, Integer idTienda, String descripcion, String calle, String noExterior, String noInterior, String colonia, String localidad, String referencia, String municipio, String estado, Integer idCatCodigoPostal, Integer idCatTipoTienda, String tipoTienda, Boolean activo, LocalDate fechaInicio) {
      this.id = id;
      this.idConfDatosEmisor = idConfDatosEmisor;
      this.emisor = emisor;
      this.idTienda = idTienda;
      this.descripcion = descripcion;
      this.calle = calle;
      this.noExterior = noExterior;
      this.noInterior = noInterior;
      this.colonia = colonia;
      this.localidad = localidad;
      this.referencia = referencia;
      this.municipio = municipio;
      this.estado = estado;
      this.idCatCodigoPostal = idCatCodigoPostal;
      this.idCatTipoTienda = idCatTipoTienda;
      this.tipoTienda = tipoTienda;
      this.activo = activo;
      this.fechaInicio = fechaInicio;
   }

   public Integer getId() {
      return this.id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public Integer getIdTienda() {
      return this.idTienda;
   }

   public void setIdTienda(Integer idTienda) {
      this.idTienda = idTienda;
   }

   public String getDescripcion() {
      return this.descripcion;
   }

   public void setDescripcion(String descripcion) {
      this.descripcion = descripcion;
   }

   public String getCalle() {
      return this.calle;
   }

   public void setCalle(String calle) {
      this.calle = calle;
   }

   public String getEmisor() {
      return this.emisor;
   }

   public void setEmisor(String emisor) {
      this.emisor = emisor;
   }

   public String getNoExterior() {
      return this.noExterior;
   }

   public void setNoExterior(String noExterior) {
      this.noExterior = noExterior;
   }

   public String getNoInterior() {
      return this.noInterior;
   }

   public void setNoInterior(String noInterior) {
      this.noInterior = noInterior;
   }

   public String getColonia() {
      return this.colonia;
   }

   public void setColonia(String colonia) {
      this.colonia = colonia;
   }

   public String getLocalidad() {
      return this.localidad;
   }

   public void setLocalidad(String localidad) {
      this.localidad = localidad;
   }

   public String getReferencia() {
      return this.referencia;
   }

   public void setReferencia(String referencia) {
      this.referencia = referencia;
   }

   public String getMunicipio() {
      return this.municipio;
   }

   public void setMunicipio(String municipio) {
      this.municipio = municipio;
   }

   public String getEstado() {
      return this.estado;
   }

   public void setEstado(String estado) {
      this.estado = estado;
   }

   public Integer getIdCatCodigoPostal() {
      return this.idCatCodigoPostal;
   }

   public void setIdCatCodigoPostal(Integer idCatCodigoPostal) {
      this.idCatCodigoPostal = idCatCodigoPostal;
   }

   public Boolean getActivo() {
      return this.activo;
   }

   public void setActivo(Boolean activo) {
      this.activo = activo;
   }

   public String getTipoTienda() {
      return this.tipoTienda;
   }

   public void setTipoTienda(String tipoTienda) {
      this.tipoTienda = tipoTienda;
   }

   public Integer getIdConfDatosEmisor() {
      return this.idConfDatosEmisor;
   }

   public void setIdConfDatosEmisor(Integer idConfDatosEmisor) {
      this.idConfDatosEmisor = idConfDatosEmisor;
   }

   public Integer getIdCatTipoTienda() {
      return this.idCatTipoTienda;
   }

   public void setIdCatTipoTienda(Integer idCatTipoTienda) {
      this.idCatTipoTienda = idCatTipoTienda;
   }

   public LocalDate getFechaInicio() {
      return this.fechaInicio;
   }

   public void setFechaInicio(LocalDate fechaInicio) {
      this.fechaInicio = fechaInicio;
   }
}
