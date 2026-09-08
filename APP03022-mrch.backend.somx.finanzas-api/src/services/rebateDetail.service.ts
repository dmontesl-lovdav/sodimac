import { getDataSource } from "@/config/typeorm-datasource.js";
import { HttpError } from "@/utils/HttpError.js";

interface CreditNoteRow {
    id: string;
    uuid: string;
    registeredAt: Date | string | null;
    amount: string | null;
    series: string | null;
    folio: string | null;
}

interface RebateRelationRow {
    rebateId: string;
    vendorNumber: number | null;
    stampedRebateId: string | null;
    invoiceFiscalUuid: string | null;
    ncFiscalUuid: string | null;
}

export async function getRebateFiscalDetail(rebateId: string) {
    const db = getDataSource();

    const relations: RebateRelationRow[] = await db.query(
        `
        SELECT
            r.rebate_uuid AS "rebateId",
            r.vendor_number AS "vendorNumber",
            sr.stamped_rebate_uuid AS "stampedRebateId",
            sr.invoice_fiscal_uuid AS "invoiceFiscalUuid",
            sr.nc_fiscal_uuid AS "ncFiscalUuid"
        FROM tenant_finance.rebate r
        LEFT JOIN tenant_finance.stamped_rebate sr
            ON sr.document_number = r.document_number
        WHERE r.rebate_uuid = $1::uuid
        `,
        [rebateId]
    );

    const relation = relations[0];

    if (!relation) {
        throw new HttpError(404, "No se encontró el descuento comercial");
    }

    let creditNotes: CreditNoteRow[] = [];

    if (relation.ncFiscalUuid) {
        creditNotes = await db.query(
            `
            SELECT
                i.invoice_uuid AS "id",
                i.fiscal_uuid AS "uuid",
                i.created_at AS "registeredAt",
                i.total::text AS "amount",
                i.series AS "series",
                i.folio AS "folio"
            FROM tenant_fiscal.invoice i
            WHERE i.fiscal_uuid = $1::uuid
              AND i.document_type = 'E'
            ORDER BY i.created_at DESC, i.invoice_uuid
            `,
            [relation.ncFiscalUuid]
        );
    }

    let message: string | null = null;

    if (!relation.stampedRebateId) {
        message = "Este descuento todavía no tiene una nota de crédito relacionada.";
    } else if (!relation.ncFiscalUuid) {
        message =
            "La relación existente no tiene registrado el UUID fiscal de la nota de crédito.";
    } else if (creditNotes.length === 0) {
        message =
            "La nota de crédito relacionada no está disponible en el repositorio fiscal.";
    }

    return {
        rebateId: relation.rebateId,
        vendorNumber: relation.vendorNumber,
        invoiceFiscalUuid: relation.invoiceFiscalUuid,
        ncFiscalUuid: relation.ncFiscalUuid,
        creditNotes,
        message,
    };
}