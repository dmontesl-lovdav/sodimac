/**
 * IDetecno.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.sodimac.facturacion.clientews.wcfemision.org.tempuri;
import com.sodimac.facturacion.clientews.wcfemision.org.datacontract.schemas._2004._07.cfdiWcfEmisionServicio_Sodimac_Clases.Resultado;
import com.sodimac.facturacion.clientews.wcfemision.org.datacontract.schemas._2004._07.cfdiWcfEmisionServicio_Sodimac_Clases.RespuestaXml;

public interface IDetecno extends java.rmi.Remote {
    public java.lang.String prueba(java.lang.String valor) throws java.rmi.RemoteException;
    public Resultado comprobanteGenerar33(java.lang.String licencia, java.lang.String cerBytes, java.lang.String keyBytes, java.lang.String passBytes, java.lang.String xml) throws java.rmi.RemoteException;
    public RespuestaXml comprobanteBuscar33(java.lang.String licencia, java.lang.String facturaId) throws java.rmi.RemoteException;
    public Resultado comprobante_BuscarPdf33(java.lang.String licencia, java.lang.String facturaId) throws java.rmi.RemoteException;
    public Resultado comprobanteCancelar33(java.lang.String licencia, java.lang.String facturaId) throws java.rmi.RemoteException;
}
