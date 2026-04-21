import { datasource } from "@/config/typeorm-datasource.js";
import { v4 as uuidv4 } from 'uuid';
import { AsyncLocalStorage } from 'async_hooks';
import { Request, Response, NextFunction } from 'express';
import * as svcAxios from "@/services/axios.service.js";
import 'dotenv/config';

type TaskContext = {
  trace_id: string;
  service_name: string;
  action: string | undefined;
  trace_front_id: string | undefined;
};

type AuditEventType = "ERROR" | "ALERTA" | "INFO";

type LogMeta = {
  tipoEvento?: AuditEventType;   // requerido por historia
  codigoError?: string | null;   // IdError / Código error
  idMensaje?: string | null;     // IdMensaje
  paso?: string | null;          // Paso
  log?: string | null;           // Stack trace / detalle técnico
};

const MAX_RESPONSE_SIZE = 5000;
export const asyncLocalStorage = new AsyncLocalStorage<TaskContext>();

export function runWithTrace(
  traceId: string,
  serviceName: string,
  action: string | undefined,
  tracefrontid: string | undefined,
  fn: () => unknown
) {
  asyncLocalStorage.run(
    {
      trace_id: traceId,
      service_name: serviceName,
      action,
      trace_front_id: tracefrontid,
    },
    fn
  );
}

export function getTraceId() {
  const store = asyncLocalStorage.getStore();
  return store?.trace_id || uuidv4();
}

export function getTraceIdV2() {
  const store = asyncLocalStorage.getStore();
  const _trace_id = store?.trace_id || uuidv4();
  if (store) {
    store.trace_id = store?.trace_id || uuidv4();
  }
  return asyncLocalStorage.getStore();
}

function inferTipoEvento(isError: boolean, meta?: LogMeta): AuditEventType {
  if (meta?.tipoEvento) return meta.tipoEvento;
  if (isError) return "ERROR";
  return "INFO";
}

export async function logActivity(
  isError: boolean,
  message: string,
  messageDetail: string | null | unknown,
  details: any = {},
  duration_ms: number = 0,
  meta: LogMeta = {}
): Promise<void> {
  try {
    const store = getTraceIdV2();
    const traceId = store?.trace_id || uuidv4();
    const action = store?.action || "";
    const traceFrontId = store?.trace_front_id || undefined;
    const serviceName = store?.service_name || "UNKNOWN";
    const userId = "system";
    const timestamp = new Date();

    /////CODIGO PARA MIGRAR MAS ADELANTE PARA HACERLO POR SERVICIOS
    // const data = {
    //   traceId: traceId,
    //   traceFrontId: traceFrontId,
    //   durationms: duration_ms,
    //   isError: isError,
    //   modulo: 'API_FINANZAS',
    //   serviceName: serviceName,
    //   action: action,
    //   message: message,
    //   messageDetail: messageDetail,
    //   userId: userId,
    //   timestamp: timestamp,
    //   details: { ...details, trace_id: traceId }
    // }

    // const CatMsgWrn = await svcAxios.axiosPost((process.env.AUDITORIA_API_URL_BBF?? "") +  process.env.AUDITORIA_API_URI_ACTIVITY_LOGS, data);



    const tipoEvento = inferTipoEvento(isError, meta);

    const query = `
      INSERT INTO core_audit.activity_logs (
        trace_id,
        trace_front_id,
        duration_ms,
        is_error,
        modulo,
        service_name,
        action,
        message,
        message_detail,
        user_id,
        timestamp,
        details,
        tipo_evento,
        codigo_error,
        id_mensaje,
        paso,
        log
      )
      VALUES (
        $1,  $2,  $3,  $4,  $5,  $6,  $7,  $8,  $9,  $10, $11, $12,
        $13, $14, $15, $16, $17
      )
    `;

    const values = [
      traceId,
      traceFrontId,
      duration_ms,
      isError,
      "API_FINANZAS",
      serviceName,
      action,
      message,
      messageDetail as any,
      userId,
      timestamp,
      { ...details, trace_id: traceId },

      // nuevos
      tipoEvento,
      meta.codigoError ?? null,
      meta.idMensaje ?? null,
      meta.paso ?? action ?? null,
      meta.log ?? null,
    ];

    await datasource.query(query, values);
  } catch (error) {
    console.error("Error al guardar el log:", error);
  }
}

export function activityLogger(serviceName: string) {
  return (req: Request, res: Response, next: NextFunction) => {
    const traceId = uuidv4();
    const startTime = process.hrtime();
    let responseBody: any;
    const tracefrontId = req.get("TraceFrontId");

    runWithTrace(traceId, serviceName, undefined, tracefrontId, () => {
      res.setHeader("X-Trace-Id", traceId);

      const details = {
        method: req.method,
        url: req.originalUrl,
        ip: req.ip,
        body: req.body,
        query: req.query,
      };

      logActivity(false, `START_${req.method}`, "START REQUEST", details, 0, {
        tipoEvento: "INFO",
        paso: `START_${req.method}`,
      });

      const originalSend = res.send.bind(res);
      const originalJson = res.json.bind(res);

      res.send = function (body) {
        responseBody = captureResponseBody(body);
        return originalSend(body);
      };

      res.json = function (body) {
        responseBody = captureResponseBody(body);
        return originalJson(body);
      };

      res.on("finish", () => {
        const [seconds, nanoseconds] = process.hrtime(startTime);
        const durationMs = seconds * 1000 + nanoseconds / 1e6;

        const responseDetails = {
          statusCode: res.statusCode,
          duration_ms: durationMs,
          endpoint: req.originalUrl,
          response_body: safeJsonParse(responseBody),
        };

        logActivity(false, `END_${req.method}`, "END REQUEST", responseDetails, durationMs, {
          tipoEvento: "INFO",
          paso: `END_${req.method}`,
        });
      });

      res.on("error", (err) => {
        const errorDetails = {
          statusCode: res.statusCode || 500,
          error_message: err.message,
        };

        logActivity(true, "TERMINA REQUEST CON ERROR", err.stack, errorDetails, 0, {
          tipoEvento: "ERROR",
          codigoError: String(res.statusCode || 500),
          idMensaje: "REQUEST_ERROR",
          log: err.stack || err.message,
          paso: actionOrFallback(req),
        });
      });

      next();
    });
  };
}

function actionOrFallback(req: Request) {
  return `${req.method} ${req.originalUrl}`;
}

function safeJsonParse(str: any) {
  try {
    if (typeof str !== "string") return str;
    return JSON.parse(str);
  } catch {
    return str;
  }
}

function captureResponseBody(body: any) {
  try {
    const content = typeof body === "object" ? JSON.stringify(body) : String(body);
    if (content.length > MAX_RESPONSE_SIZE) {
      return `{"TRUNCATED":"Response too large (${content.length} chars)"}`;
    }
    return content;
  } catch {
    return "ERROR: Unable to capture response body";
  }
}

export function logBeforeMethod(_action: string) {
  return (req: Request, _res: Response, next: NextFunction) => {
    const store = getTraceIdV2();
    if (store) {
      store.action = _action;
    }

    const details = {
      body: req.body,
      query: req.query,
    };

    logActivity(false, "INIT_METHOD", null, details, 0, {
      tipoEvento: "INFO",
      paso: _action,
    });

    next();
  };
}

export function globalErrorHandler() {
  return (req: Request, _res: Response, next: NextFunction) => {
    const details = {
      method: req.method,
      url: req.originalUrl,
      ip: req.ip,
      body: req.body,
      query: req.query,
    };

    void logActivity(true, "GlobalErrorHandler", "UNCAUGHT_ERROR", details, 0, {
      tipoEvento: "ERROR",
      codigoError: "500",
      idMensaje: "UNCAUGHT_ERROR",
      paso: `${req.method} ${req.originalUrl}`,
      log: "UNCAUGHT_ERROR",
    });

    next();
  };
}