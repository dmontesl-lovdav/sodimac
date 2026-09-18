import { catalogAcceptsReceptionTypeId, catalogDetailsToIdLabelMap, toNumericReceptionTypeId } from "../receptionTypeCatalog.js";

describe("catalogAcceptsReceptionTypeId", () => {
    it("prioriza value, luego internal y al final id", () => {
        const catalog = [
            { value: "1", internalStatus: 9, key: "TRE001" },
            { value: "X", internalStatus: 2, key: "TRE002" },
        ] as any;
        expect(catalogAcceptsReceptionTypeId(catalog, 1)).toBe(true);
        expect(catalogAcceptsReceptionTypeId(catalog, 2)).toBe(true);
        expect(catalogAcceptsReceptionTypeId(catalog, 9)).toBe(true);
        expect(catalogAcceptsReceptionTypeId(catalog, 8)).toBe(false);
    });
});

describe("toNumericReceptionTypeId", () => {
    it("convierte a número entero", () => {
        expect(toNumericReceptionTypeId("2")).toBe(2);
        expect(toNumericReceptionTypeId(3)).toBe(3);
        expect(toNumericReceptionTypeId("")).toBeUndefined();
    });
});

describe("catalogDetailsToIdLabelMap", () => {
    it("resuelve el nombre por value, internalStatus e id", () => {
        const map = catalogDetailsToIdLabelMap([
            { value: "1", internalStatus: 9, description: "Mercancía", key: "TRE001", id: 50 } as any,
        ]);
        expect(map.get(1)).toBe("Mercancía");
        expect(map.get(9)).toBe("Mercancía");
        expect(map.get(50)).toBe("Mercancía");
    });
});
