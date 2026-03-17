package com.sodimac.batch.fiscal.download.model.entity.batch;

import javax.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ctrlProcesoElemento")
public class CtrlProcesoElementoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idElemento")
    private Integer idElemento;

    @Column(name = "id_ejecucion", nullable = false)
    private Integer idEjecucion;

    @Column(name = "valor", nullable = false, length = 50)
    private String valor;

    @Column(name = "valorAlterno", length = 250)
    private String valorAlterno;

    @Column(name = "secuencia", nullable = false)
    private Integer secuencia;

    @Column(name = "estatus", nullable = false, length = 20)
    private String estatus;

    @Column(name = "fechaRegistro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "detalle_error", length = 500)
    private String detalleError;

    public CtrlProcesoElementoEntity() {}

    public Integer getIdElemento() { return idElemento; }
    public void setIdElemento(Integer idElemento) { this.idElemento = idElemento; }

    public Integer getIdEjecucion() { return idEjecucion; }
    public void setIdEjecucion(Integer idEjecucion) { this.idEjecucion = idEjecucion; }

    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }

    public String getValorAlterno() { return valorAlterno; }
    public void setValorAlterno(String valorAlterno) { this.valorAlterno = valorAlterno; }

    public Integer getSecuencia() { return secuencia; }
    public void setSecuencia(Integer secuencia) { this.secuencia = secuencia; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getDetalleError() { return detalleError; }
    public void setDetalleError(String detalleError) { this.detalleError = detalleError; }
}
