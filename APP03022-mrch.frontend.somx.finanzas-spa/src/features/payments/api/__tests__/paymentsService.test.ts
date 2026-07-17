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
});
