/**
 * @jest-environment jsdom
 */
import { beforeEach, describe, expect, it, jest } from "@jest/globals";
import React from "react";
import { createRoot } from "react-dom/client";
import { act } from "react-dom/test-utils";

const mockGetAccessContext = jest.fn(async () => ({ apps: [{ key: "pagos" }] }));
const mockGetInFlight = jest.fn(() => null);
const mockSyncFinanzasUser = jest.fn();

jest.mock("../securityService", () => ({
  securityService: {
    getAccessContext: (...args: unknown[]) => mockGetAccessContext(...args),
  },
}));

jest.mock("../currentUserKey", () => ({
  getCurrentUserKey: () => "user-1",
}));

jest.mock("@/services/finanzasUserSync", () => ({
  getFinanzasUserSyncInFlight: () => mockGetInFlight(),
  syncFinanzasUser: (...args: unknown[]) => mockSyncFinanzasUser(...args),
}));

import {
  invalidateAccessContextCache,
  useSecurityContext,
} from "../useSecurityContext";

function HookHost({ onReady }: { onReady: (api: ReturnType<typeof useSecurityContext>) => void }) {
  const api = useSecurityContext();
  React.useEffect(() => {
    onReady(api);
  }, [api, onReady]);
  return null;
}

describe("useSecurityContext", () => {
  beforeEach(() => {
    invalidateAccessContextCache();
    mockGetAccessContext.mockClear();
    mockGetInFlight.mockClear();
    mockSyncFinanzasUser.mockClear();
  });

  it("no inicia el cruce de macrorol; solo espera un sync ya en curso", async () => {
    const container = document.createElement("div");
    const root = createRoot(container);

    await act(async () => {
      root.render(
        React.createElement(HookHost, {
          onReady: () => undefined,
        })
      );
    });
    await act(async () => {
      await Promise.resolve();
      await Promise.resolve();
    });

    expect(mockSyncFinanzasUser).not.toHaveBeenCalled();
    expect(mockGetInFlight).toHaveBeenCalled();
    expect(mockGetAccessContext).toHaveBeenCalledWith("user-1");
    root.unmount();
  });
});
