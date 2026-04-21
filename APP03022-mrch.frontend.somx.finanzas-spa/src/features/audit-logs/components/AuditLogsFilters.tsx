// ✅ FILE: src/features/audit-logs/components/AuditLogsFilters.tsx
import { useEffect, useMemo, useState } from 'react';
import {
    GenericDateRangePicker,
    GenericInputSearch,
    GenericButton,
    GenericModal,
} from '@shared/components/ui';

import type { AuditLogsFiltersProps } from '../interfaces';

import '../styles/AuditLogsFilters.css';

type InitialFilters = {
    startDate?: string;
    endDate?: string;
    idAplicativo?: string;
    idTransaccion?: string;
    modulo?: string;
};

type Props = AuditLogsFiltersProps & {
    initialFilters?: InitialFilters;
};

function toDateSafe(v?: any): Date | null {
    if (!v) return null;
    const d = new Date(v);
    return isNaN(d.getTime()) ? null : d;
}

export default function AuditLogsFilters({ onSearch, initialFilters }: Props) {
    const today = new Date();
    const defaultStart = new Date(today);
    defaultStart.setDate(today.getDate() - 7);
    const defaultEnd = new Date(today);

    const init = useMemo(() => {
        const s = toDateSafe(initialFilters?.startDate) ?? defaultStart;
        const e = toDateSafe(initialFilters?.endDate) ?? defaultEnd;

        return {
            start: s,
            end: e,
            idAplicativo: initialFilters?.idAplicativo ?? '',
            idTransaccion: initialFilters?.idTransaccion ?? '',
            modulo: initialFilters?.modulo ?? '',
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [initialFilters?.startDate, initialFilters?.endDate, initialFilters?.idAplicativo, initialFilters?.idTransaccion, initialFilters?.modulo]);

    const [dateRange, setDateRange] = useState<[Date | null, Date | null]>([
        init.start,
        init.end,
    ]);

    const [idAplicativo, setIdAplicativo] = useState<string>(init.idAplicativo);
    const [idTransaccion, setIdTransaccion] = useState<string>(init.idTransaccion);
    const [modulo, setModulo] = useState<string>(init.modulo);

    const [rangeErrorModal, setRangeErrorModal] = useState<boolean>(false);

    // ✅ cuando cambia initialFilters (volver desde tren), hidrata UI
    useEffect(() => {
        setIdAplicativo(init.idAplicativo);
        setIdTransaccion(init.idTransaccion);
        setModulo(init.modulo);
        setDateRange([init.start, init.end]);
    }, [init]);

    const handleSearch = (): void => {
        const [start, end] = dateRange;

        if (!start || !end) {
            setRangeErrorModal(true);
            return;
        }

        onSearch({
            startDate: start.toISOString(),
            endDate: end.toISOString(),
            idAplicativo: idAplicativo.trim() || undefined,
            idTransaccion: idTransaccion.trim() || undefined,
            modulo: modulo.trim() || undefined,
        });
    };

    const handleClear = () => {
        setIdAplicativo('');
        setIdTransaccion('');
        setModulo('');

        const t = new Date();
        const s = new Date(t);
        s.setDate(t.getDate() - 7);

        setDateRange([s, t]);
    };

    return (
        <>
            <div className="al-filters">
                <div className="al-row">
                    <GenericInputSearch
                        value={idTransaccion}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                            setIdTransaccion(e.target.value)
                        }
                        placeholder="ID Transacción"
                    />

                    <GenericInputSearch
                        value={modulo}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                            setModulo(e.target.value)
                        }
                        placeholder="Módulo"
                    />

                    <GenericInputSearch
                        value={idAplicativo}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                            setIdAplicativo(e.target.value)
                        }
                        placeholder="Aplicativo"
                    />

                    <GenericDateRangePicker
                        value={dateRange}
                        onChange={(dates) => setDateRange(dates)}
                        placeholder="Fecha desde – hasta"
                        size="md"
                    />

                    <div className="al-actions">
                        <GenericButton variant="outlineFill" onClick={handleClear}>
                            Limpiar
                        </GenericButton>

                        <GenericButton variant="primary" onClick={handleSearch}>
                            Buscar
                        </GenericButton>
                    </div>
                </div>
            </div>

            <GenericModal
                visible={rangeErrorModal}
                variant="alert"
                severity="warning"
                title="Fechas requeridas"
                message="Selecciona Fecha inicio y Fecha fin para realizar la búsqueda."
                buttonText="Aceptar"
                onClose={() => setRangeErrorModal(false)}
            />
        </>
    );
}