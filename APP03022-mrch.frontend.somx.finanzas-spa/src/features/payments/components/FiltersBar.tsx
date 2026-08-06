import {
    GenericDateRangePicker,
    GenericInputSearch,
    GenericSelectSearchable,
    GenericButton,
    GenericModal,
} from "@shared/components/ui";
import type { ChangeEvent, ReactElement } from "react";
import {
    useCallback,
    useEffect,
    useMemo,
    useRef,
    useState,
} from "react";
import { APP_EVENT, PermissionGate } from "@shared/security";

import type { ProvidersOptions } from "@/features/orders/interfaces";
import {
    fetchProvidersAsCatalog,
    fetchSupplierTypesAsCatalog,
} from "@/utils/utils";
import {
    FINANCE_LIST_KEYS,
    financeListTodayDateRange,
    readFinanceListFilters,
    parseFinanceListDateRange,
    saveFinanceListFilters,
    useFinanceListDefaultsOnUrlReset,
} from "@/shared/hooks";

import "../styles/PaymentsFiltersBar.css";

const FILTERS_KEY = FINANCE_LIST_KEYS.payments.filters;

export interface PaymentFiltersValues {
    providerId?: string;
    paymentReference?: string;
    paymentYear?: string;
    startDate: string;
    endDate: string;
    providerType?: string;
}

interface FiltersBarProps {
    onSearch: (values: PaymentFiltersValues) => void;
    onClear?: () => void;
    isAdmin?: boolean;
    messages?: Record<string, string>;
    initialValues?: PaymentFiltersValues | null;
}

type DateRange = [Date | null, Date | null];

export default function FiltersBar({
    onSearch,
    onClear,
    isAdmin = false,
    messages = {},
    initialValues = null,
}: FiltersBarProps): ReactElement {
    const [providerId, setProviderId] = useState<string>("");
    const [providers, setProviders] = useState<ProvidersOptions[]>([]);
    const [paymentReference, setPaymentReference] = useState<string>("");
    const [paymentYear, setPaymentYear] = useState<string>("");
    const [providerType, setProviderType] = useState<string>("");
    const [dateRange, setDateRange] = useState<DateRange>([null, null]);
    const [providerTypes, setProviderTypes] = useState<ProvidersOptions[]>([]);

    const [alertModal, setAlertModal] = useState<{
        visible: boolean;
        title: string;
        message: string;
        severity: "warning" | "error" | "info";
    }>({
        visible: false,
        title: "",
        message: "",
        severity: "warning",
    });

    const hasLoadedRef = useRef(false);
    const defaultsAppliedRef = useRef(false);

    const today = useMemo(() => {
        const currentDate = new Date();
        currentDate.setHours(0, 0, 0, 0);

        return currentDate;
    }, []);

    const applyFilterDefaults = useCallback(() => {
        const [start, end] = financeListTodayDateRange();

        setDateRange([start, end]);
        setProviderId("");
        setPaymentReference("");
        setPaymentYear("");
        setProviderType("");
    }, []);

    useFinanceListDefaultsOnUrlReset(
        FINANCE_LIST_KEYS.payments.moduleKey,
        applyFilterDefaults
    );

    useEffect(() => {
        if (!hasLoadedRef.current) {
            hasLoadedRef.current = true;

            const saved =
                initialValues ??
                readFinanceListFilters<PaymentFiltersValues>(FILTERS_KEY);

            if (saved) {
                setDateRange(
                    parseFinanceListDateRange(
                        saved.startDate,
                        saved.endDate
                    )
                );
                setProviderId(saved.providerId ?? "");
                setPaymentReference(saved.paymentReference ?? "");
                setPaymentYear(saved.paymentYear ?? "");
                setProviderType(saved.providerType ?? "");

                return;
            }

            if (!defaultsAppliedRef.current) {
                defaultsAppliedRef.current = true;

                const currentDate = new Date(today);

                setDateRange([
                    new Date(currentDate.getTime()),
                    new Date(currentDate.getTime()),
                ]);
            }
        }
    }, [initialValues, today]);

    useEffect(() => {
        if (!isAdmin) {
            return;
        }

        (async () => {
            const [providerList, typeList] = await Promise.all([
                fetchProvidersAsCatalog("supplierNumber"),
                fetchSupplierTypesAsCatalog(),
            ]);

            if (providerList) {
                setProviders(providerList);
            }

            if (typeList) {
                setProviderTypes(typeList);
            }
        })();
    }, [isAdmin]);

    const formatDateStr = (date: Date): string => {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");

        return `${year}-${month}-${day}`;
    };

    const isRangeOverSixMonths = (
        start: Date,
        end: Date
    ): boolean => {
        const differenceInMonths =
            (end.getFullYear() - start.getFullYear()) * 12 +
            (end.getMonth() - start.getMonth());

        return differenceInMonths > 6;
    };

    const closeAlertModal = () => {
        setAlertModal((previous) => ({
            ...previous,
            visible: false,
        }));
    };

    const showValidationAlert = (
        message: string,
        title = "Validación"
    ) => {
        setAlertModal({
            visible: true,
            title,
            message,
            severity: "warning",
        });
    };

    const handleSearch = (): void => {
        const [initialDate, finalDate] = dateRange;

        if (!initialDate) {
            showValidationAlert(
                messages["ERR003"] ??
                "Fecha inicio es obligatoria."
            );

            return;
        }

        if (!finalDate) {
            showValidationAlert(
                messages["ERR004"] ??
                "Fecha fin es obligatoria."
            );

            return;
        }

        const start = new Date(initialDate);
        start.setHours(0, 0, 0, 0);

        const end = new Date(finalDate);
        end.setHours(0, 0, 0, 0);

        if (end > today) {
            showValidationAlert(
                "La fecha fin no puede ser posterior a la fecha actual.",
                "Validación de fechas"
            );

            return;
        }

        if (start > end) {
            showValidationAlert(
                "La fecha de inicio no puede ser mayor a la fecha fin.",
                "Validación de fechas"
            );

            return;
        }

        if (isRangeOverSixMonths(start, end)) {
            setAlertModal({
                visible: true,
                title: "Rango inválido",
                message:
                    messages["ERR002"] ??
                    "El rango máximo permitido es 6 meses.",
                severity: "warning",
            });

            return;
        }

        const payload: PaymentFiltersValues = {
            providerId:
                isAdmin && providerId
                    ? providerId
                    : undefined,
            paymentReference:
                paymentReference || undefined,
            paymentYear:
                paymentYear || undefined,
            startDate: formatDateStr(start),
            endDate: formatDateStr(end),
            providerType:
                providerType || undefined,
        };

        saveFinanceListFilters(FILTERS_KEY, payload);
        onSearch(payload);
    };

    /**
     * Limpia los filtros de texto y catálogos, pero conserva el
     * comportamiento requerido para fechas: el calendario vuelve
     * al día actual en lugar de quedar vacío.
     */
    const handleClear = (): void => {
        const [start, end] = financeListTodayDateRange();

        setProviderId("");
        setPaymentReference("");
        setPaymentYear("");
        setProviderType("");
        setDateRange([start, end]);

        closeAlertModal();
        onClear?.();
    };

    return (
        <>
            <div className="pay-filters">
                <div className="pay-filter-bar finz-filter-row">
                    {isAdmin && (
                        <>
                            <div className="pay-field">
                                <GenericSelectSearchable
                                    value={providerId}
                                    onChange={(
                                        event: {
                                            target: {
                                                value: string;
                                            };
                                        }
                                    ) =>
                                        setProviderId(
                                            event.target.value
                                        )
                                    }
                                    options={providers}
                                    placeholder="Nombre Proveedor"
                                    widthClass="gs-width-provider"
                                />
                            </div>

                            <div className="pay-field">
                                <GenericSelectSearchable
                                    value={providerType}
                                    onChange={(
                                        event: {
                                            target: {
                                                value: string;
                                            };
                                        }
                                    ) =>
                                        setProviderType(
                                            event.target.value
                                        )
                                    }
                                    options={providerTypes}
                                    placeholder="Tipo Proveedor"
                                    widthClass="gs-width-provider"
                                />
                            </div>
                        </>
                    )}

                    <div className="pay-field">
                        <GenericInputSearch
                            value={paymentReference}
                            onChange={(
                                event: ChangeEvent<HTMLInputElement>
                            ) =>
                                setPaymentReference(
                                    event.target.value
                                )
                            }
                            placeholder="Referencia Pago"
                            className="pay-input-md"
                        />
                    </div>

                    <div className="pay-field">
                        <GenericInputSearch
                            value={paymentYear}
                            onChange={(
                                event: ChangeEvent<HTMLInputElement>
                            ) => {
                                const value = event.target.value
                                    .replace(/\D/g, "")
                                    .slice(0, 4);

                                setPaymentYear(value);
                            }}
                            placeholder="Año Pago"
                            className="pay-input-sm"
                            inputMode="numeric"
                        />
                    </div>

                    <div className="pay-field pay-field-dates">
                        <GenericDateRangePicker
                            value={dateRange}
                            onChange={(dates) => {
                                setDateRange(dates);
                                closeAlertModal();
                            }}
                            placeholder="Fecha Pago"
                            size="md"
                        />
                    </div>

                    <div className="pay-actions-container finz-filter-actions">
                        <PermissionGate
                            appEvent={APP_EVENT.PAYMENTS.SEARCH}
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
                                APP_EVENT.PAYMENTS.CLEAR_FILTERS
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
                visible={alertModal.visible}
                variant="alert"
                severity={alertModal.severity}
                title={alertModal.title}
                message={alertModal.message}
                buttonText="Aceptar"
                onClose={closeAlertModal}
            />
        </>
    );
}