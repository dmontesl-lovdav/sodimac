import { useEffect, useId, useState } from 'react';

const styles = {
    wrapper: {
        position: 'relative',
        width: '100%',
    },
    input: {
        display: 'block',
        width: '100%',
        backgroundColor: 'transparent',
        outline: 'none',
        fontSize: '15px',
        lineHeight: '1.5rem',
        border: 'none',
        borderBottom: '1px solid #cbd5e1',
        padding: '1.5rem 1rem 0.5rem 1rem',
        transition: 'border-color 150ms ease',
    },
    inputFocused: {
        borderBottomColor: '#0369a1',
    },
    inputError: {
        borderBottomColor: '#f43f5e',
    },
    inputDisabled: {
        color: '#94a3b8',
        cursor: 'not-allowed',
    },
    label: {
        position: 'absolute',
        pointerEvents: 'none',
        transition: 'all 200ms ease',
        fontWeight: '500',
        lineHeight: '1.25rem',
    },
    labelFloating: {
        top: '0.375rem',
        fontSize: '0.75rem',
    },
    labelResting: {
        top: '0.9375rem',
        fontSize: '0.875rem',
        transform: 'translateY(-1px)',
    },
    labelDefault: {
        color: '#475569',
    },
    labelFocused: {
        color: '#0369a1',
    },
    labelError: {
        color: '#e11d48',
    },
    requiredMark: {
        marginLeft: '0.125rem',
        color: '#e11d48',
    },
    helperRow: {
        marginTop: '0.25rem',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        fontSize: '0.75rem',
    },
    helperDefault: {
        color: '#64748b',
    },
    helperError: {
        color: '#e11d48',
    },
};

export default function GenericInput({
    label = '',
    value = '',
    onChange,
    placeholder = '',
    required = false,
    maxLength,
    className = '',
    disabled = false,
    readOnly = false,
    name,
    type = 'text',
    error = false,
    helperText = '',
    validateEmail = false,
    requiredMarkClassName = '',
    ...props
}) {
    const id = useId();
    const counterId = `${id}-counter`;
    const helperId = `${id}-helper`;

    const hasValue = typeof value === 'string' ? value.length > 0 : !!value;
    const length = typeof value === 'string' ? value.length : 0;

    const [focused, setFocused] = useState(false);
    const [leftPad, setLeftPad] = useState(16);
    const [emailError, setEmailError] = useState('');

    useEffect(() => {
        const el = document.getElementById(id);
        if (!el) return;
        const update = () => {
            const cs = window.getComputedStyle(el);
            const pl = parseFloat(cs.paddingLeft || '16');
            setLeftPad(Number.isFinite(pl) ? pl : 16);
        };
        update();
        const ro = new ResizeObserver(update);
        ro.observe(el);
        window.addEventListener('resize', update);
        return () => {
            ro.disconnect();
            window.removeEventListener('resize', update);
        };
    }, [id]);

    useEffect(() => {
        if (!validateEmail) return;
        if (!value) {
            setEmailError('');
            return;
        }
        const valid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
        setEmailError(valid ? '' : 'El correo no tiene un formato válido');
    }, [value, validateEmail]);

    const effectivePlaceholder = focused || hasValue ? placeholder : '';
    const showError = !!error || !!emailError;
    const message = emailError || helperText;

    const describedBy = [
        message ? helperId : null,
        maxLength !== undefined ? counterId : null,
    ]
        .filter(Boolean)
        .join(' ') || undefined;

    const inputStyle = {
        ...styles.input,
        ...(focused ? styles.inputFocused : {}),
        ...(showError ? styles.inputError : {}),
        ...(disabled ? styles.inputDisabled : {}),
        color: disabled ? '#94a3b8' : '#0f172a',
    };

    const labelStyle = {
        ...styles.label,
        ...(focused || hasValue ? styles.labelFloating : styles.labelResting),
        ...(showError ? styles.labelError : focused ? styles.labelFocused : styles.labelDefault),
        left: `${leftPad}px`,
    };

    return (
        <div style={styles.wrapper} className={className}>
            <input
                id={id}
                name={name}
                type={type}
                value={value}
                onChange={onChange}
                onFocus={() => setFocused(true)}
                onBlur={() => setFocused(false)}
                placeholder={effectivePlaceholder}
                disabled={disabled}
                readOnly={readOnly}
                maxLength={maxLength}
                aria-invalid={showError}
                aria-describedby={describedBy}
                style={inputStyle}
                {...props}
            />

            {label && (
                <label
                    htmlFor={id}
                    style={labelStyle}
                    title={label}
                >
                    {label}
                    {required && <span style={styles.requiredMark} aria-hidden="true">*</span>}
                </label>
            )}

            {(message || maxLength !== undefined) && (
                <div style={styles.helperRow}>
                    <div
                        id={helperId}
                        style={showError ? styles.helperError : styles.helperDefault}
                    >
                        {message}
                    </div>

                    {maxLength !== undefined && (
                        <div
                            id={counterId}
                            style={showError ? styles.helperError : styles.helperDefault}
                        >
                            {length} / {maxLength}
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}
