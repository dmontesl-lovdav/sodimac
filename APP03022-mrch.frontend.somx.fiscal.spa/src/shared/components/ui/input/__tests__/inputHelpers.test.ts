/**
 * @jest-environment jsdom
 */
import { jest, describe, it, expect } from "@jest/globals";
jest.mock("../Input.css", () => ({}));

import { getEmailError, getInputLeftPad } from "../GenericInput";

describe("getEmailError", () => {
  it("retorna vacío cuando validateEmail es false", () => {
    expect(getEmailError("bad-email", false)).toBe("");
  });

  it("retorna vacío cuando value está vacío", () => {
    expect(getEmailError("", true)).toBe("");
  });

  it("retorna vacío con email válido", () => {
    expect(getEmailError("user@example.com", true)).toBe("");
  });

  it("retorna error con email sin @", () => {
    expect(getEmailError("invalidemail.com", true)).toBe(
      "El correo no tiene un formato válido"
    );
  });

  it("retorna error con email sin dominio", () => {
    expect(getEmailError("user@", true)).toBe(
      "El correo no tiene un formato válido"
    );
  });

  it("retorna error con espacios en el email", () => {
    expect(getEmailError("user @domain.com", true)).toBe(
      "El correo no tiene un formato válido"
    );
  });
});

describe("getInputLeftPad", () => {
  it("retorna el padding izquierdo del elemento", () => {
    const el = document.createElement("input");
    Object.defineProperty(el, "paddingLeft", { value: "24px", configurable: true });
    jest.spyOn(window, "getComputedStyle").mockReturnValue({
      paddingLeft: "24px",
    } as unknown as CSSStyleDeclaration);
    const result = getInputLeftPad(el);
    expect(result).toBe(24);
  });

  it("usa 16 como fallback cuando paddingLeft es NaN", () => {
    const el = document.createElement("input");
    jest.spyOn(window, "getComputedStyle").mockReturnValue({
      paddingLeft: "abc",
    } as unknown as CSSStyleDeclaration);
    const result = getInputLeftPad(el);
    expect(result).toBe(16);
  });

  it("usa 16 como fallback cuando paddingLeft está vacío", () => {
    const el = document.createElement("input");
    jest.spyOn(window, "getComputedStyle").mockReturnValue({
      paddingLeft: "",
    } as unknown as CSSStyleDeclaration);
    const result = getInputLeftPad(el);
    expect(result).toBe(16);
  });
});
