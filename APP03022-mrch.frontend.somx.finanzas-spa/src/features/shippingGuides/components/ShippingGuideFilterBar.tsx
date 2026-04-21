// ✅ FILE: src/features/shipping-guides/components/ShippingGuideFilterBar.tsx
import {
    GenericButton,
    GenericInputSearch,
    GenericSelectSearchable,
    GenericModal
} from "@shared/components/ui";
import { GenericDateRangePicker } from "@shared/components/ui/date";
import type { ChangeEvent, ReactElement } from "react";
import { useEffect, useMemo, useState } from "react";
import { ReceiptTypeOptions, ShippingGuideFilter } from "../interfaces";
import "../styles/shippingGuideFilterBar.css";

type ShippingGuideFilterBarProps = {
    filters?: ShippingGuideFilter;
    setFilters: (filters: ShippingGuideFilter) => void;
    disabled: boolean;
    isAdmin?: boolean;
    onCancel?: () => void;
};

const searchOptions = [
    { value: "guideNumber", label: "Guía de embarque" },
    { value: "truckPlate", label: "Placa" },
    { value: "trailerPlate", label: "Placa remolque" },
    { value: "sourceId", label: "Origen" },
    { value: "deliveryType", label: "Tipo entrega" },
];

export default function ShippingGuideFilterBar({
    filters,
    setFilters,
    isAdmin = true,
    disabled,
    onCancel,
}: ShippingGuideFilterBarProps): ReactElement {
    const [selectedField, setSelectedField] = useState<string>("guideNumber");
    const [searchValue, setSearchValue] = useState<string>("");
    const [vendorNumber, setVendorNumber] = useState<string>(filters?.vendorNumber || "");
    const [alertModal, setAlertModal] = useState({ visible: false, message: "" });

    const [range, setRange] = useState<[Date | null, Date | null]>(
        filters?.from && filters?.to
            ? [new Date(filters.from), new Date(filters.to)]
            : [null, null]
    );

    useEffect(() => {
        if (!filters) return;
        setVendorNumber(filters.vendorNumber || "");
        setRange(
            filters.from && filters.to
                ? [new Date(filters.from), new Date(filters.to)]
                : [null, null]
        );

        const fieldEntry =
            (filters.guideNumber && { field: "guideNumber", value: filters.guideNumber }) ||
            (filters.truckPlate && { field: "truckPlate", value: filters.truckPlate }) ||
            (filters.trailerPlate && { field: "trailerPlate", value: filters.trailerPlate }) ||
            (filters.sourceId && { field: "sourceId", value: filters.sourceId }) ||
            (filters.deliveryType && { field: "deliveryType", value: String(filters.deliveryType) });

        if (fieldEntry) {
            setSelectedField(fieldEntry.field);
            setSearchValue(fieldEntry.value);
        } else {
            setSelectedField("guideNumber");
            setSearchValue("");
        }
    }, [filters]);

    const isDeliveryType = selectedField === "deliveryType";

    const handleClear = () => {
        setSelectedField("guideNumber");
        setSearchValue("");
        setVendorNumber("");
        setRange([null, null]);
        setFilters({});
    };

    const onSearch = () => {
        const value = searchValue.trim();
        if (!range?.[0] || !range?.[1]) {
            setAlertModal({ visible: true, message: "Selecciona las fechas de inicio y final (obligatorias)." });
            return;
        }
        if (range[0] > range[1]) {
            setAlertModal({ visible: true, message: "La fecha de inicio no puede ser mayor a la final." });
            return;
        }

        const payload: ShippingGuideFilter = {
            vendorNumber: vendorNumber || undefined,
            from: range[0] ? range[0].toISOString().slice(0, 10) : undefined,
            to: range[1] ? range[1].toISOString().slice(0, 10) : undefined,
        };

        if (value) {
            (payload as any)[selectedField] = isDeliveryType ? Number(value) : value;
        }
        setFilters(payload);
    };

    const valueInput = useMemo(() => {
        if (isDeliveryType) {
            const safeReceiptOptions = ReceiptTypeOptions.map(opt => ({
                label: opt.label,
                value: String(opt.value)
            }));

            return (
                <GenericSelectSearchable
                    value={searchValue}
                    onChange={(event: { target: { value: string } }) => setSearchValue(event.target.value)}
                    placeholder="Tipo de entrega"
                    options={safeReceiptOptions}
                    widthClass="w-full"
                />
            );
        }

        return (
            <div className="sg-full-width">
                <GenericInputSearch
                    value={searchValue}
                    onChange={(event: ChangeEvent<HTMLInputElement>) => setSearchValue(event.target.value)}
                    placeholder="Ingresa el valor"
                    className="w-full"
                />
            </div>
        );
    }, [isDeliveryType, searchValue]);

    return (
        <>
            <div className="sg-filter-bar">
                {/* 1. Selector de campo */}
                <div className="sg-field">
                    <GenericSelectSearchable
                        value={selectedField}
                        onChange={(event: { target: { value: string } }) => {
                            setSelectedField(event.target.value);
                            setSearchValue("");
                        }}
                        placeholder="Buscar por..."
                        options={searchOptions}
                        widthClass="w-full"
                    />
                </div>

                {/* 2. Input del valor */}
                <div className="sg-field">
                    {valueInput}
                </div>

                {/* 3. Proveedor (Admin) */}
                {isAdmin && (
                    <div className="sg-field">
                        <div className="sg-full-width">
                            <GenericInputSearch
                                value={vendorNumber}
                                onChange={(event: ChangeEvent<HTMLInputElement>) => setVendorNumber(event.target.value)}
                                placeholder="Núm. proveedor (Admin)"
                                className="w-full"
                            />
                        </div>
                    </div>
                )}

                {/* 4. Fechas */}
                <div className="sg-field-dates">
                    <GenericDateRangePicker
                        value={range}
                        onChange={(dates: [Date | null, Date | null]) => setRange(dates)}
                        placeholder="Fechas (obligatorio)"
                        className="w-full"
                    />
                    <div className="sg-field">
                        <GenericButton variant="outlineFill" onClick={handleClear}>
                            Limpiar
                        </GenericButton>
                    </div>
                    <div className="sg-field">
                        <GenericButton variant="primary" onClick={onSearch}>
                            Buscar
                        </GenericButton>
                    </div>
                </div>


                {/* 5. Botones de acción */}
                <div className="sg-actions-container">
                    <GenericButton
                        variant="cancel"
                        onClick={onCancel}
                        disabled={disabled}
                    >
                        Cancelar
                    </GenericButton>
                </div>
            </div>

            <GenericModal
                visible={alertModal.visible}
                variant="alert"
                severity="warning"
                title="Fechas requeridas"
                message={alertModal.message}
                buttonText="Aceptar"
                onClose={() => setAlertModal({ visible: false, message: "" })}
            />
        </>
    );
}