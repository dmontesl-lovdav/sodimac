
package com.falabella.mdwcorp.common.schema.clientservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.falabella.mdwcorp.common.schema.clientservice package. 
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

    private final static QName _ClientService_QNAME = new QName("http://mdwcorp.falabella.com/common/schema/clientservice", "ClientService");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.falabella.mdwcorp.common.schema.clientservice
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ClientServiceTYPE }
     * 
     */
    public ClientServiceTYPE createClientServiceTYPE() {
        return new ClientServiceTYPE();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ClientServiceTYPE }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mdwcorp.falabella.com/common/schema/clientservice", name = "ClientService")
    public JAXBElement<ClientServiceTYPE> createClientService(ClientServiceTYPE value) {
        return new JAXBElement<ClientServiceTYPE>(_ClientService_QNAME, ClientServiceTYPE.class, null, value);
    }

}
