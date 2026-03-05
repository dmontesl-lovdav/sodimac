package com.sodimac.batch.process.model.entity.batch;

import javax.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ctrlProcesoDet")
public class CtrlProcesoDetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_flujo")
    private Integer idFlujo;

    @Column(name = "id_ejecucion", nullable = false)
    private Integer idEjecucion;

    @Column(name = "nombre_paso", nullable = false, length = 100)
    private String nombrePaso;

    @Column(name = "secuencia", nullable = false)
    private Integer secuencia;

    @Column(name = "fecha_inicio_registro", nullable = false)
    private LocalDateTime fechaInicioRegistro;

    @Column(name = "fecha_final_registro")
    private LocalDateTime fechaFinalRegistro;

    @Column(name = "parametros_registro", length = 255)
    private String parametrosRegistro;

    @Column(name = "detalle", length = 255)
    private String detalle;

    @Column(name = "registros_procesados")
    private Integer registrosProcesados;

    @Column(name = "estatus", nullable = false, length = 20)
    private String estatus;

    public CtrlProcesoDetEntity() {}

    public Integer getIdFlujo() { return idFlujo; }
    public void setIdFlujo(Integer idFlujo) { this.idFlujo = idFlujo; }

    public Integer getIdEjecucion() { return idEjecucion; }
    public void setIdEjecucion(Integer idEjecucion) { this.idEjecucion = idEjecucion; }

    public String getNombrePaso() { return nombrePaso; }
    public void setNombrePaso(String nombrePaso) { this.nombrePaso = nombrePaso; }

    public Integer getSecuencia() { return secuencia; }
    public void setSecuencia(Integer secuencia) { this.secuencia = secuencia; }

    public LocalDateTime getFechaInicioRegistro() { return fechaInicioRegistro; }
    public void setFechaInicioRegistro(LocalDateTime fechaInicioRegistro) { this.fechaInicioRegistro = fechaInicioRegistro; }

    public LocalDateTime getFechaFinalRegistro() { return fechaFinalRegistro; }
    public void setFechaFinalRegistro(LocalDateTime fechaFinalRegistro) { this.fechaFinalRegistro = fechaFinalRegistro; }

    public String getParametrosRegistro() { return parametrosRegistro; }
    public void setParametrosRegistro(String parametrosRegistro) { this.parametrosRegistro = parametrosRegistro; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }

    public Integer getRegistrosProcesados() { return registrosProcesados; }
    public void setRegistrosProcesados(Integer registrosProcesados) { this.registrosProcesados = registrosProcesados; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }
}
