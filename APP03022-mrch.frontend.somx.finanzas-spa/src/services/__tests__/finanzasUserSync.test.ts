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
  getCurrentUserKey: () => "4b36d77f-5b01-4889-bcfc-ef8abe130ade",
}));

jest.mock("@/store/localStore", () => ({
  localHomeStore: {
    getState: () => ({
      authentication: {
        tokenDecoded: {
          sub: "4b36d77f-5b01-4889-bcfc-ef8abe130ade",
          email: "zedlav.sd87@gmail.com",
          realm_access: {
            roles: [
              "default-roles-corp",
              "offline_access",
              "FBC_NATIONAL_SUPPLIER_FINANCE_USER",
              "FBC_NATIONAL_LOGISTICS_SUPPLIER_USER",
              "uma_authorization",
              "FBC_NATIONAL_COMMERCIAL_SUPPLIER_USER",
              "sr-readonly",
            ],
          },
          "vendors-taxs": [
            { name: "PORCELANITE", taxId: "PLA071206TS4", country: "MX" },
          ],
        },
      },
    }),
  },
}));

import { fetchCatalogDetails, fetchProviders } from "@/utils/utils";
import { resetFinanzasUserSyncForTests, syncFinanzasUser } from "../finanzasUserSync";
import {
  MACRO_ROL_PERFIL_CATALOG,
  MACRO_ROL_ROL_CATALOG,
  WRN7038_PROFILE_FALLBACK,
} from "../finanzasUserSync.helpers";

const fetchCatalog = fetchCatalogDetails as jest.MockedFunction<typeof fetchCatalogDetails>;
const fetchProvidersMock = fetchProviders as jest.MockedFunction<typeof fetchProviders>;

function catalogPath(path: string): string {
  return String(path).replace(/^\/+/, "").toUpperCase();
}

describe("syncFinanzasUser", () => {
  beforeEach(() => {
    resetFinanzasUserSyncForTests();
    mockRequest.mockReset();
    fetchCatalog.mockReset();
    fetchProvidersMock.mockReset();
    fetchProvidersMock.mockResolvedValue([
      { id: 77, supplierNumber: "1001", rfc: "PLA071206TS4" },
    ]);
  });

  it("muestra WRN7038 cuando ningún macrorol tiene conversión, sin consultar catálogos inexistentes", async () => {
    mockRequest.mockImplementation(async (path: string) => {
      if (String(path).includes("user-utility")) return { success: true };
      return {};
    });
    fetchCatalog.mockImplementation(async (path: string) => {
      if (catalogPath(path) === MACRO_ROL_PERFIL_CATALOG) {
        return {
          code: MACRO_ROL_PERFIL_CATALOG,
          details: [
            {
              externalKey: "FBC_SUPER_ADMIN_USER",
              value: "0987",
            },
          ],
        };
      }
      if (catalogPath(path) === MACRO_ROL_ROL_CATALOG) {
        return {
          code: MACRO_ROL_ROL_CATALOG,
          details: [
            {
              externalKey: "FBC_SUPER_ADMIN_USER",
              value: "1031",
            },
          ],
        };
      }
      return null;
    });

    const result = await syncFinanzasUser();
    expect(result).toEqual({
      status: "denied",
      messageKey: "WRN7038",
      deniedKind: "profile",
      message: WRN7038_PROFILE_FALLBACK,
    });
    expect(fetchCatalog).toHaveBeenCalledWith(MACRO_ROL_PERFIL_CATALOG);
    expect(fetchCatalog).toHaveBeenCalledWith(MACRO_ROL_ROL_CATALOG);
    expect(fetchCatalog).not.toHaveBeenCalledWith("CatMacroRolPerfil");
    expect(fetchCatalog).not.toHaveBeenCalledWith("CatPerfil");
    expect(fetchCatalog).not.toHaveBeenCalledWith("CatRol");
    expect(fetchCatalog.mock.calls.some(([path]) => String(path).includes("WRN7038"))).toBe(
      false
    );
    expect(mockRequest).not.toHaveBeenCalledWith(
      expect.stringContaining("/profiles"),
      "put",
      expect.anything()
    );
  });

  it("asigna el value de CATMACROROLPERFIL y CATMACROROLROLUSUARIO al usuario del catálogo", async () => {
    mockRequest.mockImplementation(async (path: string, method: string) => {
      const p = String(path);
      if (p.includes("user-utility")) return { success: true };
      if (p.includes("user-catalog") && !p.includes("catalog-detail")) {
        return {
          data: {
            items: [
              {
                id: 13,
                username: "4b36d77f-5b01-4889-bcfc-ef8abe130ade",
                email: "zedlav.sd87@gmail.com",
              },
            ],
          },
        };
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
      if (catalogPath(path) === MACRO_ROL_PERFIL_CATALOG) {
        return {
          details: [
            {
              externalKey: "FBC_NATIONAL_COMMERCIAL_SUPPLIER_USER",
              value: "0981",
            },
          ],
        };
      }
      if (catalogPath(path) === MACRO_ROL_ROL_CATALOG) {
        return {
          details: [
            {
              externalKey: "FBC_NATIONAL_COMMERCIAL_SUPPLIER_USER",
              value: "1022",
            },
          ],
        };
      }
      return null;
    });

    const result = await syncFinanzasUser();
    expect(result.status).toBe("assigned");
    expect(mockRequest).toHaveBeenCalledWith("security/profiles/981/users", "put", {
      selectedIds: [13],
      isFromFront: 1,
    });
    expect(mockRequest).toHaveBeenCalledWith("security/roles/1022/users", "put", {
      selectedIds: [13],
      isFromFront: 1,
    });
    expect(fetchCatalog).toHaveBeenCalledTimes(2);
  });

  it("no modifica perfil ni rol si ya coinciden con la conversión", async () => {
    mockRequest.mockImplementation(async (path: string) => {
      const p = String(path);
      if (p.includes("user-utility")) return { success: true };
      if (p.includes("user-catalog") && !p.includes("catalog-detail")) {
        return {
          data: {
            items: [{ id: 13, username: "4b36d77f-5b01-4889-bcfc-ef8abe130ade" }],
          },
        };
      }
      if (p.includes("catalog-detail")) {
        return { data: { profile: { id: 981 }, roles: { items: [{ id: 1022 }] } } };
      }
      return { success: true };
    });
    fetchCatalog.mockImplementation(async (path: string) => {
      if (catalogPath(path) === MACRO_ROL_PERFIL_CATALOG) {
        return {
          details: [{ externalKey: "FBC_NATIONAL_COMMERCIAL_SUPPLIER_USER", value: "0981" }],
        };
      }
      if (catalogPath(path) === MACRO_ROL_ROL_CATALOG) {
        return {
          details: [{ externalKey: "FBC_NATIONAL_COMMERCIAL_SUPPLIER_USER", value: "1022" }],
        };
      }
      return null;
    });

    const result = await syncFinanzasUser();
    expect(result.status).toBe("configured");
    expect(mockRequest).not.toHaveBeenCalledWith(
      expect.stringContaining("/users"),
      "put",
      expect.anything()
    );
  });

  it("reemplaza perfil y ajusta roles si cambió el macrorol", async () => {
    mockRequest.mockImplementation(async (path: string, method: string) => {
      const p = String(path);
      if (p.includes("user-utility")) return { success: true };
      if (p.includes("user-catalog") && !p.includes("catalog-detail")) {
        return {
          data: {
            items: [{ id: 13, username: "4b36d77f-5b01-4889-bcfc-ef8abe130ade" }],
          },
        };
      }
      if (p.includes("catalog-detail")) {
        return { data: { profile: { id: 10 }, roles: { items: [{ id: 20 }] } } };
      }
      if (p === "security/profiles/981/users" && method === "get") {
        return { data: { assigned: [] } };
      }
      if (p === "security/roles/20/users" && method === "get") {
        return { data: { assigned: [{ id: 13 }, { id: 9 }] } };
      }
      if (p === "security/roles/1022/users" && method === "get") {
        return { data: { assigned: [] } };
      }
      return { success: true };
    });
    fetchCatalog.mockImplementation(async (path: string) => {
      if (catalogPath(path) === MACRO_ROL_PERFIL_CATALOG) {
        return {
          details: [{ externalKey: "FBC_NATIONAL_COMMERCIAL_SUPPLIER_USER", value: "0981" }],
        };
      }
      if (catalogPath(path) === MACRO_ROL_ROL_CATALOG) {
        return {
          details: [{ externalKey: "FBC_NATIONAL_COMMERCIAL_SUPPLIER_USER", value: "1022" }],
        };
      }
      return null;
    });

    const result = await syncFinanzasUser();
    expect(result.status).toBe("reassigned");
    expect(mockRequest).toHaveBeenCalledWith("security/profiles/981/users", "put", {
      selectedIds: [13],
      isFromFront: 1,
    });
    expect(mockRequest).toHaveBeenCalledWith("security/roles/20/users", "put", {
      selectedIds: [9],
      isFromFront: 1,
    });
    expect(mockRequest).toHaveBeenCalledWith("security/roles/1022/users", "put", {
      selectedIds: [13],
      isFromFront: 1,
    });
  });

  it("no reemplaza el perfil del admin y solo agrega roles del macrorol", async () => {
    mockRequest.mockImplementation(async (path: string, method: string) => {
      const p = String(path);
      if (p.includes("user-utility")) return { success: true };
      if (p.includes("user-catalog") && !p.includes("catalog-detail")) {
        return {
          data: {
            items: [{ id: 13, username: "4b36d77f-5b01-4889-bcfc-ef8abe130ade" }],
          },
        };
      }
      if (p.includes("catalog-detail")) {
        return {
          data: {
            profile: { id: 10, isFromFront: false },
            roles: { items: [{ id: 50, isFromFront: false }] },
          },
        };
      }
      if (p === "security/roles/1022/users" && method === "get") {
        return { data: { assigned: [] } };
      }
      return { success: true };
    });
    fetchCatalog.mockImplementation(async (path: string) => {
      if (catalogPath(path) === MACRO_ROL_PERFIL_CATALOG) {
        return {
          details: [{ externalKey: "FBC_NATIONAL_COMMERCIAL_SUPPLIER_USER", value: "0981" }],
        };
      }
      if (catalogPath(path) === MACRO_ROL_ROL_CATALOG) {
        return {
          details: [{ externalKey: "FBC_NATIONAL_COMMERCIAL_SUPPLIER_USER", value: "1022" }],
        };
      }
      return null;
    });

    const result = await syncFinanzasUser();
    expect(result.status).toBe("reassigned");
    expect(mockRequest).not.toHaveBeenCalledWith(
      "security/profiles/981/users",
      "put",
      expect.anything()
    );
    expect(mockRequest).not.toHaveBeenCalledWith(
      "security/roles/50/users",
      "put",
      expect.anything()
    );
    expect(mockRequest).toHaveBeenCalledWith("security/roles/1022/users", "put", {
      selectedIds: [13],
      isFromFront: 1,
    });
  });
});
