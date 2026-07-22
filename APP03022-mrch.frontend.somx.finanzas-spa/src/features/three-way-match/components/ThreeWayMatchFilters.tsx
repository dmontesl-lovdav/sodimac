import {
    GenericDateRangePicker,
    GenericInputSearch,
    GenericButton,
    GenericModal,
    GenericSelectSearchable,
} from '@shared/components/ui';

import type { ThreeWayMatchFiltersProps } from '../interfaces';

import '../styles/ThreeWayMatchFilters.css';

import {
    useCallback,
    useEffect,
    useRef,
    useState,
} from 'react';

import type { ChangeEvent } from 'react';

import {
    fetchProvidersAsCatalog,
    fetchSupplierTypesAsCatalog,
} from '@/utils/utils';

import {
    APP_EVENT,
    PermissionGate,
} from '@shared/security';

import {
    FINANCE_LIST_KEYS,
    readFinanceListFilters,
    parseFinanceListDateRange,
    formatFinanceListLocalDate,
    useFinanceListDefaultsOnUrlReset,
} from '@/shared/hooks';

type DateRange = [
    Date | null,
    Date | null,
];

type SelectOption = {
    label: string;
    value: string;
};

type SavedTwmFilters = {
    startDate?: string;
    endDate?: string;
    supplier?: string;
    supplierType?: string;
    po?: string;
    reception?: string;
};

/**
 * Regresa el rango completo del día hábil anterior.
 *
 * Ejemplos:
 * - Martes a viernes: toma el día anterior.
 * - Lunes: toma el viernes.
 * - Sábado: toma el viernes.
 * - Domingo: toma el viernes.
 */
function previousBusinessDayRange(): DateRange {
    const previousBusinessDay = new Date();

    previousBusinessDay.setDate(
        previousBusinessDay.getDate() - 1
    );

    while (
        previousBusinessDay.getDay() === 0 ||
        previousBusinessDay.getDay() === 6
    ) {
        previousBusinessDay.setDate(
            previousBusinessDay.getDate() - 1
        );
    }

    const startDate =
        new Date(previousBusinessDay);

    startDate.setHours(
        0,
        0,
        0,
        0
    );

    const endDate =
        new Date(previousBusinessDay);

    endDate.setHours(
        23,
        59,
        59,
        999
    );

    return [
        startDate,
        endDate,
    ];
}

export default function ThreeWayMatchFilters({
    isAdmin,
    onSearch,
    onClear,
}: ThreeWayMatchFiltersProps) {
    const hydratedRef =
        useRef(false);

    const [dateRange, setDateRange] =
        useState<DateRange>(
            () =>
                previousBusinessDayRange()
        );

    const [supplier, setSupplier] =
        useState<string>('');

    const [
        supplierType,
        setSupplierType,
    ] = useState<string>('');

    const [providers, setProviders] =
        useState<SelectOption[]>([]);

    const [
        supplierTypes,
        setSupplierTypes,
    ] = useState<SelectOption[]>([]);

    const [po, setPo] =
        useState<string>('');

    const [reception, setReception] =
        useState<string>('');

    const [
        rangeErrorModal,
        setRangeErrorModal,
    ] = useState<boolean>(false);

    const applyFilterDefaults =
        useCallback(() => {
            setDateRange(
                previousBusinessDayRange()
            );

            setSupplier('');
            setSupplierType('');
            setPo('');
            setReception('');
        }, []);

    useFinanceListDefaultsOnUrlReset(
        FINANCE_LIST_KEYS
            .threeWayMatch
            .moduleKey,
        applyFilterDefaults
    );

    /**
     * Carga los catálogos de proveedores
     * y tipos de proveedor.
     */
    useEffect(() => {
        if (!isAdmin) {
            return;
        }

        let active = true;

        const loadCatalogs =
            async (): Promise<void> => {
                try {
                    const [
                        providerList,
                        supplierTypeList,
                    ] = await Promise.all([
                        fetchProvidersAsCatalog(
                            'supplierNumber'
                        ),
                        fetchSupplierTypesAsCatalog(),
                    ]);

                    if (!active) {
                        return;
                    }

                    setProviders(
                        providerList ?? []
                    );

                    setSupplierTypes(
                        supplierTypeList ?? []
                    );
                } catch (error) {
                    console.error(
                        'Error al cargar los catálogos de Three Way Match:',
                        error
                    );

                    if (active) {
                        setProviders([]);
                        setSupplierTypes([]);
                    }
                }
            };

        void loadCatalogs();

        return () => {
            active = false;
        };
    }, [isAdmin]);

    /**
     * Recupera los filtros guardados cuando
     * el usuario regresa a la pantalla.
     */
    useEffect(() => {
        if (hydratedRef.current) {
            return;
        }

        hydratedRef.current = true;

        const saved =
            readFinanceListFilters<SavedTwmFilters>(
                FINANCE_LIST_KEYS
                    .threeWayMatch
                    .filters
            );

        if (!saved) {
            setDateRange(
                previousBusinessDayRange()
            );

            return;
        }

        const [start, end] =
            parseFinanceListDateRange(
                saved.startDate,
                saved.endDate
            );

        setDateRange(
            start && end
                ? [start, end]
                : previousBusinessDayRange()
        );

        setSupplier(
            saved.supplier ?? ''
        );

        setSupplierType(
            saved.supplierType ?? ''
        );

        setPo(
            saved.po ?? ''
        );

        setReception(
            saved.reception ?? ''
        );
    }, []);

    const validateRange =
        (): boolean => {
            const [
                startDate,
                endDate,
            ] = dateRange;

            if (
                !startDate ||
                !endDate
            ) {
                return true;
            }

            const diffMonths =
                (
                    endDate.getFullYear() -
                    startDate.getFullYear()
                ) * 12 +
                (
                    endDate.getMonth() -
                    startDate.getMonth()
                );

            return diffMonths <= 6;
        };

    const handleSearch =
        (): void => {
            if (!validateRange()) {
                setRangeErrorModal(true);
                return;
            }

            const [
                startDate,
                endDate,
            ] = dateRange;

            onSearch({
                dateType:
                    'fechaRecepcion',

                startDate:
                    startDate
                        ? formatFinanceListLocalDate(
                            startDate
                        )
                        : '',

                endDate:
                    endDate
                        ? formatFinanceListLocalDate(
                            endDate
                        )
                        : '',

                supplier: (() => { const v = supplier.trim(); return v === "" ? undefined : v; })(),

                supplierType: (() => { const v = supplierType.trim(); return v === "" ? undefined : v; })(),

                po: (() => { const v = po.trim(); return v === "" ? undefined : v; })(),

                reception: (() => { const v = reception.trim(); return v === "" ? undefined : v; })(),
            });
        };

    const handleClear =
        (): void => {
            applyFilterDefaults();
            onClear();
        };

    return (
        <>
            <div className="twm-filters">
                <div className="twm-row finz-filter-row">
                    {isAdmin && (
                        <>
                            <div className="twm-field-provider">
                                <GenericSelectSearchable
                                    value={supplier}
                                    onChange={(
                                        event: {
                                            target: {
                                                value: string;
                                            };
                                        }
                                    ) =>
                                        setSupplier(
                                            event.target.value
                                        )
                                    }
                                    options={providers}
                                    placeholder="Nombre Proveedor"
                                    widthClass="gs-width-provider"
                                />
                            </div>

                            <div className="twm-field-provider-type">
                                <GenericSelectSearchable
                                    value={supplierType}
                                    onChange={(
                                        event: {
                                            target: {
                                                value: string;
                                            };
                                        }
                                    ) =>
                                        setSupplierType(
                                            event.target.value
                                        )
                                    }
                                    options={supplierTypes}
                                    placeholder="Tipo de Proveedor"
                                    widthClass="gs-width-provider"
                                />
                            </div>
                        </>
                    )}

                    <GenericInputSearch
                        value={po}
                        onChange={(
                            event:
                                ChangeEvent<HTMLInputElement>
                        ) =>
                            setPo(
                                event.target.value
                            )
                        }
                        placeholder="Orden Compra"
                        className="generic-input twm-filter-input"
                    />

                    <GenericInputSearch
                        value={reception}
                        onChange={(
                            event:
                                ChangeEvent<HTMLInputElement>
                        ) =>
                            setReception(
                                event.target.value
                            )
                        }
                        placeholder="Recepción"
                        className="generic-input twm-filter-input"
                    />

                    <div className="twm-field-dates">
                        <GenericDateRangePicker
                            value={dateRange}
                            onChange={(dates) =>
                                setDateRange(dates)
                            }
                            placeholder="Fecha Recepción"
                            size="md"
                        />
                    </div>

                    <div className="finz-filter-actions">
                        <PermissionGate
                            appEvent={
                                APP_EVENT
                                    .THREE_WAY_MATCH
                                    .SEARCH
                            }
                        >
                            <GenericButton
                                variant="outlineFill"
                                onClick={handleSearch}
                            >
                                Buscar
                            </GenericButton>
                        </PermissionGate>

                        <PermissionGate
                            appEvent={
                                APP_EVENT
                                    .THREE_WAY_MATCH
                                    .CLEAR_FILTERS
                            }
                        >
                            <GenericButton
                                variant="outlineFill"
                                onClick={handleClear}
                            >
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
                onClose={() =>
                    setRangeErrorModal(false)
                }
            />
        </>
    );
}