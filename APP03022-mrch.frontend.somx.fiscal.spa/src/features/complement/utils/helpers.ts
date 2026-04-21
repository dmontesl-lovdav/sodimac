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
        const local = (current.localName || current.tagName || "").split(":").pop();
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
    const pago = pagos ? findByLocalName(pagos, "Pago") : null;
    const totales = pago ? findByLocalName(pago, "Totales") : null;
    const monto = totales?.getAttribute("Monto") ?? totales?.getAttribute("Total") ?? pago?.getAttribute("Monto") ?? "";
    return {
      uuid: getAttr(timbre, "UUID") || (doc.querySelector("*[UUID]")?.getAttribute("UUID") ?? ""),
      rfcEmisor: (getAttr(emisor ?? null, "Rfc") ?? "") || (getAttr(comprobante, "EmisorRfc") ?? ""),
      nombreEmisor: (getAttr(emisor ?? null, "Nombre") ?? "") || (getAttr(emisor ?? null, "RazonSocial") ?? "") || (getAttr(comprobante, "EmisorNombre") ?? ""),
      monto: monto ? Number(monto).toLocaleString("es-MX", { minimumFractionDigits: 2 }) : "",
      fechaTimbrado: getAttr(timbre, "FechaTimbrado") ?? "",
    };
  } catch(err) {
    console.error("Error parsing XML:", err);
    return null;
  }
}



export function toPaymentHeader(row: ComplementPayment): PaymentHeaderData {
  const anio = row.paymentDate ? String(new Date(row.paymentDate).getFullYear()) : row.createdAt ? String(new Date(row.createdAt).getFullYear()) : "--";
  return {
    idProveedor: row.issuerRfc ?? "--",
    nombreProveedor: row.issuerName ?? "--",
    referenciaPago: row.paymentsUuid ?? "--",
    anioPagos: anio,
    moneda: "MXN",
    monto: row.totalAmount != null ? formatAmount(row.totalAmount) : "--",
    estatus: row.statusDescription ?? "--",
    fechaRegistro: row.createdAt ? formatDate(row.createdAt) : "--",
  };
}
