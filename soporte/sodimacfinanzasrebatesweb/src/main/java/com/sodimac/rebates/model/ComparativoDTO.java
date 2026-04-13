package com.sodimac.rebates.model;

import java.io.Serializable;
import java.util.List;

public class ComparativoDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<ComparativoAprobacion> comparativoAprobacionList;

	private String message;

	public List<ComparativoAprobacion> getComparativoAprobacionList() {
		return comparativoAprobacionList;
	}

	public void setComparativoAprobacionList(List<ComparativoAprobacion> comparativoAprobacionList) {
		this.comparativoAprobacionList = comparativoAprobacionList;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "ComparativoDTO [comparativoAprobacionList=" + comparativoAprobacionList + ", message=" + message + "]";
	}

}
