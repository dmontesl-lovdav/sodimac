
package com.falabella.mdwcorp.sod.corp.osb.schema.cliente.factura.confirmar.resp_v2018;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.falabella.mdwcorp.sod.corp.osb.schema.cliente.factura.confirmar.resp_v2018 package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ClienteFacturaConfirmarExpResp_QNAME = new QName("http://mdwcorp.falabella.com/SOD/CORP/OSB/schema/Cliente/Factura/Confirmar/Resp-v2018.02", "ClienteFacturaConfirmarExpResp");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.falabella.mdwcorp.sod.corp.osb.schema.cliente.factura.confirmar.resp_v2018
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ClienteFacturaConfirmarExpRespTYPE }
     * 
     */
    public ClienteFacturaConfirmarExpRespTYPE createClienteFacturaConfirmarExpRespTYPE() {
        return new ClienteFacturaConfirmarExpRespTYPE();
    }

    /**
     * Create an instance of {@link ResponseTYPE }
     * 
     */
    public ResponseTYPE createResponseTYPE() {
        return new ResponseTYPE();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ClienteFacturaConfirmarExpRespTYPE }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mdwcorp.falabella.com/SOD/CORP/OSB/schema/Cliente/Factura/Confirmar/Resp-v2018.02", name = "ClienteFacturaConfirmarExpResp")
    public JAXBElement<ClienteFacturaConfirmarExpRespTYPE> createClienteFacturaConfirmarExpResp(ClienteFacturaConfirmarExpRespTYPE value) {
        return new JAXBElement<ClienteFacturaConfirmarExpRespTYPE>(_ClienteFacturaConfirmarExpResp_QNAME, ClienteFacturaConfirmarExpRespTYPE.class, null, value);
    }

}
