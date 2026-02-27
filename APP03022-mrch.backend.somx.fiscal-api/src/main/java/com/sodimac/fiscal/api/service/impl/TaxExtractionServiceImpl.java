package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.model.dto.invoicexml.InvoiceXmlDto;
import com.sodimac.fiscal.api.model.entity.*;
import com.sodimac.fiscal.api.repository.TaxRepository;
import com.sodimac.fiscal.api.service.TaxExtractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
@Service
@Slf4j
@RequiredArgsConstructor
public class TaxExtractionServiceImpl implements TaxExtractionService {

    private final TaxRepository taxRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public TaxEntity extractAndSaveTaxes(UUID invoiceUuid, InvoiceXmlDto invoiceDto) {
        log.debug("Extracting taxes for invoice UUID: {}", invoiceUuid);

        if (invoiceDto.getImpuestosFactura() == null) {
            log.debug("No taxes in the invoice");
            return null;
        }

        var taxesDto = invoiceDto.getImpuestosFactura();

        // Create main tax entity
        TaxEntity taxEntity = new TaxEntity();
        taxEntity.setInvoiceUuid(invoiceUuid);

        // Consolidated totals
        if (taxesDto.getTotalImpuestosTrasladados() != null && !taxesDto.getTotalImpuestosTrasladados().isEmpty()) {
            taxEntity.setTotalTransferredTaxes(new BigDecimal(taxesDto.getTotalImpuestosTrasladados()));
        }

        if (taxesDto.getTotalImpuestosRetenidos() != null && !taxesDto.getTotalImpuestosRetenidos().isEmpty()) {
            taxEntity.setTotalWithheldTaxes(new BigDecimal(taxesDto.getTotalImpuestosRetenidos()));
        }

        // Save main entity first
        taxEntity = taxRepository.save(taxEntity);
        log.debug("Taxes saved. UUID: {}", taxEntity.getTaxUuid());

        // Save transfers (IVA, IEPS)
        log.debug("Traslados object: {}", taxesDto.getTraslados());
        log.debug("Traslados is null: {}", taxesDto.getTraslados() == null);
        if (taxesDto.getTraslados() != null) {
            log.debug("Traslados is empty: {}", taxesDto.getTraslados().isEmpty());
        }

        if (taxesDto.getTraslados() != null && !taxesDto.getTraslados().isEmpty()) {
            log.debug("Saving {} transfers", taxesDto.getTraslados().size());
            for (var transferDto : taxesDto.getTraslados()) {
                TaxTransferEntity transfer = new TaxTransferEntity();
                transfer.setTaxUuid(taxEntity.getTaxUuid());
                transfer.setTaxCode(transferDto.getImpuesto());
                transfer.setFactorType(transferDto.getTipoFactor());

                if (transferDto.getTasaOCuota() != null && !transferDto.getTasaOCuota().isEmpty()) {
                    transfer.setRateOrQuota(new BigDecimal(transferDto.getTasaOCuota()));
                }
                if (transferDto.getImporte() != null && !transferDto.getImporte().isEmpty()) {
                    transfer.setAmount(new BigDecimal(transferDto.getImporte()));
                }
                if (transferDto.getBase() != null && !transferDto.getBase().isEmpty()) {
                    transfer.setBase(new BigDecimal(transferDto.getBase()));
                }

                taxEntity.addTransfer(transfer);
            }
        }

        // Save withholdings (ISR, IVA)
        log.debug("Retenciones object: {}", taxesDto.getRetenciones());
        log.debug("Retenciones is null: {}", taxesDto.getRetenciones() == null);
        if (taxesDto.getRetenciones() != null) {
            log.debug("Retenciones is empty: {}", taxesDto.getRetenciones().isEmpty());
        }

        if (taxesDto.getRetenciones() != null && !taxesDto.getRetenciones().isEmpty()) {
            log.debug("Saving {} withholdings", taxesDto.getRetenciones().size());
            for (var withholdingDto : taxesDto.getRetenciones()) {
                TaxWithholdingEntity withholding = new TaxWithholdingEntity();
                withholding.setTaxUuid(taxEntity.getTaxUuid());
                withholding.setTaxCode(withholdingDto.getImpuesto());

                if (withholdingDto.getImporte() != null && !withholdingDto.getImporte().isEmpty()) {
                    withholding.setAmount(new BigDecimal(withholdingDto.getImporte()));
                }

                taxEntity.addWithholding(withholding);
            }
        }

        // Save with cascade relationships
        taxEntity = taxRepository.save(taxEntity);
        log.info("Taxes saved successfully. Total transfers: {}, Total withholdings: {}",
                taxEntity.getTransfers().size(),
                taxEntity.getWithholdings().size());

        return taxEntity;
    }
}
