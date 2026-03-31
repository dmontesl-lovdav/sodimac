package com.sodimac.bctfacturacion.enums;

public enum EEstatusControlVenta {
	  EN_PROCESO(1),
	  CORRECTO(2),
	  ERROR(3);
	  
	  private int id;
	  
	  EEstatusControlVenta(int id) {
	    this.id = id;
	  }
	  
	  public static EEstatusControlVenta getEstatusById(Integer estatusControlMSIPorFecha) {
	    for (EEstatusControlVenta estatus : EEstatusControlVenta.values()) {
	      if (estatus.getId() == estatusControlMSIPorFecha.intValue())
	        return estatus; 
	    } 
	    return null;
	  }
	  
	  public int getId() {
	    return this.id;
	  }
	  
	  public void setId(int id) {
	    this.id = id;
	  }
	}
