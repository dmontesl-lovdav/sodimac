import { describe, it, expect, jest } from "@jest/globals";
import React from "react";
import { renderToStaticMarkup } from "react-dom/server";

jest.mock("@shared/components/ui", () => {
  const ReactLocal = jest.requireActual<typeof import("react")>("react");

  return {
    GenericTable: ({ rows }: any) =>
      ReactLocal.createElement(
        "div",
        { "data-testid": "table" },
        String(rows?.length ?? 0)
      ),
  };
});

jest.mock("@shared/security", () => ({
  APP_EVENT: {
    ACCOUNT_STATEMENT: {
      VIEW_DETAIL: {},
      CONFIRM_REVIEW: {},
      REQUEST_REVIEW: {},
    },
  },

  // Simula permisos únicamente dentro de esta prueba.
  useSecurityContext: () => ({
    can: () => true,
  }),

  PermissionGate: ({ children }: any) => children,
}));

import AccountStatementGrid from "../AccountStatementGrid";

describe("AccountStatementGrid", () => {
  it("renderiza filas", () => {
    const html = renderToStaticMarkup(
      React.createElement(AccountStatementGrid, {
        rows: [
          {
            month: 1,
            year: 2026,
            vendorNumber: "1",
            vendorName: "Demo",
            status: 2,
            statusLabel: "Publicado",
          } as any,
        ],
        page: 1,
        perPage: 10,
        totalPages: 1,
        totalItems: 1,
        onPageChange: jest.fn(),
        onPerPageChange: jest.fn(),
        onView: jest.fn(),
        onReview: jest.fn(),
        onReject: jest.fn(),
      })
    );

    expect(html).toContain('data-testid="table"');
    expect(html).toContain(">1<");
  });
});