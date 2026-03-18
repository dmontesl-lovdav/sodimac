package com.sodimac.facturacion.clientews.wcfemision.org.tempuri;
import com.sodimac.facturacion.clientews.wcfemision.org.datacontract.schemas._2004._07.cfdiWcfEmisionServicio_Sodimac_Clases.Resultado;
import com.sodimac.facturacion.clientews.wcfemision.org.datacontract.schemas._2004._07.cfdiWcfEmisionServicio_Sodimac_Clases.RespuestaXml;

public class IDetecnoProxy implements IDetecno {
  private String _endpoint = null;
  private IDetecno iDetecno = null;
  
  public IDetecnoProxy() {
    _initIDetecnoProxy();
  }
  
  public IDetecnoProxy(String endpoint) {
    _endpoint = endpoint;
    _initIDetecnoProxy();
  }
  
  private void _initIDetecnoProxy() {
    try {
      iDetecno = (new DetecnoLocator()).getBasicHttpBinding_IDetecno();
      if (iDetecno != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)iDetecno)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)iDetecno)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (iDetecno != null)
      ((javax.xml.rpc.Stub)iDetecno)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public IDetecno getIDetecno() {
    if (iDetecno == null)
      _initIDetecnoProxy();
    return iDetecno;
  }
  
  public java.lang.String prueba(java.lang.String valor) throws java.rmi.RemoteException{
    if (iDetecno == null)
      _initIDetecnoProxy();
    return iDetecno.prueba(valor);
  }
  
  public Resultado comprobanteGenerar33(java.lang.String licencia, java.lang.String cerBytes, java.lang.String keyBytes, java.lang.String passBytes, java.lang.String xml) throws java.rmi.RemoteException{
    if (iDetecno == null)
      _initIDetecnoProxy();
    return iDetecno.comprobanteGenerar33(licencia, cerBytes, keyBytes, passBytes, xml);
  }
  
  public RespuestaXml comprobanteBuscar33(java.lang.String licencia, java.lang.String facturaId) throws java.rmi.RemoteException{
    if (iDetecno == null)
      _initIDetecnoProxy();
    return iDetecno.comprobanteBuscar33(licencia, facturaId);
  }
  
  public Resultado comprobante_BuscarPdf33(java.lang.String licencia, java.lang.String facturaId) throws java.rmi.RemoteException{
    if (iDetecno == null)
      _initIDetecnoProxy();
    return iDetecno.comprobante_BuscarPdf33(licencia, facturaId);
  }
  
  public Resultado comprobanteCancelar33(java.lang.String licencia, java.lang.String facturaId) throws java.rmi.RemoteException{
    if (iDetecno == null)
      _initIDetecnoProxy();
    return iDetecno.comprobanteCancelar33(licencia, facturaId);
  }
  
  
}