import { describe, it, expect, beforeEach, afterEach } from "@jest/globals";
import {
  buildFiscalSpaUrl,
  isCreditNoteDocumentType,
  buildFiscalDocumentViewUrl,
} from "../fiscalSpaUrl";

const OLD_ENV = process.env;

beforeEach(() => {
  process.env = { ...OLD_ENV };
});

afterEach(() => {
  process.env = OLD_ENV;
});

describe("buildFiscalSpaUrl", () => {
  describe("sin FISCAL_SPA_URL configurado", () => {
    it("devuelve /<ruta> cuando la variable de entorno está vacía", () => {
      process.env.FISCAL_SPA_URL = "";
      expect(buildFiscalSpaUrl("facturas")).toBe("/facturas");
    });

    it("elimina la barra inicial de la ruta", () => {
      process.env.FISCAL_SPA_URL = "";
      expect(buildFiscalSpaUrl("/facturas")).toBe("/facturas");
    });

    it("agrega query string cuando se pasan parámetros", () => {
      process.env.FISCAL_SPA_URL = "";
      const params = new URLSearchParams({ uuid: "abc-123" });
      expect(buildFiscalSpaUrl("facturas", params)).toBe(
        "/facturas?uuid=abc-123"
      );
    });
  });

  describe("con FISCAL_SPA_URL usando hash (#)", () => {
    it("construye URL con segmento hash correcto", () => {
      process.env.FISCAL_SPA_URL = "https://app.ejemplo.com/portal#/fiscal";
      expect(buildFiscalSpaUrl("facturas")).toBe(
        "https://app.ejemplo.com/portal#/fiscal/facturas"
      );
    });

    it("elimina barra final del hashPath antes de concatenar", () => {
      process.env.FISCAL_SPA_URL = "https://app.ejemplo.com/portal#/fiscal/";
      expect(buildFiscalSpaUrl("facturas")).toBe(
        "https://app.ejemplo.com/portal#/fiscal/facturas"
      );
    });

    it("agrega query string después del segmento hash", () => {
      process.env.FISCAL_SPA_URL = "https://app.ejemplo.com/portal#/fiscal";
      const params = new URLSearchParams({ idProveedor: "007" });
      expect(buildFiscalSpaUrl("facturas", params)).toBe(
        "https://app.ejemplo.com/portal#/fiscal/facturas?idProveedor=007"
      );
    });

    it("maneja rutas sin hashPath (solo #)", () => {
      process.env.FISCAL_SPA_URL = "https://app.ejemplo.com/portal#";
      expect(buildFiscalSpaUrl("facturas")).toBe(
        "https://app.ejemplo.com/portal#/facturas"
      );
    });
  });

  describe("con FISCAL_SPA_URL sin hash", () => {
    it("construye URL directa sin hash", () => {
      process.env.FISCAL_SPA_URL = "https://app.ejemplo.com/fiscal";
      expect(buildFiscalSpaUrl("facturas")).toBe(
        "https://app.ejemplo.com/fiscal/facturas"
      );
    });

    it("elimina barra final de la base antes de concatenar", () => {
      process.env.FISCAL_SPA_URL = "https://app.ejemplo.com/fiscal/";
      expect(buildFiscalSpaUrl("facturas")).toBe(
        "https://app.ejemplo.com/fiscal/facturas"
      );
    });

    it("incluye query string", () => {
      process.env.FISCAL_SPA_URL = "https://app.ejemplo.com/fiscal";
      const params = new URLSearchParams({ uuid: "xyz", folio: "001" });
      const url = buildFiscalSpaUrl("notas-credito", params);
      expect(url).toContain("notas-credito");
      expect(url).toContain("uuid=xyz");
      expect(url).toContain("folio=001");
    });
  });
});

describe("isCreditNoteDocumentType", () => {
  it('detecta "nota" (case-insensitive)', () => {
    expect(isCreditNoteDocumentType("Nota de Crédito")).toBe(true);
  });

  it('detecta "credito" sin tilde', () => {
    expect(isCreditNoteDocumentType("nota credito")).toBe(true);
  });

  it('detecta "crédito" con tilde', () => {
    expect(isCreditNoteDocumentType("nota crédito")).toBe(true);
  });

  it('detecta "NC" en mayúsculas', () => {
    expect(isCreditNoteDocumentType("NC")).toBe(true);
  });

  it('detecta "nc" en minúsculas', () => {
    expect(isCreditNoteDocumentType("nc")).toBe(true);
  });

  it("devuelve false para 'factura'", () => {
    expect(isCreditNoteDocumentType("factura")).toBe(false);
  });

  it("devuelve false para 'pago'", () => {
    expect(isCreditNoteDocumentType("pago")).toBe(false);
  });

  it("devuelve false cuando documentType es undefined", () => {
    expect(isCreditNoteDocumentType(undefined)).toBe(false);
  });

  it("devuelve false cuando documentType es string vacío", () => {
    expect(isCreditNoteDocumentType("")).toBe(false);
  });
});

describe("buildFiscalDocumentViewUrl", () => {
  beforeEach(() => {
    process.env.FISCAL_SPA_URL = "";
  });

  describe("tipo factura", () => {
    it("usa ruta 'facturas' cuando el tipo no es nota de crédito", () => {
      const url = buildFiscalDocumentViewUrl({ documentType: "factura" });
      expect(url).toBe("/facturas");
    });

    it("incluye idProveedor cuando providerNumber está definido", () => {
      const url = buildFiscalDocumentViewUrl({
        documentType: "factura",
        providerNumber: "P001",
      });
      expect(url).toContain("idProveedor=P001");
    });

    it("incluye uuid en los parámetros", () => {
      const url = buildFiscalDocumentViewUrl({
        documentType: "factura",
        uuid: "550e8400-e29b-41d4-a716-446655440000",
      });
      expect(url).toContain("uuid=550e8400-e29b-41d4-a716-446655440000");
    });

    it("incluye serie y folio en los parámetros", () => {
      const url = buildFiscalDocumentViewUrl({
        documentType: "factura",
        serie: "A",
        folio: "100",
      });
      expect(url).toContain("serie=A");
      expect(url).toContain("folio=100");
    });

    it("usa documentNumber como folio cuando folio no está definido", () => {
      const url = buildFiscalDocumentViewUrl({
        documentType: "factura",
        documentNumber: "200",
      });
      expect(url).toContain("folio=200");
    });

    it("no incluye parámetros vacíos en la URL", () => {
      const url = buildFiscalDocumentViewUrl({
        documentType: "factura",
        providerNumber: "",
        uuid: "  ",
      });
      expect(url).not.toContain("idProveedor");
      expect(url).not.toContain("uuid");
    });
  });

  describe("tipo nota de crédito", () => {
    it("usa ruta 'notas-credito' para tipo 'NC'", () => {
      const url = buildFiscalDocumentViewUrl({ documentType: "NC" });
      expect(url).toBe("/notas-credito");
    });

    it("usa ruta 'notas-credito' para tipo 'Nota de Crédito'", () => {
      const url = buildFiscalDocumentViewUrl({
        documentType: "Nota de Crédito",
      });
      expect(url).toBe("/notas-credito");
    });

    it("combina ruta nota de crédito con parámetros correctamente", () => {
      const url = buildFiscalDocumentViewUrl({
        documentType: "NC",
        providerNumber: "P002",
        uuid: "aaa-bbb",
        serie: "B",
        folio: "50",
      });
      expect(url).toContain("/notas-credito");
      expect(url).toContain("idProveedor=P002");
      expect(url).toContain("uuid=aaa-bbb");
      expect(url).toContain("serie=B");
      expect(url).toContain("folio=50");
    });
  });

  describe("sin documentType", () => {
    it("usa ruta 'facturas' como fallback cuando documentType es undefined", () => {
      const url = buildFiscalDocumentViewUrl({});
      expect(url).toBe("/facturas");
    });
  });
});
