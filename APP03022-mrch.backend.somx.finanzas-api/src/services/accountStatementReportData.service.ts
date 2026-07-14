import { getDataSource } from '@/config/typeorm-datasource.js';
import * as r from '@/repositories/accountStatement.repo.js';

// ─── Tablas auxiliares ────────────────────────────────────────────────────────
const T_PO       = 'tenant_finance.account_statement_purchase_order';
const T_REC      = 'tenant_finance.account_statement_reception';
const T_DISCOUNT = 'tenant_finance.account_statement_discount';
const T_INVOICE  = 'tenant_finance.account_statement_invoice';
const T_CREDIT   = 'tenant_finance.account_statement_credit_note';
const T_PAYMENT  = 'tenant_finance.account_statement_payment';

// ─── Catálogos de estatus (shared_catalogs)
const CAT_CODE_INVOICE     = 'CatEstatusFactura';
const CAT_CODE_PAYMENT     = 'CatEstatusPayment';
const CAT_CODE_CREDIT_NOTE = 'CatEstatusNotaCredito';
const CAT_LANG_ES          = 1; // id del idioma español en shared_catalogs.dictionary_lang
// status_catalog vive en tenant_finance (schema default de la conexión)
const T_STATUS_CATALOG     = 'tenant_finance.status_catalog';
// ─────────────────────────────────────────────────────────────────────────────

const ISSUER = {
    name:    'Comercializadora SDMHC S.A. de C.V. (SODIMAC MÉXICO)',
    address: 'Av. Adolfo López Mateos No. 201, Col. Santa Cruz Acatlán, CP 53150, Naucalpan de Juárez, Estado de México.',
    rfc:     'CSD161207R2A',
    phone:   '800 0625 222',
    email:   'analista@sodimac.com.mx',
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

// ─── Helpers de catálogos y proveedor ─────────────────────────────────────────

type CatalogItem  = { key: string; description: string; color: string | null; sortOrder: number };
type StatusItem   = { status: number; name: string; description: string | null };

async function fetchCatalogByCode(code: string): Promise<CatalogItem[]> {
    try {
        return await getDataSource().query(
            `SELECT
                cd.key,
                cd.color,
                cd.sort_order  AS "sortOrder",
                dl.description
             FROM shared_catalogs.catalog_header  ch
             JOIN shared_catalogs.catalog_detail   cd ON cd.header_id = ch.id  AND cd.status = 1
             JOIN shared_catalogs.dictionary_lang  dl ON dl.dict_id   = cd.dict_id AND dl.lang_id = $2
             WHERE ch.code = $1 AND ch.status = 1
             ORDER BY cd.sort_order ASC`,
            [code, CAT_LANG_ES]
        ) as CatalogItem[];
    } catch {
        return [];
    }
}

async function fetchStatusCatalog(): Promise<StatusItem[]> {
    try {
        return await getDataSource().query(
            `SELECT status, name, description
             FROM ${T_STATUS_CATALOG}
             ORDER BY status ASC`
        ) as StatusItem[];
    } catch {
        return [];
    }
}

interface SupplierInfo {
    supplierNumber: string;
    businessName:   string;
    rfc:            string;
    emailFinancial: string | null;
    emailPrincipal: string | null;
    emailCommercial:string | null;
}

async function fetchSupplierInfo(vendorNumber: number): Promise<SupplierInfo | null> {
    try {
        const rows = await getDataSource().query(
            `SELECT
                supplier_number   AS "supplierNumber",
                business_name     AS "businessName",
                rfc,
                email_financial   AS "emailFinancial",
                email_principal   AS "emailPrincipal",
                email_commercial  AS "emailCommercial"
             FROM shared_catalogs.supplier
             WHERE supplier_number::varchar = $1::varchar
               AND status = 1
             LIMIT 1`,
            [String(vendorNumber)]
        ) as SupplierInfo[];
        return rows[0] ?? null;
    } catch {
        return null;
    }
}

// ─── Transformers aux-row → shape compatible con el PDF service ───────────────
// El PDF service accede a propiedades en camelCase; las mapeamos aquí para
// no tener que modificar accountStatementPdf.service.ts.

type Row = Record<string, unknown>;

function toPurchaseOrderShape(row: Row): Row {
    return {
        purchaseOrderId:   String(row['order_number'] ?? ''),
        orderNumber:       String(row['order_number'] ?? ''),
        purchaseOrderDate: row['document_date'],
        amount:            row['amount'],
        status:            row['status'],
    };
}

function toReceptionShape(row: Row): Row {
    return {
        receptionId:     row['account_statement_reception_uuid'],
        purchaseOrderId: String(row['order_number'] ?? ''),
        receptionNumber: String(row['reception_number'] ?? ''),
        receptionDate:   row['reception_date'],
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
        rebateId:           row['account_statement_discount_uuid'],
        documentNumber:     row['document_number'],
        referenceNumber:    row['reference_number'],
        postingDate:        row['discount_date'],
        amount:             row['amount'],
        status:             row['status'],
    };
}

function toInvoiceShape(row: Row): Row {
    return {
        invoiceUuid:       row['uuid'],              // fiscal_uuid del SAT
        folio:             row['folio'],
        series:            row['series'],
        issueDate:         row['stamp_date'],
        certificationDate: row['accounting_date'],
        total:             row['amount'],
        status:            row['invoice_status'],
        documentType:      'I',
    };
}

function toCreditNoteShape(row: Row): Row {
    return {
        invoiceUuid:          row['uuid'],           // fiscal_uuid de la propia NC
        relatedInvoiceUuid:   row['document_number'],// fiscal_uuid de la factura original
        folio:                row['folio'],
        series:               row['series'],
        issueDate:            row['issue_date'],
        certificationDate:    row['accounting_date'],
        total:                row['amount'],
        status:               row['status'],
        documentType:         'E',
    };
}

// ─── Tipo del payload ─────────────────────────────────────────────────────────

export type AccountStatementReportPayload = {
    meta: {
        accountStatementUuid: string;
        vendorNumber:         number;
        year:                 number;
        month:                number;
        version:              number;
        initialBalance:       number | null;
        finalBalance:         number | null;
    };
    issuer: typeof ISSUER;
    vendor: {
        vendorNumber:    number;
        vendorName:      string;
        providerRfc:     string;
        providerCp:      string;
        providerAddress: string;
        providerContact: string;
        providerEmail:   string;
    };
    dates: {
        issueDate:    string | null;
        periodStart:  string | null;
        periodEnd:    string | null;
        generatedAt:  string;
    };
    totals: {
        totalOC:                  number;
        totalFacturasPendientes:  number;
        totalFacturasPagadas:     number;
        totalDescuentos:          number;
        totalNotasCredito:        number;
        saldoPendiente:           number;
        counts: {
            purchaseOrders: number;
            facturas:       number;
            payments:       number;
            rebates:        number;
            notasCredito:   number;
        };
    };
    purchaseOrders: Row[];
    receptions:     Row[];
    payments:       Row[];
    rebates:        Row[];
    facturas:       Row[];
    notasCredito:   Row[];
    catalogs: {
        purchaseOrderStatus: StatusItem[];
        invoiceStatus:       CatalogItem[];
        paymentStatus:       CatalogItem[];
        creditNoteStatus:    CatalogItem[];
    };
};

// ─────────────────────────────────────────────────────────────────────────────

/**
 * Construye el payload completo del estado de cuenta leyendo las tablas
 * auxiliares de tenant_finance. El proveedor y los catálogos de estatus
 * se obtienen directamente de shared_catalogs.
 */
export async function buildAccountStatementReportData(
    uuid: string
): Promise<AccountStatementReportPayload | null> {
    const row = await r.findById(uuid);
    if (!row) return null;

    const vendorNumber = Number(row.vendorNumber);
    const ds           = getDataSource();

    const [
        poRows, recRows, discountRows, paymentRows, invoiceRows, creditRows,
        supplierInfo,
        catPoStatus,
        catInvoice, catPayment, catCreditNote,
    ] = await Promise.all([
        ds.query(`SELECT * FROM ${T_PO}       WHERE account_statement_uuid = $1`, [uuid]),
        ds.query(`SELECT * FROM ${T_REC}      WHERE account_statement_uuid = $1`, [uuid]),
        ds.query(`SELECT * FROM ${T_DISCOUNT} WHERE account_statement_uuid = $1`, [uuid]),
        ds.query(`SELECT * FROM ${T_PAYMENT}  WHERE account_statement_uuid = $1`, [uuid]),
        ds.query(`SELECT * FROM ${T_INVOICE}  WHERE account_statement_uuid = $1`, [uuid]),
        ds.query(`SELECT * FROM ${T_CREDIT}   WHERE account_statement_uuid = $1`, [uuid]),
        fetchSupplierInfo(vendorNumber),
        fetchStatusCatalog(),
        fetchCatalogByCode(CAT_CODE_INVOICE),
        fetchCatalogByCode(CAT_CODE_PAYMENT),
        fetchCatalogByCode(CAT_CODE_CREDIT_NOTE),
    ]);

    const purchaseOrders = (poRows       as Row[]).map(toPurchaseOrderShape);
    const receptions     = (recRows      as Row[]).map(toReceptionShape);
    const payments       = (paymentRows  as Row[]).map(toPaymentShape);
    const rebates        = (discountRows as Row[]).map(toRebateShape);
    const facturas       = (invoiceRows  as Row[]).map(toInvoiceShape);
    const notasCredito   = (creditRows   as Row[]).map(toCreditNoteShape);

    const periodStart = row.periodStart ? new Date(row.periodStart) : new Date(row.year, row.month - 1, 1);
    const periodEnd   = row.periodEnd   ? new Date(row.periodEnd)   : new Date(row.year, row.month, 0, 23, 59, 59);

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
            vendorName:      supplierInfo?.businessName   ?? `Proveedor ${vendorNumber}`,
            providerRfc:     supplierInfo?.rfc             ?? 'N/A',
            providerCp:      '',
            providerAddress: '',
            providerContact: supplierInfo?.emailPrincipal  ?? '',
            providerEmail:   supplierInfo?.emailFinancial  ?? supplierInfo?.emailPrincipal ?? '',
        },
        dates: {
            issueDate:   toIso(row.issuedAt),
            periodStart: toIso(periodStart),
            periodEnd:   toIso(periodEnd),
            generatedAt: new Date().toISOString(),
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
            purchaseOrderStatus: catPoStatus,
            invoiceStatus:       catInvoice,
            paymentStatus:       catPayment,
            creditNoteStatus:    catCreditNote,
        },
    };
}
