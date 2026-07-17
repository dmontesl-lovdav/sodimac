import { describe, it, expect } from "@jest/globals";
import { normalizeRebateRow } from "../normalizeRebate";

describe("normalizeRebateRow", () => {
  it("mapea aliases de referencia y proveedor", () => {
    const row = normalizeRebateRow({
      referenceNumber: "REF-1",
      vendorNumber: 100,
      amount: 10,
    });
    expect(row.documentReference).toBe("REF-1");
    expect(row.supplierNumber).toBe(100);
    expect(row.vendorNumber).toBe(100);
  });

  it("usa originId cuando no hay source", () => {
    const row = normalizeRebateRow({ originId: 7 });
    expect(row.source).toBe(7);
  });

  it("usa source cuando está presente", () => {
    const row = normalizeRebateRow({ source: 3, originId: 9 });
    expect(row.source).toBe(3);
  });

  it("source indefinido si no es finito", () => {
    const row = normalizeRebateRow({ source: "x" });
    expect(row.source).toBeUndefined();
  });

  it("stampedRebate null por defecto", () => {
    const row = normalizeRebateRow({});
    expect(row.stampedRebate).toBeNull();
  });
});
