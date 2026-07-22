/**
 * Normaliza la respuesta del servicio fiscal al publicar una nota de crédito
 * y determina el resultado sin acoplar la UI a detalles del payload.
 */

export type PublishCreditNoteOutcome = "success" | "warning" | "error";

/** IDs de mensaje conocidos (catálogo fiscal) — ampliar aquí cuando el negocio agregue códigos. */
export const FISCAL_MESSAGE_ID = {
  /** Registro exitoso (ej. PART1004) */
  CREDIT_NOTE_REGISTERED: "PART1004",
  /** Alerta de publicación (ej. BUS2016) */
  PUBLISH_ALERT: "BUS2016",
} as const;

export interface CreditNotePublishApiPayload {
  message?: string;
  idMsg?: string;
  code?: string;
  errorCode?: string;
  detailError?: string;
  invoiceUuid?: string;
  fiscalUuid?: string;
  uuid?: string;
  data?: Partial<CreditNotePublishApiPayload> | Record<string, unknown>;
}

export interface NormalizedPublishCreditNoteResponse {
  /** Texto útil para logs / mensajes genéricos */
  displayText: string;
  /** Identificador de mensaje si el backend lo envía estructurado */
  messageId: string | null;
  /** UUID de la nota para filtros posteriores */
  creditNoteUuid: string | null;
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return value !== null && typeof value === "object" && !Array.isArray(value);
}

function firstNonEmptyString(...values: unknown[]): string | null {
  for (const v of values) {
    if (typeof v === "string" && v.trim()) return v.trim();
  }
  return null;
}

function extractCreditNoteUuid(payload: Record<string, unknown>): string | null {
  const nested = payload.data;
  const data = isRecord(nested) ? nested : null;

  return (
    firstNonEmptyString(
      payload.invoiceUuid,
      payload.uuid,
      payload.fiscalUuid,
      data?.invoiceUuid,
      data?.uuid,
      data?.fiscalUuid,
    ) ?? null
  );
}

/**
 * Convierte la respuesta cruda del API en un objeto estable para la capa de UI.
 */
export function normalizePublishCreditNoteResponse(raw: unknown): NormalizedPublishCreditNoteResponse {
  if (raw == null) {
    return { displayText: "", messageId: null, creditNoteUuid: null };
  }

  if (typeof raw === "string") {
    return { displayText: raw.trim(), messageId: null, creditNoteUuid: null };
  }

  if (!isRecord(raw)) {
    return { displayText: "", messageId: null, creditNoteUuid: null };
  }

  const messageId =
    firstNonEmptyString(raw.idMsg, raw.code) ??
    (typeof raw.errorCode === "string" ? ((t) => (t === "" ? null : t))(raw.errorCode.trim()) : null);

  const displayText =
    firstNonEmptyString(raw.message, raw.detailError, messageId) ?? "";

  return {
    displayText,
    messageId: messageId ? messageId.toUpperCase() : null,
    creditNoteUuid: extractCreditNoteUuid(raw),
  };
}

const SUCCESS_IDS = new Set<string>([FISCAL_MESSAGE_ID.CREDIT_NOTE_REGISTERED]);
const WARNING_IDS = new Set<string>([FISCAL_MESSAGE_ID.PUBLISH_ALERT]);
const ALL_KNOWN_IDS = [...SUCCESS_IDS, ...WARNING_IDS];

/**
 * Resuelve el resultado a partir del id estructurado o, en su defecto,
 * buscando identificadores conocidos en el texto (sin expresiones regulares).
 */
export function resolvePublishCreditNoteOutcome(
  normalized: NormalizedPublishCreditNoteResponse,
): PublishCreditNoteOutcome {
  if (normalized.messageId) {
    if (SUCCESS_IDS.has(normalized.messageId)) return "success";
    if (WARNING_IDS.has(normalized.messageId)) return "warning";
    return "error";
  }

  const text = `${normalized.displayText}`.toUpperCase();
  for (const id of ALL_KNOWN_IDS) {
    if (text.includes(id)) {
      if (SUCCESS_IDS.has(id)) return "success";
      if (WARNING_IDS.has(id)) return "warning";
    }
  }

  return "error";
}
