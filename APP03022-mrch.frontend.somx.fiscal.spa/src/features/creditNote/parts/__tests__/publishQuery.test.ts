import { describe, it, expect } from "@jest/globals";
import { parsePublishQuery, isCommercialDiscountFlow } from "../publishQuery";

describe("parsePublishQuery", () => {
  describe("query string vacío o sin parámetros relevantes", () => {
    it("devuelve strings vacíos cuando search es vacío", () => {
      const result = parsePublishQuery("");
      expect(result.supplierNumber).toBe("");
      expect(result.documentNumber).toBe("");
    });

    it("devuelve strings vacíos cuando no hay parámetros conocidos", () => {
      const result = parsePublishQuery("?foo=bar&baz=qux");
      expect(result.supplierNumber).toBe("");
      expect(result.documentNumber).toBe("");
    });
  });

  describe("supplierNumber", () => {
    it("extrae supplierNumber del query string", () => {
      const result = parsePublishQuery("?supplierNumber=P001");
      expect(result.supplierNumber).toBe("P001");
    });

    it("extrae supplierNumber junto a otros parámetros", () => {
      const result = parsePublishQuery("?supplierNumber=PROV123&otherParam=X");
      expect(result.supplierNumber).toBe("PROV123");
    });

    it("devuelve string vacío cuando supplierNumber no está en la URL", () => {
      const result = parsePublishQuery("?documentNumber=DOC001");
      expect(result.supplierNumber).toBe("");
    });
  });

  describe("documentNumber", () => {
    it("extrae documentNumber del query string", () => {
      const result = parsePublishQuery("?documentNumber=FAC-001");
      expect(result.documentNumber).toBe("FAC-001");
    });

    it("extrae documentNumber junto a supplierNumber", () => {
      const result = parsePublishQuery(
        "?supplierNumber=P002&documentNumber=DOC-99"
      );
      expect(result.supplierNumber).toBe("P002");
      expect(result.documentNumber).toBe("DOC-99");
    });

    it("devuelve string vacío cuando documentNumber no está en la URL", () => {
      const result = parsePublishQuery("?supplierNumber=P003");
      expect(result.documentNumber).toBe("");
    });
  });

  describe("valores con caracteres especiales", () => {
    it("decodifica caracteres URL-encoded", () => {
      const result = parsePublishQuery(
        "?supplierNumber=P%20001&documentNumber=DOC%2F99"
      );
      expect(result.supplierNumber).toBe("P 001");
      expect(result.documentNumber).toBe("DOC/99");
    });
  });
});

describe("isCommercialDiscountFlow", () => {
  it("devuelve true cuando documentNumber tiene contenido", () => {
    expect(isCommercialDiscountFlow({ supplierNumber: "P001", documentNumber: "DOC-1" })).toBe(true);
  });

  it("devuelve false cuando documentNumber es string vacío", () => {
    expect(isCommercialDiscountFlow({ supplierNumber: "P001", documentNumber: "" })).toBe(false);
  });

  it("devuelve false cuando documentNumber es solo espacios", () => {
    expect(isCommercialDiscountFlow({ supplierNumber: "P001", documentNumber: "   " })).toBe(false);
  });

  it("devuelve true incluso cuando supplierNumber está vacío si documentNumber tiene valor", () => {
    expect(isCommercialDiscountFlow({ supplierNumber: "", documentNumber: "DOC-999" })).toBe(true);
  });

  it("devuelve false cuando ambos campos están vacíos", () => {
    expect(isCommercialDiscountFlow({ supplierNumber: "", documentNumber: "" })).toBe(false);
  });
});
