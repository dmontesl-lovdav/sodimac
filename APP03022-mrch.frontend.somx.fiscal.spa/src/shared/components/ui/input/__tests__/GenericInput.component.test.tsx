/**
 * @jest-environment jsdom
 */
import { jest, describe, it, expect, beforeEach, afterEach } from "@jest/globals";
import { createRoot } from "react-dom/client";
import { act } from "react";
import React from "react";

import GenericInput, { isValidEmailFormat } from "../GenericInput";

describe("isValidEmailFormat", () => {
  it("acepta correos válidos", () => {
    expect(isValidEmailFormat("a@b.co")).toBe(true);
    expect(isValidEmailFormat("user.name@example.com")).toBe(true);
  });

  it("rechaza formatos inválidos", () => {
    expect(isValidEmailFormat("")).toBe(false);
    expect(isValidEmailFormat("a b@c.com")).toBe(false);
    expect(isValidEmailFormat("nodomain")).toBe(false);
    expect(isValidEmailFormat("@x.com")).toBe(false);
    expect(isValidEmailFormat("a@")).toBe(false);
    expect(isValidEmailFormat("a@b")).toBe(false);
    expect(isValidEmailFormat("a@@b.com")).toBe(false);
  });
});

describe("GenericInput", () => {
  let container: HTMLDivElement;
  let root: ReturnType<typeof createRoot>;

  beforeEach(() => {
    (global as any).ResizeObserver = class {
      observe() {}
      disconnect() {}
      unobserve() {}
    };
    container = document.createElement("div");
    document.body.appendChild(container);
    root = createRoot(container);
  });

  afterEach(() => {
    act(() => root.unmount());
    container.remove();
  });

  it("renderiza label, contador y error de email", async () => {
    await act(async () => {
      root.render(
        React.createElement(GenericInput, {
          id: "email-1",
          label: "Correo",
          value: "bad",
          required: true,
          maxLength: 40,
          validateEmail: true,
          helperText: "ayuda",
        })
      );
    });
    expect(container.textContent).toContain("Correo");
    expect(container.textContent).toContain("formato válido");
    expect(container.textContent).toContain("3 / 40");
  });

  it("no muestra error con email válido", async () => {
    await act(async () => {
      root.render(
        React.createElement(GenericInput, {
          id: "email-2",
          label: "Correo",
          value: "ok@mail.com",
          validateEmail: true,
        })
      );
    });
    expect(container.textContent).not.toContain("formato válido");
  });
});
