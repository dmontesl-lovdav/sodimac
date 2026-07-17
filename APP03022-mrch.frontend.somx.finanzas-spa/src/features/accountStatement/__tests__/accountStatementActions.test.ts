import { describe, it, expect } from "@jest/globals";
import {
  ACCOUNT_STATEMENT_STATUS,
  resolveAccountStatementStatus,
  canConfirmAccountStatement,
  canRejectAccountStatement,
  withAccountStatementStatus,
} from "../accountStatementActions";
import type { AccountStatementRecord } from "../interfaces";

const baseRow = (overrides: Partial<AccountStatementRecord> = {}): AccountStatementRecord =>
  ({
    month: 1,
    year: 2026,
    vendorNumber: "1",
    vendorName: "Demo",
    status: ACCOUNT_STATEMENT_STATUS.PUBLISHED,
    statusLabel: "Publicado",
    ...overrides,
  }) as AccountStatementRecord;

describe("accountStatementActions", () => {
  it("resolveAccountStatementStatus usa status numérico", () => {
    expect(resolveAccountStatementStatus(baseRow({ status: 3 }))).toBe(3);
  });

  it("resolveAccountStatementStatus usa statusLabel", () => {
    expect(
      resolveAccountStatementStatus(
        baseRow({ status: undefined as unknown as number, statusLabel: "Rechazado" })
      )
    ).toBe(ACCOUNT_STATEMENT_STATUS.REJECTED);
  });

  it("resolveAccountStatementStatus retorna null sin datos", () => {
    expect(
      resolveAccountStatementStatus(
        baseRow({ status: undefined as unknown as number, statusLabel: undefined })
      )
    ).toBeNull();
  });

  it("canConfirm / canReject en estatus accionables", () => {
    expect(canConfirmAccountStatement(baseRow({ status: 1 }))).toBe(true);
    expect(canRejectAccountStatement(baseRow({ status: 2 }))).toBe(true);
    expect(canConfirmAccountStatement(baseRow({ status: 5 }))).toBe(true);
  });

  it("canConfirm / canReject false en revisado", () => {
    expect(canConfirmAccountStatement(baseRow({ status: 3 }))).toBe(false);
    expect(canRejectAccountStatement(baseRow({ status: 4 }))).toBe(false);
  });

  it("withAccountStatementStatus actualiza fila", () => {
    const updated = withAccountStatementStatus(
      baseRow({ reviewedAt: "old" }),
      3,
      "Revisado",
      "2026-01-01"
    );
    expect(updated.status).toBe(3);
    expect(updated.statusLabel).toBe("Revisado");
    expect(updated.reviewedAt).toBe("2026-01-01");
  });

  it("withAccountStatementStatus conserva reviewedAt si no se pasa", () => {
    const updated = withAccountStatementStatus(
      baseRow({ reviewedAt: "keep" }),
      3,
      "Revisado"
    );
    expect(updated.reviewedAt).toBe("keep");
  });
});
