import { useState } from 'react';
import type { ChangeEvent, ReactElement } from 'react';
import { GenericInput, GenericSelect, GenericButton } from '@shared/components/ui';
import { GenericDateRangePicker } from '@shared/components/ui/date';

/* ---------- Types ---------- */
export type DateRange = [Date | null, Date | null];

export interface FiltersValues {
    vendorId?: string;
    sapDocument?: string;
    documentNumber?: string;
    dateStart?: string; // YYYY-MM-DD
    dateEnd?: string;   // YYYY-MM-DD
    rebateTypeId?: number;
    statusId?: number;
}

interface FiltersBarProps {
    onSearch: (values: FiltersValues) => void;
}

export default function FiltersBar({ onSearch }: FiltersBarProps): ReactElement {
    const [vendor, setVendor] = useState<string>('');
    const [sapDocument, setSapDocument] = useState<string>('');
    const [documentNumber, setDocumentNumber] = useState<string>('');
    const [dateRange, setDateRange] = useState<DateRange>([null, null]);
    const [rebateType, setRebateType] = useState<string>(''); // keep as string for <select>
    const [status, setStatus] = useState<string>('');

    const onChangeInput =
        (setter: (v: string) => void) =>
            (e: ChangeEvent<HTMLInputElement>) =>
                setter(e.target.value);

    const onChangeSelect =
        (setter: (v: string) => void) =>
            (e: ChangeEvent<HTMLSelectElement>) =>
                setter(e.target.value);

    const handleSubmit = () => {
        const [start, end] = dateRange;
        const payload: FiltersValues = {
            vendorId: vendor || undefined,
            sapDocument: sapDocument || undefined,
            documentNumber: documentNumber || undefined,
            dateStart: start ? start.toISOString().split('T')[0] : undefined,
            dateEnd: end ? end.toISOString().split('T')[0] : undefined,
            rebateTypeId: rebateType ? Number(rebateType) : undefined,
            statusId: status ? Number(status) : undefined,
        };
        onSearch(payload);
    };

    return (
        <div className="flex flex-wrap gap-4 items-end border border-gray-200 rounded-md p-4 bg-[#f9fafb]">
            <GenericInput
                label="Proveedor"
                placeholder="Número de proveedor"
                value={vendor}
                onChange={onChangeInput(setVendor)}
                widthClass="w-60"
            />

            <GenericInput
                label="Documento"
                placeholder="Número de documento"
                value={documentNumber}
                onChange={onChangeInput(setDocumentNumber)}
                widthClass="w-48"
            />

            <GenericInput
                label="Documento SAP"
                placeholder="SAP Doc"
                value={sapDocument}
                onChange={onChangeInput(setSapDocument)}
                widthClass="w-44"
            />

            <div className="flex flex-col w-60">
                <label className="text-sm text-gray-600 mb-1">Rango de fechas</label>
                <GenericDateRangePicker value={dateRange} onChange={setDateRange} />
            </div>

            <GenericSelect
                label="Tipo Rebate"
                value={rebateType}
                onChange={onChangeSelect(setRebateType)}
                options={[
                    { value: '', label: 'Todos' },
                    { value: '1', label: 'Merma' },
                    { value: '2', label: 'Cross' },
                    { value: '3', label: 'Coop' },
                ]}
                widthClass="w-44"
            />

            <GenericSelect
                label="Estatus"
                value={status}
                onChange={onChangeSelect(setStatus)}
                options={[
                    { value: '', label: 'Todos' },
                    { value: '1', label: 'Pendiente' },
                    { value: '2', label: 'Aprobado' },
                    { value: '3', label: 'Rechazado' },
                ]}
                widthClass="w-40"
            />

            <GenericButton onClick={handleSubmit} className="h-11">
                Buscar
            </GenericButton>
        </div>
    );
}
