package com.sodimac.fiscal.api.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FinanzasReceptionResponse {
    private String receptionId;
    private String amount;
}
