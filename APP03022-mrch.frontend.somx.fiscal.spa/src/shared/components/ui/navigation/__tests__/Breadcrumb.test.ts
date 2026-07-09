import { jest, describe, it, expect, beforeEach, afterEach } from "@jest/globals";
jest.mock("../Breadcrumb.css", () => ({}));
jest.mock("react-router-dom", () => ({ Link: ({ children }: { children: unknown }) => children }), { virtual: false });

import { getFbcHomeUrl, isHomeCrumb, buildDisplayItems } from "../Breadcrumb";

describe("getFbcHomeUrl", () => {
  const OLD_ENV = process.env;

  beforeEach(() => {
    process.env = { ...OLD_ENV };
  });

  afterEach(() => {
    process.env = OLD_ENV;
  });

  it("retorna '/' cuando FBC_HOME no está definido", () => {
    delete process.env.FBC_HOME;
    expect(getFbcHomeUrl()).toBe("/");
  });

  it("retorna '/' cuando FBC_HOME está vacío", () => {
    process.env.FBC_HOME = "   ";
    expect(getFbcHomeUrl()).toBe("/");
  });

  it("retorna la URL cuando FBC_HOME tiene valor", () => {
    process.env.FBC_HOME = "https://home.example.com";
    expect(getFbcHomeUrl()).toBe("https://home.example.com");
  });

  it("recorta espacios del valor", () => {
    process.env.FBC_HOME = "  https://home.example.com  ";
    expect(getFbcHomeUrl()).toBe("https://home.example.com");
  });
});

describe("isHomeCrumb", () => {
  it("retorna true para label 'home'", () => {
    expect(isHomeCrumb({ label: "home" })).toBe(true);
  });

  it("retorna true para label 'Home' (case insensitive)", () => {
    expect(isHomeCrumb({ label: "Home" })).toBe(true);
  });

  it("retorna true para label 'inicio'", () => {
    expect(isHomeCrumb({ label: "inicio" })).toBe(true);
  });

  it("retorna true para label 'INICIO'", () => {
    expect(isHomeCrumb({ label: "INICIO" })).toBe(true);
  });

  it("retorna false para otros labels", () => {
    expect(isHomeCrumb({ label: "Facturas" })).toBe(false);
  });

  it("ignora espacios en el label", () => {
    expect(isHomeCrumb({ label: "  home  " })).toBe(true);
  });
});

describe("buildDisplayItems", () => {
  it("antepone Inicio cuando no hay items", () => {
    const result = buildDisplayItems([]);
    expect(result).toHaveLength(1);
    expect(result[0].label).toBe("Inicio");
    expect(result[0].externalHref).toBeDefined();
  });

  it("elimina el primer item si es Home y antepone Inicio", () => {
    const items = [{ label: "Home", to: "/" }, { label: "Facturas", to: "/facturas" }];
    const result = buildDisplayItems(items);
    expect(result).toHaveLength(2);
    expect(result[0].label).toBe("Inicio");
    expect(result[1].label).toBe("Facturas");
  });

  it("conserva todos los items cuando el primero no es Home", () => {
    const items = [{ label: "Facturas", to: "/facturas" }, { label: "Detalle" }];
    const result = buildDisplayItems(items);
    expect(result).toHaveLength(3);
    expect(result[0].label).toBe("Inicio");
    expect(result[1].label).toBe("Facturas");
    expect(result[2].label).toBe("Detalle");
  });

  it("el item Inicio tiene externalHref", () => {
    const result = buildDisplayItems([{ label: "Facturas" }]);
    expect(result[0].externalHref).toBeDefined();
  });
});
