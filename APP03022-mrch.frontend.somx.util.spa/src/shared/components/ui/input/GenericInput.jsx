import { useEffect, useId, useState } from 'react';
import PropTypes from 'prop-types';
import './GenericInput.css';

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
    ...props
}) {
    const id = useId();
    const counterId = `${id}-counter`;
    const helperId = `${id}-helper`;

    const hasValue = typeof value === 'string' ? value.length > 0 : !!value;
    const length = typeof value === 'string' ? value.length : 0;

    const [focused, setFocused] = useState(false);
    const [emailError, setEmailError] = useState('');

    // Validación email
    useEffect(() => {
        if (!validateEmail) return;
        if (!value) {
            setEmailError('');
            return;
        }
        const valid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
        setEmailError(valid ? '' : 'El correo no tiene un formato válido');
    }, [value, validateEmail]);

    const showError = !!error || !!emailError;
    const message = emailError || helperText;

    const describedBy = [
        message ? helperId : null,
        maxLength !== undefined ? counterId : null,
    ].filter(Boolean).join(' ') || undefined;

    return (
        <div className={`generic-input-container ${className}`}>
            <input
                id={id}
                name={name}
                type={type}
                value={value}
                onChange={onChange}
                onFocus={() => setFocused(true)}
                onBlur={() => setFocused(false)}
                placeholder={focused || hasValue ? placeholder : ''}
                disabled={disabled}
                readOnly={readOnly}
                maxLength={maxLength}
                aria-invalid={showError}
                aria-describedby={describedBy}
                className={`generic-input ${showError ? 'error' : ''}`}
                {...props}
            />

            {label && (
                <label
                    htmlFor={id}
                    className={`
                        generic-input-label
                        ${focused || hasValue ? 'floating' : 'resting'}
                        ${focused ? 'focused' : ''}
                        ${showError ? 'error' : ''}
                    `}
                >
                    {label}
                    {required && <span className="text-rose-600">*</span>}
                </label>
            )}

            {(message || maxLength !== undefined) && (
                <div className="generic-input-footer">
                    <div
                        id={helperId}
                        className={`generic-input-helper ${showError ? 'error' : ''}`}
                    >
                        {message}
                    </div>

                    {maxLength !== undefined && (
                        <div
                            id={counterId}
                            className={`generic-input-counter ${showError ? 'error' : ''}`}
                        >
                            {length} / {maxLength}
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}

GenericInput.propTypes = {
    label: PropTypes.string,
    value: PropTypes.any,
    onChange: PropTypes.func,
    placeholder: PropTypes.string,
    required: PropTypes.bool,
    maxLength: PropTypes.number,
    className: PropTypes.string,
    disabled: PropTypes.bool,
    readOnly: PropTypes.bool,
    name: PropTypes.string,
    type: PropTypes.string,
    error: PropTypes.any,
    helperText: PropTypes.string,
    validateEmail: PropTypes.bool,
};
