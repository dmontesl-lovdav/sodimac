import {
    isCancelledInvoiceOrCreditNote,
    isCancelledReception,
    omitCancelledAccountStatementRows,
    omitPurchaseOrdersWithoutVisibleReception,
} from '../pdf/accountStatementCancelled';
import type { CatalogStatusItem } from '../interfaces/accountStatementReport';

const invoiceCatalog: CatalogStatusItem[] = [
    { key: 'EFA0013', description: 'Pendiente de Pago', color: 'Rojo' },
    { key: 'EFA0020', description: 'Cancelada', color: null },
];

const creditNoteCatalog: CatalogStatusItem[] = [
    { key: 'ENC0019', description: 'Completado', color: null },
    { key: 'ENC0020', description: 'Cancelada', color: null },
];

describe('accountStatementCancelled', () => {
    describe('isCancelledReception', () => {
        it('omite estatus 7 (cancelada) y 8 (borrada)', () => {
            expect(isCancelledReception(7)).toBe(true);
            expect(isCancelledReception('8')).toBe(true);
            expect(isCancelledReception(0)).toBe(false);
            expect(isCancelledReception('0')).toBe(false);
        });

        it('omite por etiqueta Cancelada del catálogo de recepción', () => {
            expect(
                isCancelledReception('99', [{ value: '99', label: 'Cancelada' }])
            ).toBe(true);
            expect(
                isCancelledReception('0', [{ value: '0', label: 'Abierta' }])
            ).toBe(false);
        });
    });

    describe('isCancelledInvoiceOrCreditNote', () => {
        it('omite estatus numérico 20 y claves EFA0020 / ENC0020', () => {
            expect(isCancelledInvoiceOrCreditNote(20, invoiceCatalog)).toBe(true);
            expect(isCancelledInvoiceOrCreditNote('20', creditNoteCatalog)).toBe(true);
            expect(
                isCancelledInvoiceOrCreditNote('EFA0020', invoiceCatalog)
            ).toBe(true);
            expect(
                isCancelledInvoiceOrCreditNote('ENC0020', creditNoteCatalog)
            ).toBe(true);
        });

        it('conserva facturas y NC que no están canceladas', () => {
            expect(isCancelledInvoiceOrCreditNote(13, invoiceCatalog)).toBe(false);
            expect(
                isCancelledInvoiceOrCreditNote('EFA0013', invoiceCatalog)
            ).toBe(false);
            expect(
                isCancelledInvoiceOrCreditNote('ENC0019', creditNoteCatalog)
            ).toBe(false);
        });
    });

    describe('omitCancelledAccountStatementRows', () => {
        it('deja fuera recepciones canceladas y conserva el resto', () => {
            const rows = omitCancelledAccountStatementRows(
                [{ status: '0' }, { status: 7 }, { status: '8' }],
                (status) => isCancelledReception(status)
            );
            expect(rows).toEqual([{ status: '0' }]);
        });

        it('deja fuera facturas canceladas usando el catálogo del payload', () => {
            const rows = omitCancelledAccountStatementRows(
                [
                    { status: 'EFA0013', folio: 'A' },
                    { status: 'EFA0020', folio: 'B' },
                    { status: 20, folio: 'C' },
                ],
                (status) => isCancelledInvoiceOrCreditNote(status, invoiceCatalog)
            );
            expect(rows).toEqual([{ status: 'EFA0013', folio: 'A' }]);
        });
    });

    describe('omitPurchaseOrdersWithoutVisibleReception', () => {
        it('omite la OC si el API ya no mandó su recepción cancelada (caso 855570)', () => {
            const rows = omitPurchaseOrdersWithoutVisibleReception(
                [
                    {
                        purchaseOrderId: '855394',
                        orderNumber: '855394',
                        status: '0',
                    },
                    {
                        purchaseOrderId: '855570',
                        orderNumber: '855570',
                        status: '0',
                    },
                ],
                [{ purchaseOrderId: '855394', status: '0' }]
            );
            expect(rows.map((row) => row.purchaseOrderId)).toEqual(['855394']);
        });

        it('omite la OC si su recepción visible quedó fuera por cancelada', () => {
            const visibleReceptions = omitCancelledAccountStatementRows(
                [
                    { purchaseOrderId: '855394', status: '0' },
                    { purchaseOrderId: '855570', status: '7' },
                ],
                (status) => isCancelledReception(status)
            );
            const rows = omitPurchaseOrdersWithoutVisibleReception(
                [
                    { purchaseOrderId: '855394', status: '0' },
                    { purchaseOrderId: '855570', status: '0' },
                ],
                visibleReceptions
            );
            expect(rows.map((row) => row.purchaseOrderId)).toEqual(['855394']);
        });
    });
});
