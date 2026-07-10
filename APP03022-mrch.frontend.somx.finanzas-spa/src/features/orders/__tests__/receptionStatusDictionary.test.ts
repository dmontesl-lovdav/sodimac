import { describe, it, expect } from "@jest/globals";
import {
  resolveReceptionStatusFromDictionary,
  receptionStatusDefinedIds,
  RECEPTION_STATUS_DICTIONARY,
} from "../receptionStatusDictionary";

describe("RECEPTION_STATUS_DICTIONARY", () => {
  it("contiene exactamente los ids 0 al 9", () => {
    const keys = Object.keys(RECEPTION_STATUS_DICTIONARY).map(Number);
    expect(keys.sort((a, b) => a - b)).toEqual([0, 1, 2, 3, 4, 5, 6, 7, 8, 9]);
  });

  it("cada entrada tiene shortLabel, description y pillType válidos", () => {
    const validPillTypes = ["success", "warning", "error", "info"];
    for (const entry of Object.values(RECEPTION_STATUS_DICTIONARY)) {
      expect(typeof entry.shortLabel).toBe("string");
      expect(entry.shortLabel.length).toBeGreaterThan(0);
      expect(typeof entry.description).toBe("string");
      expect(entry.description.length).toBeGreaterThan(0);
      expect(validPillTypes).toContain(entry.pillType);
    }
  });
});

describe("resolveReceptionStatusFromDictionary", () => {
  it("id 0 → 'Disponible' con pillType warning", () => {
    const result = resolveReceptionStatusFromDictionary(0);
    expect(result.label).toBe("Disponible");
    expect(result.pillType).toBe("warning");
  });

  it("id 1 → 'Consumida' con pillType success", () => {
    const result = resolveReceptionStatusFromDictionary(1);
    expect(result.label).toBe("Consumida");
    expect(result.pillType).toBe("success");
  });

  it("id 2 → 'Consumida manual' con pillType success", () => {
    const result = resolveReceptionStatusFromDictionary(2);
    expect(result.label).toBe("Consumida manual");
    expect(result.pillType).toBe("success");
  });

  it("id 3 → 'En proceso contable' con pillType warning", () => {
    const result = resolveReceptionStatusFromDictionary(3);
    expect(result.label).toBe("En proceso contable");
    expect(result.pillType).toBe("warning");
  });

  it("id 4 → 'Rechazo contable' con pillType error", () => {
    const result = resolveReceptionStatusFromDictionary(4);
    expect(result.label).toBe("Rechazo contable");
    expect(result.pillType).toBe("error");
  });

  it("id 5 → 'En proceso de pago' con pillType warning", () => {
    const result = resolveReceptionStatusFromDictionary(5);
    expect(result.label).toBe("En proceso de pago");
    expect(result.pillType).toBe("warning");
  });

  it("id 6 → 'Pagada' con pillType success", () => {
    const result = resolveReceptionStatusFromDictionary(6);
    expect(result.label).toBe("Pagada");
    expect(result.pillType).toBe("success");
  });

  it("id 7 → 'Cancelada' con pillType warning", () => {
    const result = resolveReceptionStatusFromDictionary(7);
    expect(result.label).toBe("Cancelada");
    expect(result.pillType).toBe("warning");
  });

  it("id 8 → 'Borrado' con pillType error", () => {
    const result = resolveReceptionStatusFromDictionary(8);
    expect(result.label).toBe("Borrado");
    expect(result.pillType).toBe("error");
  });

  it("id 9 → 'En validación' con pillType warning", () => {
    const result = resolveReceptionStatusFromDictionary(9);
    expect(result.label).toBe("En validación");
    expect(result.pillType).toBe("warning");
  });

  describe("estatus desconocido", () => {
    it("devuelve 'Desconocido' con pillType error para id -1", () => {
      const result = resolveReceptionStatusFromDictionary(-1);
      expect(result.label).toBe("Desconocido");
      expect(result.pillType).toBe("error");
    });

    it("devuelve 'Desconocido' con pillType error para id 99", () => {
      const result = resolveReceptionStatusFromDictionary(99);
      expect(result.label).toBe("Desconocido");
      expect(result.pillType).toBe("error");
    });

    it("acepta string numérico gracias a Number()", () => {
      const result = resolveReceptionStatusFromDictionary("1" as any);
      expect(result.label).toBe("Consumida");
    });
  });
});

describe("receptionStatusDefinedIds", () => {
  it("devuelve un array con los 10 ids definidos", () => {
    const ids = receptionStatusDefinedIds();
    expect(ids).toHaveLength(10);
  });

  it("los ids están ordenados de menor a mayor", () => {
    const ids = receptionStatusDefinedIds();
    expect(ids).toEqual([0, 1, 2, 3, 4, 5, 6, 7, 8, 9]);
  });

  it("devuelve una copia (no muta el array interno)", () => {
    const ids1 = receptionStatusDefinedIds();
    const ids2 = receptionStatusDefinedIds();
    ids1.push(999);
    expect(ids2).toHaveLength(10);
    expect(ids2).not.toContain(999);
  });
});
