import { getDataSource } from '@/config/typeorm-datasource.js';
import * as r from '@/repositories/accountStatement.repo.js';
import { Catalogs } from '@/utils/mockups.js';

// ─── Tablas auxiliares (mismas que en el batch service) ───────────────────────
const T_PO       = 'tenant_finance.account_statement_purchase_order';
const T_REC      = 'tenant_finance.account_statement_reception';
const T_DISCOUNT = 'tenant_finance.account_statement_discount';
const T_INVOICE  = 'tenant_finance.account_statement_invoice';
const T_CREDIT   = 'tenant_finance.account_statement_credit_note';
const T_PAYMENT  = 'tenant_finance.account_statement_payment';
// ─────────────────────────────────────────────────────────────────────────────

const ISSUER = {
    name: 'Comercializadora SDMHC S.A. de C.V. (SODIMAC MÉXICO)',
    address:
        'Av. Adolfo López Mateos No. 201, Col. Santa Cruz Acatlán, CP 53150, Naucalpan de Juárez, Estado de México.',
    rfc: 'CSD161207R2A',
    phone: '800 0625 222',
    email: 'analista@sodimac.com.mx',
};

function parseNum(v: unknown): number {
    if (v == null) return 0;
    if (typeof v === 'number') return v;
    const n = parseFloat(String(v).replace(/,/g, ''));
    return Number.isFinite(n) ? n : 0;
}

function toIso(value: Date | string | null | undefined): string | null {
    if (value == null) return null;
    const d = typeof value === 'string' ? new Date(value) : value;
    return Number.isNaN(d.getTime()) ? null : d.toISOString();
}

// ─── Tipo del payload ─────────────────────────────────────────────────────────

export type AccountStatementReportPayload = {
    meta: {
        accountStatementUuid: string;
        vendorNumber: number;
        year: number;
        month: number;
        version: number;
        initialBalance: number | null;
        finalBalance: number | null;
    };
    issuer: typeof ISSUER;
    vendor: {
        vendorNumber: number;
        vendorName: string;
        providerRfc: string;
        providerCp: string;
        providerAddress: string;
        providerContact: string;
        providerEmail: string;
    };
    dates: {
        issueDate: string | null;
        periodStart: string | null;
        periodEnd: string | null;
        generatedAt: string;
    };
    totals: {
        totalOC: number;
        totalFacturasPendientes: number;
        totalFacturasPagadas: number;
        totalDescuentos: number;
        totalNotasCredito: number;
        saldoPendiente: number;
        counts: {
            purchaseOrders: number;
            facturas: number;
            payments: number;
            rebates: number;
            notasCredito: number;
        };
    };
    purchaseOrders: Record<string, unknown>[];
    receptions:     Record<string, unknown>[];
    payments:       Record<string, unknown>[];
    rebates:        Record<string, unknown>[];
    facturas:       Record<string, unknown>[];
    notasCredito:   Record<string, unknown>[];
    catalogs: {
        invoiceStatus:    typeof Catalogs.CatEstatusFactura;
        paymentStatus:    typeof Catalogs.CatEstatusPago;
        creditNoteStatus: typeof Catalogs.CatEstatusNotaCredito;
    };
};

// ─── Transformers aux-row → PDF-compatible object ─────────────────────────────
// El PDF service accede a propiedades en camelCase de las entidades originales.
// Aquí mapeamos las columnas de las tablas auxiliares a esos mismos nombres.

type Row = Record<string, unknown>;

function toPurchaseOrderShape(row: Row): Row {
    return {
        purchaseOrderId:   String(row['order_number'] ?? ''),   // clave de enlace con reception
        orderNumber:       String(row['order_number'] ?? ''),
        purchaseOrderDate: row['document_date'] ?? null,
        amount:            row['amount'],
        status:            row['status'],
    };
}

function toReceptionShape(row: Row): Row {
    return {
        receptionId:     row['account_statement_reception_uuid'],
        purchaseOrderId: String(row['order_number'] ?? ''),      // misma clave de enlace que PO
        receptionNumber: String(row['reception_number'] ?? ''),
        receptionDate:   row['reception_date'] ?? null,
        amount:          row['amount'],
        status:          row['status'],
    };
}

function toPaymentShape(row: Row): Row {
    return {
        fiscalPaymentUuid: row['account_statement_payment_uuid'],
        documentNumber:    row['document_number'],
        referenceNumber:   row['reference_number'],
        paymentDate:       row['payment_date'],
        createdAt:         row['created_at'],
        currency:          row['currency'] ?? 'MXN',
        amount:            row['amount'],
        status:            row['status'],
    };
}

function toRebateShape(row: Row): Row {
    return {
        rebateId:        row['account_statement_discount_uuid'],
        documentNumber:  row['document_number'],
        referenceNumber: row['reference_number'],
        postingDate:     row['discount_date'],
        amount:          row['amount'],
        status:          row['status'],
    };
}

function toInvoiceShape(row: Row): Row {
    return {
        invoiceUuid:        row['account_statement_invoice_uuid'],
        folio:              row['folio'],
        series:             row['series'],
        issueDate:          row['stamp_date'],
        certificationDate:  row['accounting_date'],
        total:              row['amount'],
        status:             row['invoice_status'],
        documentType:       'I',
    };
}

function toCreditNoteShape(row: Row): Row {
    return {
        invoiceUuid:       row['account_statement_credit_note_uuid'],
        folio:             row['folio'],
        series:            row['series'],
        issueDate:         row['issue_date'],
        certificationDate: row['accounting_date'],
        total:             row['amount'],
        status:            row['status'],
        documentType:      'E',
    };
}

// ─────────────────────────────────────────────────────────────────────────────

/**
 * Construye el payload completo del estado de cuenta leyendo las tablas
 * auxiliares de tenant_finance (snapshot generado por el batch).
 */
export async function buildAccountStatementReportData(
    uuid: string
): Promise<AccountStatementReportPayload | null> {
    const row = await r.findById(uuid);
    if (!row) return null;

    const ds = getDataSource();

    const [poRows, recRows, discountRows, paymentRows, invoiceRows, creditRows] =
        await Promise.all([
            ds.query(`SELECT * FROM ${T_PO}       WHERE account_statement_uuid = $1`, [uuid]),
            ds.query(`SELECT * FROM ${T_REC}      WHERE account_statement_uuid = $1`, [uuid]),
            ds.query(`SELECT * FROM ${T_DISCOUNT} WHERE account_statement_uuid = $1`, [uuid]),
            ds.query(`SELECT * FROM ${T_PAYMENT}  WHERE account_statement_uuid = $1`, [uuid]),
            ds.query(`SELECT * FROM ${T_INVOICE}  WHERE account_statement_uuid = $1`, [uuid]),
            ds.query(`SELECT * FROM ${T_CREDIT}   WHERE account_statement_uuid = $1`, [uuid]),
        ]);

    const purchaseOrders = (poRows       as Row[]).map(toPurchaseOrderShape);
    const receptions     = (recRows      as Row[]).map(toReceptionShape);
    const payments       = (paymentRows  as Row[]).map(toPaymentShape);
    const rebates        = (discountRows as Row[]).map(toRebateShape);
    const facturas       = (invoiceRows  as Row[]).map(toInvoiceShape);
    const notasCredito   = (creditRows   as Row[]).map(toCreditNoteShape);

    const vendorNumber = Number(row.vendorNumber);
    const periodStart  = row.periodStart ? new Date(row.periodStart) : new Date(row.year, row.month - 1, 1);
    const periodEnd    = row.periodEnd   ? new Date(row.periodEnd)   : new Date(row.year, row.month, 0, 23, 59, 59);

    const totalOC                 = purchaseOrders.reduce((s, po) => s + parseNum(po['amount']), 0);
    const totalFacturasPendientes = facturas.reduce((s, i) => s + parseNum(i['total']), 0);
    const totalFacturasPagadas    = payments.reduce((s, p) => s + parseNum(p['amount']), 0);
    const totalDescuentos         = rebates.reduce((s, b) => s + parseNum(b['amount']), 0);
    const totalNotasCredito       = notasCredito.reduce((s, c) => s + parseNum(c['total']), 0);
    const saldoPendiente          = parseNum(row.finalBalance);

    return {
        meta: {
            accountStatementUuid: row.accountStatementUuid,
            vendorNumber,
            year:           row.year,
            month:          row.month,
            version:        row.version,
            initialBalance: row.initialBalance != null ? parseNum(row.initialBalance) : null,
            finalBalance:   row.finalBalance   != null ? parseNum(row.finalBalance)   : null,
        },
        issuer: ISSUER,
        vendor: {
            vendorNumber,
            vendorName:      `Proveedor ${vendorNumber}`,
            providerRfc:     'N/A',
            providerCp:      '',
            providerAddress: '',
            providerContact: '',
            providerEmail:   '',
        },
        dates: {
            issueDate:    toIso(row.issuedAt),
            periodStart:  toIso(periodStart),
            periodEnd:    toIso(periodEnd),
            generatedAt:  new Date().toISOString(),
        },
        totals: {
            totalOC,
            totalFacturasPendientes,
            totalFacturasPagadas,
            totalDescuentos,
            totalNotasCredito,
            saldoPendiente,
            counts: {
                purchaseOrders: purchaseOrders.length,
                facturas:       facturas.length,
                payments:       payments.length,
                rebates:        rebates.length,
                notasCredito:   notasCredito.length,
            },
        },
        purchaseOrders,
        receptions,
        payments,
        rebates,
        facturas,
        notasCredito,
        catalogs: {
            invoiceStatus:    Catalogs.CatEstatusFactura,
            paymentStatus:    Catalogs.CatEstatusPago,
            creditNoteStatus: Catalogs.CatEstatusNotaCredito,
        },
    };
}
