import {
    GenericDateRangePicker,
    GenericInputSearch,
    GenericSelectSearchable,
    GenericButton,
    GenericModal,
} from "@shared/components/ui";
import type { ChangeEvent, ReactElement } from "react";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { APP_EVENT, PermissionGate } from "@shared/security";

import type { ProvidersOptions } from "@/features/orders/interfaces";
import { fetchCatalog, fetchProvidersAsCatalog, mapCatalogResponseToFilterOptions } from "@/utils/utils";
import {
  FINANCE_LIST_KEYS,
  financeListTodayDateRange,
  readFinanceListFilters,
  parseFinanceListDateRange,
  saveFinanceListFilters,
  useFinanceListDefaultsOnUrlReset,
} from "@/shared/hooks";

const FILTERS_KEY = FINANCE_LIST_KEYS.payments.filters;

import "../styles/PaymentsFiltersBar.css";

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
    const [providerTypes, setProviderTypes] = useState<any[]>([]);
    const [alertModal, setAlertModal] = useState<{
        visible: boolean;
        title: string;
        message: string;
        severity: "warning" | "error" | "info";
    }>({ visible: false, title: "", message: "", severity: "warning" });

    const hasLoadedRef = useRef(false);
    const defaultsAppliedRef = useRef(false);

    const today = useMemo(() => {
        const t = new Date();
        t.setHours(0, 0, 0, 0);
        return t;
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
                    parseFinanceListDateRange(saved.startDate, saved.endDate)
                );
                setProviderId(saved.providerId || "");
                setPaymentReference(saved.paymentReference || "");
                setPaymentYear(saved.paymentYear || "");
                setProviderType(saved.providerType || "");
                return;
            }

            if (!defaultsAppliedRef.current) {
                defaultsAppliedRef.current = true;
                const d = new Date(today);
                setDateRange([new Date(d.getTime()), new Date(d.getTime())]);
            }
        }
    }, [initialValues, today]);

    useEffect(() => {
        if (!isAdmin) return;

        (async () => {
            const list = await fetchProvidersAsCatalog();
            const types = await fetchCatalog("CatTipoProveedor");
            if (list) setProviders(list);

            if(types) {
                //@ts-ignore
                const mapped = types.details?.map((item: any) => ({
                    label: item.description,
                    value: item.value,
                }));
                setProviderTypes([{ label: "Todos los tipos", value: "" }, ...mapped ?? []]);
            }
        })();
    }, [isAdmin]);

    const formatDateStr = (d: Date): string => {
        const y = d.getFullYear();
        const m = String(d.getMonth() + 1).padStart(2, "0");
        const day = String(d.getDate()).padStart(2, "0");
        return `${y}-${m}-${day}`;
    };

    const isRangeOverSixMonths = (start: Date, end: Date): boolean => {
        const diffMonths =
            (end.getFullYear() - start.getFullYear()) * 12 +
            (end.getMonth() - start.getMonth());
        return diffMonths > 6;
    };

    const closeAlertModal = () =>
        setAlertModal((prev) => ({ ...prev, visible: false }));

    const showValidationAlert = (message: string, title = "Validación") => {
        setAlertModal({
            visible: true,
            title,
            message,
            severity: "warning",
        });
    };

    const handleSearch = (): void => {
        const [d1, d2] = dateRange;

        if (!d1) {
            showValidationAlert(messages["ERR003"] || "Fecha inicio es obligatoria.");
            return;
        }

        if (!d2) {
            showValidationAlert(messages["ERR004"] || "Fecha fin es obligatoria.");
            return;
        }

        const start = new Date(d1);
        start.setHours(0, 0, 0, 0);

        const end = new Date(d2);
        end.setHours(0, 0, 0, 0);

        if (end > today) {
            showValidationAlert(
                "La fecha fin no puede ser posterior a la fecha actual.",
                "Validación de fechas",
            );
            return;
        }

        if (start > end) {
            showValidationAlert(
                "La fecha de inicio no puede ser mayor a la fecha fin.",
                "Validación de fechas",
            );
            return;
        }

        if (isRangeOverSixMonths(start, end)) {
            setAlertModal({
                visible: true,
                title: "Rango inválido",
                message: messages["ERR002"] || "El rango máximo permitido es 6 meses.",
                severity: "warning",
            });
            return;
        }

        const payload: PaymentFiltersValues = {
            providerId: isAdmin && providerId ? providerId : undefined,
            paymentReference: paymentReference || undefined,
            paymentYear: paymentYear || undefined,
            startDate: formatDateStr(start),
            endDate: formatDateStr(end),
            providerType: providerType || undefined,
        };

        saveFinanceListFilters(FILTERS_KEY, payload);
        onSearch(payload);
    };

    const handleClear = (): void => {
        setProviderId("");
        setPaymentReference("");
        setPaymentYear("");
        setProviderType("");
        setDateRange([null, null]);
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
                                onChange={(e: { target: { value: string } }) =>
                                    setProviderId(e.target.value)
                                }
                                options={providers}
                                placeholder="Nombre Proveedor"
                                widthClass="gs-width-provider"
                            />
                        </div>
                        <div className="pay-field">
                            <GenericSelectSearchable
                                value={providerId}
                                onChange={(e: { target: { value: string } }) =>
                                    setProviderType(e.target.value)
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
                            onChange={(e: ChangeEvent<HTMLInputElement>) =>
                                setPaymentReference(e.target.value)
                            }
                            placeholder="Referencia Pago"
                            className="pay-input-md"
                        />
                    </div>

                    <div className="pay-field">
                        <GenericInputSearch
                            value={paymentYear}
                            onChange={(e: ChangeEvent<HTMLInputElement>) => {
                                const val = e.target.value.replace(/\D/g, "").slice(0, 4);
                                setPaymentYear(val);
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
                        <PermissionGate appEvent={APP_EVENT.PAYMENTS.SEARCH}>
                            <GenericButton variant="outlineFill" onClick={handleSearch}>
                                Buscar
                            </GenericButton>
                        </PermissionGate>
                        <PermissionGate appEvent={APP_EVENT.PAYMENTS.CLEAR_FILTERS}>
                            <GenericButton variant="outlineFill" onClick={handleClear}>
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