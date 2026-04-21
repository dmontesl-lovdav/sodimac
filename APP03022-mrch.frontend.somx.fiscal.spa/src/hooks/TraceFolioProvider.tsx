import { createContext, useContext, useState, useRef, useEffect, useCallback, type ReactNode } from "react";
import { GenericTrace } from "@/shared/components/ui/misc";
import { AuditLogPayload, createTraceabilityClient, TraceFolio, TraceFolioPayload } from "@/services/TraceabilityClient";
import GenericButton from "@/shared/components/ui/button/GenericButton";

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


export function TraceFolioProvider({ children, traceFolioPayload }: TraceFolioProviderProps) {
  const traceClient = useRef(createTraceabilityClient()).current;
  const [traceFolio, setTraceFolio] = useState<TraceFolio | null>(null);
  const [traceError, setTraceError] = useState<string | null>(null);
  const [traceLoading, setTraceLoading] = useState(true);

  const getNestedValue = useCallback((value: unknown, keys: string[]): unknown => {
    let current = value;
    for (const key of keys) {
      if (!current || typeof current !== "object" || !(key in current)) return undefined;
      current = (current as Record<string, unknown>)[key];
    }
    return current;
  }, []);

  const extractErrorInfo = useCallback((value: unknown): { idError: string; mensaje: string } => {
    const fallback = { idError: "1", mensaje: "Timeout error" };
    const responseData = getNestedValue(value, ["response", "data"]);
    const source = responseData ?? value;

    if (!source || typeof source !== "object") return fallback;

    const record = source as Record<string, unknown>;
    const idErrorCandidate = [record.idError, record.errorCode, record.code, record.codigo]
      .find((item) => typeof item === "string" || typeof item === "number");
    const mensajeCandidate = [record.mensaje, record.message, record.detail, record.details]
      .find((item) => typeof item === "string");

    const idError = idErrorCandidate != null ? String(idErrorCandidate).trim() : "";
    const mensaje = typeof mensajeCandidate === "string" ? mensajeCandidate.trim() : "";

    return {
      idError: idError || fallback.idError,
      mensaje: mensaje || fallback.mensaje,
    };
  }, [getNestedValue]);

  const toLogString = useCallback((value: unknown): string => {
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
  }, [getNestedValue]);

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
        log: toLogString(log),
        tipoEvento: severity,
        idUsuario: "1",
        ...(errorInfo ? errorInfo : {}),
      };

      void traceClient.createAuditLog(payload);
    },
    [traceFolio?.data?.folioVisible, traceFolio?.trace_id, traceClient, toLogString, extractErrorInfo]
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

  const traceFooter: ReactNode = traceId ? (
    <div className="fiscal-mt-4">
      <GenericTrace traceId={folio} uuid={traceId} />
    </div>
  ) : traceLoading ? (<p> Expere a que finalice la obtención del folio</p>) : (
     <div>
      <p style={{ color: "red" }}>No se pudo obtener el folio de trazabilidad</p>
          <GenericButton onClick={() => window.location.reload()}>
            Intentar nuevamente
          </GenericButton>
     </div>
  );

  const value: TraceFolioContextValue = {
    traceId,
    traceError,
    traceLoading,
    addLog,
    headerActions,
    noTraceWarning,
    traceFooter,
  };

  return (
    <TraceFolioContext.Provider value={value}>
      {children}
    </TraceFolioContext.Provider>
  );
}
