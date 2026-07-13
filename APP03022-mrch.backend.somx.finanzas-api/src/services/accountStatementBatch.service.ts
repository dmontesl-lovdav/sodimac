import * as r from '@/repositories/accountStatement.repo.js';
import * as poRepo from '@/repositories/purchaseOrder.repo.js';
import * as recRepo from '@/repositories/reception.repo.js';
import * as rebateRepo from '@/repositories/rebate.repo.js';
import * as fpRepo from '@/repositories/fiscalPayment.repo.js';
import { getDataSource } from '@/config/typeorm-datasource.js';
import { Invoice } from '@/entities/tenant_fiscal.invoice.entity.js';
import { Addendum } from '@/entities/tenant_fiscal.addendum.entity.js';
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

// ─── Helpers ─────────────────────────────────────────────────────────────────

function parseNum(v: string | number | null | undefined): number {
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
    const orderNumber = rec.purchaseOrder?.orderNumber ?? null;
    return {
        account_statement_uuid: uuid,
        reception_number:       Number(rec.receptionNumber) || 0,
        order_number:           Number(orderNumber) || 0,
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

function mapInvoice(inv: Invoice, uuid: string): Record<string, unknown> {
    return {
        account_statement_uuid: uuid,
        invoice_type:           'PENDING',
        series:                 inv.series ?? null,
        folio:                  inv.folio ?? null,
        uuid:                   inv.fiscalUuid ? String(inv.fiscalUuid) : null,
        stamp_date:             inv.issueDate ?? null,
        accounting_date:        inv.certificationDate ?? null,
        payment_date:           null,
        currency:               'MXN',
        amount:                 parseNum(inv.total),
        exchange_rate:          1,
        base_currency:          'MXN',
        base_amount:            parseNum(inv.total),
        invoice_status:         inv.status != null ? String(inv.status) : null,
        payment_id:             null,
        created_at:             new Date(),
    };
}

function mapCreditNote(inv: Invoice, uuid: string): Record<string, unknown> {
    return {
        account_statement_uuid: uuid,
        document_number:        null,
        series:                 inv.series ?? null,
        folio:                  inv.folio ?? null,
        uuid:                   inv.fiscalUuid ? String(inv.fiscalUuid) : null,
        issue_date:             inv.issueDate ?? null,
        accounting_date:        inv.certificationDate ?? null,
        currency:               'MXN',
        amount:                 parseNum(inv.total),
        exchange_rate:          1,
        base_currency:          'MXN',
        base_amount:            parseNum(inv.total),
        status:                 inv.status != null ? String(inv.status) : null,
        created_at:             new Date(),
    };
}

// ─────────────────────────────────────────────────────────────────────────────

async function findInvoicesByVendorAndPeriod(
    vendorNumber: number,
    start: Date,
    end: Date
): Promise<Invoice[]> {
    return getDataSource()
        .getRepository(Invoice)
        .createQueryBuilder('i')
        .innerJoin(Addendum, 'a', 'a.invoiceUuid = i.invoiceUuid')
        .where('a.supplierNumber = :vendor', { vendor: vendorNumber })
        .andWhere('i.issueDate BETWEEN :start AND :end', { start, end })
        .orderBy('i.issueDate', 'ASC')
        .getMany();
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
 * - Crea el header y copia los datos fuente en las 6 tablas auxiliares.
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

            const [purchaseOrders, receptions, rebates, payments, invoices] =
                await Promise.all([
                    poRepo.findByVendorAndDateRange(vendorNumber, periodStart, periodEnd),
                    recRepo.findByVendorAndDateRange(vendorNumber, periodStart, periodEnd),
                    rebateRepo.findByVendorAndPostingDateRange(vendorNumber, periodStart, periodEnd),
                    fpRepo.findByVendorAndPaymentDateRange(vendorNumber, periodStart, periodEnd),
                    findInvoicesByVendorAndPeriod(vendorNumber, periodStart, periodEnd),
                ]);

            const facturas    = invoices.filter(i => i.documentType === 'I');
            const notasCredito = invoices.filter(i => i.documentType === 'E');

            await Promise.all([
                rawInsert(T_PO,       purchaseOrders.map(po  => mapPurchaseOrder(po,  statementUuid))),
                rawInsert(T_REC,      receptions.map(rec     => mapReception(rec,     statementUuid))),
                rawInsert(T_DISCOUNT, rebates.map(rb         => mapDiscount(rb,       statementUuid))),
                rawInsert(T_PAYMENT,  payments.map(fp        => mapPayment(fp,        statementUuid))),
                rawInsert(T_INVOICE,  facturas.map(inv       => mapInvoice(inv,       statementUuid))),
                rawInsert(T_CREDIT,   notasCredito.map(inv   => mapCreditNote(inv,    statementUuid))),
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
