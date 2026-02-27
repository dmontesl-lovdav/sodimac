package com.sodimac.fiscal.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PacCatalogDto {
    private Long pacId;
    
    @NotBlank(message = "El nombre del PAC es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    private String name;
    
    @Size(max = 254, message = "La descripción no puede exceder 254 caracteres")
    private String description;

    @Min(value = 0, message = "La prioridad debe ser mayor o igual a 0")
    private Integer priority;

    @Size(max = 100, message = "El nombre de usuario no puede exceder 100 caracteres")
    private String userName;

    @Size(max = 254, message = "La contraseña no puede exceder 254 caracteres")
    private String password;

    @Size(max = 254, message = "La URL no puede exceder 254 caracteres")
    private String url;
    
    @Size(max = 254, message = "La licencia no puede exceder 254 caracteres")
    private String license;
    
    private LocalDate validFrom;
    private LocalDate validTo;
    private BigDecimal catalogMsgId;
    
    @Min(value = 0, message = "El estado debe ser 0 o 1")
    @Max(value = 1, message = "El estado debe ser 0 o 1")
    private Integer status;
}