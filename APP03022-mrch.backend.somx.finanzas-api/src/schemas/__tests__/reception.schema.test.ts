import { CreateReceiptionSchema } from "../reception.schema.js";

describe("CreateReceiptionSchema receptionTypeId", () => {
    const sku = {
        sku: "V01",
        description: "SKU",
        quantity: 1,
        unitCost: "10.00",
        totalCost: "10.00",
        status: 0,
        createdBy: 1,
    };
    const base = {
        receptionNumber: "REC-1",
        destinationId: 1,
        amount: "10.00",
        comments: "ok",
        receptionDate: "2026-01-15",
        status: 0,
        createdBy: 1,
        receiptSkuList: [sku],
    };

    it("acepta receptionTypeId numérico y string", () => {
        expect(CreateReceiptionSchema.parse({ ...base, receptionTypeId: 2 }).receptionTypeId).toBe(2);
        expect(CreateReceiptionSchema.parse({ ...base, receptionTypeId: "3" }).receptionTypeId).toBe(3);
    });

    it("acepta alias tipoRecepcion", () => {
        expect(CreateReceiptionSchema.parse({ ...base, tipoRecepcion: "4" }).receptionTypeId).toBe(4);
    });
});
