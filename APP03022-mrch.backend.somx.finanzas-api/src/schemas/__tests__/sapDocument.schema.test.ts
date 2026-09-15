import {
    collectFiscalUuids,
    CreateSapDocumentSchema,
} from "../sapDocument.schema.js";

const fiscalUuidA = "11111111-1111-4111-8111-111111111111";
const fiscalUuidB = "22222222-2222-4222-8222-222222222222";

describe("collectFiscalUuids", () => {
    it("une fiscalUuid suelto y el arreglo sin duplicados", () => {
        expect(
            collectFiscalUuids({
                fiscalUuid: fiscalUuidA,
                fiscalUuids: [fiscalUuidA, fiscalUuidB],
            })
        ).toEqual([fiscalUuidA, fiscalUuidB]);
    });

    it("devuelve vacío cuando no hay UUIDs", () => {
        expect(collectFiscalUuids({})).toEqual([]);
    });
});

describe("CreateSapDocumentSchema", () => {
    const base = {
        documentNumber: "4500001234",
        referenceNumber: "REF-1",
        vendorNumber: 1001,
        amount: "10.50",
        source: 1,
        docSap: "5100000123",
        documentType: "KR",
    };

    it("exige al menos un UUID fiscal", () => {
        const parsed = CreateSapDocumentSchema.safeParse(base);
        expect(parsed.success).toBe(false);
    });

    it("acepta fiscalUuid único", () => {
        const parsed = CreateSapDocumentSchema.parse({
            ...base,
            fiscalUuid: fiscalUuidA,
        });
        expect(collectFiscalUuids(parsed)).toEqual([fiscalUuidA]);
        expect(parsed.sapStatus).toBe(1);
    });
});
