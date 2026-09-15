import { toSapDocumentResponse } from "../sapDocument.mapper.js";
import type { SapDocument } from "@/entities/SapDocument.entity.js";

describe("toSapDocumentResponse", () => {
    it("expone sapDocumentUuid e id y lista los UUID fiscales", () => {
        const uuid = "33333333-3333-4333-8333-333333333333";
        const fiscal = "44444444-4444-4444-8444-444444444444";
        const dto = toSapDocumentResponse({
            sapDocumentUuid: uuid,
            documentNumber: "DOC",
            referenceNumber: "REF",
            vendorNumber: 10,
            amount: 12.5 as unknown as number,
            source: 1,
            docSap: "SAP1",
            message: "ok",
            sapStatus: 1,
            documentType: "KR",
            createdBy: 7,
            createdAt: new Date("2026-09-01T00:00:00.000Z"),
            updatedBy: null,
            updatedAt: null,
            fiscalUuids: [
                {
                    id: "55555555-5555-4555-8555-555555555555",
                    sapDocumentUuid: uuid,
                    fiscalUuid: fiscal,
                    createdAt: new Date("2026-09-01T00:00:00.000Z"),
                },
            ],
        } as SapDocument);

        expect(dto.id).toBe(uuid);
        expect(dto.sapDocumentUuid).toBe(uuid);
        expect(dto.fiscalUuids).toEqual([fiscal]);
        expect(dto.docSap).toBe("SAP1");
    });
});
