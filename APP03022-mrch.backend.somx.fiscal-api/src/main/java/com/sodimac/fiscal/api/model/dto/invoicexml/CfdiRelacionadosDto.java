package com.sodimac.fiscal.api.model.dto.invoicexml;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.xml.bind.annotation.*;
import java.util.List;

/**
 * DTO para el elemento CfdiRelacionados del CFDI v4.0
 */
@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class CfdiRelacionadosDto {

    @XmlAttribute(name = "TipoRelacion")
    private String tipoRelacion;

    @XmlElement(name = "CfdiRelacionado", namespace = "http://www.sat.gob.mx/cfd/4")
    private List<CfdiRelacionadoDto> cfdiRelacionado;
}
