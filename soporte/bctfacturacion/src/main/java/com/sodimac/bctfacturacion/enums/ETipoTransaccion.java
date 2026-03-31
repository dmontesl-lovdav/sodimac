package com.sodimac.bctfacturacion.enums;

public enum ETipoTransaccion {

	ASIGNACION	(1),
	CANCELACION	(2),
	DEVOLUCION  (3);
	
	private int id;
	
	ETipoTransaccion(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public static ETipoTransaccion getTipoTransaccion(int transaccionBase) {
		for (ETipoTransaccion eTipoTransaccion : ETipoTransaccion.values()) {
			if (transaccionBase == eTipoTransaccion.getId()) {
				return eTipoTransaccion;
			}
		}
		return null;
	}
}
