/** Formato típico backend Finanzas (Zod / validación). */
type DetailErrorRow = { path?: string; message?: string };

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
      const o = row as DetailErrorRow;
      const path = typeof o.path === "string" ? o.path.trim() : "";
      const message = typeof o.message === "string" ? o.message.trim() : "";
      if (path && message) {
        parts.push(`${path}: ${message}`);
      } else if (message) {
        parts.push(message);
      } else if (path) {
        parts.push(path);
      }
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

/** Extrae mensaje legible del cuerpo JSON de error del API. */
function extractReadableApiMessage(data: Record<string, unknown>): string | undefined {
  const fromDetails = formatDetailErrors(data.detailError);
  if (fromDetails) return fromDetails;

  const msg = data.message;
  const msgStr = typeof msg === "string" ? msg.trim() : "";

  if (msgStr === "ValidationError") {
    return "Los datos enviados no son válidos. Revisa los campos e intenta de nuevo.";
  }

  if (msgStr) {
    return msgStr;
  }

  const err = data.error;  if (typeof err === "string" && err.trim()) return err.trim();

  const detail = data.detail;
  if (typeof detail === "string" && detail.trim()) return detail.trim();

  const errs = data.errors;
  if (Array.isArray(errs) && errs.length > 0) {
    const first = errs[0];
    if (typeof first === "string" && first.trim()) return first.trim();
    if (first && typeof first === "object") {
      const nested = formatDetailErrors([first]);
      if (nested) return nested;
    }
  }

  return undefined;
}

/** Extrae texto legible de Axios, timeouts, Error genéricos u objetos { message }. */
export function getErrorMessage(err: unknown, fallback = "Ocurrió un error inesperado."): string {
  if (err == null) return fallback;

  if (typeof err === "string" && err.trim()) return err.trim();

  if (!(typeof err === "object")) return fallback;

  const e = err as {
    code?: string;
    message?: string;
    response?: { data?: unknown; status?: number };
  };

  const code = e.code;
  if (code === "ECONNABORTED" || code === "ETIMEDOUT") {
    return "La solicitud tardó demasiado (tiempo de espera agotado). Intenta nuevamente.";
  }

  const fromData = (): string | undefined => {
    const raw = e.response?.data;
    if (typeof raw === "string" && raw.trim()) return raw.trim();
    if (raw && typeof raw === "object" && !Array.isArray(raw)) {
      const apiText = extractReadableApiMessage(raw as Record<string, unknown>);
      if (apiText) return apiText;
    }
    return undefined;
  };

  const fromDirectPayload = (): string | undefined => {
    if (isApiLikePayload(e as object)) {
      return extractReadableApiMessage(e as Record<string, unknown>);
    }
    return undefined;
  };

  const apiMsg = fromData() ?? fromDirectPayload();
  if (apiMsg) return apiMsg;

  if (typeof e.message === "string" && e.message.trim()) {
    if (e.message === "Network Error") {
      return "No hay conexión con el servidor. Verifica tu red e intenta nuevamente.";
    }
    return e.message.trim();
  }

  const status = e.response?.status;
  if (status === 504 || status === 408) {
    return "Tiempo de espera agotado en el servidor. Intenta nuevamente.";
  }

  return fallback;
}
