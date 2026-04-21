import {
    GenericSelectSearchable,
    GenericDateRangePicker,
    GenericInputSearch,
    GenericButton,
    GenericModal
} from '@shared/components/ui';
import type { ThreeWayMatchFiltersProps } from '../interfaces';
import '../styles/ThreeWayMatchFilters.css';
import { useState } from 'react';

export default function ThreeWayMatchFilters({ isAdmin, onSearch }: ThreeWayMatchFiltersProps) {

    const today = new Date();
    const defaultStart = new Date(today);
    defaultStart.setDate(today.getDate() - 7);

    const defaultEnd = new Date(today);
    defaultEnd.setDate(today.getDate() - 1);

    const [dateType, setDateType] = useState<string>('fechaRecepcion');
    const [dateRange, setDateRange] = useState<[Date | null, Date | null]>([
        defaultStart,
        defaultEnd
    ]);
    const [supplier, setSupplier] = useState<string>('');
    const [po, setPo] = useState<string>('');
    const [reception, setReception] = useState<string>('');

    const [rangeErrorModal, setRangeErrorModal] = useState<boolean>(false);

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
            dateType,
            startDate: start ? start.toISOString().slice(0, 10) : '',
            endDate: end ? end.toISOString().slice(0, 10) : '',
            supplier,
            po,
            reception
        });
    };

    return (
        <>
            <div className="twm-filters">
                <div className="twm-row">

                    <GenericSelectSearchable
                        value={dateType}
                        onChange={(e: { target: { value: string } }) =>
                            setDateType(e.target.value)
                        }
                        options={[
                            { value: 'fechaRecepcion', label: 'Fecha recepción' },
                            { value: 'fechaTimbrado', label: 'Fecha timbrado' },
                            { value: 'fechaOrdenCompra', label: 'Fecha orden de compra' },
                            { value: 'fechaPago', label: 'Fecha pago' },
                        ]}
                        placeholder="Selecciona tipo de fecha"
                        widthClass="gs-width-md"
                    />

                    <GenericDateRangePicker
                        value={dateRange}
                        onChange={(dates) => setDateRange(dates)}
                        placeholder="Fecha desde – hasta"
                        size="md"
                    />

                    {isAdmin && (
                        <GenericInputSearch
                            value={supplier}
                            onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                                setSupplier(e.target.value)
                            }
                            placeholder="Proveedor"
                        />
                    )}

                    <GenericInputSearch
                        value={po}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                            setPo(e.target.value)
                        }
                        placeholder="Orden compra"
                    />

                    <GenericInputSearch
                        value={reception}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                            setReception(e.target.value)
                        }
                        placeholder="Recepción"
                    />

                    <GenericButton
                        variant="outline"
                        onClick={handleSearch}>
                        Buscar
                    </GenericButton>

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
