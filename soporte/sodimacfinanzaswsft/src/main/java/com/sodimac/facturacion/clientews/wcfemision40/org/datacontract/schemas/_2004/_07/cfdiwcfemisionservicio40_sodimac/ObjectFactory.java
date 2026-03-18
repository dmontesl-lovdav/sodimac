
package com.sodimac.facturacion.clientews.wcfemision40.org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac package. 
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

    private final static QName _Resultado_QNAME = new QName("http://schemas.datacontract.org/2004/07/cfdiWcfEmisionServicio40_Sodimac.Clases", "resultado");
    private final static QName _RespuestaXml_QNAME = new QName("http://schemas.datacontract.org/2004/07/cfdiWcfEmisionServicio40_Sodimac.Clases", "respuestaXml");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Resultado }
     * 
     */
    public Resultado createResultado() {
        return new Resultado();
    }

    /**
     * Create an instance of {@link RespuestaXml }
     * 
     */
    public RespuestaXml createRespuestaXml() {
        return new RespuestaXml();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Resultado }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Resultado }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/cfdiWcfEmisionServicio40_Sodimac.Clases", name = "resultado")
    public JAXBElement<Resultado> createResultado(Resultado value) {
        return new JAXBElement<Resultado>(_Resultado_QNAME, Resultado.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RespuestaXml }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link RespuestaXml }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/cfdiWcfEmisionServicio40_Sodimac.Clases", name = "respuestaXml")
    public JAXBElement<RespuestaXml> createRespuestaXml(RespuestaXml value) {
        return new JAXBElement<RespuestaXml>(_RespuestaXml_QNAME, RespuestaXml.class, null, value);
    }

}
