import { beforeEach, describe, expect, it, jest } from "@jest/globals";

const mockRequest = jest.fn();

jest.mock("@/services/ApiClient", () => ({
  createApiClient: () => ({
    request: (...args: unknown[]) => mockRequest(...args),
    requestBinary: jest.fn(),
  }),
}));

jest.mock("@/configuration/ConfigurationBuilder", () => ({
  __esModule: true,
  localDeployment: false,
  default: { localDeployment: false },
}));

jest.mock("@/utils/utils", () => ({
  fetchCatalogDetails: jest.fn(),
  fetchProviders: jest.fn(),
}));

jest.mock("@/shared/security/currentUserKey", () => ({
  getCurrentUserKey: () => "548958bf-5538-4bdc-8e70-2011d422ecee",
}));

jest.mock("@/store/localStore", () => ({
  localHomeStore: {
    getState: () => ({
      authentication: {
        tokenDecoded: {
          sub: "548958bf-5538-4bdc-8e70-2011d422ecee",
          email: "sodfinanzas@gmail.com",
          realm_access: {
            roles: ["FBC_NATIONAL_SUPPLIER_FINANCE_USER", "offline_access"],
          },
          "vendors-taxs": [
            { name: "METAL MECANICA", taxId: "MMM031205NG4", country: "MX" },
          ],
        },
      },
    }),
  },
}));

import { fetchCatalogDetails, fetchProviders } from "@/utils/utils";
import { resetFinanzasUserSyncForTests, syncFinanzasUser } from "../finanzasUserSync";

const fetchCatalog = fetchCatalogDetails as jest.MockedFunction<typeof fetchCatalogDetails>;
const fetchProvidersMock = fetchProviders as jest.MockedFunction<typeof fetchProviders>;

function httpError(status: number, code?: string) {
  return { response: { status, data: { code, message: code ?? "error" } } };
}

describe("syncFinanzasUser", () => {
  beforeEach(() => {
    resetFinanzasUserSyncForTests();
    mockRequest.mockReset();
    fetchCatalog.mockReset();
    fetchProvidersMock.mockReset();
    fetchProvidersMock.mockResolvedValue([
      { id: 77, supplierNumber: "1001", rfc: "MMM031205NG4" },
    ]);
  });

  it("muestra WRN7038 cuando el macrorol no tiene conversión en catálogo", async () => {
    mockRequest.mockImplementation(async (path: string) => {
      if (String(path).includes("user-utility")) return { success: true };
      if (String(path).includes("user-details")) throw httpError(400, "WRN7031");
      return {};
    });
    fetchCatalog.mockImplementation(async (path: string) => {
      if (path.includes("message/WRN7038")) {
        return { key: "WRN7038", description: "sin equivalencia de macrorol" };
      }
      if (path.toUpperCase().includes("MACROROLPERFIL") || path === "CatMacroRolPerfil") {
        return {
          code: "CATMACROROLPERFIL",
          details: [
            {
              id: 1470,
              externalKey: "FBC_NATIONAL_COMMERCIAL_SUPPLIER_USER",
              value: "Proveedor Mercancía",
            },
          ],
        };
      }
      if (path.toUpperCase().includes("MACROROLROL") || path === "CatMacroRolRolUsuario") {
        return {
          code: "CATMACROROLROLUSUARIO",
          details: [
            {
              id: 1471,
              externalKey: "FBC_NATIONAL_COMMERCIAL_SUPPLIER_USER",
              value: "Proveedor - Mercancía",
            },
          ],
        };
      }
      return { details: [] };
    });

    const result = await syncFinanzasUser();
    expect(result).toEqual({
      status: "denied",
      messageKey: "WRN7038",
      deniedKind: "profile",
      message: "sin equivalencia de macrorol",
    });
    expect(mockRequest).not.toHaveBeenCalledWith(
      expect.stringContaining("/profiles"),
      "post",
      expect.anything()
    );
  });

  it("asigna perfil y rol locales con ids obtenidos de las APIs", async () => {
    mockRequest.mockImplementation(async (path: string, method: string) => {
      const p = String(path);
      if (p.includes("user-utility")) return { success: true };
      if (p.includes("user-catalog") && !p.includes("catalog-detail")) {
        return { data: { items: [{ id: 5, username: "548958bf-5538-4bdc-8e70-2011d422ecee" }] } };
      }
      if (p.includes("catalog-detail")) {
        return { data: { profile: null, roles: { items: [] } } };
      }
      if (p.includes("/users") && method === "get") {
        return { data: { assigned: [], available: [] } };
      }
      return { success: true };
    });
    fetchCatalog.mockImplementation(async (path: string) => {
      if (path.toUpperCase().includes("MACROROLPERFIL") || path === "CatMacroRolPerfil") {
        return {
          details: [
            {
              externalKey: "FBC_NATIONAL_SUPPLIER_FINANCE_USER",
              value: "Proveedor Finanzas",
            },
          ],
        };
      }
      if (path.toUpperCase().includes("MACROROLROL") || path === "CatMacroRolRolUsuario") {
        return {
          details: [
            {
              externalKey: "FBC_NATIONAL_SUPPLIER_FINANCE_USER",
              value: "Proveedor - Finanzas",
            },
          ],
        };
      }
      if (path === "CatPerfil") {
        return { details: [{ id: 10, value: "Proveedor Finanzas" }] };
      }
      if (path === "CatRol") {
        return { details: [{ id: 20, value: "Proveedor - Finanzas" }] };
      }
      return { details: [] };
    });

    const result = await syncFinanzasUser();
    expect(result.status).toBe("assigned");
    expect(mockRequest).toHaveBeenCalledWith("security/profiles/10/users", "put", { selectedIds: [5] });
    expect(mockRequest).toHaveBeenCalledWith("security/roles/20/users", "put", {
      selectedIds: [5],
    });
  });

  it("no modifica perfil ni rol si el macrorol sigue mapeando a lo mismo", async () => {
    mockRequest.mockImplementation(async (path: string) => {
      const p = String(path);
      if (p.includes("user-utility")) return { success: true };
      if (p.includes("user-catalog") && !p.includes("catalog-detail")) {
        return { data: { items: [{ id: 5, username: "548958bf-5538-4bdc-8e70-2011d422ecee" }] } };
      }
      if (p.includes("catalog-detail")) {
        return { data: { profile: { id: 10 }, roles: { items: [{ id: 20 }] } } };
      }
      return { success: true };
    });
    fetchCatalog.mockImplementation(async (path: string) => {
      if (path.toUpperCase().includes("MACROROLPERFIL") || path === "CatMacroRolPerfil") {
        return { details: [{ externalKey: "FBC_NATIONAL_SUPPLIER_FINANCE_USER", value: "Proveedor Finanzas" }] };
      }
      if (path.toUpperCase().includes("MACROROLROL") || path === "CatMacroRolRolUsuario") {
        return { details: [{ externalKey: "FBC_NATIONAL_SUPPLIER_FINANCE_USER", value: "Proveedor - Finanzas" }] };
      }
      if (path === "CatPerfil") return { details: [{ id: 10, value: "Proveedor Finanzas" }] };
      if (path === "CatRol") return { details: [{ id: 20, value: "Proveedor - Finanzas" }] };
      return { details: [] };
    });

    const result = await syncFinanzasUser();
    expect(result.status).toBe("configured");
    expect(mockRequest).not.toHaveBeenCalledWith(
      expect.stringContaining("/users"),
      "put",
      expect.anything()
    );
  });

  it("elimina el perfil/rol anterior y asigna el nuevo si cambió el macrorol", async () => {
    mockRequest.mockImplementation(async (path: string, method: string) => {
      const p = String(path);
      if (p.includes("user-utility")) return { success: true };
      if (p.includes("user-catalog") && !p.includes("catalog-detail")) {
        return { data: { items: [{ id: 5, username: "548958bf-5538-4bdc-8e70-2011d422ecee" }] } };
      }
      if (p.includes("catalog-detail")) {
        return { data: { profile: { id: 10 }, roles: { items: [{ id: 20 }] } } };
      }
      if (p === "security/profiles/11/users" && method === "get") {
        return { data: { assigned: [] } };
      }
      if (p === "security/roles/20/users" && method === "get") {
        return { data: { assigned: [{ id: 5 }, { id: 9 }] } };
      }
      if (p === "security/roles/21/users" && method === "get") {
        return { data: { assigned: [] } };
      }
      return { success: true };
    });
    fetchCatalog.mockImplementation(async (path: string) => {
      if (path.toUpperCase().includes("MACROROLPERFIL") || path === "CatMacroRolPerfil") {
        return { details: [{ externalKey: "FBC_NATIONAL_SUPPLIER_FINANCE_USER", value: "Proveedor Nuevo" }] };
      }
      if (path.toUpperCase().includes("MACROROLROL") || path === "CatMacroRolRolUsuario") {
        return { details: [{ externalKey: "FBC_NATIONAL_SUPPLIER_FINANCE_USER", value: "Rol Nuevo" }] };
      }
      if (path === "CatPerfil") {
        return {
          details: [
            { id: 10, value: "Proveedor Finanzas" },
            { id: 11, value: "Proveedor Nuevo" },
          ],
        };
      }
      if (path === "CatRol") {
        return {
          details: [
            { id: 20, value: "Proveedor - Finanzas" },
            { id: 21, value: "Rol Nuevo" },
          ],
        };
      }
      return { details: [] };
    });

    const result = await syncFinanzasUser();
    expect(result.status).toBe("reassigned");
    expect(mockRequest).toHaveBeenCalledWith("security/profiles/11/users", "put", { selectedIds: [5] });
    expect(mockRequest).toHaveBeenCalledWith("security/roles/20/users", "put", { selectedIds: [9] });
    expect(mockRequest).toHaveBeenCalledWith("security/roles/21/users", "put", { selectedIds: [5] });
  });
});
