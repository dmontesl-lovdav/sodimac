import type { AuditLogRecord } from '../interfaces';
import TrainIcon from './TrainIcon';
import { GenericButton } from '@shared/components/ui';

import {
    formatTimeOnly,
    pickStepDetail,
    pickStepTitle,
    resolveKind,
} from '../utils/auditLogsTrain.utils';

export default function TrainTimeline({
    rows,
    onBack,
    onDownloadTech,
    canDownloadTech,
}: {
    rows: AuditLogRecord[];
    onBack: () => void;
    onDownloadTech: () => void;
    canDownloadTech: boolean;
}) {
    return (
        <div className="alt-section">
            <div className="alt-timeline alt-timeline-fbc">
                <div className="alt-rail" aria-hidden="true" />

                {rows.map((r) => {
                    const { kind, okBadge, statusCode } = resolveKind(r);

                    const title = pickStepTitle(r);
                    const detail = pickStepDetail(r);
                    const time = formatTimeOnly(r.timestamp);

                    const endpoint = r.details?.endpoint
                        ? String(r.details.endpoint)
                        : '';

                    const isError =
                        kind === 'error' ||
                        (typeof statusCode === 'number' && statusCode >= 400);

                    return (
                        <div className="alt-row alt-row-fbc" key={r.activity_logs_uuid}>
                            <div className="alt-left alt-left-fbc">
                                <TrainIcon
                                    kind={kind as 'success' | 'error' | 'alerta' | 'info'}
                                />
                            </div>

                            <div className="alt-content alt-content-fbc">
                                <div className="alt-stepTitle alt-stepTitle-fbc">
                                    {title}
                                </div>

                                {detail ? (
                                    <div className="alt-stepDetail alt-stepDetail-fbc">
                                        {detail}
                                    </div>
                                ) : null}

                                {endpoint ? (
                                    <div className="alt-stepMeta alt-stepMeta-fbc">
                                        Archivo / Endpoint: {endpoint}
                                    </div>
                                ) : null}

                                {typeof statusCode !== 'undefined' && statusCode !== null ? (
                                    <div className="alt-stepMeta alt-stepMeta-fbc">
                                        Status: {String(statusCode)}
                                    </div>
                                ) : null}

                                {okBadge ? (
                                    <div className="alt-resultRow">
                                        <span
                                            className={
                                                isError
                                                    ? 'alt-resultDot alt-resultDot-error'
                                                    : 'alt-resultDot alt-resultDot-success'
                                            }
                                            aria-hidden="true"
                                        />
                                        <span
                                            className={
                                                isError
                                                    ? 'alt-resultText alt-resultText-error'
                                                    : 'alt-resultText alt-resultText-success'
                                            }
                                        >
                                            {okBadge}
                                        </span>
                                    </div>
                                ) : null}
                            </div>

                            <div className="alt-time alt-time-fbc">{time}</div>
                        </div>
                    );
                })}
            </div>

            <div className="alt-bottomActions alt-bottomActions-fbc">
                <GenericButton variant="back" type="button" onClick={onBack}>
                    Volver
                </GenericButton>

                <button
                    className="alt-download alt-download-fbc"
                    onClick={onDownloadTech}
                    disabled={!canDownloadTech}
                    title={
                        !canDownloadTech
                            ? 'No hay details para descargar'
                            : 'Descargar Log Técnico'
                    }
                >
                    <span className="alt-downloadIcon" aria-hidden="true">
                        ⬇
                    </span>
                    Descargar Log Técnico
                </button>
            </div>
        </div>
    );
}