import {
    isExcludedInvoiceOrCreditNoteStatus,
    isExcludedReceptionStatus,
} from "../accountStatementExcludedStatuses.js";

describe("accountStatementExcludedStatuses", () => {
    it("excluye factura y NC en estatus 20 (cancelada)", () => {
        expect(isExcludedInvoiceOrCreditNoteStatus(20)).toBe(true);
        expect(isExcludedInvoiceOrCreditNoteStatus("20")).toBe(true);
        expect(isExcludedInvoiceOrCreditNoteStatus(19)).toBe(false);
        expect(isExcludedInvoiceOrCreditNoteStatus(null)).toBe(false);
    });

    it("excluye recepción cancelada (7) y borrada (8), no 20", () => {
        expect(isExcludedReceptionStatus(7)).toBe(true);
        expect(isExcludedReceptionStatus("8")).toBe(true);
        expect(isExcludedReceptionStatus(20)).toBe(false);
        expect(isExcludedReceptionStatus(0)).toBe(false);
    });
});
