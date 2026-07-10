import { describe, it, expect } from "@jest/globals";
import {
  resolvePaymentStatusDisplay,
  canRelatePaymentComplement,
} from "../paymentStatusDisplay";

describe("resolvePaymentStatusDisplay", () => {
  describe("statusId conocidos", () => {
    it("id 0 → tipo warning con label 'Pendiente de complemento'", () => {
      const result = resolvePaymentStatusDisplay(0);
      expect(result.type).toBe("warning");
      expect(result.label).toBe("Pendiente de complemento");
    });

    it("id 1 → tipo success con label 'Complemento relacionado'", () => {
      const result = resolvePaymentStatusDisplay(1);
      expect(result.type).toBe("success");
      expect(result.label).toBe("Complemento relacionado");
    });

    it("id 2 → tipo error con label 'Pago cancelado'", () => {
      const result = resolvePaymentStatusDisplay(2);
      expect(result.type).toBe("error");
      expect(result.label).toBe("Pago cancelado");
    });
  });

  describe("statusId desconocido", () => {
    it("id 99 → tipo warning y label = string del id", () => {
      const result = resolvePaymentStatusDisplay(99);
      expect(result.type).toBe("warning");
      expect(result.label).toBe("99");
    });

    it("id -1 → tipo warning y label = string del id", () => {
      const result = resolvePaymentStatusDisplay(-1);
      expect(result.type).toBe("warning");
      expect(result.label).toBe("-1");
    });

    it("id 3 → tipo warning (no es 1 ni 2)", () => {
      const result = resolvePaymentStatusDisplay(3);
      expect(result.type).toBe("warning");
    });
  });

  describe("invariantes del resultado", () => {
    it("siempre devuelve un objeto con label y type", () => {
      [0, 1, 2, 10, 100].forEach((id) => {
        const result = resolvePaymentStatusDisplay(id);
        expect(typeof result.label).toBe("string");
        expect(["success", "warning", "error", "info"]).toContain(result.type);
      });
    });
  });
});

describe("canRelatePaymentComplement", () => {
  describe("basado en statusId", () => {
    it("devuelve true cuando statusId es exactamente 0", () => {
      expect(canRelatePaymentComplement({ statusId: 0 })).toBe(true);
    });

    it("devuelve false cuando statusId es 1 sin estado pendiente", () => {
      expect(canRelatePaymentComplement({ statusId: 1, status: "Completado" })).toBe(
        false
      );
    });

    it("devuelve false cuando statusId es 2 sin estado pendiente", () => {
      expect(canRelatePaymentComplement({ statusId: 2, status: "Cancelado" })).toBe(
        false
      );
    });
  });

  describe("basado en el string status", () => {
    it("devuelve true cuando status es 'Pendiente de complemento'", () => {
      expect(
        canRelatePaymentComplement({ statusId: 5, status: "Pendiente de complemento" })
      ).toBe(true);
    });

    it("devuelve true cuando status contiene 'pendiente' en minúsculas", () => {
      expect(
        canRelatePaymentComplement({ statusId: 5, status: "pendiente" })
      ).toBe(true);
    });

    it("devuelve true cuando status contiene 'PENDIENTE' en mayúsculas", () => {
      expect(
        canRelatePaymentComplement({ statusId: 5, status: "PENDIENTE" })
      ).toBe(true);
    });

    it("devuelve false cuando status no contiene 'pendiente'", () => {
      expect(
        canRelatePaymentComplement({ statusId: 5, status: "Pagado" })
      ).toBe(false);
    });

    it("devuelve false cuando status es string vacío y statusId no es 0", () => {
      expect(canRelatePaymentComplement({ statusId: 1, status: "" })).toBe(
        false
      );
    });
  });

  describe("sin campos opcionales", () => {
    it("devuelve false cuando no hay statusId ni status", () => {
      expect(canRelatePaymentComplement({})).toBe(false);
    });

    it("devuelve false cuando solo hay status vacío y statusId undefined", () => {
      expect(canRelatePaymentComplement({ status: "" })).toBe(false);
    });

    it("devuelve false cuando status es undefined y statusId no es 0", () => {
      expect(canRelatePaymentComplement({ statusId: 1 })).toBe(false);
    });
  });
});
