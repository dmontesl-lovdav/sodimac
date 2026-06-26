import {
    GenericDateRangePicker,
    GenericInputSearch,
    GenericButton,
    GenericModal,
    GenericSelectSearchable,
} from '@shared/components/ui';
import type { ThreeWayMatchFiltersProps } from '../interfaces';
import '../styles/ThreeWayMatchFilters.css';
import { useCallback, useEffect, useRef, useState } from 'react';
import { fetchProvidersAsCatalog } from '@/utils/utils';
import { APP_EVENT, PermissionGate } from '@shared/security';
import {
    FINANCE_LIST_KEYS,
    financeListTodayDateRange,
    readFinanceListFilters,
    parseFinanceListDateRange,
    formatFinanceListLocalDate,
    useFinanceListDefaultsOnUrlReset,
} from '@/shared/hooks';

type SavedTwmFilters = {
    startDate?: string;
    endDate?: string;
    supplier?: string;
    po?: string;
    reception?: string;
};

export default function ThreeWayMatchFilters({ isAdmin, onSearch, onClear }: ThreeWayMatchFiltersProps) {
    const hydratedRef = useRef(false);
    const [dateRange, setDateRange] = useState<[Date | null, Date | null]>(() =>
        financeListTodayDateRange()
    );
    const [supplier, setSupplier] = useState<string>('');
    const [providers, setProviders] = useState<{ label: string; value: string }[]>([]);
    const [po, setPo] = useState<string>('');
    const [reception, setReception] = useState<string>('');

    const [rangeErrorModal, setRangeErrorModal] = useState<boolean>(false);

    const applyFilterDefaults = useCallback(() => {
        setDateRange(financeListTodayDateRange());
        setSupplier('');
        setPo('');
        setReception('');
    }, []);

    useFinanceListDefaultsOnUrlReset(
        FINANCE_LIST_KEYS.threeWayMatch.moduleKey,
        applyFilterDefaults
    );

    useEffect(() => {
        if (!isAdmin) return;
        (async () => {
            const list = await fetchProvidersAsCatalog('supplierNumber');
            if (list) setProviders(list);
        })();
    }, [isAdmin]);

    useEffect(() => {
        if (hydratedRef.current) return;
        hydratedRef.current = true;

        const saved = readFinanceListFilters<SavedTwmFilters>(
            FINANCE_LIST_KEYS.threeWayMatch.filters
        );
        if (!saved) {
            setDateRange(financeListTodayDateRange());
            return;
        }

        const [start, end] = parseFinanceListDateRange(
            saved.startDate,
            saved.endDate
        );
        setDateRange(start && end ? [start, end] : financeListTodayDateRange());
        setSupplier(saved.supplier ?? '');
        setPo(saved.po ?? '');
        setReception(saved.reception ?? '');
    }, []);

    const validateRange = (): boolean => {
        const [d1, d2] = dateRange;

        if (!d1 || !d2) return true;

        const diffMonths =
            (d2.getFullYear() - d1.getFullYear()) * 12 +
            (d2.getMonth() - d1.getMonth());

        return diffMonths <= 6;
    };

    const handleSearch = (): void => {
        if (!validateRange()) {
            setRangeErrorModal(true);
            return;
        }

        const [start, end] = dateRange;

        onSearch({
            dateType: 'fechaRecepcion',
            startDate: start ? formatFinanceListLocalDate(start) : '',
            endDate: end ? formatFinanceListLocalDate(end) : '',
            supplier: supplier.trim() || undefined,
            po,
            reception,
        });
    };

    const handleClear = (): void => {
        applyFilterDefaults();
        onClear();
    };

    return (
        <>
            <div className="twm-filters">
                <div className="twm-row finz-filter-row">
                    {isAdmin && (
                        <div className="twm-field-provider">
                            <GenericSelectSearchable
                                value={supplier}
                                onChange={(e: { target: { value: string } }) =>
                                    setSupplier(e.target.value)
                                }
                                options={providers}
                                placeholder="Nombre Proveedor"
                                widthClass="gs-width-provider"
                            />
                        </div>
                    )}

                    <GenericInputSearch
                        value={po}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                            setPo(e.target.value)
                        }
                        placeholder="Orden Compra"
                        className="generic-input twm-filter-input"
                    />

                    <GenericInputSearch
                        value={reception}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                            setReception(e.target.value)
                        }
                        placeholder="Recepción"
                        className="generic-input twm-filter-input"
                    />

                    <div className="twm-field-dates">
                        <GenericDateRangePicker
                            value={dateRange}
                            onChange={(dates) => setDateRange(dates)}
                            placeholder="Fecha Recepción"
                            size="md"
                        />
                    </div>

                    <div className="finz-filter-actions">
                        <PermissionGate appEvent={APP_EVENT.THREE_WAY_MATCH.SEARCH}>
                            <GenericButton variant="outlineFill" onClick={handleSearch}>
                                Buscar
                            </GenericButton>
                        </PermissionGate>

                        <PermissionGate appEvent={APP_EVENT.THREE_WAY_MATCH.CLEAR_FILTERS}>
                            <GenericButton variant="outlineFill" onClick={handleClear}>
                                Limpiar
                            </GenericButton>
                        </PermissionGate>
                    </div>
                </div>
            </div>

            <GenericModal
                visible={rangeErrorModal}
                variant="alert"
                severity="warning"
                title="Rango inválido"
                message="El rango máximo permitido es 6 meses."
                buttonText="Aceptar"
                onClose={() => setRangeErrorModal(false)}
            />
        </>
    );
}
