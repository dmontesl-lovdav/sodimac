import { createApiClient, type ApiClient } from "./ApiClient";

export interface TraceFolioData {
  transaction_id_uuid: string;
  folioVisible: string;
  uuidInterno: string;
  fechaHora: string;
  codigoModulo: string;
  pantallaOrigen: string;
  caso: string;
  idUsuario: string;
  origen: string;
  estatus: string;
  metadatos: {
    rfc: string;
    proveedorId: number;
  };
}

export interface TraceFolio {
  message: string;
  data: TraceFolioData;
  count: number;
  statusCode: number;
  success: boolean;
  details: string;
  trace_id: string;
}

export interface AuditLogResponse {
  activity_logs_uuid: string;
}

export interface TraceFolioPayload {
  /** Opcional hasta que el backend asigne trace; suele omitirse al crear el folio. */
  idTransaccion?: string;
  idAplicativo: string;
  idModulo: string;
  paso: string;
  detalle: string;
  fechaHora: string;
  tipoEvento: string;
  idUsuario: string;
  idError?: string;
  idMensaje?: string;
  mensaje?: string;
  log?: string;
}

export interface AuditLogPayload {
  idTransaccion: string;
  idAplicativo: string;
  idModulo: string;
  paso: string;
  detalle: string;
  fechaHora: string;
  log: string;
  tipoEvento: string;
  idUsuario: string;
  idError?: string;
  idMensaje?: string;
  mensaje?: string;
}

export interface TraceLogEntry {
  id?: string;
  message: string;
  created_at?: string;
}

export interface TraceabilityResponse {
  id: string;
  date?: string;
  created_at?: string;
  last_update?: string;
  logs?: TraceLogEntry[];
}

const MOCK_FOLIO: TraceFolio = {
  message: "",
  data: {
    folioVisible: "",
    transaction_id_uuid: "",
    uuidInterno: "",
    fechaHora: new Date().toISOString(),
    codigoModulo: "",
    pantallaOrigen: "",
    caso: "",
    idUsuario: "",
    origen: "",
    estatus: "",
    metadatos: {
      rfc: "",
      proveedorId: 0,
    },
  },
  count: 0,
  statusCode: 0,
  success: true,
  details: "",
  trace_id: "",
};

function createMockAuditLogResponse(): AuditLogResponse {
  return {
    activity_logs_uuid: crypto.randomUUID(),
  };
}

function createMockFolio(payload?: TraceFolioPayload): TraceFolio {
  const id = crypto.randomUUID();
  const now = new Date().toISOString();
  return {
    ...MOCK_FOLIO,
    trace_id: id,
    data: {
      ...MOCK_FOLIO.data,
      folioVisible: id,
      transaction_id_uuid: id,
      uuidInterno: id,
      fechaHora: payload?.fechaHora ?? now,
      codigoModulo: payload?.idModulo ?? "",
      pantallaOrigen: payload?.idAplicativo ?? "",
      caso: payload?.paso ?? "",
      idUsuario: payload?.idUsuario ?? "",
      origen: "mock",
      estatus: "mock",
      metadatos: { rfc: "", proveedorId: 0 },
    },
    details: "[mock] finanzas no configurado o error de red — folio sintético",
  };
}

export function createTraceabilityClient(api?: ApiClient) {
  const baseUrl = process.env.FINANZAS_API_URL ?? "";
  const client = api ?? createApiClient({ baseUrl });

  return {
    createAuditLog(payload: AuditLogPayload): Promise<AuditLogResponse> {
      if (!baseUrl.trim()) {
        return Promise.resolve(createMockAuditLogResponse());
      }
      return client
        .request<AuditLogResponse>("audit-logs", "post", payload)
        .catch(() => createMockAuditLogResponse());
    },

    createFolio(payload: TraceFolioPayload): Promise<TraceFolio> {
      if (!baseUrl.trim()) {
        return Promise.resolve(createMockFolio(payload));
      }
      return client
        .request<TraceFolio>("transaction-ids", "post", payload)
        .catch(() => createMockFolio(payload));
    },

    getTraceability(id: string): Promise<TraceabilityResponse> {
      return client.request<TraceabilityResponse>(`traceability/${id}`, "get");
    },

    addLog(id: string, message: string): Promise<void> {
      if (!baseUrl.trim()) {
        return Promise.resolve();
      }
      return client
        .request<unknown>(`traceability/${id}/log`, "post", { message })
        .then(() => undefined);
    },
  };
}

export type TraceabilityClient = ReturnType<typeof createTraceabilityClient>;
