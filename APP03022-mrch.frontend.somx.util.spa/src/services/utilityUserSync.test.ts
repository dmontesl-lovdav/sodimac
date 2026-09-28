import { afterEach, beforeEach, describe, expect, it, jest } from "@jest/globals";

const mockRequest = jest.fn();

jest.mock("@/services/apiClient", () => ({
  createApiClient: () => ({
    request: (...args: unknown[]) => mockRequest(...args),
    requestBinary: jest.fn(),
  }),
}));

jest.mock("@/features/security/utils/currentUserCatalogKey", () => ({
  getCurrentUserCatalogKey: () => "4b36d77f-5b01-4889-bcfc-ef8abe130ade",
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

import { resetUtilityUserSyncForTests, syncUtilityUser } from "../utilityUserSync";
import {
  MACRO_ROL_PERFIL_CATALOG,
  MACRO_ROL_ROL_CATALOG,
  WRN7038_PROFILE_FALLBACK,
} from "../utilityUserSync.helpers";

describe("syncUtilityUser", () => {
  const originalToken = process.env.AUTH_DEFAULT_TOKEN;

  beforeEach(() => {
    process.env.AUTH_DEFAULT_TOKEN = "";
    resetUtilityUserSyncForTests();
    mockRequest.mockReset();
  });

  afterEach(() => {
    process.env.AUTH_DEFAULT_TOKEN = originalToken;
  });

  it("muestra WRN7038 cuando ningún macrorol tiene conversión", async () => {
    mockRequest.mockImplementation(async (path: string) => {
      if (String(path).includes("user-utility")) return { success: true };
      if (String(path).startsWith("catalog/")) return { details: [] };
      return {};
    });

    const result = await syncUtilityUser();
    expect(result).toEqual({
      status: "denied",
      messageKey: "WRN7038",
      deniedKind: "profile",
      message: WRN7038_PROFILE_FALLBACK,
    });
    expect(mockRequest).toHaveBeenCalledWith(`catalog/${MACRO_ROL_PERFIL_CATALOG}`, "get");
    expect(mockRequest).toHaveBeenCalledWith(`catalog/${MACRO_ROL_ROL_CATALOG}`, "get");
  });

  it("asigna perfil y rol con el value de los catálogos de conversión", async () => {
    mockRequest.mockImplementation(async (path: string, method: string) => {
      const p = String(path);
      if (p.includes("user-utility")) return { success: true };
      if (p === `catalog/${MACRO_ROL_PERFIL_CATALOG}`) {
        return {
          details: [{ externalKey: "FBC_NATIONAL_COMMERCIAL_SUPPLIER_USER", value: "0981" }],
        };
      }
      if (p === `catalog/${MACRO_ROL_ROL_CATALOG}`) {
        return {
          details: [{ externalKey: "FBC_NATIONAL_COMMERCIAL_SUPPLIER_USER", value: "1022" }],
        };
      }
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

    const result = await syncUtilityUser();
    expect(result.status).toBe("assigned");
    expect(mockRequest).toHaveBeenCalledWith("security/profiles/981/users", "put", {
      selectedIds: [13],
    });
    expect(mockRequest).toHaveBeenCalledWith("security/roles/1022/users", "put", {
      selectedIds: [13],
    });
  });
});
