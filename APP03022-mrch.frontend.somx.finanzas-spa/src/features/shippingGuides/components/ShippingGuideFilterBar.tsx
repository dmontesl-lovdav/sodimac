import { GenericButton, GenericInput, GenericSelect } from "@shared/components/ui";
import { GenericDateRangePicker } from "@shared/components/ui/date";
import type { ChangeEvent, ReactElement } from "react";
import { useEffect, useMemo, useState } from "react";
import { ReceiptTypeOptions, ShippingGuideFilter } from "../interfaces";
import "./shippingGuides.css";

type ShippingGuideFilterBarProps = {
    filters?: ShippingGuideFilter;
    setFilters: (filters: ShippingGuideFilter) => void;
    isAdmin?: boolean;
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
}: ShippingGuideFilterBarProps): ReactElement {
    const [selectedField, setSelectedField] = useState<string>("guideNumber");
    const [searchValue, setSearchValue] = useState<string>("");
    const [vendorNumber, setVendorNumber] = useState<string>(filters?.vendorNumber || "");
    const [range, setRange] = useState<Date[]>(
        filters?.from && filters?.to ? [new Date(filters?.from), new Date(filters?.to)] : []
    );

    // Sync cuando cambian filtros externos (por ejemplo al volver desde detalle)
    useEffect(() => {
        if (!filters) return;

        setVendorNumber(filters.vendorNumber || "");
        setRange(filters.from && filters.to ? [new Date(filters.from), new Date(filters.to)] : []);

        const fieldEntry =
            (filters.guideNumber && { field: "guideNumber", value: filters.guideNumber }) ||
            (filters.truckPlate && { field: "truckPlate", value: filters.truckPlate }) ||
            (filters.trailerPlate && { field: "trailerPlate", value: filters.trailerPlate }) ||
            (filters.sourceId && { field: "sourceId", value: filters.sourceId }) ||
            (filters.deliveryType && { field: "deliveryType", value: filters.deliveryType });

        if (fieldEntry) {
            setSelectedField(fieldEntry.field);
            setSearchValue(fieldEntry.value);
        } else {
            setSelectedField("guideNumber");
            setSearchValue("");
        }
    }, [filters]);

    const isDeliveryType = selectedField === "deliveryType";

    const onSearch = () => {
        const value = searchValue.trim();

        if (!range?.[0] || !range?.[1]) {
            alert("Selecciona la fecha de registro inicio y final (obligatorias).");
            return;
        }

        if (range[0] > range[1]) {
            alert("La fecha inicio no puede ser mayor a la final.");
            return;
        }

        const payload: ShippingGuideFilter = {
            vendorNumber: vendorNumber || undefined,
            from: range[0]?.toISOString(),
            to: range[1]?.toISOString(),
        };

        switch (selectedField) {
            case "guideNumber":
                payload.guideNumber = value || undefined;
                break;
            case "truckPlate":
                payload.truckPlate = value || undefined;
                break;
            case "trailerPlate":
                payload.trailerPlate = value || undefined;
                break;
            case "sourceId":
                payload.sourceId = value || undefined;
                break;
            case "deliveryType":
                payload.deliveryType = value || undefined;
                break;
            default:
                break;
        }

        setFilters(payload);
    };

    const valueInput = useMemo(() => {
        if (isDeliveryType) {
            return (
                <GenericSelect
                    label="Valor"
                    value={searchValue}
                    onChange={(event: ChangeEvent<HTMLInputElement>) => { setSearchValue(event.target.value); }}
                    placeholder="Selecciona tipo de entrega"
                    options={ReceiptTypeOptions}
                    widthClass="w-full"
                />
            );
        }

        return (
            <GenericInput
                label="Valor"
                value={searchValue}
                onChange={(event: ChangeEvent<HTMLInputElement>) => { setSearchValue(event.target.value); }}
                placeholder="Ingresa el valor"
            />
        );
    }, [isDeliveryType, searchValue]);

    return (
        <div className="sg-filter-bar">
            <div className="sg-field">
                <GenericSelect
                    label="Buscar por"
                    value={selectedField}
                    onChange={(event: ChangeEvent<HTMLInputElement>) => { setSelectedField(event.target.value); setSearchValue(""); }}
                    placeholder="Selecciona filtro"
                    options={searchOptions}
                    widthClass="w-full"
                />
            </div>

            <div className="sg-field">
                {valueInput}
            </div>

            {isAdmin && (
                <div className="sg-field">
                    <GenericInput
                        label="Número de proveedor (solo admin)"
                        value={vendorNumber}
                        onChange={(event: ChangeEvent<HTMLInputElement>) => { setVendorNumber(event.target.value); }}
                        placeholder="Todos los proveedores"
                    />
                </div>
            )}

            <div className="sg-field">
                <GenericDateRangePicker
                    label="Fecha de registro"
                    value={range}
                    onChange={(dates: Date[]) => { setRange(dates); }}
                    placeholder="Rango (obligatorio)"
                />
            </div>

            <GenericButton
                variant="outlineFill"
                className="sg-button"
                onClick={onSearch}
            >
                Buscar
            </GenericButton>
        </div>
    );
}
