import { useState } from 'react';
import type { ChangeEvent, ReactElement } from 'react';
import { PAYMENT_STATUSES } from '../../interfaces';

export type DateRange = [Date | null, Date | null];

export interface PaymentFiltersValues {
    providerId?: string;
    paymentNumber?: string;
    statusId?: number;
    startDate: string;
    endDate: string;
}

interface FiltersBarProps {
    onSearch: (values: PaymentFiltersValues) => void;
    isAdmin?: boolean;
    messages?: Record<string, string>;
}

export default function FiltersBar({ 
    onSearch, 
    isAdmin = false,
    messages = {} 
}: FiltersBarProps): ReactElement {
    const [providerId, setProviderId] = useState<string>('');
    const [paymentNumber, setPaymentNumber] = useState<string>('');
    const [status, setStatus] = useState<string>('');
    const [dateRange, setDateRange] = useState<DateRange>([new Date(), new Date()]);
    const [error, setError] = useState<string>('');

    const onChangeInput = (setter: (v: string) => void) =>
        (e: ChangeEvent<HTMLInputElement>) => {
            const value = e.target.value;
            if (setter === setPaymentNumber && value && !/^\d*$/.test(value)) {
                return;
            }
            setter(value);
        };

    const onChangeSelect = (setter: (v: string) => void) =>
        (e: ChangeEvent<HTMLSelectElement>) => setter(e.target.value);

    const validateDates = (start: Date | null, end: Date | null): string | null => {
        if (!start || !end) {
            return 'Las fechas de inicio y fin son obligatorias';
        }

        const now = new Date();
        const oneMonthFromNow = new Date(now);
        oneMonthFromNow.setMonth(oneMonthFromNow.getMonth() + 1);

        if (end > oneMonthFromNow) {
            return messages['ERR001'] || 'La fecha final no puede exceder un mes desde la fecha actual';
        }

        const sixMonthsBefore = new Date(end);
        sixMonthsBefore.setMonth(sixMonthsBefore.getMonth() - 6);
        
        if (start < sixMonthsBefore) {
            return messages['ERR002'] || 'El periodo máximo de consulta es de 6 meses';
        }

        if (start > end) {
            return 'La fecha de inicio no puede ser mayor a la fecha fin';
        }

        return null;
    };

    const handleSubmit = () => {
        const [start, end] = dateRange;
        
        const validationError = validateDates(start, end);
        if (validationError) {
            setError(validationError);
            return;
        }

        setError('');

        const payload: PaymentFiltersValues = {
            providerId: isAdmin && providerId ? providerId : undefined,
            paymentNumber: paymentNumber || undefined,
            statusId: status ? Number(status) : undefined,
            startDate: start!.toISOString().split('T')[0],
            endDate: end!.toISOString().split('T')[0]
        };
        
        onSearch(payload);
    };

    const handleClear = () => {
        setProviderId('');
        setPaymentNumber('');
        setStatus('');
        setDateRange([new Date(), new Date()]);
        setError('');
    };

    return (
        <div className="space-y-3">
            <h2 className="text-base font-medium text-gray-700">Parametros de busqueda</h2>
            
            <div className="border border-gray-200 rounded-lg p-4 bg-white">
                <div className="flex items-center gap-3">
                    <div className="relative">
                        <select
                            value={paymentNumber}
                            onChange={(e) => setPaymentNumber(e.target.value)}
                            className="appearance-none border border-gray-300 rounded px-3 py-2 pr-8 text-sm bg-white h-9 w-52 focus:outline-none focus:border-gray-400"
                        >
                            <option value="">Número de pago</option>
                            <option value="242195">242195</option>
                            <option value="242196">242196</option>
                        </select>
                        <span className="absolute right-2 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none text-xs">
                            ▼
                        </span>
                    </div>

                    <div className="relative">
                        <select
                            value={status}
                            onChange={onChangeSelect(setStatus)}
                            className="appearance-none border border-gray-300 rounded px-3 py-2 pr-8 text-sm bg-white h-9 w-52 focus:outline-none focus:border-gray-400"
                        >
                            <option value="">Estatus pago</option>
                            {PAYMENT_STATUSES.map(s => (
                                <option key={s.id} value={s.id.toString()}>
                                    {s.description}
                                </option>
                            ))}
                        </select>
                        <span className="absolute right-2 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none text-xs">
                            ▼
                        </span>
                    </div>

                    <div className="relative">
                        <input
                            type="text"
                            placeholder="Fecha Inicio"
                            value={dateRange[0] ? dateRange[0].toLocaleDateString('es-ES') : ''}
                            onFocus={(e) => {
                                const input = e.target as HTMLInputElement;
                                input.type = 'date';
                                if (dateRange[0]) {
                                    input.value = dateRange[0].toISOString().split('T')[0];
                                }
                            }}
                            onBlur={(e) => {
                                const input = e.target as HTMLInputElement;
                                if (!input.value) {
                                    input.type = 'text';
                                }
                            }}
                            onChange={(e) => {
                                const newDate = e.target.value ? new Date(e.target.value + 'T00:00:00') : null;
                                setDateRange([newDate, dateRange[1]]);
                            }}
                            className="border border-gray-300 rounded px-3 py-2 text-sm h-9 w-44 focus:outline-none focus:border-gray-400"
                        />
                    </div>

                    <div className="relative">
                        <input
                            type="text"
                            placeholder="Fecha Final"
                            value={dateRange[1] ? dateRange[1].toLocaleDateString('es-ES') : ''}
                            onFocus={(e) => {
                                const input = e.target as HTMLInputElement;
                                input.type = 'date';
                                if (dateRange[1]) {
                                    input.value = dateRange[1].toISOString().split('T')[0];
                                }
                            }}
                            onBlur={(e) => {
                                const input = e.target as HTMLInputElement;
                                if (!input.value) {
                                    input.type = 'text';
                                }
                            }}
                            onChange={(e) => {
                                const newDate = e.target.value ? new Date(e.target.value + 'T00:00:00') : null;
                                setDateRange([dateRange[0], newDate]);
                            }}
                            className="border border-gray-300 rounded px-3 py-2 text-sm h-9 w-44 focus:outline-none focus:border-gray-400"
                        />
                    </div>

                    <div className="flex-1"></div>

                    <button
                        onClick={handleSubmit}
                        className="px-6 py-2 bg-white border border-gray-900 text-gray-900 rounded text-sm font-medium hover:bg-gray-50 h-9"
                    >
                        Consult
                    </button>
                </div>
            </div>

            {error && (
                <div className="bg-red-50 border border-red-300 text-red-800 px-4 py-2 rounded-md">
                    {error}
                </div>
            )}
        </div>
    );
}
