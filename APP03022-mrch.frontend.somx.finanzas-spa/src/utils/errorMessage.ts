/** Formato típico backend Finanzas (Zod / validación). */
type DetailErrorRow = { path?: string; message?: string };

/**
 * Algunos endpoints concatenan el mensaje de negocio con `undefined`,
 * `HttpError:` y el stack de Node. Deja solo el texto útil para la UI.
 */
function sanitizeErrorText(text: string): string {
  const markers = ["undefinedHttpError:", "undefined\nHttpError:", "HttpError:", "\n    at ", "\nat "];
  let result = text;
  for (const marker of markers) {
    const idx = result.indexOf(marker);
    if (idx !== -1) {
      result = result.slice(0, idx);
    }
  }
  return result.replace(/undefined$/i, "").trim();
}

function formatObjectDetailRow(row: DetailErrorRow): string | undefined {
  const path = typeof row.path === "string" ? row.path.trim() : "";
  const message = typeof row.message === "string" ? row.message.trim() : "";
  if (path && message) return `${path}: ${message}`;
  if (message) return message;
  if (path) return path;
  return undefined;
}

function formatDetailErrors(detailError: unknown): string | undefined {
  if (!Array.isArray(detailError) || detailError.length === 0) {
    return undefined;
  }
  const parts: string[] = [];
  for (const row of detailError) {
    if (row == null) continue;
    if (typeof row === "string" && row.trim()) {
      parts.push(row.trim());
      continue;
    }
    if (typeof row === "object") {
      const formatted = formatObjectDetailRow(row as DetailErrorRow);
      if (formatted) parts.push(formatted);
    }
  }
  if (parts.length === 0) return undefined;
  return parts.join("\n");
}

function isApiLikePayload(obj: object): obj is Record<string, unknown> {
  const r = obj as Record<string, unknown>;
  return (
    "detailError" in r ||
    "success" in r ||
    (typeof r.message === "string" && r.httpStatus != null)
  );
}

function firstErrorsMessage(errs: unknown): string | undefined {
  if (!Array.isArray(errs) || errs.length === 0) return undefined;
  const first = errs[0];
  if (typeof first === "string" && first.trim()) return first.trim();
  if (first && typeof first === "object") {
    return formatDetailErrors([first]);
  }
  return undefined;
}

/** Extrae mensaje legible del cuerpo JSON de error del API. */
function extractReadableApiMessage(data: Record<string, unknown>): string | undefined {
  // detailError (array Zod o string de negocio) tiene prioridad sobre message genérico (EXC00x).
  const fromDetails = formatDetailErrors(data.detailError);
  if (fromDetails) return sanitizeErrorText(fromDetails);

  if (typeof data.detailError === "string" && data.detailError.trim()) {
    const fromDetailString = sanitizeErrorText(data.detailError.trim());
    if (fromDetailString) return fromDetailString;
  }

  const msg = data.message;
  const msgStr = typeof msg === "string" ? msg.trim() : "";

  if (msgStr === "ValidationError") {
    return "Los datos enviados no son válidos. Revisa los campos e intenta de nuevo.";
  }
  if (msgStr) return sanitizeErrorText(msgStr);

  if (typeof data.error === "string" && data.error.trim()) {
    return sanitizeErrorText(data.error.trim());
  }
  if (typeof data.detail === "string" && data.detail.trim()) {
    return sanitizeErrorText(data.detail.trim());
  }

  const fromErrors = firstErrorsMessage(data.errors);
  return fromErrors ? sanitizeErrorText(fromErrors) : undefined;
}

function messageFromResponseData(data: unknown): string | undefined {
  if (typeof data === "string" && data.trim()) return sanitizeErrorText(data.trim());
  if (data && typeof data === "object" && !Array.isArray(data)) {
    return extractReadableApiMessage(data as Record<string, unknown>);
  }
  return undefined;
}

function messageFromAxiosLike(e: {
  code?: string;
  message?: string;
  response?: { data?: unknown; status?: number };
}): string | undefined {
  const code = e.code;
  if (code === "ECONNABORTED" || code === "ETIMEDOUT") {
    return "La solicitud tardó demasiado (tiempo de espera agotado). Intenta nuevamente.";
  }

  const fromData = messageFromResponseData(e.response?.data);
  if (fromData) return fromData;

  if (isApiLikePayload(e as object)) {
    const fromPayload = extractReadableApiMessage(e as Record<string, unknown>);
    if (fromPayload) return fromPayload;
  }

  if (typeof e.message === "string" && e.message.trim()) {
    if (e.message === "Network Error") {
      return "No hay conexión con el servidor. Verifica tu red e intenta nuevamente.";
    }
    return sanitizeErrorText(e.message.trim());
  }

  const status = e.response?.status;
  if (status === 504 || status === 408) {
    return "Tiempo de espera agotado en el servidor. Intenta nuevamente.";
  }

  return undefined;
}

/** Extrae texto legible de Axios, timeouts, Error genéricos u objetos { message }. */
export function getErrorMessage(err: unknown, fallback = "Ocurrió un error inesperado."): string {
  if (err == null) return fallback;
  if (typeof err === "string" && err.trim()) return err.trim();
  if (!(typeof err === "object")) return fallback;

  return messageFromAxiosLike(err as {
    code?: string;
    message?: string;
    response?: { data?: unknown; status?: number };
  }) ?? fallback;
}
