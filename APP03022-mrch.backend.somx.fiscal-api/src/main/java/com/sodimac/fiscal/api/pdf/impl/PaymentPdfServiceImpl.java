// com.sodimac.fiscal.api.pdf.impl.PaymentPdfServiceImpl.java
package com.sodimac.fiscal.api.pdf.impl;

import com.sodimac.fiscal.api.exception.FiscalException;
import com.sodimac.fiscal.api.model.enums.FiscalMessageCode;
import com.sodimac.fiscal.api.service.MessageCatalogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.fop.apps.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import org.w3c.dom.*;

import com.sodimac.fiscal.api.pdf.PaymentPdfService;
import com.sodimac.fiscal.api.util.QrCodeGenerator;

@Service
@Slf4j
public class PaymentPdfServiceImpl implements PaymentPdfService {

    private final ResourceLoader resourceLoader;
    private final MessageCatalogService messageCatalog;

    @Value("${pdf.watermark.text:Documento informativo. No sustituye al XML oficial}")
    private String watermarkText;

    private volatile Path tempXslDir;

    public PaymentPdfServiceImpl(ResourceLoader resourceLoader, MessageCatalogService messageCatalog) {
        this.resourceLoader = resourceLoader;
        this.messageCatalog = messageCatalog;
    }

    @Override
    public byte[] renderFromXml(String xml) {
        if (!StringUtils.hasText(xml)) {
            messageCatalog.throwException(FiscalMessageCode.ERR001);
        }
        // El Complemento de Pago (CFDI tipo P) trae Version="4.0"
        if (!(xml.contains("Version=\"4.0\"") || xml.contains("Version='4.0'"))) {
            messageCatalog.throwException(FiscalMessageCode.BUS022);
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // 1) Copiar XSL y recursos al dir temporal
            Path xslDir = ensureTempXslDir();
            Path xslFile = xslDir.resolve("FormatoComp4.0.xsl");
            if (!Files.exists(xslFile)) {
                throw new IllegalStateException("No se encontró el XSL: " + xslFile);
            }

            // 2) Preparar FOP + Transformer
            System.setProperty("javax.xml.transform.TransformerFactory",
                    "org.apache.xalan.processor.TransformerFactoryImpl");
            FopFactory fopFactory = FopFactory.newInstance(new URI("."));
            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

            TransformerFactory tf = TransformerFactory.newInstance();

            try (InputStream xslIs = Files.newInputStream(xslFile);
                 InputStream xmlIs = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {

                Transformer transformer = tf.newTransformer(new StreamSource(xslIs));

                // 3) Parámetros genéricos del XSL
                String baseUrl = xslDir.toUri().toString(); // file:/.../xsl-fiscal-XXXX/
                transformer.setParameter("xsltfilePath", baseUrl);
                transformer.setParameter("xsltfilePathQR", baseUrl);
                transformer.setParameter("watermarkText", watermarkText);

                // 4) Parámetros específicos de Complemento de Pago
                addPaymentParams(xml, transformer, xslDir);

                // 5) Transformar (XML -> XSL-FO -> PDF)
                Source src = new StreamSource(xmlIs);
                Result res = new SAXResult(fop.getDefaultHandler());
                transformer.transform(src, res);
            }

            return out.toByteArray();
        } catch (FiscalException e) {
            throw e; // Re-lanzar excepciones de negocio
        } catch (Exception e) {
            log.error("Error generando PDF de Complemento de Pago", e);
            messageCatalog.throwException(FiscalMessageCode.ERR034, e.getMessage(), e);
            return null; // Nunca alcanza aquí
        }
    }

    // ======== Helpers ========

    private Path ensureTempXslDir() throws Exception {
        if (tempXslDir != null) return tempXslDir;
        synchronized (this) {
            if (tempXslDir != null) return tempXslDir;
            Path dir = Files.createTempDirectory("xsl-fiscal-");
            // Copiamos los recursos del complemento
            copyResource("classpath:/xsl/FormatoComp4.0.xsl", dir, "FormatoComp4.0.xsl");
            copyResource("classpath:/xsl/no_logo.jpg", dir, "no_logo.jpg");
            copyResource("classpath:/xsl/no_logo.jpg", dir, "no_logo.jpg"); // opcional
            log.info("XSL e imágenes (complemento) copiadas a {}", dir);
            tempXslDir = dir;
            return dir;
        }
    }

    private void copyResource(String location, Path dir, String fileName) throws Exception {
        Resource res = resourceLoader.getResource(location);
        if (!res.exists()) {
            throw new IllegalStateException("Recurso no encontrado: " + location);
        }
        try (InputStream in = res.getInputStream()) {
            Files.copy(in, dir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void addPaymentParams(String xml, Transformer t, Path xslDir) {
        try {
            // Documento con namespaces
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            Document doc = f.newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            // Namespaces
            final String NS_CFDI = "http://www.sat.gob.mx/cfd/4";
            final String NS_TFD  = "http://www.sat.gob.mx/TimbreFiscalDigital";
            final String NS_PAG  = "http://www.sat.gob.mx/Pagos20";

            Element comprobante = (Element) doc.getElementsByTagNameNS(NS_CFDI, "Comprobante").item(0);
            Element emisor      = (Element) doc.getElementsByTagNameNS(NS_CFDI, "Emisor").item(0);
            Element receptor    = (Element) doc.getElementsByTagNameNS(NS_CFDI, "Receptor").item(0);
            Element timbre      = (Element) doc.getElementsByTagNameNS(NS_TFD,  "TimbreFiscalDigital").item(0);

            // Datos base para QR
            String totalStr    = attr(comprobante, "Total");                 // En P: suele ser 0
            BigDecimal total   = new BigDecimal(totalStr).setScale(6, RoundingMode.HALF_UP);
            String rfcEmisor   = attr(emisor, "Rfc");
            String rfcReceptor = attr(receptor, "Rfc");
            String uuid        = attr(timbre, "UUID");
            String selloCfd    = attr(timbre, "SelloCFD");
            String fe          = selloCfd.substring(selloCfd.length() - 8);  // últimos 8 chars

            // URL QR SAT (mismo formato que factura)
            String urlQr = "https://verificacfdi.facturaelectronica.sat.gob.mx/default.aspx?&re="
                    + rfcEmisor + "&rr=" + rfcReceptor + "&tt=" + total.toPlainString() + "&id=" + uuid + "&fe=" + fe;

            // Generar PNG del QR al lado del XSL
            String fsDir = xslDir.toAbsolutePath().toString() + File.separator;
            String qrFileName = new QrCodeGenerator()
                    .generateQRCodeImage(urlQr, 530, 530, fsDir, rfcReceptor, uuid);

            // Parámetros que el XSL espera (ver cabecera del XSL).
            t.setParameter("qrCodeFileName", qrFileName);

            // Forma de Pago del complemento (Pagos 2.0)
            Element pago = (Element) doc.getElementsByTagNameNS(NS_PAG, "Pago").item(0);
            if (pago != null) {
                String formaPagoP = attr(pago, "FormaDePagoP");
                t.setParameter("sFormaPagoComplemento", formaPagoP);
                t.setParameter("sFormaPagoComplementoDesc", mapFormaPago(formaPagoP));
            }

            // Totales (sumatoria de DoctoRelacionado)
            NodeList doctos = doc.getElementsByTagNameNS(NS_PAG, "DoctoRelacionado");
            BigDecimal impSaldoAntTotal = BigDecimal.ZERO;
            BigDecimal impPagadoTotal   = BigDecimal.ZERO;
            BigDecimal impSaldoInsTotal = BigDecimal.ZERO;
            for (int i = 0; i < doctos.getLength(); i++) {
                Element d = (Element) doctos.item(i);
                impSaldoAntTotal = impSaldoAntTotal.add(new BigDecimal(attr(d, "ImpSaldoAnt")));
                impPagadoTotal   = impPagadoTotal.add(new BigDecimal(attr(d, "ImpPagado")));
                impSaldoInsTotal = impSaldoInsTotal.add(new BigDecimal(attr(d, "ImpSaldoInsoluto")));
            }
            t.setParameter("impSaldoAntTotal", impSaldoAntTotal.toPlainString());
            t.setParameter("impPagadoTotal",   impPagadoTotal.toPlainString());
            t.setParameter("impSaldoInsolutoTotal", impSaldoInsTotal.toPlainString());

            // Otros parámetros del XSL — si no los tienes mapeados, los dejamos vacíos
            t.setParameter("usoCFDIDesc", "");
            t.setParameter("regimenFiscalDesc", "");
            t.setParameter("regimenFiscalReceptorDesc", "");
            t.setParameter("tipoComprobanteDesc", "COMPLEMENTO DE PAGO");
            t.setParameter("tipoDeComprobanteDesc", "P");
            t.setParameter("moneda", "XXX");
            t.setParameter("monedaDesc", "Sin divisa");

        } catch (Exception e) {
            log.error("Error generando parámetros del XSL (Complemento de Pago)", e);
            // No abortamos: el XSL tiene defaults razonables
        }
    }

    private static String attr(Element e, String name) {
        return (e != null && e.hasAttribute(name)) ? e.getAttribute(name) : "0";
    }

    // Mapeo básico SAT para Forma de Pago (suficiente para la mayoría)
    private static String mapFormaPago(String clave) {
        switch (clave) {
            case "01": return "Efectivo";
            case "02": return "Cheque nominativo";
            case "03": return "Transferencia electrónica de fondos";
            case "04": return "Tarjeta de crédito";
            case "28": return "Tarjeta de débito";
            case "99": return "Por definir";
            default:   return "Forma de pago " + clave;
        }
    }
}
