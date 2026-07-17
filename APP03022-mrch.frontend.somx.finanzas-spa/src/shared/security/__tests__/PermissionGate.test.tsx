import { describe, it, expect, jest } from "@jest/globals";
import React from "react";
import { renderToStaticMarkup } from "react-dom/server";

jest.mock("../useSecurityContext", () => ({
  useSecurityContext: () => ({
    isLoading: false,
    error: null,
    hasApp: () => true,
    hasAnyApp: () => true,
    hasEventInAnyApp: () => true,
    hasEvent: () => true,
    hasPermission: () => true,
    hasProfile: () => true,
  }),
}));

import { PermissionGate } from "../PermissionGate";

describe("PermissionGate", () => {
  it("renderiza children (bypass actual)", () => {
    const html = renderToStaticMarkup(
      React.createElement(
        PermissionGate,
        { appEvent: { app: "X", event: "Y" } },
        React.createElement("span", null, "ok")
      )
    );
    expect(html).toContain("ok");
  });
});
