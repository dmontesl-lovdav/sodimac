import { describe, it, expect } from "@jest/globals";
import { paymentsService } from "../paymentsService";
import type { PaymentRecord } from "../../interfaces";

describe("paymentsService.exportPaymentsCsv", () => {
  it("genera blob CSV con BOM", () => {
    const rows: PaymentRecord[] = [
      {
        idPago: "1",
        documentNumber: "D",
        documentReference: "REF",
        providerNumber: "10",
        providerName: "Demo",
        currency: "MXN",
        amount: 100,
        documentType: "RE",
        sapDocument: "S",
        paymentDate: "2026-01-01",
        paymentYear: "2026",
        status: "Pendiente",
        statusId: 0,
        createdAt: "2026-01-01",
        updatedAt: "",
      },
    ];
    const blob = paymentsService.exportPaymentsCsv(rows);
    expect(blob).toBeInstanceOf(Blob);
    expect(blob.type).toContain("csv");
  });

  it("usa las mismas columnas visibles del listado de pagos", async () => {
    const rows: PaymentRecord[] = [
      {
        idPago: "1",
        documentNumber: "D",
        documentReference: "REF",
        providerNumber: "10",
        providerName: "Demo",
        currency: "MXN",
        amount: 100,
        documentType: "RE",
        sapDocument: "S",
        paymentDate: "2026-01-01",
        paymentYear: "2026",
        status: "Pendiente",
        statusId: 0,
        createdAt: "01/01/2026",
        updatedAt: "02/01/2026",
      },
    ];
    const blob = paymentsService.exportPaymentsCsv(rows, [
      {
        supplierNumber: "10",
        businessName: "Proveedor Demo",
        supplierType: { code: "TRANSPORTE" },
      },
    ]);
    const text = await new Promise<string>((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(String(reader.result ?? ""));
      reader.onerror = () => reject(reader.error);
      reader.readAsText(blob);
    });
    const headerLine = text.split(/\r?\n/)[0] ?? "";

    expect(headerLine).toContain("Referencia Pago");
    expect(headerLine).toContain("Año Pago");
    expect(headerLine).toContain("Tipo Proveedor");
    expect(headerLine).toContain("Fecha Registro");
    expect(headerLine).not.toContain("Fecha Pago");
    expect(headerLine).not.toContain("Fecha Actualización");
    expect(text).toContain("Transporte");
    expect(text).toContain("Pendiente de complemento");
  });
});
