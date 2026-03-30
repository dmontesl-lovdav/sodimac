package com.sodimac.oc.batch.dto.detecno;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthToken {

	private boolean success;
	@JsonProperty(value = "acc_tkn")
	private String accTkn;
	@JsonProperty(value = "ref_tkn")
	private String refTkn;

	public AuthToken() {
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getAccTkn() {
		return accTkn;
	}

	public void setAccTkn(String accTkn) {
		this.accTkn = accTkn;
	}

	public String getRefTkn() {
		return refTkn;
	}

	public void setRefTkn(String refTkn) {
		this.refTkn = refTkn;
	}

}
