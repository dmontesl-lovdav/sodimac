package com.sodimac.facturacion.clientews.wcfrfccheck.org.tempuri;
import com.sodimac.facturacion.clientews.wcfrfccheck.org.datacontract.schemas._2004._07.WCFRfcCheck_Clases.Resultado;

public class IService1Proxy implements IService1 {
  private String _endpoint = null;
  private IService1 iService1 = null;
  
  public IService1Proxy() {
    _initIService1Proxy();
  }
  
  public IService1Proxy(String endpoint) {
    _endpoint = endpoint;
    _initIService1Proxy();
  }
  
  private void _initIService1Proxy() {
    try {
      iService1 = (new Service1Locator()).getBasicHttpsBinding_IService1();
      if (iService1 != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)iService1)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)iService1)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (iService1 != null)
      ((javax.xml.rpc.Stub)iService1)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public IService1 getIService1() {
    if (iService1 == null)
      _initIService1Proxy();
    return iService1;
  }
  
  public Resultado consultaRFC(java.lang.String usuario, java.lang.String pass, java.lang.String rfc, java.lang.String licencia) throws java.rmi.RemoteException{
    if (iService1 == null)
      _initIService1Proxy();
    return iService1.consultaRFC(usuario, pass, rfc, licencia);
  }
  
  
}