package com.sodimac.fiscal.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDto {
    private UUID invoiceUuid;
    private String placeOfIssue;
    private String paymentMethod;
    @NotBlank(message = "El tipo de documento es obligatorio")
    @Pattern(regexp = "[IETNP]", message = "El tipo de documento debe ser I, E, T, N o P")
    private String documentType;
    
    @NotNull(message = "El total es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El total debe ser mayor que 0")
    @Digits(integer = 14, fraction = 2, message = "El total no puede tener más de 14 dígitos enteros y 2 decimales")
    private BigDecimal total;
    
    @DecimalMin(value = "0.0", message = "El tipo de cambio debe ser mayor que 0")
    private BigDecimal exchangeRate;
    
    @Size(max = 3, message = "La moneda no puede exceder 3 caracteres")
    private String currency;
    
    @DecimalMin(value = "0.0", message = "El descuento debe ser mayor o igual que 0")
    private BigDecimal discount;
    
    @NotNull(message = "El subtotal es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El subtotal debe ser mayor que 0")
    @Digits(integer = 14, fraction = 2, message = "El subtotal no puede tener más de 14 dígitos enteros y 2 decimales")
    private BigDecimal subtotal;
    
    @Size(max = 10, message = "Las condiciones de pago no pueden exceder 10 caracteres")
    private String paymentConditions;
    
    @Size(max = 10, message = "La forma de pago no puede exceder 10 caracteres")
    private String paymentForm;
    
    @NotNull(message = "La fecha de emisión es obligatoria")
    private LocalDate issueDate;
    private LocalDateTime certificationDate;
    private String folio;
    private String series;
    private BigDecimal version;
    private String xmlContent;
    private Integer status;
    private UUID issuerUuid;
    private UUID receiverUuid;
}