import { describe, it, expect, jest } from "@jest/globals";
import React from "react";
import { renderToStaticMarkup } from "react-dom/server";
import { MemoryRouter } from "react-router-dom";
import Breadcrumb from "../Breadcrumb";

describe("Breadcrumb", () => {
  it("agrega Inicio y marca el último como current", () => {
    const html = renderToStaticMarkup(
      React.createElement(
        MemoryRouter,
        null,
        React.createElement(Breadcrumb, {
          items: [{ label: "Pagos", to: "/pagos" }, { label: "Detalle" }],
        })
      )
    );
    expect(html).toContain("Inicio");
    expect(html).toContain("Detalle");
    expect(html).toContain("breadcrumb-current");
  });

  it("omite home duplicado al inicio", () => {
    const html = renderToStaticMarkup(
      React.createElement(
        MemoryRouter,
        null,
        React.createElement(Breadcrumb, {
          items: [{ label: "Inicio" }, { label: "Finanzas" }],
        })
      )
    );
    expect(html.match(/Inicio/g)?.length).toBe(1);
  });

  it("renderiza botón cuando hay onClick", () => {
    const onClick = jest.fn();
    const html = renderToStaticMarkup(
      React.createElement(
        MemoryRouter,
        null,
        React.createElement(Breadcrumb, {
          items: [{ label: "A", onClick }, { label: "B" }],
        })
      )
    );
    expect(html).toContain("<button");
  });
});
