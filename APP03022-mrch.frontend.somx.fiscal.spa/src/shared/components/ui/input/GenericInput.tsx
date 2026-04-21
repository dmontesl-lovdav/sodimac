import { useEffect, useId, useState } from 'react';
import './Input.css';

export interface GenericInputProps extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'value' | 'onChange'> {
  label?: string;
  value?: string;
  onChange?: React.ChangeEventHandler<HTMLInputElement>;
  placeholder?: string;
  required?: boolean;
  maxLength?: number;
  className?: string;
  disabled?: boolean;
  readOnly?: boolean;
  name?: string;
  type?: string;
  error?: boolean;
  helperText?: string;
  validateEmail?: boolean;
  requiredMarkClassName?: string;
}

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
  id: idProp,
  ...props
}: GenericInputProps): React.ReactElement {
  const id = useId();
  const actualId = idProp ?? id;
  const counterId = `${actualId}-counter`;
  const helperId = `${actualId}-helper`;

  const hasValue = typeof value === 'string' ? value.length > 0 : !!value;
  const length = typeof value === 'string' ? value.length : 0;

  const [focused, setFocused] = useState(false);
  const [leftPad, setLeftPad] = useState(16);
  const [emailError, setEmailError] = useState('');

  useEffect(() => {
    const el = document.getElementById(actualId);
    if (!el) return;
    const update = (): void => {
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
  }, [actualId]);

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

  const rootClass = `fiscal-input-root ${className}`.trim();
  const inputClass = `fiscal-input-field ${showError ? 'fiscal-input-field-error' : ''}`.trim();
  const labelClass = `fiscal-input-label ${focused || hasValue ? 'fiscal-input-label-floating' : 'fiscal-input-label-resting'} ${showError ? 'fiscal-input-label-error' : focused ? 'fiscal-input-label-focused' : 'fiscal-input-label-default'} ${requiredMarkClassName}`.trim();

  return (
    <div
      className={rootClass}
      style={{ ['--fiscal-input-label-left' as string]: `${leftPad}px` } as React.CSSProperties}
    >
      <input
        id={actualId}
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
        aria-describedby={describedBy || undefined}
        className={inputClass}
        {...props}
      />

      {label ? (
        <label htmlFor={actualId} className={labelClass} title={label}>
          {label}
          {required ? <span className="fiscal-input-required" aria-hidden="true">*</span> : null}
        </label>
      ) : null}

      {(message || maxLength !== undefined) ? (
        <div className="fiscal-input-helper-row">
          <div id={helperId} className={showError ? 'fiscal-input-helper-error' : 'fiscal-input-helper-default'}>
            {message}
          </div>
          {maxLength !== undefined ? (
            <div id={counterId} className={showError ? 'fiscal-input-helper-error' : 'fiscal-input-helper-default'}>
              {length} / {maxLength}
            </div>
          ) : null}
        </div>
      ) : null}
    </div>
  );
}
