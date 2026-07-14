import * as r from '@/repositories/accountStatement.repo.js';
import type { ListAccountStatementQuery } from '@/schemas/accountStatement.schema.js';
import { generatePdfBuffer } from './accountStatementPdf.service.js';
import { buildAccountStatementReportData } from './accountStatementReportData.service.js';

const STATUS_GENERATED = 1;
const STATUS_PUBLISHED = 2;
const STATUS_REVIEWED = 3;
const STATUS_REJECTED = 4;
const STATUS_REPROCESSED = 5;

function statusToLabel(status: number): string {
    switch (status) {
        case STATUS_GENERATED: return 'Generado';
        case STATUS_PUBLISHED: return 'Publicado';
        case STATUS_REVIEWED: return 'Revisado';
        case STATUS_REJECTED: return 'Rechazado';
        case STATUS_REPROCESSED: return 'Reprocesado';
        default: return String(status);
    }
}


export async function search(query: ListAccountStatementQuery, allowedVendors: string[] | null = null) {
    const month = query.month === 'all' ? undefined : query.month;
    const page = query.page ?? 1;
    const pageSize = query.pageSize ?? 10;
    const { rows, total } = await r.findByFilters({
        vendorNumber: query.vendorNumber,
        supplierType: query.supplierType,
        year: query.year,
        month: month ?? 'all',
        allowedVendors,
        limit: pageSize,
        offset: (page - 1) * pageSize,
    });
    const totalPages = Math.max(1, Math.ceil(total / pageSize));
    const items = rows.map(({ statement: row, vendorName, supplierTypeId, supplierTypeCode, supplierTypeDescription }) => ({
        accountStatementUuid: row.accountStatementUuid,
        vendorNumber: Number(row.vendorNumber),
        vendorName,
        supplierType: {
            id: supplierTypeId ?? 0,
            code: supplierTypeCode ?? '',
            description: supplierTypeDescription ?? '',
        },
        year: row.year,
        month: row.month,
        status: row.status,
        statusLabel: statusToLabel(row.status),
        processedAt: row.processedAt,
        reviewedAt: row.reviewedAt,
    }));
    return { items, totalItems: total, totalPages, currentPage: page };
}

export async function getById(uuid: string, allowedVendors: string[] | null = null) {
    const row = await r.findById(uuid, allowedVendors);
    if (!row) return null;
    return {
        accountStatementUuid: row.accountStatementUuid,
        vendorNumber: Number(row.vendorNumber),
        vendorName: '',
        year: row.year,
        month: row.month,
        version: row.version,
        status: row.status,
        statusLabel: statusToLabel(row.status),
        initialBalance: row.initialBalance,
        finalBalance: row.finalBalance,
        processedAt: row.processedAt,
        reviewedAt: row.reviewedAt,
        issuedAt: row.issuedAt,
        periodStart: row.periodStart,
    };
}

export async function confirmReview(uuid: string) {
    return r.updateReviewStatus(uuid, STATUS_REVIEWED, new Date());
}

export async function requestReview(uuid: string) {
    return r.updateReviewStatus(uuid, STATUS_REJECTED, new Date());
}

export async function getPdf(uuid: string): Promise<Buffer | null> {
    const row = await r.findById(uuid);
    if (!row) return null;
    return generatePdfBuffer(uuid);
}

export async function getReportData(uuid: string, allowedVendors: string[] | null = null) {
    const row = await r.findById(uuid, allowedVendors);
    if (!row) return null;
    return buildAccountStatementReportData(uuid);
}
