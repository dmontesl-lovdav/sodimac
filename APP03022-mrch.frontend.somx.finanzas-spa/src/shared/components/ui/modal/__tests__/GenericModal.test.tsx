import { describe, it, expect, jest } from "@jest/globals";
import React from "react";
import { renderToStaticMarkup } from "react-dom/server";
import GenericModal from "../GenericModal";

describe("GenericModal", () => {
  it("no renderiza si visible=false", () => {
    const html = renderToStaticMarkup(
      React.createElement(GenericModal, { visible: false })
    );
    expect(html).toBe("");
  });

  it("renderiza loading", () => {
    const html = renderToStaticMarkup(
      React.createElement(GenericModal, {
        visible: true,
        variant: "loading",
        message: "Espere",
      })
    );
    expect(html).toContain("Espere");
    expect(html).toContain("gm-loading");
  });

  it("renderiza alert", () => {
    const html = renderToStaticMarkup(
      React.createElement(GenericModal, {
        visible: true,
        variant: "alert",
        title: "Atención",
        message: "Msg",
        severity: "warning",
        onClose: jest.fn(),
      })
    );
    expect(html).toContain("Atención");
    expect(html).toContain("Msg");
  });

  it("renderiza confirm con messageConfirm", () => {
    const html = renderToStaticMarkup(
      React.createElement(GenericModal, {
        visible: true,
        variant: "confirm",
        title: "Confirmar",
        messageConfirm: "¿Seguro?",
        onConfirm: jest.fn(),
        onCancel: jest.fn(),
      })
    );
    expect(html).toContain("¿Seguro?");
    expect(html).toContain("gm-btn-confirm");
  });
});
