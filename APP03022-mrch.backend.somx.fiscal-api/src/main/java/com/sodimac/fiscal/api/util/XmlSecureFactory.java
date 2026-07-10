package com.sodimac.fiscal.api.util;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;

/**
 * Fábricas XML endurecidas contra ataques XXE (Sonar java:S2755).
 *
 * <p>Los CFDI/pagos que procesa fiscal-api provienen de terceros (PAC, proveedores),
 * justo el vector de XXE. Estas fábricas deshabilitan DTD y entidades externas.
 * Los CFDI del SAT no declaran DOCTYPE, por lo que rechazar DOCTYPE es seguro.</p>
 */
public final class XmlSecureFactory {

    private XmlSecureFactory() {
        // utility class
    }

    /**
     * {@link DocumentBuilderFactory} con procesamiento seguro y DOCTYPE deshabilitado.
     */
    public static DocumentBuilderFactory newDocumentBuilderFactory() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("No se pudo endurecer DocumentBuilderFactory contra XXE", e);
        }
        return factory;
    }

    /**
     * {@link TransformerFactory} sin acceso a DTD ni hojas de estilo externas.
     */
    public static TransformerFactory newTransformerFactory() {
        TransformerFactory tf = TransformerFactory.newInstance();
        tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        return tf;
    }
}
