
package com.falabella.mdwcorp.sod.corp.osb.schema.cliente.factura.confirmar.req_v2018;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.falabella.mdwcorp.sod.corp.osb.schema.cliente.factura.confirmar.req_v2018 package. 
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

    private final static QName _ClienteFacturaConfirmarExpReq_QNAME = new QName("http://mdwcorp.falabella.com/SOD/CORP/OSB/schema/Cliente/Factura/Confirmar/Req-v2018.02", "ClienteFacturaConfirmarExpReq");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.falabella.mdwcorp.sod.corp.osb.schema.cliente.factura.confirmar.req_v2018
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ClienteFacturaConfirmarExpReqTYPE }
     * 
     */
    public ClienteFacturaConfirmarExpReqTYPE createClienteFacturaConfirmarExpReqTYPE() {
        return new ClienteFacturaConfirmarExpReqTYPE();
    }

    /**
     * Create an instance of {@link ComprobantesTYPE }
     * 
     */
    public ComprobantesTYPE createComprobantesTYPE() {
        return new ComprobantesTYPE();
    }

    /**
     * Create an instance of {@link BodyTYPE }
     * 
     */
    public BodyTYPE createBodyTYPE() {
        return new BodyTYPE();
    }

    /**
     * Create an instance of {@link ComprobanteTYPE }
     * 
     */
    public ComprobanteTYPE createComprobanteTYPE() {
        return new ComprobanteTYPE();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ClienteFacturaConfirmarExpReqTYPE }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mdwcorp.falabella.com/SOD/CORP/OSB/schema/Cliente/Factura/Confirmar/Req-v2018.02", name = "ClienteFacturaConfirmarExpReq")
    public JAXBElement<ClienteFacturaConfirmarExpReqTYPE> createClienteFacturaConfirmarExpReq(ClienteFacturaConfirmarExpReqTYPE value) {
        return new JAXBElement<ClienteFacturaConfirmarExpReqTYPE>(_ClienteFacturaConfirmarExpReq_QNAME, ClienteFacturaConfirmarExpReqTYPE.class, null, value);
    }

}
