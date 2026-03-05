package com.sodimac.batch.process.model.entity.batch;

import javax.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ctrlLog")
public class CtrlLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Integer idLog;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "id_ejecucion")
    private Integer idEjecucion;

    @Column(name = "log", columnDefinition = "TEXT")
    private String log;

    @Column(name = "nivel", nullable = false, length = 20)
    private String nivel;

    @Column(name = "fase", length = 50)
    private String fase;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    public CtrlLogEntity() {}

    public Integer getIdLog() { return idLog; }
    public void setIdLog(Integer idLog) { this.idLog = idLog; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getIdEjecucion() { return idEjecucion; }
    public void setIdEjecucion(Integer idEjecucion) { this.idEjecucion = idEjecucion; }

    public String getLog() { return log; }
    public void setLog(String log) { this.log = log; }

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    public String getFase() { return fase; }
    public void setFase(String fase) { this.fase = fase; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
