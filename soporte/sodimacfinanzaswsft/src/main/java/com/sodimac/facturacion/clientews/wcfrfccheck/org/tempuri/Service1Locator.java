/**
 * Service1Locator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.sodimac.facturacion.clientews.wcfrfccheck.org.tempuri;

public class Service1Locator extends org.apache.axis.client.Service implements Service1 {

    public Service1Locator() {
    }


    public Service1Locator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public Service1Locator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for BasicHttpsBinding_IService1
    private java.lang.String BasicHttpsBinding_IService1_address = "https://www.detecno.com.mx/RFCCheck/WCFRfcCheck/Detecno.svc";

    public java.lang.String getBasicHttpsBinding_IService1Address() {
        return BasicHttpsBinding_IService1_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String BasicHttpsBinding_IService1WSDDServiceName = "BasicHttpsBinding_IService1";

    public java.lang.String getBasicHttpsBinding_IService1WSDDServiceName() {
        return BasicHttpsBinding_IService1WSDDServiceName;
    }

    public void setBasicHttpsBinding_IService1WSDDServiceName(java.lang.String name) {
        BasicHttpsBinding_IService1WSDDServiceName = name;
    }

    public IService1 getBasicHttpsBinding_IService1() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(BasicHttpsBinding_IService1_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getBasicHttpsBinding_IService1(endpoint);
    }

    public IService1 getBasicHttpsBinding_IService1(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            BasicHttpsBinding_IService1Stub _stub = new BasicHttpsBinding_IService1Stub(portAddress, this);
            _stub.setPortName(getBasicHttpsBinding_IService1WSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setBasicHttpsBinding_IService1EndpointAddress(java.lang.String address) {
        BasicHttpsBinding_IService1_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (IService1.class.isAssignableFrom(serviceEndpointInterface)) {
                BasicHttpsBinding_IService1Stub _stub = new BasicHttpsBinding_IService1Stub(new java.net.URL(BasicHttpsBinding_IService1_address), this);
                _stub.setPortName(getBasicHttpsBinding_IService1WSDDServiceName());
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
        if ("BasicHttpsBinding_IService1".equals(inputPortName)) {
            return getBasicHttpsBinding_IService1();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://tempuri.org/", "Service1");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://tempuri.org/", "BasicHttpsBinding_IService1"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("BasicHttpsBinding_IService1".equals(portName)) {
            setBasicHttpsBinding_IService1EndpointAddress(address);
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
