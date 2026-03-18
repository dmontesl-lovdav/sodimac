
package com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.req_v2018;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.req_v2018 package. 
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

    private final static QName _ClienteTicketObtenerExpReq_QNAME = new QName("http://mdwcorp.falabella.com/SOD/CORP/OSB/schema/Cliente/Ticket/Obtener/Req-v2018.02", "ClienteTicketObtenerExpReq");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.req_v2018
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ClienteTicketObtenerExpReqTYPE }
     * 
     */
    public ClienteTicketObtenerExpReqTYPE createClienteTicketObtenerExpReqTYPE() {
        return new ClienteTicketObtenerExpReqTYPE();
    }

    /**
     * Create an instance of {@link BodyTYPE }
     * 
     */
    public BodyTYPE createBodyTYPE() {
        return new BodyTYPE();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ClienteTicketObtenerExpReqTYPE }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mdwcorp.falabella.com/SOD/CORP/OSB/schema/Cliente/Ticket/Obtener/Req-v2018.02", name = "ClienteTicketObtenerExpReq")
    public JAXBElement<ClienteTicketObtenerExpReqTYPE> createClienteTicketObtenerExpReq(ClienteTicketObtenerExpReqTYPE value) {
        return new JAXBElement<ClienteTicketObtenerExpReqTYPE>(_ClienteTicketObtenerExpReq_QNAME, ClienteTicketObtenerExpReqTYPE.class, null, value);
    }

}
