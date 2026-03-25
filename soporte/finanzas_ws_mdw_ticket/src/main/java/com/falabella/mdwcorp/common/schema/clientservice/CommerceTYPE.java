
package com.falabella.mdwcorp.common.schema.clientservice;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para Commerce_TYPE.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * <p>
 * <pre>
 * &lt;simpleType name="Commerce_TYPE">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Banco"/>
 *     &lt;enumeration value="CMR"/>
 *     &lt;enumeration value="Falabella"/>
 *     &lt;enumeration value="Seguros"/>
 *     &lt;enumeration value="Sodimac"/>
 *     &lt;enumeration value="Tottus"/>
 *     &lt;enumeration value="Movil"/>
 *     &lt;enumeration value="Viajes"/>
 *     &lt;enumeration value="Falabella Movil"/>
 *     &lt;enumeration value="CrateAndBarrel"/>
 *     &lt;enumeration value="Telco CMR"/>
 *     &lt;enumeration value="Connect"/>
 *     &lt;enumeration value="Web Movil Falabella"/>
 *     &lt;enumeration value="BackOffice"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "Commerce_TYPE")
@XmlEnum
public enum CommerceTYPE {

    @XmlEnumValue("Banco")
    BANCO("Banco"),
    CMR("CMR"),
    @XmlEnumValue("Falabella")
    FALABELLA("Falabella"),
    @XmlEnumValue("Seguros")
    SEGUROS("Seguros"),
    @XmlEnumValue("Sodimac")
    SODIMAC("Sodimac"),
    @XmlEnumValue("Tottus")
    TOTTUS("Tottus"),
    @XmlEnumValue("Movil")
    MOVIL("Movil"),
    @XmlEnumValue("Viajes")
    VIAJES("Viajes"),
    @XmlEnumValue("Falabella Movil")
    FALABELLA_MOVIL("Falabella Movil"),
    @XmlEnumValue("CrateAndBarrel")
    CRATE_AND_BARREL("CrateAndBarrel"),
    @XmlEnumValue("Telco CMR")
    TELCO_CMR("Telco CMR"),
    @XmlEnumValue("Connect")
    CONNECT("Connect"),
    @XmlEnumValue("Web Movil Falabella")
    WEB_MOVIL_FALABELLA("Web Movil Falabella"),
    @XmlEnumValue("BackOffice")
    BACK_OFFICE("BackOffice");
    private final String value;

    CommerceTYPE(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CommerceTYPE fromValue(String v) {
        for (CommerceTYPE c: CommerceTYPE.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
