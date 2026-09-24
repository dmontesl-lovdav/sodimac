import { describe, it, expect, jest } from "@jest/globals";
import React from "react";
import { renderToStaticMarkup } from "react-dom/server";

jest.mock("@shared/components/ui/navigation/Breadcrumb", () => {
  const ReactLocal = require("react");
  return {
    __esModule: true,
    default: () => ReactLocal.createElement("nav", null, "bc"),
  };
});
jest.mock("@shared/components/ui/navigation/financeBreadcrumb", () => ({
  breadcrumbFinanceHomePage: [],
}));
jest.mock("@shared/components/ui/modal/GenericModal", () => ({
  __esModule: true,
  default: () => null,
}));
jest.mock("@shared/security", () => ({
  APP_KEYS: {
    CARTA_PORTE: "a",
    RECEPTIONS: "b",
    DISCOUNTS: "c",
    INVOICES: "d",
    CREDIT_NOTES: "e",
    PAYMENT_COMPLEMENTS: "f",
    ACCOUNT_STATEMENT: "g",
    THREE_WAY_MATCH: "h",
    MIGO: "i",
  },
  PermissionGate: ({ children }: any) => children,
  invalidateAccessContextCache: jest.fn(),
}));
jest.mock("react-router-dom", () => {
  const ReactLocal = require("react");
  return {
    Link: ({ children, to }: any) =>
      ReactLocal.createElement("a", { href: to }, children),
  };
});
jest.mock("../api", () => ({
  getHealthcheck: jest.fn(),
}));
jest.mock("@/configuration/ConfigurationBuilder", () => ({
  __esModule: true,
  localDeployment: false,
  default: { localDeployment: false },
}));
jest.mock("@/services/finanzasUserSync", () => ({
  syncFinanzasUser: jest.fn(() => Promise.resolve({ status: "skipped" })),
  getFinanzasUserSyncInFlight: jest.fn(() => null),
}));

import FinanzasContainer from "../FinanzasContainer";
import { syncFinanzasUser } from "@/services/finanzasUserSync";

describe("FinanzasContainer", () => {
  it("renderiza tarjetas por defecto", () => {
    const html = renderToStaticMarkup(React.createElement(FinanzasContainer));
    expect(html).toContain("finanzas-root");
    expect(html).toContain("Guías de embarque");
    expect(html).toContain("Healthcheck");
  });

  it("inicia el cruce de macrorol al cargar la tarjeta de finanzas", () => {
    renderToStaticMarkup(React.createElement(FinanzasContainer));
    expect(syncFinanzasUser).toHaveBeenCalled();
  });
});
