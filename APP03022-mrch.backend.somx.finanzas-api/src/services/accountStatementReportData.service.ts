import { getDataSource } from '@/config/typeorm-datasource.js';
import * as r from '@/repositories/accountStatement.repo.js';
import * as poRepo from '@/repositories/purchaseOrder.repo.js';
import * as recRepo from '@/repositories/reception.repo.js';
import * as rebateRepo from '@/repositories/rebate.repo.js';
import * as fpRepo from '@/repositories/fiscalPayment.repo.js';
import { Invoice } from '@/entities/tenant_fiscal.invoice.entity.js';
import { Addendum } from '@/entities/tenant_fiscal.addendum.entity.js';
import type { PurchaseOrder } from '@/entities/PurchaseOrder.entity.js';
import type { Reception } from '@/entities/Reception.entity.js';
import type { Rebate } from '@/entities/Rebate.entity.js';
import type { FiscalPayment } from '@/entities/FiscalPayment.entity.js';
import { Catalogs } from '@/utils/mockups.js';

const ISSUER = {
    name: 'Comercializadora SDMHC S.A. de C.V. (SODIMAC MÉXICO)',
    address:
        'Av. Adolfo López Mateos No. 201, Col. Santa Cruz Acatlán, CP 53150, Naucalpan de Juárez, Estado de México.',
    rfc: 'CSD161207R2A',
    phone: '800 0625 222',
    email: 'analista@sodimac.com.mx',
};

function parseNum(v: string | number | null | undefined): number {
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

function serializeEntity<T extends object>(entity: T): Record<string, unknown> {
    return JSON.parse(JSON.stringify(entity)) as Record<string, unknown>;
}

async function findInvoicesByVendorAndPeriod(
    vendorNumber: number,
    start: Date,
    end: Date
) {
    return getDataSource()
        .getRepository(Invoice)
        .createQueryBuilder('i')
        .innerJoin(Addendum, 'a', 'a.invoiceUuid = i.invoiceUuid')
        .where('a.supplierNumber = :vendor', { vendor: vendorNumber })
        .andWhere('i.issueDate BETWEEN :start AND :end', { start, end })
        .orderBy('i.issueDate', 'ASC')
        .getMany();
}

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
    receptions: Record<string, unknown>[];
    payments: Record<string, unknown>[];
    rebates: Record<string, unknown>[];
    facturas: Record<string, unknown>[];
    notasCredito: Record<string, unknown>[];
    catalogs: {
        invoiceStatus: typeof Catalogs.CatEstatusFactura;
        paymentStatus: typeof Catalogs.CatEstatusPago;
        creditNoteStatus: typeof Catalogs.CatEstatusNotaCredito;
    };
};

/**
 * Arma el JSON completo del estado de cuenta (misma fuente de datos que el PDF legacy).
 */
export async function buildAccountStatementReportData(
    uuid: string
): Promise<AccountStatementReportPayload | null> {
    const row = await r.findById(uuid);
    if (!row) return null;

    const vendorNumber = Number(row.vendorNumber);
    const startDate = row.periodStart
        ? new Date(row.periodStart)
        : new Date(row.year, row.month - 1, 1);
    const endDate = row.periodEnd
        ? new Date(row.periodEnd)
        : new Date(row.year, row.month, 0, 23, 59, 59);

    const [purchaseOrders, receptions, rebates, payments, invoices] =
        await Promise.all([
            poRepo.findByVendorAndDateRange(vendorNumber, startDate, endDate),
            recRepo.findByVendorAndDateRange(vendorNumber, startDate, endDate),
            rebateRepo.findByVendorAndPostingDateRange(
                vendorNumber,
                startDate,
                endDate
            ),
            fpRepo.findByVendorAndPaymentDateRange(
                vendorNumber,
                startDate,
                endDate
            ),
            findInvoicesByVendorAndPeriod(vendorNumber, startDate, endDate),
        ]);

    const facturas = invoices.filter((inv: Invoice) => inv.documentType === 'I');
    const notasCredito = invoices.filter(
        (inv: Invoice) => inv.documentType === 'E'
    );

    const totalOC = purchaseOrders.reduce(
        (s: number, po: PurchaseOrder) => s + (Number(po.amount) || 0),
        0
    );
    const totalFacturasPendientes = facturas.reduce(
        (s: number, i: Invoice) => s + (Number(i.total) || 0),
        0
    );
    const totalFacturasPagadas = payments.reduce(
        (s: number, p: FiscalPayment) => s + parseNum(p.amount),
        0
    );
    const totalDescuentos = rebates.reduce(
        (s: number, b: Rebate) => s + (Number(b.amount) || 0),
        0
    );
    const totalNotasCredito = notasCredito.reduce(
        (s: number, i: Invoice) => s + (Number(i.total) || 0),
        0
    );
    const saldoPendiente = parseNum(row.finalBalance);

    return {
        meta: {
            accountStatementUuid: row.accountStatementUuid,
            vendorNumber,
            year: row.year,
            month: row.month,
            version: row.version,
            initialBalance:
                row.initialBalance != null
                    ? parseNum(row.initialBalance)
                    : null,
            finalBalance:
                row.finalBalance != null ? parseNum(row.finalBalance) : null,
        },
        issuer: ISSUER,
        vendor: {
            vendorNumber,
            vendorName: `Proveedor ${vendorNumber}`,
            providerRfc: 'N/A',
            providerCp: '',
            providerAddress: '',
            providerContact: '',
            providerEmail: '',
        },
        dates: {
            issueDate: toIso(row.issuedAt),
            periodStart: toIso(startDate),
            periodEnd: toIso(endDate),
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
                facturas: facturas.length,
                payments: payments.length,
                rebates: rebates.length,
                notasCredito: notasCredito.length,
            },
        },
        purchaseOrders: purchaseOrders.map(serializeEntity),
        receptions: receptions.map(serializeEntity),
        payments: payments.map(serializeEntity),
        rebates: rebates.map(serializeEntity),
        facturas: facturas.map(serializeEntity),
        notasCredito: notasCredito.map(serializeEntity),
        catalogs: {
            invoiceStatus: Catalogs.CatEstatusFactura,
            paymentStatus: Catalogs.CatEstatusPago,
            creditNoteStatus: Catalogs.CatEstatusNotaCredito,
        },
    };
}
