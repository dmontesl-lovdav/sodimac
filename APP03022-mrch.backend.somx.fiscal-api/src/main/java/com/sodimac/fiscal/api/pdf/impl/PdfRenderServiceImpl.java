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

import com.sodimac.fiscal.api.pdf.PdfRenderService;
import com.sodimac.fiscal.api.util.QrCodeGenerator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Service
@Slf4j
public class PdfRenderServiceImpl implements PdfRenderService {

    private final ResourceLoader resourceLoader;
    private final MessageCatalogService messageCatalog;

    @Value("${pdf.watermark.text:Documento informativo. No sustituye al XML oficial}")
    private String watermarkText;

    // Directorio temporal donde copiamos XSL + PNG una sola vez
    private volatile Path tempXslDir;

    public PdfRenderServiceImpl(ResourceLoader resourceLoader, MessageCatalogService messageCatalog) {
        this.resourceLoader = resourceLoader;
        this.messageCatalog = messageCatalog;
    }

    private void addQrParams(String xml,
            Transformer transformer,
            Path xslDir) {

        try {
            // 0) Limpiar cualquier basura antes del primer '<'
            String cleanedXml = xml;
            int firstLt = cleanedXml.indexOf('<');
            if (firstLt > 0) {
                cleanedXml = cleanedXml.substring(firstLt);
            }

            // 1) Parsear el XML a Document (similar a UtilsFile.ObtenerDocumentXml)
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true); // importante para cfdi: y tfd:
            DocumentBuilder builder = factory.newDocumentBuilder();

            try (ByteArrayInputStream is = new ByteArrayInputStream(cleanedXml.getBytes(StandardCharsets.UTF_8))) {
                Document document = builder.parse(is);

                // 2) Leer datos básicos del CFDI
                Node nNode = document.getElementsByTagName("cfdi:Comprobante").item(0);
                Element comprobante = (Element) nNode;

                String totalStr = comprobante.getAttribute("Total");

                nNode = document.getElementsByTagName("cfdi:Emisor").item(0);
                Element emisor = (Element) nNode;
                String rfcEmisor = emisor.getAttribute("Rfc");

                nNode = document.getElementsByTagName("cfdi:Receptor").item(0);
                Element receptor = (Element) nNode;
                String rfcReceptor = receptor.getAttribute("Rfc");

                nNode = document.getElementsByTagName("tfd:TimbreFiscalDigital").item(0);
                Element timbre = (Element) nNode;
                String uuid = timbre.getAttribute("UUID");
                String selloCfd = timbre.getAttribute("SelloCFD");

                // 3) Preparar campos especiales del QR
                String fe = selloCfd.substring(selloCfd.length() - 8);

                // Total con 6 decimales (SAT pide 6 decimales)
                BigDecimal total = new BigDecimal(totalStr);
                String totalForQr = total.setScale(6, RoundingMode.HALF_UP).toPlainString();

                // 4) Construir la URL del QR SAT
                String urlCodeQR = "https://verificacfdi.facturaelectronica.sat.gob.mx/default.aspx?&re=" + rfcEmisor +
                        "&rr=" + rfcReceptor +
                        "&tt=" + totalForQr +
                        "&id=" + uuid +
                        "&fe=" + fe;

                // 5) Generar el PNG en el directorio temporal del XSL
                String fsDir = xslDir.toAbsolutePath().toString() + File.separator;

                QrCodeGenerator qrCodeGenerator = new QrCodeGenerator();
                String qrFileName = qrCodeGenerator
                        .generateQRCodeImage(urlCodeQR, 530, 530, fsDir, rfcReceptor, uuid);

                // 6) Pasar parámetros al XSL
                String baseUrl = xslDir.toUri().toString(); // "file:/.../xsl-fiscal-XXXX/"

                transformer.setParameter("xsltfilePathQR", baseUrl);
                transformer.setParameter("qrCodeFileName", qrFileName);
            }

        } catch (Exception e) {
            log.error("Error generando QR para el PDF", e);
            // Fallback: deja el ícono si falla
            transformer.setParameter("qrCodeFileName", "no_logo.jpg");
        }
    }

    @Override
    public byte[] renderFromXml(String xml) {
        if (!StringUtils.hasText(xml)) {
            messageCatalog.throwException(FiscalMessageCode.ERR001);
        }

        if (!(xml.contains("Version=\"4.0\"") || xml.contains("Version='4.0'"))) {
            messageCatalog.throwException(FiscalMessageCode.BUS022);
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Path xslDir = ensureTempXslDir();
            Path xslFile = xslDir.resolve("Formato4.0.xsl");
            if (!Files.exists(xslFile)) {
                throw new IllegalStateException("No se encontró el XSL: " + xslFile);
            }

            System.setProperty("javax.xml.transform.TransformerFactory",
                    "org.apache.xalan.processor.TransformerFactoryImpl");
            FopFactory fopFactory = FopFactory.newInstance(new java.net.URI("."));
            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

            TransformerFactory tf = TransformerFactory.newInstance();

            try (InputStream xslIs = Files.newInputStream(xslFile);
                    InputStream xmlIs = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {

                Transformer transformer = tf.newTransformer(new StreamSource(xslIs));

                String basePath = xslDir.toUri().toString();
                // Logo y recursos XSL
                transformer.setParameter("xsltfilePath", basePath);

                // Parámetros del QR (para que src="url(file:/.../no_logo.jpg)" sea válido)
                transformer.setParameter("xsltfilePathQR", basePath);
                // Marca de agua
                transformer.setParameter("watermarkText", watermarkText);
                addQrParams(xml, transformer, xslDir);

                Source src = new StreamSource(xmlIs);
                Result res = new SAXResult(fop.getDefaultHandler());
                transformer.transform(src, res);
            }

            return out.toByteArray();
        } catch (FiscalException e) {
            throw e; // Re-lanzar excepciones de negocio
        } catch (Exception e) {
            log.error("Error generando PDF a partir del XML", e);
            messageCatalog.throwException(FiscalMessageCode.ERR033, e.getMessage(), e);
            return null; // Nunca alcanza aquí
        }
    }

    /**
     * Copia Formato4.0.xsl y no_logo.jpg desde classpath:/xsl a un
     * directorio temporal.
     */
    private Path ensureTempXslDir() throws Exception {
        if (tempXslDir != null) {
            return tempXslDir;
        }
        synchronized (this) {
            if (tempXslDir != null) {
                return tempXslDir;
            }
            Path dir = Files.createTempDirectory("xsl-fiscal-");
            copyResourceToDir("classpath:/xsl/Formato4.0.xsl", dir, "Formato4.0.xsl");
            copyResourceToDir("classpath:/xsl/no_logo.jpg", dir, "no_logo.jpg");
            Path imagePath = dir.resolve("no_logo.jpg");
            log.info("XSL e imagen copiadas a {}", dir);
            log.warn(">>> Logo exists? {}", Files.exists(imagePath));
            tempXslDir = dir;
            return dir;
        }
    }

    private void copyResourceToDir(String location, Path dir, String fileName) throws Exception {
        Resource res = resourceLoader.getResource(location);
        if (!res.exists()) {
            throw new IllegalStateException("Recurso no encontrado en classpath: " + location);
        }
        try (InputStream in = res.getInputStream()) {
            Files.copy(in, dir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
