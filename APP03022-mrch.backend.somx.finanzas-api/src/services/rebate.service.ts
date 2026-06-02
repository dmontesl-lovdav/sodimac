import * as r from "@/repositories/rebate.repo.js";
import type {
    ListRebateQuery,
    CreateRebateDto,
    UpdateRebateDto,
    RebateFilterDto,
} from "@/schemas/rebate.schema.js";
import type { Rebate } from "@/entities/Rebate.entity.js";
import { Between, LessThanOrEqual, MoreThanOrEqual, FindOptionsWhere, Like } from "typeorm";

export async function list(q: ListRebateQuery) {
    const filter: FindOptionsWhere<Rebate> = {};

    if (q.status !== undefined) filter.status = q.status;
    if (q.vendorNumber !== undefined) filter.vendorNumber = q.vendorNumber;
    if (q.documentNumber !== undefined) {
        filter.documentNumber = Like(`%${q.documentNumber}%`);
    }
    if (q.source !== undefined) filter.source = q.source;
    if (q.periodId !== undefined) filter.periodId = q.periodId;

    if (q.from && q.to) filter.postingDate = Between(q.from, q.to);
    else if (q.from) filter.postingDate = MoreThanOrEqual(q.from);
    else if (q.to) filter.postingDate = LessThanOrEqual(q.to);

    return r.findAll(filter, q.limit ?? 20);
}

// Obtener rebates publicados (status = 1)
export async function listPublished(q: ListRebateQuery) {
    const filter: FindOptionsWhere<Rebate> = { status: 1 };

    if (q.vendorNumber !== undefined) filter.vendorNumber = q.vendorNumber;
    if (q.documentNumber !== undefined) {
        filter.documentNumber = Like(`%${q.documentNumber}%`);
    }
    if (q.source !== undefined) filter.source = q.source;
    if (q.periodId !== undefined) filter.periodId = q.periodId;

    if (q.from && q.to) filter.postingDate = Between(q.from, q.to);
    else if (q.from) filter.postingDate = MoreThanOrEqual(q.from);
    else if (q.to) filter.postingDate = LessThanOrEqual(q.to);

    return r.findAll(filter, q.limit ?? 20);
}

// Obtener rebates por número de proveedor
export async function listByVendor(vendorNumber: number, q: ListRebateQuery) {
    const filter: FindOptionsWhere<Rebate> = {
        vendorNumber: vendorNumber,
        status: 1  // Solo publicados
    };

    if (q.documentNumber !== undefined) {
        filter.documentNumber = Like(`%${q.documentNumber}%`);
    }
    if (q.source !== undefined) filter.source = q.source;
    if (q.periodId !== undefined) filter.periodId = q.periodId;

    if (q.from && q.to) filter.postingDate = Between(q.from, q.to);
    else if (q.from) filter.postingDate = MoreThanOrEqual(q.from);
    else if (q.to) filter.postingDate = LessThanOrEqual(q.to);

    return r.findAll(filter, q.limit ?? 20);
}

// Búsqueda con filtros dinámicos
export async function searchWithFilters(filters: RebateFilterDto) {
    return r.findWithDynamicFilters(filters);
}

// Generar reporte CSV de rebates publicados
export async function generatePublishedRebatesCsvReport(): Promise<string> {
    const rebates = await r.findAllPublished();
    return generateCsvFromRebates(rebates);
}

// Generar reporte CSV de rebates filtrados
export async function generateFilteredRebatesCsvReport(filters: RebateFilterDto): Promise<string> {
    const rebates = await r.findWithDynamicFilters(filters);
    return generateCsvFromRebates(rebates);
}

// Helper para generar CSV
function generateCsvFromRebates(rebates: Rebate[]): string {
    const headers = [
        'UUID',
        'Document Number',
        'Reference Number',
        'SAP Document',
        'Vendor Number',
        'Amount',
        'Source',
        'Period ID',
        'Due Date',
        'Posting Date',
        'Status',
        'Stamped Rebate UUID',
        'Stamped Document Number',
        'Stamped Reference',
        'Stamped Status'
    ];

    const rows = rebates.map(r => [
        r.rebateId,
        r.documentNumber,
        r.referenceNumber || '',
        r.sapDocument || '',
        r.vendorNumber || '',
        r.amount || '',
        r.source || '',
        r.periodId || '',
        r.dueDate?.toISOString().split('T')[0] || '',
        r.postingDate?.toISOString().split('T')[0] || '',
        r.status || '',
        r.stampedRebate?.stampedRebateUuid || '',
        r.stampedRebate?.documentNumber || '',
        r.stampedRebate?.referenceNumber || '',
        r.stampedRebate?.status || ''
    ]);

    const csvLines = [
        headers.join(','),
        ...rows.map(row => row.map(cell => `"${cell}"`).join(','))
    ];

    return csvLines.join('\n');
}

export async function get(id: string) {
    return r.findById(id);
}

export async function getByDocumentNumber(documentNumber: string) {
    return r.findByDocumentNumber(documentNumber);
}

export async function create(dto: CreateRebateDto) {
    const data = {
        ...dto,
        createdAt: new Date(),
        updatedAt: new Date(),
    };

    return r.createOne(data as any);
}

export async function update(id: string, dto: UpdateRebateDto) {
    const patch = {
        ...dto,
        updatedAt: new Date(),
    };

    return r.updateOne(id, patch as any);
}

export async function remove(id: string) {
    await r.deleteOne(id);
}
