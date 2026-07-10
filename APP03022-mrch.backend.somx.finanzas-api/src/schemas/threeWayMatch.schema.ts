import { z } from "zod";

export const ListThreeWayMatchQuerySchema = z.object({
    tipoFecha: z.enum([
        "fechaRecepcion",
        "fechaTimbrado",
        "fechaOrdenCompra",
        "fechaPago",
    ]),

    fechaInicio: z.coerce.date(),
    fechaFin: z.coerce.date(),

    numeroProveedor: z.string().optional(),

    // Filtro por tipo de proveedor
    tipoProveedor: z.coerce
        .number()
        .int()
        .positive()
        .optional(),

    ordenCompra: z.string().optional(),
    recepcion: z.string().optional(),

    page: z.coerce
        .number()
        .int()
        .min(1)
        .optional(),

    limit: z.coerce
        .number()
        .int()
        .min(1)
        .max(1000)
        .optional(),
});

export type ListThreeWayMatchQuery = z.infer<
    typeof ListThreeWayMatchQuerySchema
>;