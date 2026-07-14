/**
 * @jest-environment jsdom
 */
import { jest, describe, it, expect, beforeEach, afterEach } from "@jest/globals";

jest.mock("axios", () => ({
  __esModule: true,
  default: { get: jest.fn() },
}));

jest.mock("@/store/localStore", () => ({
  localHomeStore: {
    getState: jest.fn(() => ({ authentication: {} })),
  },
}));

import axios from "axios";
import { localHomeStore } from "@/store/localStore";
import { securityService } from "../securityService";

const mockedGet = axios.get as jest.MockedFunction<typeof axios.get>;
const mockedGetState = localHomeStore.getState as jest.MockedFunction<typeof localHomeStore.getState>;

describe("securityService.getAccessContext", () => {
  const OLD_ENV = process.env;

  beforeEach(() => {
    process.env = { ...OLD_ENV };
    mockedGet.mockReset();
    mockedGetState.mockReturnValue({ authentication: {} } as any);
  });

  afterEach(() => {
    process.env = OLD_ENV;
  });

  it("retorna null cuando no hay base URL", async () => {
    delete process.env.UTIL_SECURITY_API_URL;
    delete process.env.REACT_APP_UTIL_SECURITY_API_URL;
    delete process.env.CATALOGS_API_URL;
    delete process.env.REACT_APP_CATALOGS_API_URL;
    await expect(securityService.getAccessContext("user-1")).resolves.toBeNull();
    expect(mockedGet).not.toHaveBeenCalled();
  });

  it("retorna null cuando userKey está vacío", async () => {
    process.env.UTIL_SECURITY_API_URL = "https://sec.example.com/";
    await expect(securityService.getAccessContext("")).resolves.toBeNull();
  });

  it("quita trailing slashes de la URL base y envía Bearer token del store", async () => {
    process.env.UTIL_SECURITY_API_URL = "https://sec.example.com///";
    mockedGetState.mockReturnValue({
      authentication: { token: "store-token" },
    } as any);
    mockedGet.mockResolvedValue({
      data: { data: { user: { key: "u1" }, apps: [] } },
    } as any);

    const result = await securityService.getAccessContext("user-1");
    expect(result?.user?.key).toBe("u1");
    expect(mockedGet).toHaveBeenCalledWith(
      "https://sec.example.com/security/user-details/user-1",
      expect.objectContaining({
        headers: expect.objectContaining({ Authorization: "Bearer store-token" }),
      })
    );
  });

  it("usa idToken si token no existe y cae a env token", async () => {
    process.env.CATALOGS_API_URL = "https://cat.example.com";
    process.env.REACT_APP_AUTH_DEFAULT_TOKEN = "env-token";
    mockedGetState.mockReturnValue({ authentication: {} } as any);
    mockedGet.mockResolvedValue({
      data: { apps: [{ key: "FISCAL" }] },
    } as any);

    const result = await securityService.getAccessContext("u2");
    expect(result?.apps?.[0].key).toBe("FISCAL");
    expect(mockedGet.mock.calls[0][1]?.headers?.Authorization).toBe("Bearer env-token");
  });

  it("no agrega Authorization cuando no hay token", async () => {
    process.env.REACT_APP_UTIL_SECURITY_API_URL = "https://sec2.example.com";
    delete process.env.REACT_APP_AUTH_DEFAULT_TOKEN;
    delete process.env.AUTH_DEFAULT_TOKEN;
    mockedGetState.mockReturnValue({ authentication: {} } as any);
    mockedGet.mockResolvedValue({ data: { apps: [] } } as any);

    await securityService.getAccessContext("u3");
    expect(mockedGet.mock.calls[0][1]?.headers?.Authorization).toBeUndefined();
  });
});
