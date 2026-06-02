import { z } from "zod/v4";
import {
    CreateReceiptionSchema,
} from "@/schemas/reception.schema.js";

import {
    CreateShippingGuideSchemaList,
    CreateShippingGuideSchema
} from "@/schemas/shippingGuide.schema.js";

/** Query GET: los parámetros vienen como string; coercion explícita (como las fechas). */
const optionalQueryInt = (): z.ZodType<number | undefined> =>
    z.preprocess(
        (val) =>
            val === "" || val === null || typeof val === "undefined" ? undefined : val,
        z.coerce.number().int().optional()
    );

export const ListPurchaseOrderQuerySchema = z.object({
    purchaseOrderDateAtInitial: z.coerce.date(),
    purchaseOrderDateAtEnd: z.coerce.date(),
    purchaseOrderId: z.string().optional(),
    orderNumber: z.string().optional(),
    originId: optionalQueryInt(),
    supplierNumber: optionalQueryInt(),
    status: optionalQueryInt(),
    pageNumber: z.string(),
    pageSize: z.string()
});

/** Para actualizar (PATCH) — incluimos updatedBy */
export const UpdateStatusReceptionSchema = z.object({
    supplierNumber: z.number().int().min(1,{ message: "Invalid field `supplierNumber` on PurchaseOrder. value : NotAllowed"}),
    orderNumber: z.string().nonempty({message: "Invalid field `orderNumber` on PurchaseOrder. value cannot be empty, null or blank"}),
    receptionNumber: z.string().nonempty({message: "Invalid field `receptionNumber` on PurchaseOrder. value cannot be empty, null or blank"}),
    status: z.number().int().min(1,{message: "Invalid field `status` on Reception. value : 0"}),
    uuid: z.uuid().optional(),
    comments: z.string()
}).strict();



// Actualizar estado de recepción
export const UpdateStatusReceptionSchemaByUuid = z.object({
    comment: z.string().nonempty({message: "Invalid field `comment` on Reception. value cannot be empty, null or blank"}),
    status: z.number().int().min(1,{message: "Invalid field `status` on Reception. value : 0"})
}).strict();

export const GuideNumberSchema = z.object({
guide: z.string()
});

/** Para crear */
export const CreatePurchaseOrderSchema = CreateShippingGuideSchemaList.extend({
    supplierNumber: z.number().int().min(1,{ message: "Invalid field `supplierNumber` on v. value : null or empty" }),
    orderNumber: z.string().nonempty({message: "Invalid field `orderNumber` on PurchaseOrder. value cannot be empty, null or blank"}),
    //originId: z.string().nonempty({message: "Invalid field `originId` on PurchaseOrder. value cannot be empty, null or blank"}),
    amount: z.string().regex(/^\d+(\.\d{1,2})?$/, "decimal with 2 places"),
    purchaseOrderDate: z.coerce.date().nonoptional({message: "Invalid field `purchaseOrderDate` on PurchaseOrder. value cannot be empty, null or blank"}),
    status: z.number().int().min(0,{message: "Invalid field `status` on PurchaseOrder. value : 0"}),
    createdBy: z.number().int().min(1,{message: "Invalid field `createdBy` on PurchaseOrder. value : 0"}),
    receptionList: z.array(CreateReceiptionSchema).min(1),
    guideNumber: z.array(GuideNumberSchema).optional(),  //Aplica para Carta Porte
    shippingGuideList: z.array(CreateShippingGuideSchema).optional(), //Aplica para Carta Porte
    origen: z.string()

}).superRefine((data, ctx) => {
    const orderAmountTotal = parseFloat(data.amount);

    let receptionList = data.receptionList;
    let sumAmountReceptionList = 0.00;
    receptionList.forEach(rec => {
    sumAmountReceptionList += parseFloat(rec.amount);
    });


    if (orderAmountTotal < sumAmountReceptionList) {

        ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "El total de la orden de compra no puede ser menor que la suma de las recepciones",
        path: ["PurchaesOrder.amount < Sum(receptionList[i].amount)"],
        });
    }

    for (let i = 0; i < receptionList.length; i++) {
        let reception = receptionList.at(i);
        let receptionSkuList = reception?.receiptSkuList;

        let sumAmountReceptionSkuList = 0.00;
        if(receptionSkuList != undefined) {
                receptionSkuList.forEach(recSku => {
                sumAmountReceptionSkuList += parseFloat(recSku.totalCost);
            });

            if (parseFloat(reception!.amount) != sumAmountReceptionSkuList) {
            ctx.addIssue({
                code: z.ZodIssueCode.custom,
                message: "El total de la recepción debe ser igual que la suma del valor de los articulos",
                path: ["receptionList[" + i + "].amount < Sum(receptionSkuList[i].totalCost)"],
            });
        }

        }

    }

});

export const CreatePurchaseOrderSchemaList = z.object({
  data: z.array(CreatePurchaseOrderSchema)
});





export type ListPurchaseOrderQueryDto = z.infer<typeof ListPurchaseOrderQuerySchema>;
export type CreatePurchaseOrderDto = z.infer<typeof CreatePurchaseOrderSchema>;
export type UpdatePurchaseOrderDto = z.infer<typeof UpdateStatusReceptionSchema>;
export type GuideNumberDto = z.infer<typeof GuideNumberSchema>;




