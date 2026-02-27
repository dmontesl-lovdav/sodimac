package com.sodimac.fiscal.api.model.dto.invoicexml;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.xml.bind.annotation.*;

/**
 * DTO para el elemento CfdiRelacionado del CFDI v4.0
 */
@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class CfdiRelacionadoDto {

    @XmlAttribute(name = "UUID")
    private String uuid;
}
