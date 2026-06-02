import { uuid, z } from "zod/v4";



export const UUID = z.uuid();

const multerFile = z.object({
    fieldname: z.string(),
    originalname: z.string(),
    encoding: z.string(),
    mimetype: z.string(),
    destination: z.string().optional(),
    filename: z.string().optional(),
    path: z.string().optional(),
    size: z.number()
});
const ACCEPTED_IMAGE_TYPES = ["text/csv", "application/xml", "text/xml"];

export const CreateShipingGuideDocumentSchema = z.object({
    fileName: z.string().min(1).max(100),
    fileType: z.number().int(),
    status: z.number().int().default(1)
});

export const CreateShippingGuideSchemaParent = z.object({
    content: z.string(),
    files: z.array(multerFile).length(2, { message: "La guía carta porte no contiene un archivo XML y CSV asociado, favor de validar." }).max(2).refine((files) => {
        return files.every((file) => ACCEPTED_IMAGE_TYPES.includes(file.mimetype));
    })
});


export const CreateShippingGuideSchema = z.object({
    guideNumber: z.string().min(1).max(50),
    vendorNumber: z.number().int(),
    truckPlate: z.string().max(20),
    trailerPlate: z.string().max(20).optional().nullable(),
    driverName: z.string().max(100).optional().nullable(),
    driverLicense: z.string().max(50).optional().nullable(),
    originId: z.number().int(),
    destinationId: z.number().int(),
    deliveryType: z.number().int(),
    status: z.number().int().default(1),
    comments: z.string().optional().nullable(),
    deliveryDate: z.coerce.date(),
    shippingDate: z.coerce.date().optional().nullable(),
    estimatedArrival: z.coerce.date().optional().nullable(),
    actualArrival: z.coerce.date().optional().nullable(),
    sentAt: z.coerce.date().optional().nullable(),
    createdBy: z.number().int().optional().nullable(),
    shipingGuideDocumentList: z.array(CreateShipingGuideDocumentSchema), // Make the list optional
    // .refine((list) => {
    //   // If the list is defined, ensure its length is 0 or at least 2
    //   if (list !== undefined) {
    //     return list.length === 0 || list.length >= 2;
    //   }
    //   return true; // If undefined, it's valid as it's optional
    // }, {
    //   message: "List must be empty or contain at least 2 items if provided.",
    // }),
});

export const CreateShippingGuideSchemaList = z.object({
  shippingGuideList: z.array(CreateShippingGuideSchema)   //Aplica para Carta Porte
});

export const UpdateShippingGuideSchema = z.object({
    guideNumber: z.string().min(1).max(50).optional(),
    vendorNumber: z.number().int().optional(),
    truckPlate: z.string().max(20).optional().nullable(),
    trailerPlate: z.string().max(20).optional().nullable(),
    driverName: z.string().max(100).optional().nullable(),
    driverLicense: z.string().max(50).optional().nullable(),
    originId: z.number().int().optional(),
    destinationId: z.number().int().optional().nullable(),
    deliveryType: z.number().int().optional(),
    status: z.number().int().optional(),
    comments: z.string().optional().nullable(),
    deliveryDate: z.coerce.date().optional(),
    estimatedArrival: z.coerce.date().optional().nullable(),
    actualArrival: z.coerce.date().optional().nullable(),
    sentAt: z.coerce.date().optional().nullable(),
    updatedBy: z.number().int().optional(),
}).strict();

export const ListShippingGuideQuerySchema = z.object({
    id: z.coerce.string().optional(),
    guideNumber: z.coerce.string().optional(),
    status: z.coerce.number().int().optional(),
    vendorNumber: z.coerce.number().int().optional(),
    originId: z.coerce.number().int().optional(),
    destinationId: z.coerce.number().int().optional(),
    deliveryType: z.coerce.number().optional(),
    isStatusUpdated: z.boolean().optional(),

    // AHORA opcionales (porque a veces solo mandas uno o ninguno)
    from: z.coerce.date().optional(),
    to: z.coerce.date().optional(),

    // Los query params llegan como string → coerce
    // y pon defaults para que no explote si no los mandas
    pageNumber: z.coerce.number().int().default(1),
    pageSize: z.coerce.number().int().default(10),
});
export const ShippingGuideSummary = z.object({
    purchaseOrderId: z.string(),
    orderNumber: z.string(),
    status: z.number().int()
});


export const ShippginGuideSummaryListSchema = z.object({
  data: z.array(z.string())   //Lista de shippingGuideId
});

export const CancelShippingGuidesSchema = z.object({
    shippingGuideIds: z.array(UUID).min(1),
    reasonId: z.coerce.number().int(),
    comment: z.string().max(254).optional().default(""),
});

// export const ListShippingGuideQuerySchema = z.object({
//     id: z.coerce.string().optional(),
//     guideNumber: z.coerce.string().optional(),
//     status: z.coerce.number().int().optional(),
//     vendorNumber: z.coerce.number().int().optional(),
//     originId: z.coerce.number().int().optional(),
//     destinationId: z.coerce.number().int().optional(),
//     deliveryType: z.coerce.number().optional(),
//     from: z.coerce.date(),
//     to: z.coerce.date(),
//     pageNumber: z.number().int(),
//     pageSize: z.number().int().optional(),
// });



export const IdParamSchema = z.object({ uuid: UUID });
export const IdParamGuideSchema = z.object({ idGuide: z.string() });

export type CreateShippingGuideDto = z.infer<typeof CreateShippingGuideSchema>;
export type UpdateShippingGuideDto = z.infer<typeof UpdateShippingGuideSchema>;
export type ListShippingGuideQuery = z.infer<typeof ListShippingGuideQuerySchema>;
export type CreateShippingGuideDtoList = z.infer<typeof CreateShippingGuideSchemaList>;
export type ShippginGuideSummaryListDto = z.infer<typeof ShippginGuideSummaryListSchema>;
export type CancelShippingGuidesDto = z.infer<typeof CancelShippingGuidesSchema>;
export type IdParamSchemaDto = z.infer<typeof IdParamSchema>;
export type IdParamGuideDto = z.infer<typeof IdParamGuideSchema>;







