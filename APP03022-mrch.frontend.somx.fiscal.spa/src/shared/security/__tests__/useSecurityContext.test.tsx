/**
 * @jest-environment jsdom
 */
import { jest, describe, it, expect, beforeEach, afterEach } from "@jest/globals";
import { createRoot } from "react-dom/client";
import { act } from "react";
import React from "react";

jest.mock("./../currentUserKey", () => ({
  getCurrentUserKey: jest.fn(() => "user-test"),
}), { virtual: false });

jest.mock("./../securityService", () => ({
  securityService: {
    getAccessContext: jest.fn(),
  },
}));

jest.mock("@/store/hooks/useAppSelector", () => ({
  useAppSelector: () => ({}),
}));

import { getCurrentUserKey } from "../currentUserKey";
import { securityService } from "../securityService";
import {
  clearSecurityContextCache,
  useSecurityContext,
} from "../useSecurityContext";

function Probe({ onReady }: { onReady: (ctx: ReturnType<typeof useSecurityContext>) => void }) {
  const ctx = useSecurityContext();
  React.useEffect(() => {
    onReady(ctx);
  }, [ctx, onReady]);
  return null;
}

describe("useSecurityContext", () => {
  let container: HTMLDivElement;
  let root: ReturnType<typeof createRoot>;

  beforeEach(() => {
    clearSecurityContextCache();
    (getCurrentUserKey as jest.Mock).mockReturnValue("user-test");
    (securityService.getAccessContext as jest.Mock).mockResolvedValue({
      apps: [{ key: "FISCAL", events: [{ key: "VIEW" }] }],
      profiles: [{ key: "ADMIN" }],
      permissions: [{ key: "P1" }],
      roles: [{ key: "R1" }],
    });
    container = document.createElement("div");
    document.body.appendChild(container);
    root = createRoot(container);
  });

  afterEach(() => {
    act(() => {
      root.unmount();
    });
    container.remove();
    clearSecurityContextCache();
  });

  it("expone checkers de apps/eventos/permisos", async () => {
    let latest: ReturnType<typeof useSecurityContext> | null = null;
    await act(async () => {
      root.render(
        React.createElement(Probe, {
          onReady: (ctx) => {
            latest = ctx;
          },
        })
      );
    });
    await act(async () => {
      await Promise.resolve();
      await Promise.resolve();
    });

    expect(latest).toBeTruthy();
    expect(latest!.hasApp("FISCAL")).toBe(true);
    expect(latest!.hasAnyApp(["X", "FISCAL"])).toBe(true);
    expect(latest!.hasEvent("FISCAL", "VIEW")).toBe(true);
    expect(latest!.hasEvent("", "VIEW")).toBe(false);
    expect(latest!.hasEventInAnyApp("VIEW")).toBe(true);
    expect(latest!.hasPermission("P1")).toBe(true);
    expect(latest!.hasProfile("ADMIN")).toBe(true);
  });

  it("sin userKey no queda en loading", async () => {
    (getCurrentUserKey as jest.Mock).mockReturnValue("");
    let latest: ReturnType<typeof useSecurityContext> | null = null;
    await act(async () => {
      root.render(
        React.createElement(Probe, {
          onReady: (ctx) => {
            latest = ctx;
          },
        })
      );
    });
    expect(latest?.isLoading).toBe(false);
  });
});
