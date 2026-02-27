
import { v4 as uuidv4 } from 'uuid';
import { AsyncLocalStorage } from 'async_hooks';
import { Request, Response, NextFunction } from 'express';
import * as svc from "@/services/activityLogs.service.js";
import {
    type CreateActivityLogDto
} from "@/schemas/activityLog.schema.js";

type TaskContext = {
  trace_id: string,
  service_name: string ,
  action: string | undefined
};
const MAX_RESPONSE_SIZE = 5000; // Límite en caracteres (configurable)
export const asyncLocalStorage = new AsyncLocalStorage<TaskContext>();


/**
 * Context Manager
 */
export function runWithTrace(traceId: string, serviceName: string, action: string | undefined , fn: () => unknown) {
  asyncLocalStorage.run(
                        { trace_id: traceId,
                          service_name: serviceName,
                          action: action
                        }
                        , fn);
}

export function getTraceId() {
  const store = asyncLocalStorage.getStore();
  return store?.trace_id || uuidv4(); // fallback si no hay contexto
}

export function getTraceIdV2() {
  const store = asyncLocalStorage.getStore();
  const _trace_id = store?.trace_id || uuidv4(); 
  if(store){
      store.trace_id = store?.trace_id || uuidv4(); // fallback si no hay contexto
  }
  return asyncLocalStorage.getStore();
}



export async function logActivity(
  isError: boolean,
  message: string,
  messageDetail: string | null | unknown,
  details: any = {},
  duration_ms: number = 0
): Promise<void> {
  try {


    const store = getTraceIdV2();
    const traceId = store?.trace_id;
    const action = store?.action || '';
    const serviceName = store?.service_name;
    const userId= 'system';
    const timestamp = new Date();
    let _messageDetail = '';
if (typeof messageDetail === "string") {
      _messageDetail = messageDetail; // Type of 'value' is now narrowed to 'string' within this block
    }

    const dto: CreateActivityLogDto = {
        traceId: traceId ? traceId : "",
        serviceName: serviceName ? serviceName : "",
        action: action ? action : "",
        modulo: "API_AUDITORIA",
        userId: "USR_API_AUDITORIA",
        isError: isError,
        message: message,
        messageDetail: _messageDetail,
        details: details,
        durationms: duration_ms,
        timestamp: timestamp
    };
    const created =  await svc.CreateLogActivity(dto);
  } catch (error) {
    console.error('Error al guardar el log:', error);
  }
}


export function activityLogger(serviceName: string) {
  return (req: Request, res: Response, next: NextFunction) => {
    const traceId = uuidv4();
    const startTime = process.hrtime(); // Más preciso que Date.now()
    let responseBody: any; // Aquí guardaremos el body

    runWithTrace(traceId, serviceName, undefined, () => {
      res.setHeader('X-Trace-Id', traceId);

      // Log inicial del request
      const userId = (req as any).user?.id || req.headers['x-user-id'] || 'system';
      const details = {
        method: req.method,
        url: req.originalUrl,
        ip: req.ip,
        body: req.body,
        query: req.query
      };
      //LOG INICIAL
      logActivity(false, `START_${req.method}`,'START REQUEST', details);

      
      // Interceptar res.send y res.json
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



      // Log final cuando termina la respuesta
      res.on('finish', () => {
        
        const [seconds, nanoseconds] = process.hrtime(startTime);
        const durationMs = (seconds * 1000 + nanoseconds / 1e6);

        const responseDetails = {
          statusCode: res.statusCode,
          duration_ms: durationMs,
          endpoint: req.originalUrl,
          response_body: JSON.parse(responseBody) // Aquí está el body capturado
        };
          logActivity(false, `END_${req.method}`, 'END REQUEST', responseDetails, durationMs);
      });

      
      // Capturar errores en la respuesta
      res.on('error', (err) => {
        const errorDetails = {
          statusCode: res.statusCode || 500,
          error_message: err.message
        };
        logActivity(true, "TERMINA REQUEST CON ERROR", err.stack, errorDetails);
      });

      next();
    });
  }
}

export function globalErrorHandler() {
  return (req: Request, res: Response, next: NextFunction) => {
      const userId = (req as any).user?.id || req.headers['x-user-id'] || 'system';
      const details = {
        method: req.method,
        url: req.originalUrl,
        ip: req.ip,
        body: req.body,
        query: req.query
      };
      //LOG ERRROR
      logActivity(true, 'GlobalErrorHandler', 'UNCAUGHT_ERROR', details);
      next();
  }
}

// Función para capturar el body con límite
function captureResponseBody(body: any) {
  try {
    let content = typeof body === 'object' ? JSON.stringify(body) : String(body);
    if (content.length > MAX_RESPONSE_SIZE) {
      return `{"TRUNCATED": "Response too large (${content.length} chars)"}`;
    }
    return content;
  } catch (err) {
    return 'ERROR: Unable to capture response body';
  }
}



export function logBeforeMethod(_action: string) {
  return (req: Request, res: Response, next: NextFunction) => {
    const store = getTraceIdV2();
    if(store){
          store.action = _action;
    }


    const userId = 'system';
      const details = {
        body: req.body,
        query: req.query
      };
    logActivity(false,'INIT_METHOD', '', details);
    next();
  };
}


