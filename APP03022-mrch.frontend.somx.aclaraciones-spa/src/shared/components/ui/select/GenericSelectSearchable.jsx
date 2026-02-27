import { useEffect, useMemo, useRef, useState } from 'react';
import './styles/GenericSelectSearchable.css';

export default function GenericSelectSearchable({
    value,
    onChange,
    options = [],
    placeholder = 'Buscar…',
    widthClass = 'gs-width-default',
    containerClassName = '',
}) {
    const [open, setOpen] = useState(false);
    const [query, setQuery] = useState('');
    const ref = useRef(null);

    const filtered = useMemo(() => {
        if (!query) return options;
        return options.filter(o =>
            o.label.toLowerCase().includes(query.toLowerCase())
        );
    }, [query, options]);

    useEffect(() => {
        const handler = (e) => {
            if (!ref.current?.contains(e.target)) {
                setOpen(false);
            }
        };
        document.addEventListener('mousedown', handler);
        return () => document.removeEventListener('mousedown', handler);
    }, []);

    useEffect(() => {
        if (!value) {
            setQuery('');
        } else {
            const found = options.find(o => String(o.value) === String(value));
            if (found) setQuery(found.label);
        }
    }, [value, options]);

    const handleSelect = (opt) => {
        onChange({ target: { value: opt.value } });
        setQuery(opt.label);
        setOpen(false);
    };

    const handleClear = () => {
        setQuery('');
        onChange({ target: { value: '' } });
        setOpen(false);
    };

    return (
        <div
            ref={ref}
            className={`generic-select-searchable ${widthClass} ${containerClassName}`}
        >
            <input
                value={query}
                placeholder={placeholder}
                onChange={(e) => {
                    setQuery(e.target.value);
                    setOpen(true);
                }}
                onFocus={() => setOpen(true)}
                className="gss-input"
            />

            {query && (
                <button
                    type="button"
                    className="gss-clear"
                    onClick={handleClear}
                    aria-label="Limpiar"
                >
                    ✕
                </button>
            )}

            <span className="generic-select-caret">▾</span>

            {open && (
                <div className="gss-dropdown">
                    {filtered.length === 0 && (
                        <div className="gss-empty">Sin resultados</div>
                    )}

                    {filtered.map(opt => (
                        <div
                            key={opt.value}
                            className="gss-option"
                            onClick={() => handleSelect(opt)}
                        >
                            {opt.label}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
