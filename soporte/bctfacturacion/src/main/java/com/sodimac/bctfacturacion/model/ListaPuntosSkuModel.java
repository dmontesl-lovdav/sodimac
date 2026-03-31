package com.sodimac.bctfacturacion.model;

import java.util.ArrayList;

public class ListaPuntosSkuModel extends ArrayList<PuntosSkuModel> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PuntosSkuModel getPuntosSku(String sku) {
		for (PuntosSkuModel skuModel : this) {
			if (skuModel.getSku().equals(sku)) {
				return skuModel;
			}
		}
		return null;
	}
}
