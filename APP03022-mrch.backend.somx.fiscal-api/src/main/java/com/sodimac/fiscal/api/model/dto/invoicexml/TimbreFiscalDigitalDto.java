package com.sodimac.fiscal.api.model.dto.invoicexml;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.xml.bind.annotation.*;

/**
 * DTO para el TimbreFiscalDigital - Complemento crítico de validación fiscal del SAT.
 *
 * El Timbre Fiscal Digital es el complemento más importante de un CFDI, ya que
 * contiene la certificación oficial del SAT que valida la autenticidad y
 * legalidad del comprobante fiscal. Sin este timbre, la factura no tiene
 * validez fiscal ante las autoridades mexicanas.
 *
 * Elementos de seguridad incluidos:
 * - UUID único e irrepetible del comprobante
 * - Sellos digitales del emisor y del SAT
 * - Certificados de autenticación
 * - Timestamp oficial del timbrado
 * - Información del proveedor de certificación autorizado (PAC)
 *
 * @author g_dco018
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class TimbreFiscalDigitalDto {

    @XmlAttribute(name = "Version")
    private String version;           // Versión del timbre fiscal (ej: "1.1")

    @XmlAttribute(name = "UUID")
    private String uuid;             // UUID único del comprobante fiscal

    @XmlAttribute(name = "FechaTimbrado")
    private String fechaTimbrado;    // Fecha y hora del timbrado

    @XmlAttribute(name = "RfcProvCertif")
    private String rfcProvCertif;    // RFC del proveedor de certificación

    @XmlAttribute(name = "SelloCFD")
    private String selloCFD;         // Sello del Comprobante Fiscal Digital

    @XmlAttribute(name = "NoCertificadoSAT")
    private String noCertificadoSAT; // Número de certificado del SAT

    @XmlAttribute(name = "SelloSAT")
    private String selloSAT;         // Sello digital del SAT
}