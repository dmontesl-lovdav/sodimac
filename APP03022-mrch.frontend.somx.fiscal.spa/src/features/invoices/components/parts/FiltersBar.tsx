import { useState } from 'react';
import type { ReactElement } from 'react';
import { INVOICE_STATUSES } from '../../interfaces';

export type DateRange = [Date | null, Date | null];

export interface InvoiceFiltersValues {
    startDate: string;
    endDate: string;
    providerId?: string;
    serie?: string;
    folio?: string;
    statusId?: number;
}

interface FiltersBarProps {
    onSearch: (values: InvoiceFiltersValues) => void;
    isAdmin?: boolean;
    messages?: Record<string, string>;
}

const styles = {
    container: {},
    title: {
        fontSize: '1rem',
        fontWeight: 500,
        color: '#374151',
        marginBottom: '0.75rem',
    },
    filterBox: {
        border: '1px solid #e5e7eb',
        borderRadius: '0.5rem',
        padding: '1rem',
        backgroundColor: '#ffffff',
    },
    filterRow: {
        display: 'flex',
        alignItems: 'center',
        gap: '0.75rem',
        flexWrap: 'wrap' as const,
    },
    inputWrapper: {
        position: 'relative' as const,
    },
    input: {
        border: '1px solid #d1d5db',
        borderRadius: '0.25rem',
        padding: '0.5rem 0.75rem',
        fontSize: '0.875rem',
        height: '2.25rem',
        outline: 'none',
        transition: 'border-color 150ms',
    },
    inputFocused: {
        borderColor: '#9ca3af',
    },
    select: {
        appearance: 'none' as const,
        border: '1px solid #d1d5db',
        borderRadius: '0.25rem',
        padding: '0.5rem 2rem 0.5rem 0.75rem',
        fontSize: '0.875rem',
        backgroundColor: '#ffffff',
        height: '2.25rem',
        outline: 'none',
        transition: 'border-color 150ms',
    },
    selectCaret: {
        position: 'absolute' as const,
        right: '0.5rem',
        top: '50%',
        transform: 'translateY(-50%)',
        color: '#9ca3af',
        pointerEvents: 'none' as const,
        fontSize: '0.75rem',
    },
    spacer: {
        flex: 1,
    },
    buttonGroup: {
        display: 'flex',
        gap: '0.5rem',
    },
    button: {
        padding: '0.5rem 1.5rem',
        backgroundColor: '#ffffff',
        border: '1px solid #111827',
        color: '#111827',
        borderRadius: '0.25rem',
        fontSize: '0.875rem',
        fontWeight: 500,
        height: '2.25rem',
        cursor: 'pointer',
        transition: 'background-color 150ms',
    },
    buttonHover: {
        backgroundColor: '#f9fafb',
    },
    errorBox: {
        backgroundColor: '#fef2f2',
        border: '1px solid #fca5a5',
        color: '#991b1b',
        padding: '0.5rem 1rem',
        borderRadius: '0.375rem',
        marginTop: '0.75rem',
    },
};

export default function FiltersBar({
    onSearch,
    isAdmin = false,
    messages = {}
}: FiltersBarProps): ReactElement {
    const [startDate, setStartDate] = useState<Date | null>(new Date());
    const [endDate, setEndDate] = useState<Date | null>(new Date());
    const [providerId, setProviderId] = useState<string>('');
    const [serie, setSerie] = useState<string>('');
    const [folio, setFolio] = useState<string>('');
    const [status, setStatus] = useState<string>('');
    const [error, setError] = useState<string>('');

    const validateDates = (start: Date | null, end: Date | null): string | null => {
        if (!start || !end) {
            return 'Las fechas de inicio y fin son obligatorias';
        }

        if (start > end) {
            return messages['WRN7000'] || 'La fecha inicio no puede ser superior a la fecha final';
        }

        const sixMonthsLater = new Date(start);
        sixMonthsLater.setMonth(sixMonthsLater.getMonth() + 6);

        if (end > sixMonthsLater) {
            return messages['WRN7005'] || 'El periodo de búsqueda no puede ser superior a 6 meses, favor de validar';
        }

        return null;
    };

    const handleSubmit = () => {
        const validationError = validateDates(startDate, endDate);
        if (validationError) {
            setError(validationError);
            return;
        }

        setError('');

        const payload: InvoiceFiltersValues = {
            startDate: startDate!.toISOString().split('T')[0],
            endDate: endDate!.toISOString().split('T')[0],
            providerId: isAdmin && providerId ? providerId : undefined,
            serie: serie || undefined,
            folio: folio || undefined,
            statusId: status ? Number(status) : undefined
        };

        onSearch(payload);
    };

    const handleClear = () => {
        setStartDate(new Date());
        setEndDate(new Date());
        setProviderId('');
        setSerie('');
        setFolio('');
        setStatus('');
        setError('');
    };

    return (
        <div style={styles.container}>
            <h2 style={styles.title}>Parametros de busqueda</h2>

            <div style={styles.filterBox}>
                <div style={styles.filterRow}>
                    {/* Fecha Inicio */}
                    <div style={styles.inputWrapper}>
                        <input
                            type="text"
                            placeholder="Fecha Inicio"
                            value={startDate ? startDate.toLocaleDateString('es-ES') : ''}
                            onFocus={(e) => {
                                const input = e.target as HTMLInputElement;
                                input.type = 'date';
                                if (startDate) {
                                    input.value = startDate.toISOString().split('T')[0];
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
                                setStartDate(newDate);
                            }}
                            style={{ ...styles.input, width: '11rem' }}
                        />
                    </div>

                    {/* Fecha Final */}
                    <div style={styles.inputWrapper}>
                        <input
                            type="text"
                            placeholder="Fecha Final"
                            value={endDate ? endDate.toLocaleDateString('es-ES') : ''}
                            onFocus={(e) => {
                                const input = e.target as HTMLInputElement;
                                input.type = 'date';
                                if (endDate) {
                                    input.value = endDate.toISOString().split('T')[0];
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
                                setEndDate(newDate);
                            }}
                            style={{ ...styles.input, width: '11rem' }}
                        />
                    </div>

                    {/* ID Proveedor */}
                    {isAdmin && (
                        <div style={styles.inputWrapper}>
                            <input
                                type="text"
                                placeholder="ID Proveedor"
                                value={providerId}
                                onChange={(e) => setProviderId(e.target.value.replace(/\D/g, ''))}
                                maxLength={10}
                                style={{ ...styles.input, width: '11rem' }}
                            />
                        </div>
                    )}

                    {/* Serie */}
                    <div style={styles.inputWrapper}>
                        <input
                            type="text"
                            placeholder="Serie"
                            value={serie}
                            onChange={(e) => setSerie(e.target.value.slice(0, 30))}
                            maxLength={30}
                            style={{ ...styles.input, width: '8rem' }}
                        />
                    </div>

                    {/* Folio */}
                    <div style={styles.inputWrapper}>
                        <input
                            type="text"
                            placeholder="Folio"
                            value={folio}
                            onChange={(e) => setFolio(e.target.value.slice(0, 30))}
                            maxLength={30}
                            style={{ ...styles.input, width: '8rem' }}
                        />
                    </div>

                    {/* Estatus */}
                    <div style={styles.inputWrapper}>
                        <select
                            value={status}
                            onChange={(e) => setStatus(e.target.value)}
                            style={{ ...styles.select, width: '12rem' }}
                        >
                            <option value="">Estatus factura</option>
                            {INVOICE_STATUSES.map(s => (
                                <option key={s.id} value={s.id.toString()}>
                                    {s.description}
                                </option>
                            ))}
                        </select>
                        <span style={styles.selectCaret}>▼</span>
                    </div>

                    <div style={styles.spacer}></div>

                    {/* Botones */}
                    <div style={styles.buttonGroup}>
                        <button
                            onClick={handleClear}
                            style={styles.button}
                            onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f9fafb'}
                            onMouseLeave={(e) => e.currentTarget.style.backgroundColor = '#ffffff'}
                        >
                            Limpiar filtros
                        </button>
                        <button
                            onClick={handleSubmit}
                            style={styles.button}
                            onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f9fafb'}
                            onMouseLeave={(e) => e.currentTarget.style.backgroundColor = '#ffffff'}
                        >
                            Buscar
                        </button>
                    </div>
                </div>
            </div>

            {error && (
                <div style={styles.errorBox}>
                    {error}
                </div>
            )}
        </div>
    );
}
