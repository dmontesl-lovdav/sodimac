package com.sodimac.facturacion.models;

import java.util.List;

public class MultipleModalResponse extends ResponseBase {

	
	private List<MultipleModalItem> multipleModalItems;
	
	public MultipleModalResponse(boolean success, String message, List<MultipleModalItem> multipleModalItems) {
		super(success, message);
		this.multipleModalItems = multipleModalItems;
		// TODO Auto-generated constructor stub
	}

	public List<MultipleModalItem> getMultipleModalItems() {
		return multipleModalItems;
	}

	public void setMultipleModalItems(List<MultipleModalItem> multipleModalItems) {
		this.multipleModalItems = multipleModalItems;
	}

}
