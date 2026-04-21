// ✅ FILE: src/features/audit-logs/utils/auditLogsTrain.utils.ts
import type { AuditLogRecord } from '../interfaces';

export type TipoEvento = 'ERROR' | 'ALERTA' | 'INFO';

export function safeDateISO(v?: any): string | null {
    if (!v) return null;
    try {
        const d = new Date(v);
        if (isNaN(d.getTime())) return null;
        return d.toISOString();
    } catch {
        return null;
    }
}

export function formatTimeOnly(iso?: string | null) {
    if (!iso) return '-';
    const d = new Date(iso);
    if (isNaN(d.getTime())) return '-';
    return d.toLocaleTimeString([], {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
    });
}

export function formatDateOnly(iso?: string | null) {
    if (!iso) return '-';
    const d = new Date(iso);
    if (isNaN(d.getTime())) return '-';
    const dd = String(d.getDate()).padStart(2, '0');
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const yyyy = d.getFullYear();
    return `${dd}-${mm}-${yyyy}`;
}

export function getTipo(r: AuditLogRecord) {
    return (r.tipo_evento ?? (r.is_error ? 'ERROR' : 'INFO')) as TipoEvento;
}

export function getEstadoFinal(last?: AuditLogRecord | null) {
    if (!last) return { label: '-', kind: 'info' as const };
    const tipo = getTipo(last);
    if (tipo === 'ERROR') return { label: 'Error', kind: 'error' as const };
    return { label: 'Exitoso', kind: 'success' as const };
}

export function pickStepTitle(r: AuditLogRecord) {
    return r.paso ?? r.action ?? '-';
}

export function pickStepDetail(r: AuditLogRecord) {
    if (r.message_detail) return r.message_detail;
    if (r.message) return r.message;

    const m = r.details?.response_body?.message;
    if (m) return String(m);

    const ep = r.details?.endpoint;
    if (ep) return String(ep);

    return '';
}

export function findBestTechRow(rows: AuditLogRecord[]) {
    for (let i = rows.length - 1; i >= 0; i--) {
        const r = rows[i];
        if (r.details?.response_body) return r;
    }
    for (let i = rows.length - 1; i >= 0; i--) {
        const r = rows[i];
        if (r.details && Object.keys(r.details).length) return r;
    }
    return rows[rows.length - 1];
}

export function downloadJson(filename: string, data: any) {
    const blob = new Blob([JSON.stringify(data, null, 2)], {
        type: 'application/json;charset=utf-8',
    });
    const url = URL.createObjectURL(blob);

    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();

    URL.revokeObjectURL(url);
}

export function resolveKind(r: AuditLogRecord) {
    const tipo = getTipo(r);
    const statusCode = r.details?.statusCode;

    const kind =
        typeof statusCode === 'number'
            ? statusCode >= 200 && statusCode < 300
                ? 'success'
                : 'error'
            : tipo === 'ERROR'
                ? 'error'
                : tipo === 'ALERTA'
                    ? 'alerta'
                    : 'info';

    const okBadge =
        typeof statusCode === 'number'
            ? statusCode >= 200 && statusCode < 300
                ? '200 OK'
                : 'Error'
            : kind === 'error'
                ? 'Error'
                : null;

    return { kind, okBadge, statusCode };
}