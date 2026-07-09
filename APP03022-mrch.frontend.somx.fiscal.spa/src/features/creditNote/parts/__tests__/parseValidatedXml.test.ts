import { describe, it, expect } from "@jest/globals";
import { parseValidatedXml, getXmlValidationMessage } from "../parseValidatedXml";

const BASE_XML = {
  emisor: { rfc: "ABC123456789", nombre: "Proveedor S.A." },
  comprobante: {
    serie: "A",
    folio: "100",
    subTotal: "5000.00",
    fecha: "2024-01-15",
    tipoDeComprobante: "E",
    uuidRelacionado: "uuid-rel-001",
    formaPago: "03",
  },
  receptor: { usoCFDI: "G03" },
  timbreFiscalDigital: { uuid: "timbre-uuid-001" },
};

// ---------------------------------------------------------------------------
// parseValidatedXml
// ---------------------------------------------------------------------------

describe("parseValidatedXml", () => {
  describe("entradas inválidas → null", () => {
    it("null → null", () => {
      expect(parseValidatedXml(null)).toBeNull();
    });

    it("undefined → null", () => {
      expect(parseValidatedXml(undefined)).toBeNull();
    });

    it("string → null", () => {
      expect(parseValidatedXml("<xml/>")).toBeNull();
    });

    it("número → null", () => {
      expect(parseValidatedXml(42)).toBeNull();
    });

    it("array → null", () => {
      expect(parseValidatedXml([])).toBeNull();
    });
  });

  describe("objeto sin los campos requeridos → null", () => {
    it("sin emisor → null", () => {
      const { emisor: _, ...sinEmisor } = BASE_XML;
      expect(parseValidatedXml(sinEmisor)).toBeNull();
    });

    it("sin comprobante → null", () => {
      const { comprobante: _, ...sinComprobante } = BASE_XML;
      expect(parseValidatedXml(sinComprobante)).toBeNull();
    });

    it("sin timbreFiscalDigital → null", () => {
      const { timbreFiscalDigital: _, ...sinTimbre } = BASE_XML;
      expect(parseValidatedXml(sinTimbre)).toBeNull();
    });

    it("objeto vacío → null", () => {
      expect(parseValidatedXml({})).toBeNull();
    });
  });

  describe("datos válidos", () => {
    it("extrae rfcEmisor", () => {
      const result = parseValidatedXml(BASE_XML);
      expect(result?.rfcEmisor).toBe("ABC123456789");
    });

    it("extrae nombreProveedor", () => {
      const result = parseValidatedXml(BASE_XML);
      expect(result?.nombreProveedor).toBe("Proveedor S.A.");
    });

    it("extrae serie y folio del comprobante", () => {
      const result = parseValidatedXml(BASE_XML);
      expect(result?.serie).toBe("A");
      expect(result?.folio).toBe("100");
    });

    it("extrae monto desde subTotal", () => {
      const result = parseValidatedXml(BASE_XML);
      expect(result?.monto).toBe("5000.00");
    });

    it("usa total cuando subTotal no está presente", () => {
      const data = {
        ...BASE_XML,
        comprobante: { ...BASE_XML.comprobante, subTotal: undefined, total: "4500.00" },
      };
      const result = parseValidatedXml(data);
      expect(result?.monto).toBe("4500.00");
    });

    it("extrae fechaTimbrado", () => {
      const result = parseValidatedXml(BASE_XML);
      expect(result?.fechaTimbrado).toBe("2024-01-15");
    });

    it("extrae usoCfdi del receptor", () => {
      const result = parseValidatedXml(BASE_XML);
      expect(result?.usoCfdi).toBe("G03");
    });

    it("extrae tipoDeComprobante", () => {
      const result = parseValidatedXml(BASE_XML);
      expect(result?.tipoDeComprobante).toBe("E");
    });

    it("extrae uuid del timbre", () => {
      const result = parseValidatedXml(BASE_XML);
      expect(result?.uuid).toBe("timbre-uuid-001");
    });

    it("extrae uuidRelacionado del comprobante", () => {
      const result = parseValidatedXml(BASE_XML);
      expect(result?.uuidRelacionado).toBe("uuid-rel-001");
    });

    it("extrae formaPago", () => {
      const result = parseValidatedXml(BASE_XML);
      expect(result?.formaPago).toBe("03");
    });

    it("los campos opcionales ausentes se convierten a string vacío", () => {
      const data = {
        ...BASE_XML,
        receptor: undefined,
        comprobante: {
          ...BASE_XML.comprobante,
          uuidRelacionado: undefined,
          formaPago: undefined,
        },
      };
      const result = parseValidatedXml(data);
      expect(result?.usoCfdi).toBe("");
      expect(result?.uuidRelacionado).toBe("");
      expect(result?.formaPago).toBe("");
    });
  });
});

// ---------------------------------------------------------------------------
// getXmlValidationMessage
// ---------------------------------------------------------------------------

describe("getXmlValidationMessage", () => {
  describe("entradas inválidas → ok: false con mensaje genérico", () => {
    it("null → ok false", () => {
      const r = getXmlValidationMessage(null);
      expect(r.ok).toBe(false);
      expect(r.message).toBe("El archivo XML no es válido.");
    });

    it("undefined → ok false", () => {
      const r = getXmlValidationMessage(undefined);
      expect(r.ok).toBe(false);
    });

    it("string → ok false", () => {
      const r = getXmlValidationMessage("texto");
      expect(r.ok).toBe(false);
    });

    it("array → ok false", () => {
      const r = getXmlValidationMessage([]);
      expect(r.ok).toBe(false);
    });

    it("objeto sin metadatos → ok false", () => {
      const r = getXmlValidationMessage({ data: "algo" });
      expect(r.ok).toBe(false);
      expect(r.message).toBe("El archivo XML no es válido.");
    });
  });

  describe("metadatos con estado SUCCESS", () => {
    it("estado SUCCESS → ok true", () => {
      const r = getXmlValidationMessage({
        metadatos: { estado: "SUCCESS", mensaje: "XML validado." },
      });
      expect(r.ok).toBe(true);
    });

    it("devuelve el mensaje del metadato cuando está disponible", () => {
      const r = getXmlValidationMessage({
        metadatos: { estado: "SUCCESS", mensaje: "Procesado correctamente." },
      });
      expect(r.message).toBe("Procesado correctamente.");
    });

    it("usa fallback 'XML validado correctamente.' cuando mensaje está ausente y ok es true", () => {
      const r = getXmlValidationMessage({
        metadatos: { estado: "SUCCESS" },
      });
      expect(r.ok).toBe(true);
      expect(r.message).toBe("XML validado correctamente.");
    });
  });

  describe("metadatos con estado diferente a SUCCESS", () => {
    it("estado FAIL → ok false", () => {
      const r = getXmlValidationMessage({
        metadatos: { estado: "FAIL", mensaje: "Estructura inválida." },
      });
      expect(r.ok).toBe(false);
    });

    it("devuelve el mensaje del metadato cuando ok es false", () => {
      const r = getXmlValidationMessage({
        metadatos: { estado: "ERROR", mensaje: "RFC no corresponde." },
      });
      expect(r.message).toBe("RFC no corresponde.");
    });

    it("usa fallback 'El archivo XML no es válido.' cuando mensaje está ausente y ok es false", () => {
      const r = getXmlValidationMessage({
        metadatos: { estado: "FAIL" },
      });
      expect(r.ok).toBe(false);
      expect(r.message).toBe("El archivo XML no es válido.");
    });
  });
});
