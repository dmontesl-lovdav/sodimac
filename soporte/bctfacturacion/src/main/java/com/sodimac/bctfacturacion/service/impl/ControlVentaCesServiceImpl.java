package com.sodimac.bctfacturacion.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.bctfacturacion.repository.ces.ControVentaCesRepository;
import com.sodimac.bctfacturacion.service.ControlVentaCesService;

@Service
public class ControlVentaCesServiceImpl implements ControlVentaCesService {
  
  @Autowired
  private ControVentaCesRepository controlVentaCesRepository;
  
  public Integer getIdControlCes() {
    return this.controlVentaCesRepository.getIdControlCes();
  }
  
  public Integer getIdControlCesPorFecha(String fecha) {
    return this.controlVentaCesRepository.getIdControlCesByFecha(fecha);
  }
  
  public Integer getEstatusControlCesPorFecha(Integer pIdControlVentaCes) {
    return this.controlVentaCesRepository.getEstatusControlCesByFecha(pIdControlVentaCes);
  }
  
  public boolean existeControlCes(String pFecha) {
    Object[] result = this.controlVentaCesRepository.existeControlCes(pFecha);
    if (result != null && result.length > 0) {
      Integer existe = Integer.valueOf(Integer.parseInt(result[0].toString()));
      if (existe.intValue() == 0)
        return false; 
      return true;
    } 
    return false;
  }
  
  @Transactional("transactionManagerCes")
  public void registraControlCes(Integer pIdControlVentaCes, String pFecha, Integer pTotalVentasCes) {
    this.controlVentaCesRepository.registraControlCes(pIdControlVentaCes, pFecha, pTotalVentasCes);
  }
  
  @Transactional("transactionManagerCes")
  public void actualizaControlCes(Integer pIdControlVentaCes, Integer pEstatus, Integer pTotalRegistrados) {
    this.controlVentaCesRepository.actualizaControlCes(pIdControlVentaCes, pEstatus, pTotalRegistrados);
  }

  public String[] getTipoTransCesPermitidos() {
	  return this.controlVentaCesRepository.getTipoTransCesPermitidos();
  }
  
}
