import { randomUUID } from 'node:crypto';
import { AsyncLocalStorage } from 'async_hooks';
import type { Request, Response, NextFunction } from 'express';
import * as auditRepo from "@/repositories/auditLog.repo.js";

type TaskContext = {
    trace_id: string;
    service_name: string;
    action: string | undefined;
    trace_front_id: string | undefined;
};

const MAX_RESPONSE_SIZE = 5000;
export const asyncLocalStorage = new AsyncLocalStorage<TaskContext>();

export function runWithTrace(
    traceId: string,
    serviceName: string,
    action: string | undefined,
    traceFrontId: string | undefined,
    fn: () => unknown
) {
    asyncLocalStorage.run(
        { trace_id: traceId, service_name: serviceName, action, trace_front_id: traceFrontId },
        fn
    );
}

export function getTraceId() {
    const store = asyncLocalStorage.getStore();
    return store?.trace_id || randomUUID();
}

export function getTraceIdV2() {
    const store = asyncLocalStorage.getStore();
    if (store) {
        store.trace_id = store.trace_id || randomUUID();
    }
    return store;
}

export async function logActivity(
    isError: boolean,
    message: string,
    messageDetail: string | null | unknown,
    details: any = {},
    _duration_ms: number = 0
): Promise<void> {
    try {
        const store = getTraceIdV2();
        const traceId = store?.trace_id ?? randomUUID();
        const action = store?.action || '';
        const serviceName = store?.service_name ?? '';
        const timestamp = new Date();

        let _messageDetail: string | null = null;
        if (typeof messageDetail === 'string') {
            _messageDetail = messageDetail;
        }

        await auditRepo.createOne({
            trace_id: traceId,
            service_name: serviceName,
            modulo: 'API_UTIL',
            paso: action,
            detalle: _messageDetail,
            tipo_evento: isError ? 'ERROR' : 'INFO',
            message,
            user_id: 'USR_API_UTIL',
            timestamp,
            details,
        });
    } catch (error) {
        console.error('Error al guardar el log:', error);
    }
}

export function activityLogger(serviceName: string) {
    return (req: Request, res: Response, next: NextFunction) => {
        const traceId = randomUUID();
        const startTime = process.hrtime();
        let responseBody: any;
        const traceFrontId = req.get('TraceFrontId') ?? undefined;

        runWithTrace(traceId, serviceName, undefined, traceFrontId, () => {
            res.setHeader('X-Trace-Id', traceId);

            const details = {
                method: req.method,
                url: req.originalUrl,
                ip: req.ip,
                body: req.body,
                query: req.query
            };
            logActivity(false, `START_${req.method}`, 'START REQUEST', details);

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

            res.on('finish', () => {
                const [seconds, nanoseconds] = process.hrtime(startTime);
                const durationMs = (seconds * 1000 + nanoseconds / 1e6);

                let parsedResponse: unknown;
                try {
                    parsedResponse = responseBody ? JSON.parse(responseBody) : null;
                } catch {
                    parsedResponse = responseBody;
                }

                const responseDetails = {
                    statusCode: res.statusCode,
                    duration_ms: durationMs,
                    endpoint: req.originalUrl,
                    response_body: parsedResponse
                };
                logActivity(false, `END_${req.method}`, 'END REQUEST', responseDetails, durationMs);
            });

            res.on('error', (err) => {
                const errorDetails = {
                    statusCode: res.statusCode || 500,
                    error_message: err.message
                };
                logActivity(true, "TERMINA REQUEST CON ERROR", err.stack, errorDetails);
            });

            next();
        });
    };
}

export function globalErrorHandler() {
    return (req: Request, _res: Response, next: NextFunction) => {
        const details = {
            method: req.method,
            url: req.originalUrl,
            ip: req.ip,
            body: req.body,
            query: req.query
        };
        logActivity(true, 'GlobalErrorHandler', 'UNCAUGHT_ERROR', details);
        next();
    };
}

function captureResponseBody(body: any) {
    try {
        const content = typeof body === 'object' ? JSON.stringify(body) : String(body);
        if (content.length > MAX_RESPONSE_SIZE) {
            return `{"TRUNCATED": "Response too large (${content.length} chars)"}`;
        }
        return content;
    } catch {
        return 'ERROR: Unable to capture response body';
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
            query: req.query
        };
        logActivity(false, 'INIT_METHOD', '', details);
        next();
    };
}
