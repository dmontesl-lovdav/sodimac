import { randomUUID } from 'node:crypto';
import { AsyncLocalStorage } from 'async_hooks';
import type { Request, Response, NextFunction } from 'express';
import * as auditRepo from "@/repositories/auditLog.repo.js";

type TaskContext = {
    trace_id: string;
    service_name: string;
    action: string | undefined;
    trace_front_id: string | undefined;
    has_error?: boolean;
};

type LogMeta = {
    tipoEvento?: 'ERROR' | 'ALERTA' | 'INFO';
    codigoError?: string | null;
    idMensaje?: string | null;
    paso?: string | null;
    log?: string | null;
};

const MAX_RESPONSE_SIZE = 5000;
const monitoredResponses = new WeakSet<Response>();
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

function enabled() {
    return process.env.UTIL_AUDIT_ENABLED?.trim().toLowerCase() !== 'false';
}

// No se guardan cuerpos completos, tokens ni archivos en la auditoría HTTP.
function safeDetails(value: unknown, depth = 0): unknown {
    if (value == null || typeof value === 'boolean' || typeof value === 'number') return value;
    if (typeof value === 'string') return value.slice(0, MAX_RESPONSE_SIZE);
    if (Buffer.isBuffer(value)) return '[BINARY OMITTED]';
    if (depth >= 4) return '[TRUNCATED]';
    if (Array.isArray(value)) return value.slice(0, 20).map(item => safeDetails(item, depth + 1));
    if (typeof value !== 'object') return String(value);
    return Object.fromEntries(Object.entries(value).slice(0, 30).map(([key, item]) => [
        key,
        /authorization|cookie|password|passwd|secret|token|credential|private.?key/i.test(key)
            ? '[REDACTED]' : safeDetails(item, depth + 1),
    ]));
}

export async function logActivity(
    isError: boolean,
    message: string,
    messageDetail: unknown,
    details: any = {},
    _duration_ms: number = 0,
    meta: LogMeta = {}
): Promise<void> {
    if (!enabled()) return;
    try {
        const store = getTraceIdV2();
        const traceId = store?.trace_id ?? randomUUID();
        const action = meta.paso ?? store?.action ?? '';
        const serviceName = store?.service_name ?? 'UtilApi';
        const timestamp = new Date();
        const tipoEvento = isError ? 'ERROR' : meta.tipoEvento ?? 'INFO';
        if (store && tipoEvento === 'ERROR') store.has_error = true;
        const front = store?.trace_front_id;
        const validFront = front && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(front)
            ? front : null;
        const sanitized = safeDetails(details);
        const values = {
            ...(sanitized && typeof sanitized === 'object' ? sanitized : { detail: sanitized }),
            trace_id: traceId,
            ...(front && !validFront ? { trace_front_id_original: front.slice(0, 200) } : {}),
        };

        await auditRepo.createOne({
            trace_id: traceId,
            trace_front_id: validFront,
            duration_ms: Number.isFinite(_duration_ms) ? Math.max(0, _duration_ms) : 0,
            service_name: serviceName.slice(0, 100),
            modulo: 'API_UTIL',
            paso: action.slice(0, 100),
            detalle: typeof messageDetail === 'string' ? messageDetail.slice(0, MAX_RESPONSE_SIZE) : null,
            tipo_evento: tipoEvento,
            codigo_error: meta.codigoError?.slice(0, 100) ?? null,
            id_mensaje: meta.idMensaje?.slice(0, 100) ?? message.slice(0, 100),
            log: meta.log?.slice(0, MAX_RESPONSE_SIZE) ?? null,
            message: message.slice(0, 100),
            // attachAuthToken no verifica identidad; conservar el usuario técnico existente.
            user_id: 'USR_API_UTIL',
            timestamp,
            details: values,
        });
    } catch (error) {
        console.error('UTIL_AUDIT_WRITE_FAILED', {
            trace_id: asyncLocalStorage.getStore()?.trace_id,
            cause: error instanceof Error ? error.name : 'UnknownError',
        });
    }
}

function responseSummary(body: unknown): Record<string, unknown> | undefined {
    try {
        if (typeof body === 'string' && body.length <= MAX_RESPONSE_SIZE) body = JSON.parse(body);
        if (!body || typeof body !== 'object' || Buffer.isBuffer(body) || Array.isArray(body)) return undefined;
        const record = body as Record<string, unknown>;
        const summary: Record<string, unknown> = {};
        for (const key of ['success', 'isValid', 'valid', 'code', 'errorCode', 'status', 'httpStatus', 'message']) {
            const value = record[key];
            if (typeof value === 'boolean' || typeof value === 'number') summary[key] = value;
            else if (typeof value === 'string') summary[key] = value.slice(0, 1000);
        }
        return summary;
    } catch {
        return undefined;
    }
}

export function activityLogger(serviceName: string) {
    return (req: Request, res: Response, next: NextFunction) => {
        if (!enabled() || monitoredResponses.has(res)) return next();
        monitoredResponses.add(res);
        const traceId = randomUUID();
        const startTime = process.hrtime.bigint();
        const endpoint = req.originalUrl.split('?')[0] || '/';
        const resource = endpoint.startsWith('/api/') ? endpoint.split('/')[2] : undefined;
        const service = serviceName === 'UtilApi' && resource ? `UtilApi:${resource}` : serviceName;
        const traceFrontId = req.get('TraceFrontId') ?? undefined;

        runWithTrace(traceId, service, `${req.method} ${endpoint}`, traceFrontId, () => {
            const context = asyncLocalStorage.getStore()!;
            let summary: Record<string, unknown> | undefined;
            let completed = false;
            res.setHeader('X-Trace-Id', traceId);
            void logActivity(false, `START_${req.method}`, 'START REQUEST', {
                method: req.method,
                endpoint,
                ip: req.ip,
                content_type: req.get('Content-Type'),
                content_length: req.get('Content-Length'),
            });

            const originalSend = res.send.bind(res);
            const originalJson = res.json.bind(res);
            res.json = function (body) {
                summary = responseSummary(body);
                return originalJson(body);
            };
            res.send = function (body) {
                if (summary === undefined) summary = responseSummary(body);
                return originalSend(body);
            };

            const finish = (aborted: boolean) => asyncLocalStorage.run(context, () => {
                if (completed) return;
                completed = true;
                const durationMs = Number(process.hrtime.bigint() - startTime) / 1e6;
                const businessError = summary?.success === false || summary?.isValid === false || summary?.valid === false
                    || (typeof summary?.httpStatus === 'number' && summary.httpStatus >= 400);
                const isError = aborted || res.statusCode >= 400 || businessError || context.has_error === true;
                const responseCode = summary?.code || summary?.errorCode;
                const code = !isError ? null : aborted ? 'REQUEST_ABORTED'
                    : responseCode ? String(responseCode)
                    : res.statusCode >= 400 ? String(res.statusCode) : 'REQUEST_ERROR';
                void logActivity(isError, aborted ? 'REQUEST_ABORTED' : `END_${req.method}`, 'END REQUEST', {
                    method: req.method,
                    endpoint,
                    statusCode: res.statusCode,
                    duration_ms: durationMs,
                    aborted,
                    business_error: businessError,
                    response_summary: summary,
                    route: req.route?.path,
                }, durationMs, { codigoError: code });
            });
            res.once('finish', () => finish(false));
            res.once('close', () => finish(!res.writableFinished));
            res.once('error', (err: Error) => asyncLocalStorage.run(context, () => {
                void logActivity(true, 'RESPONSE_ERROR', err.name, { endpoint }, 0, {
                    codigoError: 'RESPONSE_ERROR',
                    log: stackDetail(err),
                });
                finish(true);
            }));

            next();
        });
    };
}

function stackDetail(err: unknown): string | null {
    // Omitir mensajes de excepción que pueden contener SQL, JSON o credenciales.
    return err instanceof Error ? `${err.name}\n${err.stack?.split('\n').slice(1).join('\n') ?? ''}`.slice(0, MAX_RESPONSE_SIZE) : null;
}

export function globalErrorHandler() {
    return (err: unknown, req: Request, _res: Response, next: NextFunction) => {
        const record = err && typeof err === 'object' ? err as Record<string, unknown> : {};
        void logActivity(true, 'ERROR_HANDLER', err instanceof Error ? err.name : 'UnknownError', {
            method: req.method,
            endpoint: req.originalUrl.split('?')[0],
        }, 0, {
            codigoError: typeof record.code === 'string' || typeof record.code === 'number' ? String(record.code) : null,
            log: stackDetail(err),
        });
        next(err);
    };
}

export function logBeforeMethod(_action: string) {
    return (_req: Request, _res: Response, next: NextFunction) => {
        const store = getTraceIdV2();
        if (store) {
            store.action = _action;
        }
        void logActivity(false, 'INIT_METHOD', '', {});
        next();
    };
}
