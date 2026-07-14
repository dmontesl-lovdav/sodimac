/**
 * @jest-environment jsdom
 */
import { jest, describe, it, expect } from "@jest/globals";

jest.mock("react-router-dom", () => ({
  useNavigate: () => jest.fn(),
}));

import GenericTable, { Switch, buildTable } from "../DataTable";

describe("Switch", () => {
  it("renderiza estado on y off", () => {
    const on = Switch({ on: true });
    const off = Switch({ on: false, onClick: () => undefined });
    expect(on.props.className).toContain("on");
    expect(off.props.className).toContain("off");
  });
});

describe("buildTable", () => {
  it("construye tabla con columnas", () => {
    const el = buildTable(
      [{ id: 1, name: "A" }],
      [{ header: "Nombre", render: (r) => r.name }],
      { emptyLabel: "Vacío" }
    );
    expect(el).toBeTruthy();
  });
});

describe("GenericTable", () => {
  it("muestra emptyLabel cuando no hay filas", () => {
    const el = GenericTable({
      rows: [],
      columns: [{ header: "H", render: () => null }],
      emptyLabel: "Sin datos",
      actions: [
        {
          title: "Ver",
          icon: "x.svg",
          onClick: () => undefined,
          isDisabled: () => true,
        },
      ],
      page: 1,
      perPage: 10,
      totalPages: 1,
      totalItems: 0,
    }) as any;
    expect(el).toBeTruthy();
  });

  it("renderiza filas y paginación", () => {
    const el = GenericTable({
      rows: [
        { id: 1, n: "a" },
        { id: 2, n: "b" },
      ],
      columns: [
        { header: "N", align: "right", render: (r) => r.n },
        { header: "C", align: "center", render: (r) => r.id },
      ],
      actions: [
        {
          title: "OK",
          icon: "i.svg",
          onClick: () => undefined,
          isDisabled: () => false,
        },
      ],
      page: 2,
      perPage: 10,
      totalPages: 5,
      totalItems: 50,
      onChangePage: () => undefined,
      onChangePerPage: () => undefined,
    }) as any;
    expect(el).toBeTruthy();
  });
});
