import { GenericButton, GenericDateRangePicker, GenericInput, GenericSelect } from '@shared/components/ui';
import { format } from 'date-fns';
import { useState } from 'react';
import '../styles/RequestFilters.css';

export default function RequestFilters({
    reasons = [],
    statusClazzOptions = [],
    filters = {},
    setFilters,
    targetState: targetState,
    targetStateFunction: targetStateFunction
}) {
    const [criteria, setCriteria] = useState(filters.criteria);
    const [dateFrom, setDateFrom] = useState(
        filters.dateFrom ? new Date(filters.dateFrom) : null
    );
    const [dateTo, setDateTo] = useState(
        filters.dateTo ? new Date(filters.dateTo) : null
    );
    const [reason, setReason] = useState(filters.reason);
    const [clazz, setClazz] = useState(filters.clazz);

    const handleSearch = () => {
        setFilters({
            criteria,
            dateFrom: dateFrom ? format(dateFrom, 'yyyy-MM-dd') : '',
            dateTo: dateTo ? format(dateTo, 'yyyy-MM-dd') : '',
            reason,
            clazz,
        });
        targetState && targetStateFunction && targetStateFunction(targetState);
    };

    const handleReset = () => {
        setCriteria('');
        setDateFrom(null);
        setDateTo(null);
        setReason('');
        setClazz('');
        setFilters({
            criteria: '',
            dateFrom: '',
            dateTo: '',
            reason: '',
            clazz: '',
        });
        targetState && targetStateFunction && targetStateFunction(targetState);
    };

    return (
        <div className="rf-grid">

            <GenericInput
                className="rf-mt--2"
                value={criteria}
                onChange={(e) => setCriteria(e.target.value)}
                name="criteria"
                label="Orden"
                placeholder="Orden de compra o solicitud"
                type="text"
                autoComplete="off"
            />

            <GenericDateRangePicker
                value={[dateFrom, dateTo]}
                onChange={(dates) => {
                    setDateFrom(dates[0]);
                    setDateTo(dates[1]);
                }}
                size="lg"
                inputClassName="rf-date-input-padding"
            />

            <GenericSelect
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                options={reasons}
                placeholder="Categoría"
                widthClass="rf-full"
            />

            <GenericSelect
                value={clazz}
                onChange={(e) => setClazz(e.target.value)}
                options={statusClazzOptions}
                placeholder="Estado"
                widthClass="rf-full"
            />

            <div className="rf-actions">
                <GenericButton variant="outlineFill" onClick={handleSearch}>
                    Buscar
                </GenericButton>
                <GenericButton variant="outline" onClick={handleReset}>
                    Reset
                </GenericButton>
            </div>
        </div>
    );
}
