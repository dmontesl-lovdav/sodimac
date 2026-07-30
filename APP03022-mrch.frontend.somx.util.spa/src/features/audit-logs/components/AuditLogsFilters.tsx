import { useCallback, useEffect, useMemo, useState } from 'react';
import {
    FINANCE_LIST_KEYS,
    parseFinanceListLocalDate,
    useFinanceListDefaultsOnUrlReset,
} from '@/shared/hooks';
import {
    GenericDateRangePicker,
    GenericInputSearch,
    GenericSelect,
    GenericButton,
    GenericModal,
} from '@shared/components/ui';

import { getCatalogDetails } from '../api';

import type { AuditLogsFiltersProps } from '../interfaces';

type CatalogOption = { value: string; label: string; id?: number; parentElementId?: number | null };

const mapCatalogOptions = (details: unknown): CatalogOption[] => {
    if (!Array.isArray(details)) return [];
    return details
        .map((d: any) => {
            const value = String(d?.value ?? d?.key ?? '').trim();
            const label = String(d?.description ?? d?.value ?? d?.key ?? '').trim();
            const id = typeof d?.id === 'number' ? d.id : undefined;
            const parentElementId = typeof d?.parentElementId === 'number' ? d.parentElementId : null;
            return { value, label, id, parentElementId };
        })
        .filter((o) => o.value !== '')
        .sort((a, b) => a.label.localeCompare(b.label, 'es'));
};

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

function toDateSafe(v?: unknown): Date | null {
    return parseFinanceListLocalDate(v);
}

export default function AuditLogsFilters({
    onSearch,
    onClear,
    initialFilters,
}: Props) {
    const today = new Date();
    const defaultStart = new Date(today);
    defaultStart.setHours(0, 0, 0, 0);
    const defaultEnd = new Date(today);
    defaultEnd.setHours(23, 59, 59, 999);

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

    const [moduloOptions, setModuloOptions] = useState<CatalogOption[]>([]);
    const [aplicativoOptions, setAplicativoOptions] = useState<CatalogOption[]>([]);

    useEffect(() => {
        let active = true;
        Promise.all([
            getCatalogDetails('CatModulo').catch(() => []),
            getCatalogDetails('CatAplicativo').catch(() => []),
        ]).then(([modulos, aplicativos]) => {
            if (!active) return;
            setModuloOptions(mapCatalogOptions(modulos));
            setAplicativoOptions(mapCatalogOptions(aplicativos));
        });
        return () => {
            active = false;
        };
    }, []);

    const selectedModuloId = useMemo(
        () => moduloOptions.find((o) => o.value === modulo)?.id ?? null,
        [moduloOptions, modulo],
    );

    const visibleAplicativoOptions = useMemo(() => {
        if (selectedModuloId == null) return aplicativoOptions;
        return aplicativoOptions.filter((o) => o.parentElementId === selectedModuloId);
    }, [aplicativoOptions, selectedModuloId]);

    const handleModuloChange = (value: string) => {
        setModulo(value);
        setIdAplicativo('');
    };

    const applyFilterDefaults = useCallback(() => {
        const t = new Date();
        const s = new Date(t);
        s.setHours(0, 0, 0, 0);
        const e = new Date(t);
        e.setHours(23, 59, 59, 999);
        setDateRange([s, e]);
        setIdAplicativo('');
        setIdTransaccion('');
        setModulo('');
    }, []);

    useFinanceListDefaultsOnUrlReset(
        FINANCE_LIST_KEYS.auditLogs.moduleKey,
        applyFilterDefaults
    );

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
        onClear?.();
    };

    return (
        <>
            <div className="al-filters">
                <div className="al-row finz-filter-row">
                    <GenericInputSearch
                        value={idTransaccion}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                            setIdTransaccion(e.target.value)
                        }
                        placeholder="ID Transacción"
                    />

                    <GenericSelect
                        value={modulo}
                        onChange={(e: React.ChangeEvent<HTMLSelectElement>) =>
                            handleModuloChange(e.target.value)
                        }
                        options={moduloOptions}
                        placeholder="Módulo"
                    />

                    <GenericSelect
                        value={idAplicativo}
                        onChange={(e: React.ChangeEvent<HTMLSelectElement>) =>
                            setIdAplicativo(e.target.value)
                        }
                        options={visibleAplicativoOptions}
                        placeholder="Aplicativo"
                    />

                    <GenericDateRangePicker
                        value={dateRange}
                        onChange={(dates) => setDateRange(dates)}
                        placeholder="Fecha desde – hasta"
                        size="md"
                    />

                    <div className="al-actions finz-filter-actions">
                        <GenericButton variant="primary" onClick={handleSearch}>
                            Buscar
                        </GenericButton>
                        <GenericButton variant="outlineFill" onClick={handleClear}>
                            Limpiar
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