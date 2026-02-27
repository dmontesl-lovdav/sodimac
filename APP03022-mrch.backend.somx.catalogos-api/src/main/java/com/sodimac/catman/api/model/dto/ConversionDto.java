package com.sodimac.catman.api.model.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConversionDto {
    private Integer idConversion;
    private Integer idElementoOrigen;
    private String elementoOrigen;
    private String valorElementoOrigen;
    private String estatusElementoOrigen;
    private String catalogoElementoOrigen;
    private Integer idElemento;
    private String elemento;
    private String valor;
    private String catalogoOrigen;
    private LocalDate fechaInicioVigencia;
    private LocalDate fechaFinVigencia;
    private String estatus;
    private Boolean esPrincipal;
    private String idUsuarioRegistro;
    private LocalDateTime fechaRegistro;
    private String idUsuarioActualizacion;
    private LocalDateTime fechaActualizacion;
}

