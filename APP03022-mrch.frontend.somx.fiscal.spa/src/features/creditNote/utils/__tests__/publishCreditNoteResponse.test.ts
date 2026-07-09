import { describe, it, expect } from "@jest/globals";
import {
  normalizePublishCreditNoteResponse,
  resolvePublishCreditNoteOutcome,
  FISCAL_MESSAGE_ID,
} from "../publishCreditNoteResponse";

// ---------------------------------------------------------------------------
// normalizePublishCreditNoteResponse
// ---------------------------------------------------------------------------

describe("normalizePublishCreditNoteResponse", () => {
  describe("entradas vacías / primitivas", () => {
    it("null → todo vacío", () => {
      const r = normalizePublishCreditNoteResponse(null);
      expect(r).toEqual({ displayText: "", messageId: null, creditNoteUuid: null });
    });

    it("undefined → todo vacío", () => {
      const r = normalizePublishCreditNoteResponse(undefined);
      expect(r).toEqual({ displayText: "", messageId: null, creditNoteUuid: null });
    });

    it("string plano → displayText con trim, sin messageId ni uuid", () => {
      const r = normalizePublishCreditNoteResponse("  Nota publicada  ");
      expect(r.displayText).toBe("Nota publicada");
      expect(r.messageId).toBeNull();
      expect(r.creditNoteUuid).toBeNull();
    });

    it("número → todo vacío (no es objeto ni string)", () => {
      const r = normalizePublishCreditNoteResponse(42);
      expect(r).toEqual({ displayText: "", messageId: null, creditNoteUuid: null });
    });

    it("array → todo vacío (Array no es Record)", () => {
      const r = normalizePublishCreditNoteResponse([]);
      expect(r).toEqual({ displayText: "", messageId: null, creditNoteUuid: null });
    });
  });

  describe("payload con messageId vía idMsg", () => {
    it("extrae idMsg como messageId en MAYÚSCULAS", () => {
      const r = normalizePublishCreditNoteResponse({ idMsg: "part1004", message: "OK" });
      expect(r.messageId).toBe("PART1004");
    });

    it("usa code cuando idMsg está ausente", () => {
      const r = normalizePublishCreditNoteResponse({ code: "bus2016", message: "Alerta" });
      expect(r.messageId).toBe("BUS2016");
    });

    it("usa errorCode cuando idMsg y code están ausentes", () => {
      const r = normalizePublishCreditNoteResponse({ errorCode: "ERR001" });
      expect(r.messageId).toBe("ERR001");
    });

    it("messageId es null cuando todos los campos de id están vacíos", () => {
      const r = normalizePublishCreditNoteResponse({ message: "solo texto" });
      expect(r.messageId).toBeNull();
    });
  });

  describe("payload con displayText", () => {
    it("prioriza message sobre detailError", () => {
      const r = normalizePublishCreditNoteResponse({
        message: "Texto principal",
        detailError: "Texto secundario",
      });
      expect(r.displayText).toBe("Texto principal");
    });

    it("usa detailError cuando message está ausente", () => {
      const r = normalizePublishCreditNoteResponse({ detailError: "Detalle del error" });
      expect(r.displayText).toBe("Detalle del error");
    });

    it("cae al messageId como displayText cuando no hay message ni detailError", () => {
      const r = normalizePublishCreditNoteResponse({ idMsg: "PART1004" });
      expect(r.displayText).toBe("PART1004");
    });
  });

  describe("extracción de creditNoteUuid", () => {
    it("extrae invoiceUuid del nivel raíz", () => {
      const r = normalizePublishCreditNoteResponse({
        invoiceUuid: "uuid-raiz-001",
      });
      expect(r.creditNoteUuid).toBe("uuid-raiz-001");
    });

    it("extrae uuid del nivel raíz cuando invoiceUuid falta", () => {
      const r = normalizePublishCreditNoteResponse({ uuid: "uuid-raiz-002" });
      expect(r.creditNoteUuid).toBe("uuid-raiz-002");
    });

    it("extrae fiscalUuid del nivel raíz cuando los anteriores faltan", () => {
      const r = normalizePublishCreditNoteResponse({ fiscalUuid: "uuid-raiz-003" });
      expect(r.creditNoteUuid).toBe("uuid-raiz-003");
    });

    it("extrae invoiceUuid del objeto data anidado", () => {
      const r = normalizePublishCreditNoteResponse({
        data: { invoiceUuid: "uuid-nested-001" },
      });
      expect(r.creditNoteUuid).toBe("uuid-nested-001");
    });

    it("extrae uuid del objeto data anidado", () => {
      const r = normalizePublishCreditNoteResponse({
        data: { uuid: "uuid-nested-002" },
      });
      expect(r.creditNoteUuid).toBe("uuid-nested-002");
    });

    it("prioriza uuid raíz sobre uuid en data", () => {
      const r = normalizePublishCreditNoteResponse({
        invoiceUuid: "raiz",
        data: { invoiceUuid: "anidado" },
      });
      expect(r.creditNoteUuid).toBe("raiz");
    });

    it("devuelve null cuando no hay ningún uuid", () => {
      const r = normalizePublishCreditNoteResponse({ message: "sin uuid" });
      expect(r.creditNoteUuid).toBeNull();
    });

    it("ignora uuids que son strings vacíos o solo espacios", () => {
      const r = normalizePublishCreditNoteResponse({
        invoiceUuid: "   ",
        uuid: "",
        data: { fiscalUuid: "válido-004" },
      });
      expect(r.creditNoteUuid).toBe("válido-004");
    });
  });
});

// ---------------------------------------------------------------------------
// resolvePublishCreditNoteOutcome
// ---------------------------------------------------------------------------

describe("resolvePublishCreditNoteOutcome", () => {
  describe("resolución por messageId estructurado", () => {
    it(`messageId PART1004 → "success"`, () => {
      const outcome = resolvePublishCreditNoteOutcome({
        displayText: "",
        messageId: FISCAL_MESSAGE_ID.CREDIT_NOTE_REGISTERED,
        creditNoteUuid: null,
      });
      expect(outcome).toBe("success");
    });

    it(`messageId BUS2016 → "warning"`, () => {
      const outcome = resolvePublishCreditNoteOutcome({
        displayText: "",
        messageId: FISCAL_MESSAGE_ID.PUBLISH_ALERT,
        creditNoteUuid: null,
      });
      expect(outcome).toBe("warning");
    });

    it(`messageId desconocido → "error"`, () => {
      const outcome = resolvePublishCreditNoteOutcome({
        displayText: "",
        messageId: "ERR999",
        creditNoteUuid: null,
      });
      expect(outcome).toBe("error");
    });
  });

  describe("resolución por texto cuando messageId es null", () => {
    it(`displayText contiene PART1004 → "success"`, () => {
      const outcome = resolvePublishCreditNoteOutcome({
        displayText: "Registro exitoso PART1004 completado",
        messageId: null,
        creditNoteUuid: null,
      });
      expect(outcome).toBe("success");
    });

    it(`displayText contiene BUS2016 → "warning"`, () => {
      const outcome = resolvePublishCreditNoteOutcome({
        displayText: "Alerta BUS2016 en el proceso",
        messageId: null,
        creditNoteUuid: null,
      });
      expect(outcome).toBe("warning");
    });

    it(`displayText sin id conocido → "error"`, () => {
      const outcome = resolvePublishCreditNoteOutcome({
        displayText: "Error inesperado del servidor",
        messageId: null,
        creditNoteUuid: null,
      });
      expect(outcome).toBe("error");
    });

    it("displayText vacío y messageId null → error", () => {
      const outcome = resolvePublishCreditNoteOutcome({
        displayText: "",
        messageId: null,
        creditNoteUuid: null,
      });
      expect(outcome).toBe("error");
    });

    it("la búsqueda en texto es case-insensitive", () => {
      const outcome = resolvePublishCreditNoteOutcome({
        displayText: "resultado: part1004",
        messageId: null,
        creditNoteUuid: null,
      });
      expect(outcome).toBe("success");
    });
  });

  describe("FISCAL_MESSAGE_ID constantes", () => {
    it("CREDIT_NOTE_REGISTERED es PART1004", () => {
      expect(FISCAL_MESSAGE_ID.CREDIT_NOTE_REGISTERED).toBe("PART1004");
    });

    it("PUBLISH_ALERT es BUS2016", () => {
      expect(FISCAL_MESSAGE_ID.PUBLISH_ALERT).toBe("BUS2016");
    });
  });
});
