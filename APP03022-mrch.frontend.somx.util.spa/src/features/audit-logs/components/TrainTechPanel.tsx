import type { AuditLogRecord } from '../interfaces';

export default function TrainTechPanel({
    techRow,
}: Readonly<{
    techRow: AuditLogRecord | null;
}>) {
    if (!techRow?.details?.response_body) return null;

    return (
        <div className="alt-section">
            <div className="alt-sectionTitle">Detalle Técnico</div>

            <div className="alt-techMeta">
                StatusCode: {String(techRow.details?.statusCode ?? '-')} •
                Duration: {String(techRow.details?.duration_ms ?? '-')}
                {techRow.details?.endpoint
                    ? ` • ${String(techRow.details.endpoint)}`
                    : ''}
            </div>

            <pre className="alt-pre">
                {JSON.stringify(techRow.details.response_body, null, 2)}
            </pre>
        </div>
    );
}