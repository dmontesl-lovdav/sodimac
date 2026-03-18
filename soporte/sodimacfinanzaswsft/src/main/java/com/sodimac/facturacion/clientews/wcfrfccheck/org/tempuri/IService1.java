/**
 * IService1.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.sodimac.facturacion.clientews.wcfrfccheck.org.tempuri;
import com.sodimac.facturacion.clientews.wcfrfccheck.org.datacontract.schemas._2004._07.WCFRfcCheck_Clases.Resultado;

public interface IService1 extends java.rmi.Remote {
    public Resultado consultaRFC(java.lang.String usuario, java.lang.String pass, java.lang.String rfc, java.lang.String licencia) throws java.rmi.RemoteException;
}
