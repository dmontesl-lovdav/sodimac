package com.sodimac.fiscal.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssuerDto {
    private UUID issuerUuid;
    
    @NotBlank(message = "El nombre del emisor es obligatorio")
    @Size(max = 254, message = "El nombre no puede exceder 254 caracteres")
    private String name;
    
    @NotBlank(message = "El RFC es obligatorio")
    @Size(min = 12, max = 13, message = "El RFC debe tener entre 12 y 13 caracteres")
    @Pattern(regexp = "^[A-Z&Ñ]{3,4}[0-9]{6}[A-Z0-9]{3}$", message = "El RFC no tiene un formato válido")
    private String rfc;
    
    @NotBlank(message = "El régimen fiscal es obligatorio")
    @Size(min = 3, max = 3, message = "El régimen fiscal debe tener 3 caracteres")
    private String taxRegime;
}