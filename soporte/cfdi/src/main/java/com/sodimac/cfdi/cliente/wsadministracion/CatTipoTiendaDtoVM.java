package com.sodimac.cfdi.cliente.wsadministracion;

public class CatTipoTiendaDtoVM {

	private Integer id;
	private String tipotienda;
	
	public CatTipoTiendaDtoVM() {}
	
	public CatTipoTiendaDtoVM(Integer id, String tipotienda) {
		this.id = id;
		this.tipotienda = tipotienda;
	}
	
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getTipotienda() {
		return tipotienda;
	}
	public void setTipotienda(String tipotienda) {
		this.tipotienda = tipotienda;
	}

}
