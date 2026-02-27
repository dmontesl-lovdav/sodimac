package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.dto.invoicexml.InvoiceXmlDto;
import com.sodimac.fiscal.api.model.entity.TaxEntity;

import java.util.UUID;

/**
 * Service for extracting and saving taxes from fiscal invoices and credit notes.
 *
 * Extracts information for:
 * - Transferred taxes (IVA, IEPS)
 * - Withheld taxes (ISR, IVA)
 * - Consolidated totals
 *
 * @author Sodimac Tech Team
 * @since 2025-11-11
 */
public interface TaxExtractionService {

    /**
     * Extracts and saves taxes from an invoice.
     *
     * @param invoiceUuid Invoice UUID
     * @param invoiceDto DTO with parsed XML information
     * @return Saved tax entity, or null if no taxes
     */
    TaxEntity extractAndSaveTaxes(UUID invoiceUuid, InvoiceXmlDto invoiceDto);
}
