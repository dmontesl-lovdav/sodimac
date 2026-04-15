import { randomUUID } from 'node:crypto';
import { AsyncLocalStorage } from 'async_hooks';
import type { Request, Response, NextFunction } from 'express';
import * as svc from "@/services/activityLogs.service.js";
import { type CreateActivityLogDto } from "@/schemas/activityLog.schema.js";

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
    duration_ms: number = 0
): Promise<void> {
    try {
        const store = getTraceIdV2();
        const traceId = store?.trace_id;
        const action = store?.action || '';
        const traceFrontId = store?.trace_front_id;
        const serviceName = store?.service_name;
        const userId = 'system';
        const timestamp = new Date();
        let _messageDetail = '';
        if (typeof messageDetail === "string") {
            _messageDetail = messageDetail;
        }

        const dto: CreateActivityLogDto = {
            traceId: traceId ?? "",
            traceFrontId: traceFrontId,
            serviceName: serviceName ?? "",
            action: action ?? "",
            modulo: "API_UTIL",
            userId: "USR_API_UTIL",
            isError,
            message,
            messageDetail: _messageDetail,
            details,
            durationms: duration_ms,
            timestamp
        };
        await svc.CreateLogActivity(dto);
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

                let parsedResponse: unknown = responseBody;
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
