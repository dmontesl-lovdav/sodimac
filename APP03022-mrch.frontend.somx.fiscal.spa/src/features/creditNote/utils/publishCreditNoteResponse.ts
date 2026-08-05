/**
 * Normaliza la respuesta del servicio fiscal al publicar una nota de crédito.
 */
export type PublishCreditNoteOutcome = "success" | "warning" | "error";

export const FISCAL_MESSAGE_ID = {
  CREDIT_NOTE_REGISTERED: "PART1004",
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
  data?: Partial<CreditNotePublishApiPayload>;
}

export interface NormalizedPublishCreditNoteResponse {
  displayText: string;
  messageId: string | null;
  creditNoteUuid: string | null;
}

const EMPTY_RESPONSE: NormalizedPublishCreditNoteResponse = {
  displayText: "",
  messageId: null,
  creditNoteUuid: null,
};

const MESSAGE_OUTCOME: Record<string, PublishCreditNoteOutcome> = {
  [FISCAL_MESSAGE_ID.CREDIT_NOTE_REGISTERED]: "success",
  [FISCAL_MESSAGE_ID.PUBLISH_ALERT]: "warning",
};

function isRecord(value: unknown): value is Record<string, unknown> {
  return value !== null && typeof value === "object" && !Array.isArray(value);
}

function getString(...values: unknown[]): string | null {
  for (const value of values) {
    if (typeof value === "string") {
      const text = value.trim();

      if (text !== "") {
        return text;
      }
    }
  }

  return null;
}

export function normalizePublishCreditNoteResponse(
  raw: unknown,
): NormalizedPublishCreditNoteResponse {
  if (typeof raw === "string") {
    return {
      ...EMPTY_RESPONSE,
      displayText: raw.trim(),
    };
  }

  if (!isRecord(raw)) {
    return EMPTY_RESPONSE;
  }

  const data = isRecord(raw.data) ? raw.data : {};

  const messageId = getString(raw.idMsg, raw.code, raw.errorCode);

  return {
    displayText: getString(raw.message, raw.detailError, messageId) || "",
    messageId: messageId ? messageId.toUpperCase() : null,
    creditNoteUuid: getString(
      raw.invoiceUuid,
      raw.uuid,
      raw.fiscalUuid,
      data.invoiceUuid,
      data.uuid,
      data.fiscalUuid,
    ),
  };
}

export function resolvePublishCreditNoteOutcome(
  normalized: NormalizedPublishCreditNoteResponse,
): PublishCreditNoteOutcome {
  if (normalized.messageId) {
    const outcome = MESSAGE_OUTCOME[normalized.messageId];

    if (outcome) {
      return outcome;
    }
  }

  const text = normalized.displayText.toUpperCase();

  for (const id of Object.keys(MESSAGE_OUTCOME)) {
    if (text.includes(id)) {
      return MESSAGE_OUTCOME[id];
    }
  }

  return "error";
}