import { describe, it, expect, jest } from "@jest/globals";

jest.mock("../../store/localStore", () => ({
  localHomeStore: {
    getState: () => ({ authentication: { token: "store-token" } }),
  },
}));

import { createAuthenticator } from "../Authenticator";

describe("createAuthenticator", () => {
  it("isAdmin / isProveedor ok con defaultToken", async () => {
    const auth = createAuthenticator({
      adminGroup: "A",
      proveedorGroup: "P",
      defaultToken: "abc",
    });
    await expect(auth.isAdmin()).resolves.toBe(true);
    await expect(auth.isProveedor()).resolves.toBe(true);
  });

  it("usa token del store cuando no hay defaultToken", async () => {
    const auth = createAuthenticator({
      adminGroup: "A",
      proveedorGroup: "P",
    });
    await expect(auth.isAdmin()).resolves.toBe(true);
  });
});
