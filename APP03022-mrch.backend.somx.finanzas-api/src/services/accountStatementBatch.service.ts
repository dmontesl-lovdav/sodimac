import * as r from '@/repositories/accountStatement.repo.js';
import * as poRepo from '@/repositories/purchaseOrder.repo.js';
import * as recRepo from '@/repositories/reception.repo.js';
import * as rebateRepo from '@/repositories/rebate.repo.js';
import * as fpRepo from '@/repositories/fiscalPayment.repo.js';
import { getDataSource } from '@/config/typeorm-datasource.js';
import type { PurchaseOrder } from '@/entities/PurchaseOrder.entity.js';
import type { Reception } from '@/entities/Reception.entity.js';
import type { Rebate } from '@/entities/Rebate.entity.js';
import type { FiscalPayment } from '@/entities/FiscalPayment.entity.js';
import type { BatchAccountStatementBody } from '@/schemas/accountStatement.schema.js';

// ─── Tablas auxiliares (tenant_finance) ──────────────────────────────────────
const T_PO       = 'tenant_finance.account_statement_purchase_order';
const T_REC      = 'tenant_finance.account_statement_reception';
const T_DISCOUNT = 'tenant_finance.account_statement_discount';
const T_INVOICE  = 'tenant_finance.account_statement_invoice';
const T_CREDIT   = 'tenant_finance.account_statement_credit_note';
const T_PAYMENT  = 'tenant_finance.account_statement_payment';
// ─────────────────────────────────────────────────────────────────────────────

const STATUS_GENERATED = 1;

// ─── Tipos internos ───────────────────────────────────────────────────────────

interface InvoiceRow {
    invoice_uuid:       string;
    fiscal_uuid:        string | null;
    document_type:      string;
    total:              string | number;
    currency:           string;
    exchange_rate:      string | number;
    folio:              string | null;
    series:             string | null;
    issue_date:         Date | string | null;
    certification_date: Date | string | null;
    accounting_date:    Date | string | null;
    status:             number | null;
    /** fiscal_uuid de la factura original (solo para notas de crédito) */
    related_fiscal_uuid: string | null;
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

function parseNum(v: unknown): number {
    if (v == null) return 0;
    if (typeof v === 'number') return v;
    const n = parseFloat(String(v).replace(/,/g, ''));
    return Number.isFinite(n) ? n : 0;
}

async function rawInsert(table: string, rows: Record<string, unknown>[]): Promise<void> {
    if (!rows.length) return;
    const cols = Object.keys(rows[0]!);
    const placeholders = rows
        .map((_, ri) =>
            `(${cols.map((_, ci) => `$${ri * cols.length + ci + 1}`).join(', ')})`
        )
        .join(', ');
    const params = rows.flatMap(row => cols.map(c => row[c]));
    await getDataSource().query(
        `INSERT INTO ${table} (${cols.join(', ')}) VALUES ${placeholders}`,
        params
    );
}

// ─── Mappers source → auxiliar ────────────────────────────────────────────────

function mapPurchaseOrder(po: PurchaseOrder, uuid: string): Record<string, unknown> {
    return {
        account_statement_uuid: uuid,
        order_number:           Number(po.orderNumber) || 0,
        document_date:          po.purchaseOrderDate ?? null,
        due_date:               null,
        currency:               'MXN',
        amount:                 parseNum(po.amount),
        exchange_rate:          1,
        base_currency:          'MXN',
        base_amount:            parseNum(po.amount),
        status:                 po.status != null ? String(po.status) : null,
        source_id:              po.originId ?? null,
        created_at:             new Date(),
    };
}

function mapReception(rec: Reception, uuid: string): Record<string, unknown> {
    return {
        account_statement_uuid: uuid,
        reception_number:       Number(rec.receptionNumber) || 0,
        order_number:           Number(rec.purchaseOrder?.orderNumber) || 0,
        document_date:          rec.purchaseOrder?.purchaseOrderDate ?? null,
        reception_date:         rec.receptionDate ?? null,
        due_date:               null,
        currency:               'MXN',
        amount:                 parseNum(rec.amount),
        exchange_rate:          1,
        base_currency:          'MXN',
        base_amount:            parseNum(rec.amount),
        status:                 rec.status != null ? String(rec.status) : null,
        source_id:              rec.originId ?? null,
        created_at:             new Date(),
    };
}

function mapDiscount(rb: Rebate, uuid: string): Record<string, unknown> {
    return {
        account_statement_uuid: uuid,
        document_number:        rb.documentNumber ?? null,
        reference_number:       rb.referenceNumber ?? null,
        series:                 null,
        folio:                  null,
        uuid:                   rb.sapDocument ?? null,
        discount_date:          rb.postingDate ?? null,
        accounting_date:        null,
        currency:               'MXN',
        amount:                 parseNum(rb.amount),
        exchange_rate:          1,
        base_currency:          'MXN',
        base_amount:            parseNum(rb.amount),
        status:                 rb.status != null ? String(rb.status) : null,
        created_at:             new Date(),
    };
}

function mapPayment(fp: FiscalPayment, uuid: string): Record<string, unknown> {
    return {
        account_statement_uuid: uuid,
        payment_id:             null,
        document_number:        fp.documentNumber ?? null,
        reference_number:       fp.referenceNumber ?? null,
        payment_date:           fp.paymentDate ?? null,
        currency:               fp.currency ?? 'MXN',
        amount:                 parseNum(fp.amount),
        exchange_rate:          1,
        base_currency:          'MXN',
        base_amount:            parseNum(fp.amount),
        status:                 fp.status != null ? String(fp.status) : null,
        created_at:             new Date(),
    };
}

function mapInvoice(row: InvoiceRow, uuid: string): Record<string, unknown> {
    return {
        account_statement_uuid: uuid,
        invoice_type:           'PENDING',
        series:                 row.series ?? null,
        folio:                  row.folio ?? null,
        // fiscal_uuid del SAT (no el UUID interno del registro)
        uuid:                   row.fiscal_uuid ? String(row.fiscal_uuid) : null,
        stamp_date:             row.issue_date ?? null,
        accounting_date:        row.accounting_date ?? null,
        payment_date:           null,
        currency:               row.currency ?? 'MXN',
        amount:                 parseNum(row.total),
        exchange_rate:          parseNum(row.exchange_rate) || 1,
        base_currency:          'MXN',
        base_amount:            parseNum(row.total),
        invoice_status:         row.status != null ? String(row.status) : null,
        payment_id:             null,
        created_at:             new Date(),
    };
}

function mapCreditNote(row: InvoiceRow, uuid: string): Record<string, unknown> {
    return {
        account_statement_uuid: uuid,
        // fiscal_uuid de la factura original relacionada (via related_cfdi)
        document_number:        row.related_fiscal_uuid ?? null,
        series:                 row.series ?? null,
        folio:                  row.folio ?? null,
        // fiscal_uuid de la propia nota de crédito
        uuid:                   row.fiscal_uuid ? String(row.fiscal_uuid) : null,
        issue_date:             row.issue_date ?? null,
        accounting_date:        row.accounting_date ?? null,
        currency:               row.currency ?? 'MXN',
        amount:                 parseNum(row.total),
        exchange_rate:          parseNum(row.exchange_rate) || 1,
        base_currency:          'MXN',
        base_amount:            parseNum(row.total),
        status:                 row.status != null ? String(row.status) : null,
        created_at:             new Date(),
    };
}

// ─── Query enriquecida de facturas y notas de crédito ─────────────────────────

/**
 * Obtiene facturas (type='I') y notas de crédito (type='E') del proveedor
 * en el período, incluyendo el fiscal_uuid de la factura original relacionada
 * para las NCs (via tenant_fiscal.related_cfdi).
 */
async function findInvoicesAndCreditNotes(
    vendorNumber: number,
    start: Date,
    end: Date
): Promise<InvoiceRow[]> {
    const rows = await getDataSource().query(
        `SELECT
            i.invoice_uuid,
            i.fiscal_uuid,
            i.document_type,
            i.total,
            i.currency,
            i.exchange_rate,
            i.folio,
            i.series,
            i.issue_date,
            i.certification_date,
            i.accounting_date,
            i.status,
            ri.fiscal_uuid AS related_fiscal_uuid
         FROM tenant_fiscal.invoice i
         INNER JOIN tenant_fiscal.addendum a
                 ON a.invoice_uuid = i.invoice_uuid
         LEFT JOIN tenant_fiscal.related_cfdi rc
                ON rc.invoice_uuid = i.invoice_uuid
         LEFT JOIN tenant_fiscal.invoice ri
                ON ri.invoice_uuid = rc.related_invoice_uuid
         WHERE a.supplier_number = $1
           AND i.document_type IN ('I', 'E')
           AND i.issue_date BETWEEN $2 AND $3
         ORDER BY i.document_type DESC, i.issue_date ASC`,
        [vendorNumber, start, end]
    );
    return rows as InvoiceRow[];
}

// ─── Exports ──────────────────────────────────────────────────────────────────

export interface BatchResult {
    created: number;
    skipped: number;
    errors: Array<{ vendorNumber: number; reason: string }>;
    details: Array<{ vendorNumber: number; accountStatementUuid: string; status: 'created' | 'skipped' }>;
}

/**
 * Genera estados de cuenta en batch.
 * - Ignora si ya existe un registro activo (status > 0) para vendor+year+month.
 * - Crea el header y copia los datos fuente en las 6 tablas auxiliares,
 *   filtrando correctamente por proveedor y usando UUIDs fiscales (SAT).
 */
export async function batchGenerate(body: BatchAccountStatementBody): Promise<BatchResult> {
    const now   = new Date();
    const year  = body.year  ?? now.getFullYear();
    const month = body.month ?? (now.getMonth() + 1);

    const vendorNumbers: number[] = body.supplierNumber
        ? [body.supplierNumber]
        : await r.findAllVendorNumbers();

    const result: BatchResult = { created: 0, skipped: 0, errors: [], details: [] };

    for (const vendorNumber of vendorNumbers) {
        try {
            const existing = await r.findExistingActive(vendorNumber, year, month);
            if (existing) {
                result.skipped++;
                result.details.push({
                    vendorNumber,
                    accountStatementUuid: existing.accountStatementUuid,
                    status: 'skipped',
                });
                continue;
            }

            const periodStart = new Date(year, month - 1, 1);
            const periodEnd   = new Date(year, month, 0, 23, 59, 59);

            const statement = await r.createStatement({
                vendorNumber,
                year,
                month,
                version:     1,
                status:      STATUS_GENERATED,
                periodStart,
                periodEnd,
                issuedAt:    now,
                processedAt: now,
                createdAt:   now,
                updatedAt:   now,
            });

            const statementUuid = statement.accountStatementUuid;

            const [purchaseOrders, receptions, rebates, payments, invoiceRows] =
                await Promise.all([
                    poRepo.findByVendorAndDateRange(vendorNumber, periodStart, periodEnd),
                    recRepo.findByVendorAndDateRange(vendorNumber, periodStart, periodEnd),
                    rebateRepo.findByVendorAndPostingDateRange(vendorNumber, periodStart, periodEnd),
                    fpRepo.findByVendorAndPaymentDateRange(vendorNumber, periodStart, periodEnd),
                    findInvoicesAndCreditNotes(vendorNumber, periodStart, periodEnd),
                ]);

            const facturas     = invoiceRows.filter(i => i.document_type === 'I');
            const notasCredito = invoiceRows.filter(i => i.document_type === 'E');

            await Promise.all([
                rawInsert(T_PO,       purchaseOrders.map(po  => mapPurchaseOrder(po,        statementUuid))),
                rawInsert(T_REC,      receptions.map(rec     => mapReception(rec,            statementUuid))),
                rawInsert(T_DISCOUNT, rebates.map(rb         => mapDiscount(rb,              statementUuid))),
                rawInsert(T_PAYMENT,  payments.map(fp        => mapPayment(fp,               statementUuid))),
                rawInsert(T_INVOICE,  facturas.map(inv       => mapInvoice(inv,              statementUuid))),
                rawInsert(T_CREDIT,   notasCredito.map(inv   => mapCreditNote(inv,           statementUuid))),
            ]);

            result.created++;
            result.details.push({ vendorNumber, accountStatementUuid: statementUuid, status: 'created' });
        } catch (err) {
            result.errors.push({
                vendorNumber,
                reason: err instanceof Error ? err.message : String(err),
            });
        }
    }

    return result;
}

/**
 * Elimina lógicamente el estado de cuenta y todos sus registros en las 6 tablas auxiliares.
 */
export async function softDeleteWithChildren(uuid: string): Promise<{ deleted: boolean }> {
    const statement = await r.findById(uuid);
    if (!statement) return { deleted: false };

    const ds = getDataSource();

    await Promise.all([
        r.softDelete(uuid),
        ds.query(`UPDATE ${T_PO}       SET status = '0' WHERE account_statement_uuid = $1`, [uuid]),
        ds.query(`UPDATE ${T_REC}      SET status = '0' WHERE account_statement_uuid = $1`, [uuid]),
        ds.query(`UPDATE ${T_DISCOUNT} SET status = '0' WHERE account_statement_uuid = $1`, [uuid]),
        ds.query(`UPDATE ${T_PAYMENT}  SET status = '0' WHERE account_statement_uuid = $1`, [uuid]),
        ds.query(`UPDATE ${T_INVOICE}  SET invoice_status = '0' WHERE account_statement_uuid = $1`, [uuid]),
        ds.query(`UPDATE ${T_CREDIT}   SET status = '0' WHERE account_statement_uuid = $1`, [uuid]),
    ]);

    return { deleted: true };
}
