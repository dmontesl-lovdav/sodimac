/**
 * @jest-environment jsdom
 */
import { describe, it, expect } from "@jest/globals";

jest.mock("@/services/TraceabilityClient", () => ({
  createTraceabilityClient: () => ({
    createFolio: async () => null,
    createAuditLog: () => undefined,
  }),
}));
jest.mock("@/shared/components/ui/misc", () => ({
  GenericTrace: () => null,
}));
jest.mock("@/shared/components/ui/button/GenericButton", () => ({
  __esModule: true,
  default: () => null,
}));
jest.mock("@/utils/getUserIdFromStore", () => ({
  getUserIdFromStore: () => "1",
}));

import {
  getNestedValue,
  pickFirstString,
  pickFirstStringOrNumber,
  extractErrorInfo,
  toLogString,
} from "../TraceFolioProvider";

describe("getNestedValue", () => {
  it("navega claves anidadas", () => {
    expect(getNestedValue({ a: { b: 1 } }, ["a", "b"])).toBe(1);
    expect(getNestedValue({ a: 1 }, ["a", "b"])).toBeUndefined();
  });
});

describe("pickFirstString / pickFirstStringOrNumber", () => {
  it("elige el primer string no vacío", () => {
    expect(pickFirstString(["", "  ", "ok"])).toBe("ok");
    expect(pickFirstString([])).toBe("");
  });

  it("acepta números", () => {
    expect(pickFirstStringOrNumber([null, 42])).toBe("42");
    expect(pickFirstStringOrNumber([""])).toBe("");
  });
});

describe("extractErrorInfo", () => {
  it("usa response.data cuando existe", () => {
    const info = extractErrorInfo({
      response: { data: { errorCode: "E1", message: "Fail", title: "T" } },
    });
    expect(info.idError).toBe("E1");
    expect(info.mensaje).toBe("Fail");
    expect(info.idMensaje).toBe("T");
  });

  it("usa fallback para valores no objeto", () => {
    expect(extractErrorInfo("x")).toEqual({ idError: "1", mensaje: "Timeout error" });
  });
});

describe("toLogString", () => {
  it("serializa string, null, Error y objetos", () => {
    expect(toLogString("hola")).toBe("hola");
    expect(toLogString(null)).toBe("");
    expect(toLogString({ response: { data: { a: 1 } } })).toContain('"a":1');
    const err = new Error("boom");
    expect(JSON.parse(toLogString(err)).message).toBe("boom");
    expect(toLogString({ x: 1 })).toContain('"x":1');
  });
});
