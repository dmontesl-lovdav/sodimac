import { useState } from 'react';
import type { ChangeEvent, ReactElement } from 'react';
import { GenericInput, GenericSelect, GenericButton } from '@shared/components/ui';
import { GenericDateRangePicker } from '@shared/components/ui/date';
import './FiltersBar.css';

export type DateRange = [Date | null, Date | null];

export interface FiltersValues {
    vendorId?: string;
    sapDocument?: string;
    documentNumber?: string;
    dateStart?: string;
    dateEnd?: string;
    rebateTypeId?: number;
    statusId?: number;
}

interface FiltersBarProps {
    onSearch: (values: FiltersValues) => void;
    onClear?: () => void;
}

export default function FiltersBar({ onSearch, onClear }: FiltersBarProps): ReactElement {
    const [vendor, setVendor] = useState<string>('');
    const [sapDocument, setSapDocument] = useState<string>('');
    const [documentNumber, setDocumentNumber] = useState<string>('');
    const [dateRange, setDateRange] = useState<DateRange>([null, null]);
    const [rebateType, setRebateType] = useState<string>('');
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

    const handleClear = () => {
        setVendor('');
        setSapDocument('');
        setDocumentNumber('');
        setDateRange([null, null]);
        setRebateType('');
        setStatus('');
        onClear?.();
    };

    return (
        <div className="rb-filters">
            <div className="finz-filter-row">
                <div className="rb-field">
                    <GenericInput
                        label="Proveedor"
                        placeholder="Número de proveedor"
                        value={vendor}
                        onChange={onChangeInput(setVendor)}
                    />
                </div>

                <div className="rb-field">
                    <GenericInput
                        label="Documento"
                        placeholder="Documento"
                        value={documentNumber}
                        onChange={onChangeInput(setDocumentNumber)}
                    />
                </div>

                <div className="rb-field">
                    <GenericInput
                        label="Documento SAP"
                        placeholder="SAP Doc"
                        value={sapDocument}
                        onChange={onChangeInput(setSapDocument)}
                    />
                </div>

                <div className="rb-field">
                    <GenericDateRangePicker
                        value={dateRange}
                        onChange={setDateRange}
                        placeholder="Rango de fechas"
                        size="md"
                    />
                </div>

                <div className="rb-field">
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
                        widthClass="gs-width-default"
                    />
                </div>

                <div className="rb-field">
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
                        widthClass="gs-width-default"
                    />
                </div>

                <div className="finz-filter-actions">
                    <GenericButton variant="outlineFill" onClick={handleSubmit}>
                        Buscar
                    </GenericButton>
                    <GenericButton variant="outlineFill" onClick={handleClear}>
                        Limpiar
                    </GenericButton>
                </div>
            </div>
        </div>
    );
}
