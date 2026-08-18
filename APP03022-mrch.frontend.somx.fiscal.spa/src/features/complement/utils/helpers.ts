import { formatAmount, formatDate } from "@/utils/utils";
import type { ComplementPayment, PaymentHeaderData, XmlComplementPreview } from "../interfaces";

export function parseComplementXml(xmlText: string): XmlComplementPreview | null {
  try {
    const parser = new DOMParser();
    const doc = parser.parseFromString(xmlText, "text/xml");

    const parserError = doc.getElementsByTagName("parsererror")[0];
    if (parserError) return null;

    const findByLocalName = (root: Document | Element, name: string): Element | null => {
      const elements = root.getElementsByTagName("*");
      for (let i = 0; i < elements.length; i += 1) {
        const current = elements[i];
        const local = (current.localName ?? current.tagName ?? "").split(":").pop();
        if (local === name) return current;
      }
      return null;
    };

    const getAttr = (el: Element | null, name: string): string =>
      el?.getAttribute(name) ?? "";

    const comprobante = findByLocalName(doc, "Comprobante");
    const emisor = comprobante ? findByLocalName(comprobante, "Emisor") : null;
    const timbre = findByLocalName(doc, "TimbreFiscalDigital");
    const complemento = findByLocalName(doc, "Complemento");
    const pagos = complemento ? findByLocalName(complemento, "Pagos") : null;
    // En Pagos 2.0, Totales es hermano de Pago (no hijo)
    const totales = pagos ? findByLocalName(pagos, "Totales") : null;
    const pago = pagos ? findByLocalName(pagos, "Pago") : null;

    const montoRaw =
      getAttr(pago, "Monto") ||
      getAttr(totales, "MontoTotalPagos") ||
      getAttr(totales, "Monto") ||
      getAttr(totales, "Total") ||
      "";

    const fechaPagoRaw = getAttr(pago, "FechaPago");
    const fechaTimbradoRaw = getAttr(timbre, "FechaTimbrado");

    return {
      uuid:
        [getAttr(timbre, "UUID"), doc.querySelector("*[UUID]")?.getAttribute("UUID") ?? ""].find(
          (v) => v !== ""
        ) ?? "",
      rfcEmisor:
        [getAttr(emisor, "Rfc"), getAttr(comprobante, "EmisorRfc")].find((v) => v !== "") ?? "",
      nombreEmisor:
        [
          getAttr(emisor, "Nombre"),
          getAttr(emisor, "RazonSocial"),
          getAttr(comprobante, "EmisorNombre"),
        ].find((v) => v !== "") ?? "",
      serie: getAttr(comprobante, "Serie"),
      folio: getAttr(comprobante, "Folio"),
      monto: montoRaw
        ? Number(montoRaw).toLocaleString("es-MX", { minimumFractionDigits: 2 })
        : "",
      tipoComprobante:
        getAttr(comprobante, "TipoDeComprobante") ||
        getAttr(comprobante, "tipoDeComprobante") ||
        "",
      formaDePagoP: getAttr(pago, "FormaDePagoP"),
      fechaPago: fechaPagoRaw ? formatDate(fechaPagoRaw) : "",
      fechaTimbrado: fechaTimbradoRaw ? formatDate(fechaTimbradoRaw) : "",
    };
  } catch {
    return null;
  }
}

function resolvePaymentYear(row: ComplementPayment): string {
  if (row.paymentDate) {
    return String(new Date(row.paymentDate).getFullYear());
  }
  if (row.createdAt) {
    return String(new Date(row.createdAt).getFullYear());
  }
  return "--";
}

export function toPaymentHeader(row: ComplementPayment): PaymentHeaderData {
  return {
    uuid: row.fiscalUuid ?? row.paymentsUuid ?? "--",
    rfcProveedor: row.issuerRfc ?? "--",
    idProveedor: row.issuerRfc ?? "--",
    nombreProveedor: row.issuerName ?? "--",
    referenciaPago: row.paymentsUuid ?? "--",
    anioPagos: resolvePaymentYear(row),
    moneda: "MXN",
    monto: row.totalAmount != null ? formatAmount(row.totalAmount) : "--",
    status: row.statusDescription ?? "--",
    fechaRegistro: row.createdAt ? formatDate(row.createdAt) : "--",
  };
}
