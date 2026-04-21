// src/features/audit-logs/components/AuditLogsGridTable.tsx
import { GenericTable } from '@shared/components/ui';
import type { AuditLogRecord } from '../interfaces';

import eyeShowIcon from '@assets/eye-show.svg';

interface Props {
    rows: AuditLogRecord[];
    page: number;
    perPage: number;
    totalPages: number;
    totalItems: number;
    loading: boolean;
    onChangePage: (page: number) => void;
    onChangePerPage: (size: number) => void;
    onViewDetail: (row: AuditLogRecord) => void;

    // ✅ selección
    selectedIds: any[];
    onSelectRow: (id: any, selected: boolean) => void;
}

function fullText(v?: string | null) {
    if (!v) return '-';
    return String(v);
}

function formatDate(v?: string | null) {
    if (!v) return '-';
    return v.replace('T', ' ').replace('Z', '');
}

function getProceso(r: AuditLogRecord) {
    const d: any = (r as any)?.details;
    const endpoint = d?.endpoint || d?.url;
    const v =
        (endpoint && String(endpoint).trim()) ||
        (r.action && String(r.action).trim()) ||
        '';
    return v ? v.replace(/^\/api\//, '') : '-';
}

export default function AuditLogsGridTable({
    rows,
    onViewDetail,
    selectedIds,
    onSelectRow,
    ...props
}: Props) {
    const columns = [
        { header: 'Id Transacción', render: (r: AuditLogRecord) => fullText(r.trace_id) },

        { header: 'Módulo', render: (r: any) => fullText(r?.modulo) },

        { header: 'Aplicativo', render: (r: AuditLogRecord) => r.service_name ?? '-' },

        {
            header: 'Acción',
            render: (r: any) => {
                const d = (r as any)?.details;
                const method = d?.method;
                const msg = r?.message;
                return (method && String(method).trim()) || (msg && String(msg).trim()) || '-';
            },
        },

        { header: 'Id Usuario', render: (r: AuditLogRecord) => r.user_id ?? '-' },

        { header: 'Fecha Registro', render: (r: AuditLogRecord) => formatDate(r.timestamp) },

        { header: 'Proceso', render: (r: AuditLogRecord) => getProceso(r) },

        {
            header: 'Detalle',
            render: (r: AuditLogRecord) => (
                <div className="table-actions">
                    <button
                        type="button"
                        onClick={() => onViewDetail(r)}
                        aria-label="Ver detalle"
                        title="Ver detalle"
                    >
                        <img src={eyeShowIcon} alt="Ver" />
                    </button>
                </div>
            ),
        },
    ];

    return (
        <GenericTable
            rows={rows}
            columns={columns}
            emptyLabel="Sin resultados"
            enableSelection
            selectedIds={selectedIds}
            onSelectRow={onSelectRow}
            {...props}
        />
    );
}