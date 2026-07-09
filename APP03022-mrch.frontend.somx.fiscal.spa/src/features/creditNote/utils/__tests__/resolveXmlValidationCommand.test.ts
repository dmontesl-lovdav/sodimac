import { describe, it, expect } from "@jest/globals";
import { resolveXmlValidationCommand } from "../resolveXmlValidationCommand";
import type { CreditNoteXmlData } from "../../parts/types";

const BASE: CreditNoteXmlData = {
  rfcEmisor: "RFC123",
  nombreProveedor: "Proveedor SA",
  serie: "A",
  folio: "1",
  monto: "1000",
  fechaTimbrado: "2024-01-01",
  usoCfdi: "G01",
  tipoDeComprobante: "E",
  uuid: "uuid-001",
  uuidRelacionado: "uuid-factura",
  formaPago: "01",
};

describe("resolveXmlValidationCommand", () => {
  it("devuelve invalid cuando tipoDeComprobante no es E", () => {
    const parsed: CreditNoteXmlData = { ...BASE, tipoDeComprobante: "I" };
    const result = resolveXmlValidationCommand({}, parsed);
    expect(result.isValid).toBe(false);
    expect(result.dataMsg).toBe("");
    expect(result.relatedInvoiceUuid).toBe("");
    expect(result.alert).toContain("nota de crédito válida");
  });

  it("devuelve valid con mensaje cuando validación es exitosa y hay uuidRelacionado", () => {
    const data = { metadatos: { estado: "SUCCESS", mensaje: "XML correcto" } };
    const result = resolveXmlValidationCommand(data, BASE);
    expect(result.isValid).toBe(true);
    expect(result.dataMsg).toBe("XML correcto");
    expect(result.relatedInvoiceUuid).toBe("uuid-factura");
    expect(result.alert).toBeNull();
  });

  it("alerta cuando validación es exitosa pero uuidRelacionado está vacío", () => {
    const data = { metadatos: { estado: "SUCCESS", mensaje: "OK" } };
    const parsed: CreditNoteXmlData = { ...BASE, uuidRelacionado: "   " };
    const result = resolveXmlValidationCommand(data, parsed);
    expect(result.isValid).toBe(true);
    expect(result.relatedInvoiceUuid).toBe("   ");
    expect(result.alert).toContain("factura relacionada");
  });

  it("devuelve invalid con mensaje de error cuando validación falla", () => {
    const data = { metadatos: { estado: "ERROR", mensaje: "XML inválido" } };
    const result = resolveXmlValidationCommand(data, BASE);
    expect(result.isValid).toBe(false);
    expect(result.dataMsg).toBe("");
    expect(result.alert).toBe("XML inválido");
  });

  it("usa mensaje por defecto cuando metadatos no tiene mensaje", () => {
    const data = { metadatos: { estado: "ERROR" } };
    const result = resolveXmlValidationCommand(data, BASE);
    expect(result.isValid).toBe(false);
    expect(result.alert).toBe("El archivo XML no es válido.");
  });

  it("devuelve invalid cuando data es null", () => {
    const result = resolveXmlValidationCommand(null, BASE);
    expect(result.isValid).toBe(false);
    expect(result.alert).toBe("El archivo XML no es válido.");
  });
});
