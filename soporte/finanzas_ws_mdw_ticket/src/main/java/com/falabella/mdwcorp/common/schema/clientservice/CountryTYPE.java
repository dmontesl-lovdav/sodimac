
package com.falabella.mdwcorp.common.schema.clientservice;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para Country_TYPE.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * <p>
 * <pre>
 * &lt;simpleType name="Country_TYPE">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="AR"/>
 *     &lt;enumeration value="BR"/>
 *     &lt;enumeration value="CL"/>
 *     &lt;enumeration value="CO"/>
 *     &lt;enumeration value="PE"/>
 *     &lt;enumeration value="UY"/>
 *     &lt;enumeration value="MX"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "Country_TYPE")
@XmlEnum
public enum CountryTYPE {

    AR,
    BR,
    CL,
    CO,
    PE,
    UY,
    MX;

    public String value() {
        return name();
    }

    public static CountryTYPE fromValue(String v) {
        return valueOf(v);
    }

}
