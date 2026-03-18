/**
 * DetecnoLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.sodimac.facturacion.clientews.wcfemision.org.tempuri;

public class DetecnoLocator extends org.apache.axis.client.Service implements Detecno {

    public DetecnoLocator() {
    }


    public DetecnoLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public DetecnoLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for BasicHttpBinding_IDetecno
    private java.lang.String BasicHttpBinding_IDetecno_address = "http://10.235.137.83:8057/cfdiWcfEmisionServicio_Sodimac_test/Detecno.svc";

    public java.lang.String getBasicHttpBinding_IDetecnoAddress() {
        return BasicHttpBinding_IDetecno_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String BasicHttpBinding_IDetecnoWSDDServiceName = "BasicHttpBinding_IDetecno";

    public java.lang.String getBasicHttpBinding_IDetecnoWSDDServiceName() {
        return BasicHttpBinding_IDetecnoWSDDServiceName;
    }

    public void setBasicHttpBinding_IDetecnoWSDDServiceName(java.lang.String name) {
        BasicHttpBinding_IDetecnoWSDDServiceName = name;
    }

    public IDetecno getBasicHttpBinding_IDetecno() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(BasicHttpBinding_IDetecno_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getBasicHttpBinding_IDetecno(endpoint);
    }

    public IDetecno getBasicHttpBinding_IDetecno(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            BasicHttpBinding_IDetecnoStub _stub = new BasicHttpBinding_IDetecnoStub(portAddress, this);
            _stub.setPortName(getBasicHttpBinding_IDetecnoWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setBasicHttpBinding_IDetecnoEndpointAddress(java.lang.String address) {
        BasicHttpBinding_IDetecno_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (IDetecno.class.isAssignableFrom(serviceEndpointInterface)) {
                BasicHttpBinding_IDetecnoStub _stub = new BasicHttpBinding_IDetecnoStub(new java.net.URL(BasicHttpBinding_IDetecno_address), this);
                _stub.setPortName(getBasicHttpBinding_IDetecnoWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("BasicHttpBinding_IDetecno".equals(inputPortName)) {
            return getBasicHttpBinding_IDetecno();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://tempuri.org/", "Detecno");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://tempuri.org/", "BasicHttpBinding_IDetecno"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("BasicHttpBinding_IDetecno".equals(portName)) {
            setBasicHttpBinding_IDetecnoEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
