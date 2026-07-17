import { describe, it, expect, jest, beforeEach } from "@jest/globals";

const mockRequest = jest.fn();

jest.mock("@/services/ApiClient", () => ({
  createApiClient: () => ({
    request: (...args: unknown[]) => mockRequest(...args),
    requestBinary: jest.fn(),
  }),
}));

import { DiscountsClient } from "../DiscountsClient";

describe("DiscountsClient", () => {
  beforeEach(() => {
    mockRequest.mockReset();
  });

  it("arma query y normaliza filas", async () => {
    mockRequest.mockResolvedValue([
      { documentNumber: "D1", vendorNumber: 1, amount: 5 },
    ]);

    const rows = await DiscountsClient.get({
      from: "2026-01-01",
      to: "2026-01-31",
      pageNumber: 2,
      pageSize: 20,
      status: 1,
      supplierNumber: 10,
      supplierType: 2,
      documentNumber: " DOC ",
      sapDocument: "SAP",
      source: 3,
    });

    expect(mockRequest).toHaveBeenCalled();
    const path = String(mockRequest.mock.calls[0][0]);
    expect(path).toContain("rebates?");
    expect(path).toContain("status=1");
    expect(path).toContain("page=1");
    expect(rows).toHaveLength(1);
    expect(rows[0].documentNumber).toBe("D1");
  });

  it("retorna [] si la respuesta no es array", async () => {
    mockRequest.mockResolvedValue({ foo: 1 });
    await expect(
      DiscountsClient.get({
        from: "2026-01-01",
        to: "2026-01-31",
        pageNumber: 1,
        pageSize: 10,
      })
    ).resolves.toEqual([]);
  });

  it("retorna [] en 404", async () => {
    mockRequest.mockRejectedValue({ response: { status: 404 } });
    await expect(
      DiscountsClient.get({
        from: "2026-01-01",
        to: "2026-01-31",
        pageNumber: 1,
        pageSize: 10,
      })
    ).resolves.toEqual([]);
  });

  it("relanza errores distintos de 404", async () => {
    mockRequest.mockRejectedValue({ response: { status: 500 } });
    await expect(
      DiscountsClient.get({
        from: "2026-01-01",
        to: "2026-01-31",
        pageNumber: 1,
        pageSize: 10,
      })
    ).rejects.toEqual({ response: { status: 500 } });
  });
});
