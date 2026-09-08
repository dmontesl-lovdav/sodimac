/**
 * @jest-environment node
 */
import { describe, expect, it } from "@jest/globals";
import { canViewAccounting, INVOICE_STATUS_VER_CONTABILIDAD } from "../accountingStatus";
import {
  buildAccountingSearchParams,
  parseAccountingSearchParams,
} from "../accountingQuery";

describe("canViewAccounting", () => {
  it("habilita estatus del tren contable", () => {
    expect(canViewAccounting(15, INVOICE_STATUS_VER_CONTABILIDAD)).toBe(true);
    expect(canViewAccounting(9, INVOICE_STATUS_VER_CONTABILIDAD)).toBe(true);
  });

  it("deshabilita cancelada y estatus previos al flujo SAP", () => {
    expect(canViewAccounting(20, INVOICE_STATUS_VER_CONTABILIDAD)).toBe(false);
    expect(canViewAccounting(2, INVOICE_STATUS_VER_CONTABILIDAD)).toBe(false);
    expect(canViewAccounting(null, INVOICE_STATUS_VER_CONTABILIDAD)).toBe(false);
  });
});

describe("accountingQuery", () => {
  it("serializa y recupera la cabecera y el detalle", () => {
    const qs = buildAccountingSearchParams({
      series: "A",
      folio: "1",
      fiscalUuid: "uuid-1",
      subtotal: 10,
      noOrdenCompra: "OC1",
      noRecepcion: "R1",
      numeroProveedor: "99",
      supplierName: "Prov",
      statusName: "Pendiente de Pago",
      documentNumber: "DOC-1",
      sapDocument: "SAP-1",
      sapMessage: "OK",
      accountingDate: "2026-09-01",
    });
    const parsed = parseAccountingSearchParams(`?${qs.toString()}`);
    expect(parsed.series).toBe("A");
    expect(parsed.sapDocument).toBe("SAP-1");
    expect(parsed.accountingDate).toBe("2026-09-01");
  });
});
