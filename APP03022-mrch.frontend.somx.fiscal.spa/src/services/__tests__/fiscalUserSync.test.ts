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
              "FBC_NATIONAL_SUPPLIER_FINANCE_USER",
              "FBC_NATIONAL_LOGISTICS_SUPPLIER_USER",
              "FBC_NATIONAL_COMMERCIAL_SUPPLIER_USER",
            ],
          },
        },
      },
    }),
  },
}));

import { fetchCatalogDetails } from "@/utils/utils";
import { resetFiscalUserSyncForTests, syncFiscalUser } from "../fiscalUserSync";
import {
  MACRO_ROL_PERFIL_CATALOG,
  MACRO_ROL_ROL_CATALOG,
  WRN7038_PROFILE_FALLBACK,
} from "../fiscalUserSync.helpers";

const fetchCatalog = fetchCatalogDetails as jest.MockedFunction<typeof fetchCatalogDetails>;

function catalogPath(path: string): string {
  return String(path).replace(/^\/+/, "").toUpperCase();
}

describe("syncFiscalUser", () => {
  beforeEach(() => {
    resetFiscalUserSyncForTests();
    mockRequest.mockReset();
    fetchCatalog.mockReset();
  });

  it("muestra WRN7038 cuando ningún macrorol tiene conversión", async () => {
    mockRequest.mockImplementation(async (path: string) => {
      if (String(path).includes("user-utility")) return { success: true };
      return {};
    });
    fetchCatalog.mockImplementation(async (path: string) => {
      if (catalogPath(path) === MACRO_ROL_PERFIL_CATALOG) {
        return { details: [{ externalKey: "FBC_SUPER_ADMIN_USER", value: "0987" }] };
      }
      if (catalogPath(path) === MACRO_ROL_ROL_CATALOG) {
        return { details: [{ externalKey: "FBC_SUPER_ADMIN_USER", value: "1031" }] };
      }
      return null;
    });

    const result = await syncFiscalUser();
    expect(result).toEqual({
      status: "denied",
      messageKey: "WRN7038",
      deniedKind: "profile",
      message: WRN7038_PROFILE_FALLBACK,
    });
    expect(fetchCatalog).toHaveBeenCalledWith(MACRO_ROL_PERFIL_CATALOG);
    expect(fetchCatalog).toHaveBeenCalledWith(MACRO_ROL_ROL_CATALOG);
    expect(fetchCatalog).not.toHaveBeenCalledWith("CatPerfil");
  });

  it("asigna perfil y rol con el value de los catálogos de conversión", async () => {
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
        return { data: { profile: null, roles: { items: [] } } };
      }
      if (p.includes("/users") && method === "get") {
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

    const result = await syncFiscalUser();
    expect(result.status).toBe("assigned");
    expect(mockRequest).toHaveBeenCalledWith("security/profiles/981/users", "put", {
      selectedIds: [13],
      isFromFront: 1,
    });
    expect(mockRequest).toHaveBeenCalledWith("security/roles/1022/users", "put", {
      selectedIds: [13],
      isFromFront: 1,
    });
  });
});
