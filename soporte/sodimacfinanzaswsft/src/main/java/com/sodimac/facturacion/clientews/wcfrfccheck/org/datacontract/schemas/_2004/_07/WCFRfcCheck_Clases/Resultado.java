/**
 * Resultado.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.sodimac.facturacion.clientews.wcfrfccheck.org.datacontract.schemas._2004._07.WCFRfcCheck_Clases;

public class Resultado  implements java.io.Serializable {
    private java.lang.String errorMessage;

    private java.lang.Boolean validate;

    private java.lang.String estatusLiencia;

    private java.lang.String estatusRfc;

    private java.lang.String modulo;

    public Resultado() {
    }

    public Resultado(
           java.lang.String errorMessage,
           java.lang.Boolean validate,
           java.lang.String estatusLiencia,
           java.lang.String estatusRfc,
           java.lang.String modulo) {
           this.errorMessage = errorMessage;
           this.validate = validate;
           this.estatusLiencia = estatusLiencia;
           this.estatusRfc = estatusRfc;
           this.modulo = modulo;
    }


    /**
     * Gets the errorMessage value for this Resultado.
     * 
     * @return errorMessage
     */
    public java.lang.String getErrorMessage() {
        return errorMessage;
    }


    /**
     * Sets the errorMessage value for this Resultado.
     * 
     * @param errorMessage
     */
    public void setErrorMessage(java.lang.String errorMessage) {
        this.errorMessage = errorMessage;
    }


    /**
     * Gets the validate value for this Resultado.
     * 
     * @return validate
     */
    public java.lang.Boolean getValidate() {
        return validate;
    }


    /**
     * Sets the validate value for this Resultado.
     * 
     * @param validate
     */
    public void setValidate(java.lang.Boolean validate) {
        this.validate = validate;
    }


    /**
     * Gets the estatusLiencia value for this Resultado.
     * 
     * @return estatusLiencia
     */
    public java.lang.String getEstatusLiencia() {
        return estatusLiencia;
    }


    /**
     * Sets the estatusLiencia value for this Resultado.
     * 
     * @param estatusLiencia
     */
    public void setEstatusLiencia(java.lang.String estatusLiencia) {
        this.estatusLiencia = estatusLiencia;
    }


    /**
     * Gets the estatusRfc value for this Resultado.
     * 
     * @return estatusRfc
     */
    public java.lang.String getEstatusRfc() {
        return estatusRfc;
    }


    /**
     * Sets the estatusRfc value for this Resultado.
     * 
     * @param estatusRfc
     */
    public void setEstatusRfc(java.lang.String estatusRfc) {
        this.estatusRfc = estatusRfc;
    }


    /**
     * Gets the modulo value for this Resultado.
     * 
     * @return modulo
     */
    public java.lang.String getModulo() {
        return modulo;
    }


    /**
     * Sets the modulo value for this Resultado.
     * 
     * @param modulo
     */
    public void setModulo(java.lang.String modulo) {
        this.modulo = modulo;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Resultado)) return false;
        Resultado other = (Resultado) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.errorMessage==null && other.getErrorMessage()==null) || 
             (this.errorMessage!=null &&
              this.errorMessage.equals(other.getErrorMessage()))) &&
            ((this.validate==null && other.getValidate()==null) || 
             (this.validate!=null &&
              this.validate.equals(other.getValidate()))) &&
            ((this.estatusLiencia==null && other.getEstatusLiencia()==null) || 
             (this.estatusLiencia!=null &&
              this.estatusLiencia.equals(other.getEstatusLiencia()))) &&
            ((this.estatusRfc==null && other.getEstatusRfc()==null) || 
             (this.estatusRfc!=null &&
              this.estatusRfc.equals(other.getEstatusRfc()))) &&
            ((this.modulo==null && other.getModulo()==null) || 
             (this.modulo!=null &&
              this.modulo.equals(other.getModulo())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getErrorMessage() != null) {
            _hashCode += getErrorMessage().hashCode();
        }
        if (getValidate() != null) {
            _hashCode += getValidate().hashCode();
        }
        if (getEstatusLiencia() != null) {
            _hashCode += getEstatusLiencia().hashCode();
        }
        if (getEstatusRfc() != null) {
            _hashCode += getEstatusRfc().hashCode();
        }
        if (getModulo() != null) {
            _hashCode += getModulo().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Resultado.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/WCFRfcCheck.Clases", "resultado"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("errorMessage");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/WCFRfcCheck.Clases", "ErrorMessage"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("validate");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/WCFRfcCheck.Clases", "Validate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("estatusLiencia");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/WCFRfcCheck.Clases", "estatusLiencia"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("estatusRfc");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/WCFRfcCheck.Clases", "estatusRfc"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("modulo");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/WCFRfcCheck.Clases", "modulo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
