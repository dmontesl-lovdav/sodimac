/**
 * @jest-environment node
 */
import { catalogDetailsToReceptionTypeOptions, filterByReceptionType } from "../receptionTypeQuery";
import { buildPurchaseOrdersQuery } from "../purchaseOrderListQuery";

describe("filterByReceptionType", () => {
  it("filtra por IdTipoRecepcion", () => {
    const rows = [
      { receptionTypeId: 1 },
      { receptionTypeId: "2" },
      { receptionTypeId: 1 },
    ];
    expect(filterByReceptionType(rows, 1)).toHaveLength(2);
    expect(filterByReceptionType(rows, "2")).toHaveLength(1);
  });

  it("no filtra cuando el criterio está vacío", () => {
    const rows = [{ receptionTypeId: 1 }];
    expect(filterByReceptionType(rows, "")).toEqual(rows);
    expect(filterByReceptionType(rows, " ")).toEqual(rows);
  });
});

describe("catalogDetailsToReceptionTypeOptions", () => {
  it("prioriza value, luego internal y al final id", () => {
    const options = catalogDetailsToReceptionTypeOptions({
      details: [
        { description: "Mercancía", value: "1", internalStatus: 99, id: 100 },
        { description: "Transporte", value: "", internal_status: 2, id: 200 },
        { description: "Servicios", id: 4 },
      ],
    });
    expect(options).toEqual([
      { label: "Todos los tipos", value: "" },
      { label: "Mercancía", value: "1" },
      { label: "Transporte", value: "2" },
      { label: "Servicios", value: "4" },
    ]);
  });
});

describe("buildPurchaseOrdersQuery", () => {
  it("incluye receptionTypeId en la query", () => {
    const qs = buildPurchaseOrdersQuery({
      purchaseOrderDateAtInitial: "2026-01-01T00:00:00.000Z",
      purchaseOrderDateAtEnd: "2026-01-31T23:59:59.999Z",
      pageNumber: 1,
      pageSize: 10,
      receptionTypeId: "1",
    });
    expect(qs.get("receptionTypeId")).toBe("1");
  });

  it("omite receptionTypeId cuando el filtro es Todos", () => {
    const qs = buildPurchaseOrdersQuery({
      purchaseOrderDateAtInitial: "2026-01-01T00:00:00.000Z",
      purchaseOrderDateAtEnd: "2026-01-31T23:59:59.999Z",
      pageNumber: 1,
      pageSize: 10,
      receptionTypeId: " ",
    });
    expect(qs.get("receptionTypeId")).toBeNull();
  });
});
