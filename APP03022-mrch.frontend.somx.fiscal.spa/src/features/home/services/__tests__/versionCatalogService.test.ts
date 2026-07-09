import { jest, describe, it, expect, beforeEach } from "@jest/globals";

jest.mock("@/services/ApiClient", () => ({
  createApiClient: () => ({ request: jest.fn() }),
}), { virtual: true });

import { versionCatalogService, type VersionCatalogDto } from "../versionCatalogService";

const SAMPLE: VersionCatalogDto[] = [
  {
    versionId: 1,
    name: "CFDI 4.0",
    description: "Versión 4.0",
    version: "4.0",
    documentType: "I",
    pacId: null,
    validFrom: "2024-01-01",
    validTo: null,
    structureUrl: null,
    status: 1,
  },
];

describe("versionCatalogService.checkConnection", () => {
  beforeEach(() => {
    jest.restoreAllMocks();
  });

  it("retorna online:true cuando el API responde correctamente", async () => {
    jest.spyOn(versionCatalogService, "getVersionCatalog").mockResolvedValue(SAMPLE as any);
    const result = await versionCatalogService.checkConnection();
    expect(result.online).toBe(true);
    expect(result.message).toContain("activa");
    expect(result.count).toBe(1);
  });

  it("incluye el conteo de registros en el mensaje", async () => {
    const two = [...SAMPLE, { ...SAMPLE[0], versionId: 2 }];
    jest.spyOn(versionCatalogService, "getVersionCatalog").mockResolvedValue(two as any);
    const result = await versionCatalogService.checkConnection();
    expect(result.count).toBe(2);
    expect(result.message).toContain("2");
  });

  it("retorna count:0 cuando el array está vacío", async () => {
    jest.spyOn(versionCatalogService, "getVersionCatalog").mockResolvedValue([] as any);
    const result = await versionCatalogService.checkConnection();
    expect(result.online).toBe(true);
    expect(result.count).toBe(0);
  });

  it("retorna online:false cuando el API lanza error con message", async () => {
    jest.spyOn(versionCatalogService, "getVersionCatalog").mockRejectedValue({ message: "Network error" });
    const result = await versionCatalogService.checkConnection();
    expect(result.online).toBe(false);
    expect(result.message).toContain("Network error");
  });

  it("retorna mensaje genérico cuando el error no tiene message", async () => {
    jest.spyOn(versionCatalogService, "getVersionCatalog").mockRejectedValue({});
    const result = await versionCatalogService.checkConnection();
    expect(result.online).toBe(false);
    expect(result.message).toContain("conexión");
  });
});
