import type { SapDocument } from "@/entities/SapDocument.entity.js";

export type SapDocumentResponse = {
    id: string;
    sapDocumentUuid: string;
    documentNumber: string;
    referenceNumber: string;
    vendorNumber: number;
    amount: number;
    source: number;
    docSap: string;
    message: string | null;
    sapStatus: number;
    documentType: string;
    fiscalUuids: string[];
    createdBy: number | null;
    createdAt: Date;
    updatedBy: number | null;
    updatedAt: Date | null;
};

export function toSapDocumentResponse(row: SapDocument): SapDocumentResponse {
    return {
        id: row.sapDocumentUuid,
        sapDocumentUuid: row.sapDocumentUuid,
        documentNumber: row.documentNumber,
        referenceNumber: row.referenceNumber,
        vendorNumber: Number(row.vendorNumber),
        amount: Number(row.amount),
        source: Number(row.source),
        docSap: row.docSap,
        message: row.message ?? null,
        sapStatus: Number(row.sapStatus),
        documentType: row.documentType,
        fiscalUuids: (row.fiscalUuids ?? []).map((item) => item.fiscalUuid),
        createdBy: row.createdBy == null ? null : Number(row.createdBy),
        createdAt: row.createdAt,
        updatedBy: row.updatedBy == null ? null : Number(row.updatedBy),
        updatedAt: row.updatedAt ?? null,
    };
}
