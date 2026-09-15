/**
 * @jest-environment node
 */
import { describe, expect, it } from "@jest/globals";
import { canViewAccounting, INVOICE_STATUS_VER_CONTABILIDAD } from "../accountingStatus";
import {
  buildAccountingSearchParams,
  parseAccountingSearchParams,
} from "../accountingQuery";
import { toAccountingDetailRow, unwrapSapDocumentList } from "../accountingApi";

describe("canViewAccounting", () => {
  it("habilita estatus del tren contable", () => {
    expect(canViewAccounting(15, INVOICE_STATUS_VER_CONTABILIDAD)).toBe(true);
    expect(canViewAccounting(9, INVOICE_STATUS_VER_CONTABILIDAD)).toBe(true);
  });

  it("deshabilita cancelada y sin estatus", () => {
    expect(canViewAccounting(20, INVOICE_STATUS_VER_CONTABILIDAD)).toBe(false);
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

describe("unwrapSapDocumentList", () => {
  it("acepta arreglo directo o envuelto en data", () => {
    expect(unwrapSapDocumentList([{ documentNumber: "1" }])).toHaveLength(1);
    expect(unwrapSapDocumentList({ data: [{ documentNumber: "2" }] })).toHaveLength(1);
    expect(unwrapSapDocumentList(null)).toEqual([]);
  });
});

describe("toAccountingDetailRow", () => {
  it("mapea campos SAP a las columnas de Ver Contabilidad", () => {
    expect(
      toAccountingDetailRow({
        documentNumber: "4500",
        docSap: "5100",
        message: "ok",
        createdAt: "2026-09-01T12:00:00.000Z",
      })
    ).toEqual({
      documentNumber: "4500",
      sapDocument: "5100",
      sapMessage: "ok",
      accountingDate: "2026-09-01T12:00:00.000Z",
    });
  });
});
