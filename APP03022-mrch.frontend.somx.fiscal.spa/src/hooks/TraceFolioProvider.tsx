import { createContext, useContext, useState, useRef, useEffect, useCallback, useMemo, type ReactNode } from "react";
import { GenericTrace } from "@/shared/components/ui/misc";
import { AuditLogPayload, createTraceabilityClient, TraceFolio, TraceFolioPayload } from "@/services/TraceabilityClient";
import GenericButton from "@/shared/components/ui/button/GenericButton";
import { getUserIdFromStore } from "@/utils/getUserIdFromStore";

export interface TraceFolioContextValue {
  traceId: string | null;
  traceError: string | null;
  traceLoading: boolean;
  addLog: (details: string, module: string, step: string, severity: string, log: unknown) => void;
  headerActions: ReactNode;
  noTraceWarning: ReactNode;
  traceFooter: ReactNode;
}

const TraceFolioContext = createContext<TraceFolioContextValue | null>(null);

export function useTraceFolio(): TraceFolioContextValue {
  const ctx = useContext(TraceFolioContext);
  if (!ctx) throw new Error("useTraceFolio debe usarse dentro de TraceFolioProvider");
  return ctx;
}

interface TraceFolioProviderProps {
  children: ReactNode;
  traceFolioPayload: TraceFolioPayload;
}


export function getNestedValue(value: unknown, keys: string[]): unknown {
  let current = value;
  for (const key of keys) {
    if (!current || typeof current !== "object" || !(key in current)) return undefined;
    current = (current as Record<string, unknown>)[key];
  }
  return current;
}

export function pickFirstString(candidates: unknown[]): string {
  for (const item of candidates) {
    if (typeof item === "string" && item.trim()) return item.trim();
  }
  return "";
}

export function pickFirstStringOrNumber(candidates: unknown[]): string {
  for (const item of candidates) {
    if (typeof item === "string" || typeof item === "number") {
      const value = String(item).trim();
      if (value) return value;
    }
  }
  return "";
}

export function extractErrorInfo(value: unknown): { idError: string; mensaje: string; idMensaje?: string } {
  const fallback = { idError: "1", mensaje: "Timeout error" };
  const responseData = getNestedValue(value, ["response", "data"]);
  const source = responseData ?? value;

  if (!source || typeof source !== "object") return fallback;

  const record = source as Record<string, unknown>;
  const idError = pickFirstStringOrNumber([record.idError, record.errorCode, record.code, record.codigo]);
  const mensaje = pickFirstString([record.mensaje, record.message, record.detail, record.details]);
  const idMensaje = pickFirstStringOrNumber([record.idMensaje, record.error, record.title, record.name]);

  return {
    idError: idError ?? fallback.idError,
    mensaje: mensaje ?? fallback.mensaje,
    ...(idMensaje ? { idMensaje } : {}),
  };
}

export function toLogString(value: unknown): string {
  if (typeof value === "string") return value;
  if (value == null) return "";
  const responseData = getNestedValue(value, ["response", "data"]);
  if (responseData != null) {
    try {
      return JSON.stringify(responseData);
    } catch {
      return String(responseData);
    }
  }
  if (value instanceof Error) {
    return JSON.stringify({
      name: value.name,
      message: value.message,
      stack: value.stack,
    });
  }
  try {
    return JSON.stringify(value);
  } catch {
    return String(value);
  }
}

export function TraceFolioProvider({ children, traceFolioPayload }: TraceFolioProviderProps) {
  const traceClient = useRef(createTraceabilityClient()).current;
  const [traceFolio, setTraceFolio] = useState<TraceFolio | null>(null);
  const [traceError, setTraceError] = useState<string | null>(null);
  const [traceLoading, setTraceLoading] = useState(true);

  useEffect(() => {
    if(!traceFolioPayload) {
      setTraceError("No se proporcionó información para generar el folio de trazabilidad");
      setTraceLoading(false);
      return;
    }
    traceClient
      .createFolio(traceFolioPayload)
      .then((folio) =>
        folio?.data?.folioVisible ? setTraceFolio(folio) : setTraceError("No se obtuvo folio de trazabilidad")
      )
      .catch(() => setTraceError("No se pudo conectar al servicio de trazabilidad"))
      .finally(() => setTraceLoading(false));
  }, [traceClient, traceFolioPayload]);

  const addLog = useCallback(
    (details: string, module: string, step: string, severity: string, log: unknown) => {
      const idTransaccion = traceFolio?.trace_id;
      if (!traceFolio?.data?.folioVisible || !idTransaccion) return;

      const errorInfo = severity === "ERROR" ? extractErrorInfo(log) : null;

      const payload: AuditLogPayload = {
        idTransaccion,
        idAplicativo: "fiscal-front",
        idModulo: module,
        paso: step,
        detalle: details,
        fechaHora: new Date().toISOString(),
        log: toLogString(log),
        tipoEvento: severity,
        idUsuario: getUserIdFromStore() ?? "1",
        ...(errorInfo
          ? {
              idError: errorInfo.idError,
              mensaje: errorInfo.mensaje,
              ...(errorInfo.idMensaje ? { idMensaje: errorInfo.idMensaje } : {}),
            }
          : {}),
      };

      traceClient.createAuditLog(payload);
    },
    [traceFolio?.data?.folioVisible, traceFolio?.trace_id, traceClient]
  );

  const traceId = traceFolio?.trace_id ?? null;
  const folio = traceFolio?.data?.folioVisible ?? null;
  const hasTraceId = Boolean(traceId);
  const showNoTraceWarning = !traceLoading && !hasTraceId;

  const headerActions: ReactNode = traceLoading ? (
    <span className="fiscal-font-medium">Obteniendo folio...</span>
  ) : (
    <GenericTrace traceId={folio} uuid={traceId} />
  );

  const noTraceWarning: ReactNode = showNoTraceWarning ? (
    <p className="fiscal-mb-4" style={{ color: "var(--color-error, #b91c1c)" }}>
      {traceError ?? "No es posible realizar la acción requerida sin folio de trazabilidad."}
    </p>
  ) : null;

  const traceFooter: ReactNode = (() => {
    if (traceId) {
      return (
        <div className="fiscal-mt-4">
          <GenericTrace traceId={folio} uuid={traceId} />
        </div>
      );
    }
    if (traceLoading) {
      return <p> Expere a que finalice la obtención del folio</p>;
    }
    return (
      <div>
        <p style={{ color: "red" }}>No se pudo obtener el folio de trazabilidad</p>
        <GenericButton onClick={() => window.location.reload()}>
          Intentar nuevamente
        </GenericButton>
      </div>
    );
  })();

  const value: TraceFolioContextValue = useMemo(() => ({
    traceId,
    traceError,
    traceLoading,
    addLog,
    headerActions,
    noTraceWarning,
    traceFooter,
  }), [traceId, traceError, traceLoading, addLog, headerActions, noTraceWarning, traceFooter]);

  return (
    <TraceFolioContext.Provider value={value}>
      {children}
    </TraceFolioContext.Provider>
  );
}
