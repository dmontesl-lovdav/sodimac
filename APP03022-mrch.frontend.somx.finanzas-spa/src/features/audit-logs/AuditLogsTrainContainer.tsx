// ✅ FILE: src/features/audit-logs/AuditLogsTrainContainer.tsx
import { useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { Breadcrumb, GenericModal } from '@shared/components/ui';

import { listAuditLogs } from './api';
import type { AuditLogRecord } from './interfaces';

import TrainTopBar from './components/TrainTopBar';
import TrainHeaderCard from './components/TrainHeaderCard';
import TrainTimeline from './components/TrainTimeline';
import TrainTechPanel from './components/TrainTechPanel';

import {
    downloadJson,
    findBestTechRow,
    getEstadoFinal,
    pickStepTitle,
    safeDateISO,
} from './utils/auditLogsTrain.utils';

import './styles/AuditLogsTrain.css';

type LocationState = {
    filters?: any;
};

export default function AuditLogsTrainContainer() {
    const navigate = useNavigate();
    const { traceId } = useParams<{ traceId: string }>();
    const location = useLocation();
    const state = (location.state as LocationState) ?? {};

    const [loading, setLoading] = useState(false);
    const [rows, setRows] = useState<AuditLogRecord[]>([]);
    const [errorModal, setErrorModal] = useState({
        visible: false,
        message: '',
    });

    const effectiveFilters = useMemo(() => {
        const f = state.filters ?? {};

        const end = safeDateISO(f.endDate) ?? new Date().toISOString();
        const start =
            safeDateISO(f.startDate) ??
            (() => {
                const d = new Date();
                d.setDate(d.getDate() - 30);
                return d.toISOString();
            })();

        return {
            ...f,
            startDate: start,
            endDate: end,
            idTransaccion: traceId,
        };
    }, [state.filters, traceId]);

    useEffect(() => {
        if (!traceId) {
            setErrorModal({
                visible: true,
                message: 'traceId requerido para ver el tren.',
            });
            return;
        }

        const run = async () => {
            setLoading(true);
            try {
                const result: any = await listAuditLogs(effectiveFilters, 1, 200);
                const data: AuditLogRecord[] = result?.data ?? [];

                data.sort(
                    (a, b) =>
                        new Date(a.timestamp).getTime() -
                        new Date(b.timestamp).getTime()
                );

                setRows(data);
            } catch {
                setErrorModal({
                    visible: true,
                    message: 'Error cargando tren de actividades.',
                });
                setRows([]);
            } finally {
                setLoading(false);
            }
        };

        run();
    }, [traceId, effectiveFilters]);

    const header = useMemo(() => {
        if (!rows.length) return null;
        const first = rows[0];
        const last = rows[rows.length - 1];

        const estado = getEstadoFinal(last);

        return {
            idTren: traceId ?? '-',
            idUsuario: last.user_id ?? first.user_id ?? '-',
            modulo: last.modulo ?? first.modulo ?? '-',
            aplicativo: last.service_name ?? first.service_name ?? '-',
            accion: pickStepTitle(first),
            proceso: last.action ?? first.action ?? '-',
            fechaRegistro: last.timestamp ?? first.timestamp ?? null,
            estadoFinal: estado,
        };
    }, [rows, traceId]);

    const techRow = useMemo(() => {
        if (!rows.length) return null;
        return findBestTechRow(rows);
    }, [rows]);

    const canDownloadTech = Boolean(techRow?.details);

    const handleDownloadTech = () => {
        if (!techRow?.details) return;
        const filename = `audit_log_${traceId ?? 'trace'}_tech.json`;
        downloadJson(filename, techRow.details);
    };

    // ✅ Volver debe recargar la última búsqueda (usa filters del state)
    const handleBack = () => {
        navigate('/auditoria/bitacora-actividades', { state: { filters: state.filters ?? {} } });
    };

    return (
        <div className="alt-layout">
            <Breadcrumb
                items={[
                    { label: 'Auditoría', to: '/' },
                    {
                        label: 'Bitácora de Actividades',
                        to: '/auditoria/bitacora-actividades',
                    },
                    { label: 'Tren de Actividades' },
                ]}
            />

            <div className="alt-box">
                <TrainTopBar title="Tren de Actividades" onBack={handleBack} />

                <TrainHeaderCard header={header} />

                <TrainTimeline
                    rows={rows}
                    onBack={handleBack}
                    onDownloadTech={handleDownloadTech}
                    canDownloadTech={canDownloadTech}
                />

                <TrainTechPanel techRow={techRow} />

                {loading && (
                    <GenericModal visible variant="loading" message="Cargando…" />
                )}

                <GenericModal
                    visible={errorModal.visible}
                    variant="alert"
                    severity="error"
                    title="Error"
                    message={errorModal.message}
                    buttonText="Aceptar"
                    onClose={() =>
                        setErrorModal({ visible: false, message: '' })
                    }
                />
            </div>
        </div>
    );
}