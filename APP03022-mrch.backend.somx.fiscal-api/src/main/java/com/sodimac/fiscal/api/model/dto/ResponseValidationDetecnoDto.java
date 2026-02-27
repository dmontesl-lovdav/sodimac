package com.sodimac.fiscal.api.model.dto;

public class ResponseValidationDetecnoDto {
	
	private String creditoActual;
	private String creditoDisponible;
	private String creditoFinal;
	private String creditoInicio;
	private String errorCode;
	private String errorDesc;
	private String errorMessage;
	private String errorModule;
	private String errorType;
	private String status;
	private String validate;
	
	public ResponseValidationDetecnoDto() {}

	public ResponseValidationDetecnoDto(String creditoActual, String creditoDisponible, String creditoFinal, String creditoInicio, String errorCode,
			String errorDesc, String errorMessage, String errorModule, String errorType, String status,
			String validate) {
		super();
		this.creditoActual = creditoActual;
		this.creditoDisponible = creditoDisponible;
		this.creditoFinal = creditoFinal;
		this.creditoInicio = creditoInicio;
		this.errorCode = errorCode;
		this.errorDesc = errorDesc;
		this.errorMessage = errorMessage;
		this.errorModule = errorModule;
		this.errorType = errorType;
		this.status = status;
		this.validate = validate;
	}




	public String getCreditoActual() {
		return creditoActual;
	}
	public void setCreditoActual(String creditoActual) {
		this.creditoActual = creditoActual;
	}
	public String getCreditoDisponible() {
		return creditoDisponible;
	}
	public void setCreditoDisponible(String creditoDisponible) {
		this.creditoDisponible = creditoDisponible;
	}
	public String getCreditoFinal() {
		return creditoFinal;
	}
	public void setCreditoFinal(String creditoFinal) {
		this.creditoFinal = creditoFinal;
	}
	public String getCreditoInicio() {
		return creditoInicio;
	}
	public void setCreditoInicio(String creditoInicio) {
		this.creditoInicio = creditoInicio;
	}
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public String getErrorDesc() {
		return errorDesc;
	}
	public void setErrorDesc(String errorDesc) {
		this.errorDesc = errorDesc;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public String getErrorModule() {
		return errorModule;
	}
	public void setErrorModule(String errorModule) {
		this.errorModule = errorModule;
	}
	public String getErrorType() {
		return errorType;
	}
	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getValidate() {
		return validate;
	}
	public void setValidate(String validate) {
		this.validate = validate;
	}
	
	

}
