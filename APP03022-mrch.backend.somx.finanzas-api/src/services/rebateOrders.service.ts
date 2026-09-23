import { z } from "zod";
import { getDataSource } from "@/config/typeorm-datasource.js";
import { HttpError } from "@/utils/HttpError.js";

const decimal = z.string().regex(/^-?\d{1,24}(\.\d{1,8})?$/);
const date = z.string().regex(/^\d{4}-\d{2}-\d{2}$/).refine(value => {
    const parsed = new Date(`${value}T00:00:00Z`);
    return Number.isFinite(parsed.getTime()) && parsed.toISOString().slice(0, 10) === value;
});
export const RebateOrdersSchema = z.object({
    documentNumber: z.string().trim().min(1).max(100),
    vendorNumber: z.number().int(),
    source: z.number().int(),
    periodId: z.number().int(),
    orders: z.array(z.object({
        purchaseOrder: z.string().trim().min(1).max(100),
        receivedAmount: decimal,
        receptionDate: date,
        discountType: z.number().int(),
        discountAmount: decimal,
    }).strict()).min(1).max(10000),
}).strict().refine(value => value.orders.every(row => row.discountType === value.source), {
    message: "El tipo de descuento del detalle debe coincidir con la cabecera",
});

export async function syncRebateOrders(input: z.infer<typeof RebateOrdersSchema>) {
    return getDataSource().transaction(async manager => {
        // Identidad exacta, sin filtros de estado ni fechas ni creación de cabeceras.
        const matches: { rebateId: string }[] = await manager.query(`
            SELECT rebate_uuid AS "rebateId" FROM tenant_finance.rebate
            WHERE document_number = $1 AND vendor_number = $2
              AND source = $3 AND period_id = $4
            FOR UPDATE`, [input.documentNumber, input.vendorNumber, input.source, input.periodId]);
        if (!matches.length) throw new HttpError(404, "El descuento aún no existe en FBC");
        if (matches.length !== 1) throw new HttpError(409, "Hay varias cabeceras para la misma clave; requiere conciliación");
        const rebateId = matches[0]!.rebateId;
        await manager.query(`
            INSERT INTO tenant_finance.rebate_purchase_order_snapshot (rebate_uuid, orders)
            VALUES ($1::uuid, $2::jsonb)
            ON CONFLICT (rebate_uuid) DO UPDATE
            SET orders = EXCLUDED.orders, updated_at = CURRENT_TIMESTAMP`,
            [rebateId, JSON.stringify(input.orders)]);
        return { rebateId, count: input.orders.length };
    });
}

export async function getRebateOrders(rebateId: string) {
    const rows = await getDataSource().query(`
        SELECT r.rebate_uuid AS "rebateId", s.orders, s.updated_at AS "updatedAt"
        FROM tenant_finance.rebate r
        LEFT JOIN tenant_finance.rebate_purchase_order_snapshot s USING (rebate_uuid)
        WHERE r.rebate_uuid = $1::uuid`, [rebateId]);
    if (!rows.length) throw new HttpError(404, "No se encontró el descuento comercial");
    return {
        rebateId: rows[0].rebateId,
        orders: rows[0].orders ?? [],
        updatedAt: rows[0].updatedAt,
        message: rows[0].orders == null ? "El detalle de órdenes aún no está disponible." : null,
    };
}
