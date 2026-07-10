import type { CreditNoteXmlData } from "./types";

export function parseValidatedXml(data: unknown, providers: any): CreditNoteXmlData | null {
  if (!data || typeof data !== "object") return null;

  const root = data as Record<string, unknown>;
  const emisor = root.emisor as Record<string, unknown> | undefined;
  const comprobante = root.comprobante as Record<string, unknown> | undefined;
  const receptor = root.receptor as Record<string, unknown> | undefined;
  const timbre = root.timbreFiscalDigital as Record<string, unknown> | undefined;
  if (!emisor || !comprobante || !timbre) return null;

  const supplierNumber = providers?.find((p: any) => p.rfc == emisor.rfc)?.idProveedor;
  return {
    rfcEmisor: String(emisor.rfc ?? ""),
    nombreProveedor: String(emisor.nombre ?? ""),
    numeroProveedor: String(supplierNumber ?? ""),
    serie: String(comprobante.serie ?? ""),
    folio: String(comprobante.folio ?? ""),
    monto: String(comprobante.subTotal ?? comprobante.total ?? ""),
    fechaTimbrado: String(comprobante.fecha ?? ""),
    usoCfdi: String(receptor?.usoCFDI ?? ""),
    tipoDeComprobante: String(comprobante.tipoDeComprobante ?? ""),
    uuid: String(timbre.uuid ?? ""),
    uuidRelacionado: String(comprobante.uuidRelacionado ?? ""),
    formaPago: String(comprobante.formaPago ?? ""),
  };
}

export function getXmlValidationMessage(data: unknown): {
  ok: boolean;
  message: string;
} {
  if (!data || typeof data !== "object") {
    return { ok: false, message: "El archivo XML no es válido." };
  }
  const metadatos = (data as Record<string, unknown>).metadatos as
    | Record<string, unknown>
    | undefined;
  const ok = metadatos?.estado === "SUCCESS";
  return {
    ok,
    message: String(metadatos?.mensaje ?? (ok ? "XML validado correctamente." : "El archivo XML no es válido.")),
  };
}
