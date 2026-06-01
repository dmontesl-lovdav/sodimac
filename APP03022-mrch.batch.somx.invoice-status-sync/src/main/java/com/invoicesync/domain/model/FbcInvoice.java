package com.invoicesync.domain.model;

import com.invoicesync.domain.enums.InvoiceFlowStatus;

import java.time.LocalDateTime;

public class FbcInvoice {

    private final String idProveedor;
    private final String serie;
    private final String folio;
    private final String uuid;
    private final InvoiceFlowStatus currentStatus;
    private final LocalDateTime lastUpdated;

    private FbcInvoice(Builder builder) {
        this.idProveedor = builder.idProveedor;
        this.serie = builder.serie;
        this.folio = builder.folio;
        this.uuid = builder.uuid;
        this.currentStatus = builder.currentStatus;
        this.lastUpdated = builder.lastUpdated;
    }

    public String getDocumentNumber() {
        return serie + folio;
    }

    public String getIdProveedor() {
        return idProveedor;
    }

    public String getSerie() {
        return serie;
    }

    public String getFolio() {
        return folio;
    }

    public String getUuid() {
        return uuid;
    }

    public InvoiceFlowStatus getCurrentStatus() {
        return currentStatus;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public FbcInvoice withNewStatus(InvoiceFlowStatus newStatus) {
        return new Builder()
                .idProveedor(this.idProveedor)
                .serie(this.serie)
                .folio(this.folio)
                .uuid(this.uuid)
                .currentStatus(newStatus)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String idProveedor;
        private String serie;
        private String folio;
        private String uuid;
        private InvoiceFlowStatus currentStatus;
        private LocalDateTime lastUpdated;

        public Builder idProveedor(String idProveedor) {
            this.idProveedor = idProveedor;
            return this;
        }

        public Builder serie(String serie) {
            this.serie = serie;
            return this;
        }

        public Builder folio(String folio) {
            this.folio = folio;
            return this;
        }

        public Builder uuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder currentStatus(InvoiceFlowStatus currentStatus) {
            this.currentStatus = currentStatus;
            return this;
        }

        public Builder lastUpdated(LocalDateTime lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        public FbcInvoice build() {
            return new FbcInvoice(this);
        }
    }
}
