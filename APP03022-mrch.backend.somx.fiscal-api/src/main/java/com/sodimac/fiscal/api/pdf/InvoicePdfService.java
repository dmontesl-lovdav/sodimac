package com.sodimac.fiscal.api.pdf;

import com.sodimac.fiscal.api.model.entity.InvoiceEntity;
import com.sodimac.fiscal.api.pdf.PdfRenderService;
import com.sodimac.fiscal.api.repository.InvoiceRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class InvoicePdfService {

    private final InvoiceRepository invoiceRepository;
    private final PdfRenderService pdfRenderService;

    public InvoicePdfService(InvoiceRepository invoiceRepository,
            PdfRenderService pdfRenderService) {
        this.invoiceRepository = invoiceRepository;
        this.pdfRenderService = pdfRenderService;
    }

    public String getXmlByFiscalUuid(UUID invoiceUuid) {
        InvoiceEntity invoice = invoiceRepository.findByFiscalUuid(invoiceUuid)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró factura con invoiceUuid " + invoiceUuid));

        return invoice.getXmlContent();
    }

    public byte[] generatePdfFromFiscalUuid(UUID invoiceUuid) {
        String xml = getXmlByFiscalUuid(invoiceUuid);
        return pdfRenderService.renderFromXml(xml);
    }
}
