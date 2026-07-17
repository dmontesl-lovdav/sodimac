import { describe, it, expect, jest } from "@jest/globals";
import React from "react";
import { renderToStaticMarkup } from "react-dom/server";
import GenericInput from "../GenericInput";

describe("GenericInput", () => {
  it("renderiza input con label", () => {
    const html = renderToStaticMarkup(
      React.createElement(GenericInput, {
        label: "Nombre",
        value: "x",
        onChange: jest.fn(),
      })
    );
    expect(html).toContain("generic-textfield");
    expect(html).toContain("Nombre");
  });

  it("aplica tone error", () => {
    const html = renderToStaticMarkup(
      React.createElement(GenericInput, {
        label: "Campo",
        value: "",
        error: true,
        onChange: jest.fn(),
      })
    );
    expect(html).toContain("tone-error");
  });
});
