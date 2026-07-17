import { describe, it, expect, jest } from "@jest/globals";
import React from "react";
import { renderToStaticMarkup } from "react-dom/server";

jest.mock("@shared/components/ui", () => {
  const ReactLocal = require("react");
  return {
    GenericTable: ({ rows }: any) =>
      ReactLocal.createElement("div", null, `rows:${rows.length}`),
  };
});

import ThreeWayMatchGridTable from "../ThreeWayMatchGridTable";

describe("ThreeWayMatchGridTable", () => {
  it("renderiza tabla", () => {
    const html = renderToStaticMarkup(
      React.createElement(ThreeWayMatchGridTable, {
        rows: [{ ordenCompra: "OC1" } as any],
        isAdmin: true,
        page: 1,
        perPage: 10,
        totalPages: 1,
        totalItems: 1,
        loading: false,
        onChangePage: jest.fn(),
        onChangePerPage: jest.fn(),
      })
    );
    expect(html).toContain("rows:1");
  });
});
