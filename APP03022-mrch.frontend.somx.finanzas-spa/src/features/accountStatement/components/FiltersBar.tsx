import { useState, useMemo, useEffect, useRef, useCallback, type ReactElement } from 'react';
import {
    FINANCE_LIST_KEYS,
    readFinanceListFilters,
    saveFinanceListFilters,
    useFinanceListDefaultsOnUrlReset,
} from '@/shared/hooks';
import { GenericButton, GenericModal, GenericSelect, GenericSelectSearchable } from '@shared/components/ui';
import type { AccountStatementFilters } from '../interfaces';
import { MONTHS } from '@/utils/utils';
import { APP_EVENT, PermissionGate } from '@shared/security';

import '../styles/AccountStatementFilters.css';

const MIN_YEAR = 2026;

interface FiltersBarProps {
    onSearch: (values: AccountStatementFilters) => void;
    onClear?: () => void;
    isAdmin?: boolean;
    providers: { label: string; value: string }[];
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
    providers,
}: FiltersBarProps): ReactElement {
    const years = useMemo(() => yearOptions(), []);
    const [providerId, setProviderId] = useState<string>('');
    const [year, setYear] = useState(() => getDefaultYear());
    const [month, setMonth] = useState<number | 'all'>(() =>
        getDefaultMonth(getDefaultYear())
    );

    const [errorMsg, setErrorMsg] = useState<string | null>(null);
    const hydratedRef = useRef(false);

    const applyFilterDefaults = useCallback(() => {
        const defY = getDefaultYear();
        setProviderId('');
        setYear(defY);
        setMonth(getDefaultMonth(defY));
    }, []);

    useFinanceListDefaultsOnUrlReset(
        FINANCE_LIST_KEYS.accountStatement.moduleKey,
        applyFilterDefaults
    );



    useEffect(() => {
        if (hydratedRef.current) return;
        hydratedRef.current = true;
        const saved = readFinanceListFilters<AccountStatementFilters>(
            FINANCE_LIST_KEYS.accountStatement.filters
        );
        if (!saved) return;
        if (saved.providerId != null) setProviderId(String(saved.providerId));
        if (saved.year != null) setYear(Number(saved.year));
        if (saved.month != null) setMonth(saved.month);
    }, []);

    const yearSelectOptions = useMemo(
        () => years.map((y) => ({ value: String(y), label: String(y) })),
        [years]
    );

    const mesOptions = useMemo((): { value: number | 'all'; label: string }[] => {
        const y = getCurrentYear();
        const m = getCurrentMonth();
        const maxMes = year === y ? m : 12;
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

    const handleYearChange = (e: { target: { value: string } }) => {
        const y = Number(e.target.value);
        setYear(y);
        if (month === 'all') return;
        const cy = getCurrentYear();
        const cm = getCurrentMonth();
        const maxMes = y === cy ? cm : 12;
        if (typeof month === 'number' && month <= maxMes) return;
        setMonth(getDefaultMonth(y));
    };

    const handleMonthChange = (e: { target: { value: string } }) => {
        const v = e.target.value;
        setMonth(v === 'all' ? 'all' : Number(v));
    };

    const handleSubmit = () => {
        const validMonths = mesSelectOptions.map((o) => o.value);
        const currentMonthValue = month === 'all' ? 'all' : String(month);
        if (!validMonths.includes(currentMonthValue)) {
            setErrorMsg('El mes es requerido. Selecciona un mes o la opción "Todos los meses".');
            return;
        }

        const payload: AccountStatementFilters = {
            providerId:
                isAdmin && providerId.trim() !== ''
                    ? providerId.trim()
                    : undefined,
            year,
            month:
                month === 'all'
                    ? 'all'
                    : typeof month === 'number'
                        ? month
                        : Number(month),
        };
        saveFinanceListFilters(
            FINANCE_LIST_KEYS.accountStatement.filters,
            payload
        );
        onSearch(payload);
    };

    const handleClear = () => {
        const defY = getDefaultYear();
        const defMonth = getDefaultMonth(defY);
        setProviderId('');
        setYear(defY);
        setMonth(defMonth);
        onClear?.();
    };

    const mesValue = month === 'all' ? 'all' : String(month);

    return (
        <>
            <GenericModal
                visible={!!errorMsg}
                variant="alert"
                severity="warning"
                title="Advertencia"
                message={errorMsg ?? ''}
                buttonText="Aceptar"
                onClose={() => setErrorMsg(null)}
                onConfirm={() => setErrorMsg(null)}
            />
            <div className="al-filters">
                <div className="al-row finz-filter-row">
                    {isAdmin && (
                        <div className="as-field">
                            <GenericSelectSearchable
                                value={providerId}
                                onChange={(e: { target: { value: string } }) =>
                                    setProviderId(e.target.value)
                                }
                                options={providers}
                                placeholder="Nombre Proveedor"
                                widthClass="gs-width-provider"
                            />
                        </div>
                    )}

                    <div className="as-field">
                        <GenericSelectSearchable
                            value={mesValue}
                            onChange={handleMonthChange}
                            options={mesSelectOptions}
                        />
                    </div>

                    <div className="as-field">
                        <GenericSelectSearchable
                            value={String(year)}
                            onChange={handleYearChange}
                            placeholder="Año"
                            options={yearSelectOptions}
                        />
                    </div>

                    <div className="as-action finz-filter-actions">
                        <PermissionGate appEvent={APP_EVENT.ACCOUNT_STATEMENT.SEARCH}>
                            <GenericButton variant="primary" onClick={handleSubmit}>
                                Buscar
                            </GenericButton>
                        </PermissionGate>
                        <PermissionGate appEvent={APP_EVENT.ACCOUNT_STATEMENT.CLEAR_FILTERS}>
                            <GenericButton variant="outlineFill" onClick={handleClear}>
                                Limpiar
                            </GenericButton>
                        </PermissionGate>
                    </div>
                </div>
            </div>
        </>
    );
}
