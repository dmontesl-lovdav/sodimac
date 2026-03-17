package com.sodimac.batch.fiscal.download.model.entity.batch;

import javax.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ctrlProcesoCab")
public class CtrlProcesoCabEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ejecucion")
    private Integer idEjecucion;

    @Column(name = "id_proceso", nullable = false)
    private Integer idProceso;

    @Column(name = "registros_origen")
    private Integer registrosOrigen;

    @Column(name = "registros_destino")
    private Integer registrosDestino;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_final")
    private LocalDateTime fechaFinal;

    @Column(name = "estatus", nullable = false, length = 20)
    private String estatus;

    @Column(name = "duracion_segundos", insertable = false, updatable = false)
    private Integer duracionSegundos;

    @Column(name = "mensaje_error", length = 1000)
    private String mensajeError;

    public CtrlProcesoCabEntity() {}

    public Integer getIdEjecucion() { return idEjecucion; }
    public void setIdEjecucion(Integer idEjecucion) { this.idEjecucion = idEjecucion; }

    public Integer getIdProceso() { return idProceso; }
    public void setIdProceso(Integer idProceso) { this.idProceso = idProceso; }

    public Integer getRegistrosOrigen() { return registrosOrigen; }
    public void setRegistrosOrigen(Integer registrosOrigen) { this.registrosOrigen = registrosOrigen; }

    public Integer getRegistrosDestino() { return registrosDestino; }
    public void setRegistrosDestino(Integer registrosDestino) { this.registrosDestino = registrosDestino; }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDateTime getFechaFinal() { return fechaFinal; }
    public void setFechaFinal(LocalDateTime fechaFinal) { this.fechaFinal = fechaFinal; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }

    public Integer getDuracionSegundos() { return duracionSegundos; }
    public void setDuracionSegundos(Integer duracionSegundos) { this.duracionSegundos = duracionSegundos; }

    public String getMensajeError() { return mensajeError; }
    public void setMensajeError(String mensajeError) { this.mensajeError = mensajeError; }
}
