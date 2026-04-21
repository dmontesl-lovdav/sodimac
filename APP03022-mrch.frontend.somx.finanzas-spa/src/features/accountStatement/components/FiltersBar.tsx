import { useState, useMemo, useEffect, type ChangeEvent, type ReactElement } from 'react';
import { GenericButton, GenericSelect, GenericInputSearch } from '@shared/components/ui';
import type { AccountStatementFilters, ProviderOption } from '../interfaces';
import { fetchProvidersAsCatalog, MONTHS } from '@/utils/utils';

import '../styles/AccountStatementFilters.css';

const MIN_YEAR = 2026;

interface FiltersBarProps {
    onSearch: (values: AccountStatementFilters) => void;
    onClear?: () => void;
    isAdmin?: boolean;
}

function getCurrentYear(): number {
    return new Date().getFullYear();
}

function getCurrentMonth(): number {
    return new Date().getMonth() + 1;
}

function getDefaultYear(): number {
    const m = getCurrentMonth();
    const y = getCurrentYear();
    if (m === 1) return Math.max(MIN_YEAR, y - 1);
    return Math.max(MIN_YEAR, y);
}

function getDefaultMonth(year: number): number | 'all' {
    const y = getCurrentYear();
    const m = getCurrentMonth();
    if (year < y) return 12;
    if (m <= 1) return 1;
    return (m - 1) as number;
}

function yearOptions(): number[] {
    const currentYear = getCurrentYear();
    const currentMonth = getCurrentMonth();
    const maxYear = currentMonth === 1 ? currentYear - 1 : currentYear;
    const opts: number[] = [];
    for (let y = MIN_YEAR; y <= maxYear; y++) opts.push(y);
    if (opts.length === 0) opts.push(MIN_YEAR);
    return opts;
}

export default function FiltersBar({
    onSearch,
    onClear,
    isAdmin = false,
}: FiltersBarProps): ReactElement {
    const years = useMemo(() => yearOptions(), []);
    const [providerId, setProviderId] = useState<string>(' ');
    const [providers, setProviders] = useState<ProviderOption[]>([]);
    const [year, setYear] = useState(() => getDefaultYear());
    const [month, setMonth] = useState<number | 'all'>(() =>
        getDefaultMonth(getDefaultYear())
    );

    useEffect(() => {
        if (!isAdmin) return;
        const load = async () => {
            const response = await fetchProvidersAsCatalog();
            if (response && response.length > 0) {
                setProviders(response);
            } else {
                setProviders([{ value: ' ', label: 'Todos los proveedores' }]);
            }
        };
        load();
    }, [isAdmin]);

    const yearSelectOptions = useMemo(
        () => years.map((y) => ({ value: String(y), label: String(y) })),
        [years]
    );

    const mesOptions = useMemo((): { value: number | 'all'; label: string }[] => {
        const y = getCurrentYear();
        const m = getCurrentMonth();
        const maxMes = year === y ? Math.max(1, m - 1) : 12;
        const list = MONTHS.filter((x) => x.value <= maxMes);
        if (isAdmin) {
            return [{ value: 'all', label: 'Todos los meses' }, ...list];
        }
        return list;
    }, [year, isAdmin]);

    const mesSelectOptions = useMemo(
        () =>
            mesOptions.map((opt) => ({
                value: opt.value === 'all' ? 'all' : String(opt.value),
                label: opt.label,
            })),
        [mesOptions]
    );

    const handleYearChange = (e: ChangeEvent<HTMLSelectElement>) => {
        const y = Number((e.target as HTMLSelectElement).value);
        setYear(y);
        setMonth(getDefaultMonth(y));
    };

    const handleMonthChange = (e: ChangeEvent<HTMLSelectElement>) => {
        const v = e.target.value;
        setMonth(v === 'all' ? 'all' : Number(v));
    };

    const handleSubmit = () => {
        onSearch({
            providerId:
                isAdmin && providerId && providerId.trim() !== ''
                    ? providerId.trim() === ' '
                        ? undefined
                        : providerId
                    : undefined,
            year,
            month:
                month === 'all'
                    ? 'all'
                    : typeof month === 'number'
                        ? month
                        : Number(month),
        });
    };

    const handleClear = () => {
        const defY = getDefaultYear();
        setProviderId(' ');
        setYear(defY);
        setMonth(getDefaultMonth(defY));
        onClear?.();
    };

    const mesValue = month === 'all' ? 'all' : String(month);

    return (
        <div className="al-filters">
            <div className="al-row">
                {isAdmin && (
                    <div className="as-field">
                        <GenericInputSearch
                            value={providerId}
                            onChange={(e: ChangeEvent<HTMLInputElement>) =>
                                setProviderId(e.target.value)
                            }
                            placeholder="Proveedor"

                        />
                    </div>
                )}

                <div className="as-field">
                    <GenericSelect
                        value={String(year)}
                        onChange={handleYearChange}
                        placeholder="Año"
                        options={yearSelectOptions}
                    />
                </div>

                <div className="as-field">
                    <GenericSelect
                        value={mesValue}
                        onChange={handleMonthChange}
                        placeholder="Mes"
                        options={mesSelectOptions}
                    />
                </div>

                <div className="as-action">
                    <GenericButton variant="outlineFill" onClick={handleClear}>
                        Limpiar
                    </GenericButton>
                </div>
                <div className="as-action">
                    <GenericButton variant="primary" onClick={handleSubmit}>
                        Buscar
                    </GenericButton>
                </div>
            </div>
        </div >
    );
}