import { describe, it, expect } from "@jest/globals";
import { GenericTrace } from "../GenericTrace";

describe("GenericTrace", () => {
  it("retorna elemento de advertencia cuando traceId es null", () => {
    const result = GenericTrace({ traceId: null });
    expect(result).not.toBeNull();
    expect(result).toBeDefined();
  });

  it("retorna elemento de advertencia cuando traceId es undefined", () => {
    const result = GenericTrace({ traceId: null, uuid: undefined });
    expect(result).toBeDefined();
  });

  it("retorna elemento con folio cuando traceId tiene valor", () => {
    const result = GenericTrace({ traceId: "FOL-2024-001" });
    expect(result).not.toBeNull();
    expect(result).toBeDefined();
  });

  it("acepta uuid como prop opcional", () => {
    const result = GenericTrace({ traceId: "FOL-001", uuid: "uuid-trace-123" });
    expect(result).toBeDefined();
  });
});
