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
  codigoModulo: string;
  pantallaOrigen: string;
  caso: string;
  metadatos: any;
  idUsuario: string;
  origen: string;
}

export interface AuditLogPayload {
  idTransaccion: string;
  idAplicativo: string;
  idModulo: string;
  paso: string;
  detalle: string;
  log: string;
  tipoEvento: string;
  idUsuario: string;
  idError?: string;
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
  trace_id: ""
};

function createMockFolio(): TraceFolio {
  return {
    ...MOCK_FOLIO,
    data: {
      ...MOCK_FOLIO.data,
      folioVisible: crypto.randomUUID(),
    },
  };
}

export function createTraceabilityClient(api?: ApiClient) {
  const baseUrl = process.env.API_FINANZAS_URL || ""; 
  const client = api ?? createApiClient({ baseUrl });

  return {
    createAuditLog(payload: AuditLogPayload): Promise<AuditLogResponse> {
       return client.execute<AuditLogResponse>("audit-logs", "post", payload);
    },

    createFolio(payload: TraceFolioPayload): Promise<TraceFolio> {
      // TODO: cuando el servicio esté online, reemplazar por:
       return client.execute<TraceFolio>("transaction-ids", "post", payload);
      if (!baseUrl.trim()) {
        return Promise.resolve(createMockFolio());
      }
      return client.execute<TraceFolio>("folio", "post", {}).catch(() => createMockFolio());
    },

    getTraceability(id: string): Promise<TraceabilityResponse> {
      // TODO: cuando el servicio esté online, reemplazar por:
      return client.execute<TraceabilityResponse>(`traceability/${id}`, "get");
      /*return Promise.resolve({
        id,
        date: MOCK_FOLIO.date,
        created_at: MOCK_FOLIO.created_at,
        last_update: MOCK_FOLIO.last_update,
        logs: [],
      });*/
    },

    addLog(id: string, message: string): Promise<void> {
      // TODO: cuando el servicio esté online:
      // return client.execute(`traceability/${id}/log`, "post", { message });
      return Promise.resolve();
    },
  };
}

export type TraceabilityClient = ReturnType<typeof createTraceabilityClient>;
