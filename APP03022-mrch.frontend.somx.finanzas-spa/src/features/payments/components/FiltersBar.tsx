import {
    GenericDateRangePicker,
    GenericInputSearch,
    GenericButton,
    GenericModal,
} from "@shared/components/ui";
import type { ChangeEvent, ReactElement } from "react";
import { useEffect, useMemo, useRef, useState } from "react";

import "../styles/PaymentsFiltersBar.css";

export interface PaymentFiltersValues {
    providerId?: string;
    paymentReference?: string;
    paymentYear?: string;
    startDate: string;
    endDate: string;
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
    const [paymentReference, setPaymentReference] = useState<string>("");
    const [paymentYear, setPaymentYear] = useState<string>("");

    const [dateRange, setDateRange] = useState<DateRange>([null, null]);

    const [error, setError] = useState<string>("");
    const [rangeErrorModal, setRangeErrorModal] = useState<boolean>(false);

    const hasLoadedRef = useRef(false);

    const today = useMemo(() => {
        const t = new Date();
        t.setHours(0, 0, 0, 0);
        return t;
    }, []);

    useEffect(() => {
        if (!initialValues || hasLoadedRef.current) return;

        const start = initialValues.startDate
            ? new Date(initialValues.startDate + "T00:00:00")
            : null;

        const end = initialValues.endDate
            ? new Date(initialValues.endDate + "T00:00:00")
            : null;

        setDateRange([start, end]);
        setProviderId(initialValues.providerId || "");
        setPaymentReference(initialValues.paymentReference || "");
        setPaymentYear(initialValues.paymentYear || "");

        hasLoadedRef.current = true;
    }, [initialValues]);

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

    const handleSearch = (): void => {
        setError("");

        const [d1, d2] = dateRange;

        if (!d1) {
            setError(messages["ERR003"] || "Fecha inicio es obligatoria.");
            return;
        }

        if (!d2) {
            setError(messages["ERR004"] || "Fecha fin es obligatoria.");
            return;
        }

        const start = new Date(d1);
        start.setHours(0, 0, 0, 0);

        const end = new Date(d2);
        end.setHours(0, 0, 0, 0);

        if (end > today) {
            setError("La fecha fin no puede ser posterior a la fecha actual.");
            return;
        }

        if (start > end) {
            setError("La fecha de inicio no puede ser mayor a la fecha fin.");
            return;
        }

        if (isRangeOverSixMonths(start, end)) {
            setRangeErrorModal(true);
            return;
        }

        const payload: PaymentFiltersValues = {
            providerId: isAdmin && providerId ? providerId : undefined,
            paymentReference: paymentReference || undefined,
            paymentYear: paymentYear || undefined,
            startDate: formatDateStr(start),
            endDate: formatDateStr(end),
        };

        onSearch(payload);
    };

    const handleClear = (): void => {
        setProviderId("");
        setPaymentReference("");
        setPaymentYear("");
        setDateRange([null, null]);
        setError("");
        onClear?.();
    };

    return (
        <>
            <div className="pay-filters">
                <div className="pay-filter-bar">
                    <div className="pay-field pay-field-dates">
                        <GenericDateRangePicker
                            value={dateRange}
                            onChange={(dates) => {
                                setDateRange(dates);
                                setError("");
                            }}
                            placeholder="Rango de fecha pago"
                            size="md"
                        />
                    </div>

                    {isAdmin && (
                        <div className="pay-field">
                            <GenericInputSearch
                                value={providerId}
                                onChange={(e: ChangeEvent<HTMLInputElement>) =>
                                    setProviderId(e.target.value)
                                }
                                placeholder="Número de proveedor"
                                className="pay-input-md"
                            />
                        </div>
                    )}

                    <div className="pay-field">
                        <GenericInputSearch
                            value={paymentReference}
                            onChange={(e: ChangeEvent<HTMLInputElement>) =>
                                setPaymentReference(e.target.value)
                            }
                            placeholder="Referencia de pago"
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
                            placeholder="Año de pago"
                            className="pay-input-sm"
                            inputMode="numeric"
                        />
                    </div>

                    <div className="pay-actions-container">
                        <GenericButton variant="outlineFill" onClick={handleClear}>
                            Limpiar
                        </GenericButton>

                        <GenericButton variant="primary" onClick={handleSearch}>
                            Buscar
                        </GenericButton>
                    </div>
                </div>

                {error && <div className="pay-inline-error">{error}</div>}
            </div>

            <GenericModal
                visible={rangeErrorModal}
                variant="alert"
                severity="warning"
                title="Rango inválido"
                message={messages["ERR002"] || "El rango máximo permitido es 6 meses."}
                buttonText="Aceptar"
                onClose={() => setRangeErrorModal(false)}
            />
        </>
    );
}