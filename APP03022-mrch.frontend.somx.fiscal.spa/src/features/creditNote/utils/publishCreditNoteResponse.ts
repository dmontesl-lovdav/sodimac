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

function isNonEmptyString(value: unknown): value is string {
  return typeof value === "string" && value.trim() !== "";
}

function firstNonEmptyString(...values: unknown[]): string | null {
  const found = values.find(isNonEmptyString);
  return found ? found.trim() : null;
}

function extractCreditNoteUuid(payload: Record<string, unknown>): string | null {
  const nested = payload.data;
  const data = isRecord(nested) ? nested : null;

  return firstNonEmptyString(
    payload.invoiceUuid,
    payload.uuid,
    payload.fiscalUuid,
    data?.invoiceUuid,
    data?.uuid,
    data?.fiscalUuid,
  );
}

/** Convierte un string vacío/solo-espacios en null; deja el resto trimeado. */
function emptyToNull(value: string): string | null {
  const trimmed = value.trim();
  return trimmed === "" ? null : trimmed;
}

const EMPTY_RESPONSE: NormalizedPublishCreditNoteResponse = {
  displayText: "",
  messageId: null,
  creditNoteUuid: null,
};

/** Resuelve el messageId estructurado (idMsg/code) o, en su defecto, el errorCode. */
function resolveMessageId(raw: Record<string, unknown>): string | null {
  const structured = firstNonEmptyString(raw.idMsg, raw.code);
  if (structured) return structured;
  return isNonEmptyString(raw.errorCode) ? emptyToNull(raw.errorCode) : null;
}

/**
 * Convierte la respuesta cruda del API en un objeto estable para la capa de UI.
 */
export function normalizePublishCreditNoteResponse(raw: unknown): NormalizedPublishCreditNoteResponse {
  if (typeof raw === "string") {
    return { ...EMPTY_RESPONSE, displayText: raw.trim() };
  }

  if (!isRecord(raw)) {
    return EMPTY_RESPONSE;
  }

  const messageId = resolveMessageId(raw);
  return {
    displayText: firstNonEmptyString(raw.message, raw.detailError, messageId) ?? "",
    messageId: messageId ? messageId.toUpperCase() : null,
    creditNoteUuid: extractCreditNoteUuid(raw),
  };
}

const SUCCESS_IDS = new Set<string>([FISCAL_MESSAGE_ID.CREDIT_NOTE_REGISTERED]);
const WARNING_IDS = new Set<string>([FISCAL_MESSAGE_ID.PUBLISH_ALERT]);
const ALL_KNOWN_IDS = [...SUCCESS_IDS, ...WARNING_IDS];

/** Clasifica un id de mensaje conocido; null si no pertenece a ningún catálogo. */
function classifyMessageId(id: string): PublishCreditNoteOutcome | null {
  if (SUCCESS_IDS.has(id)) return "success";
  if (WARNING_IDS.has(id)) return "warning";
  return null;
}

/** Busca, dentro de un texto libre, alguno de los identificadores conocidos del catálogo. */
function classifyMessageFromText(text: string): PublishCreditNoteOutcome | null {
  const upperText = text.toUpperCase();
  const matchedId = ALL_KNOWN_IDS.find((id) => upperText.includes(id));
  return matchedId ? classifyMessageId(matchedId) : null;
}

/**
 * Resuelve el resultado a partir del id estructurado o, en su defecto,
 * buscando identificadores conocidos en el texto (sin expresiones regulares).
 */
export function resolvePublishCreditNoteOutcome(
  normalized: NormalizedPublishCreditNoteResponse,
): PublishCreditNoteOutcome {
  const outcome = normalized.messageId
    ? classifyMessageId(normalized.messageId)
    : classifyMessageFromText(normalized.displayText);
  return outcome ?? "error";
}
