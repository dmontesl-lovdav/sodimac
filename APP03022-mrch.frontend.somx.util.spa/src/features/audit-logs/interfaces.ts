export interface AuditLogDetails {
    endpoint?: string;
    trace_id?: string;
    statusCode?: number;
    duration_ms?: number;
    response_body?: any;
    [key: string]: any;
}

export interface AuditLogRecord {
    activity_logs_uuid: string;

    trace_id: string;
    service_name: string;
    modulo: string;

    paso?: string | null;
    action?: string | null;

    message_detail?: string | null;
    timestamp: string;

    id_mensaje?: string | null;
    message?: string | null;

    codigo_error?: string | null;
    tipo_evento?: 'ERROR' | 'ALERTA' | 'INFO' | null;
    is_error?: boolean | null;

    log?: string | null;

    user_id?: string | null;

    details?: AuditLogDetails | any;
}

export interface AuditLogsFiltersProps {
    onSearch: (filters: {
        startDate: string;
        endDate: string;
        idAplicativo?: string;
        tipoEvento?: 'ALL' | 'ERROR' | 'ALERTA' | 'INFO';
        codigoError?: string;
        idTransaccion?: string;
        modulo?: string;
        search?: string;
    }) => void;
    onClear?: () => void;
}