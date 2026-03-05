package com.sodimac.batch.process.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StatusUpdateResponseDto {
    private Boolean success;
    private String code;
    private String message;
    private UUID invoiceUuid;
    private UUID fiscalUuid;
    private String documentType;
    private Integer estatusAnterior;
    private Integer estatusNuevo;
    private String estatusNuevoNombre;

    public StatusUpdateResponseDto() {}

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public UUID getInvoiceUuid() { return invoiceUuid; }
    public void setInvoiceUuid(UUID invoiceUuid) { this.invoiceUuid = invoiceUuid; }

    public UUID getFiscalUuid() { return fiscalUuid; }
    public void setFiscalUuid(UUID fiscalUuid) { this.fiscalUuid = fiscalUuid; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public Integer getEstatusAnterior() { return estatusAnterior; }
    public void setEstatusAnterior(Integer estatusAnterior) { this.estatusAnterior = estatusAnterior; }

    public Integer getEstatusNuevo() { return estatusNuevo; }
    public void setEstatusNuevo(Integer estatusNuevo) { this.estatusNuevo = estatusNuevo; }

    public String getEstatusNuevoNombre() { return estatusNuevoNombre; }
    public void setEstatusNuevoNombre(String estatusNuevoNombre) { this.estatusNuevoNombre = estatusNuevoNombre; }
}
