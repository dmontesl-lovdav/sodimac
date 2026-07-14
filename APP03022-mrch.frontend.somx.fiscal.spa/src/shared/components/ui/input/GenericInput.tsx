import { useEffect, useId, useState } from 'react';
import './Input.css';

export function getInputLeftPad(el: HTMLElement): number {
  const cs = window.getComputedStyle(el);
  const pl = parseFloat(cs.paddingLeft || '16');
  return Number.isFinite(pl) ? pl : 16;
}

/** Validación de correo sin regex con backtracking (evita S5852 / ReDoS). */
export function isValidEmailFormat(value: string): boolean {
  if (!value || value.includes(' ') || value.includes('\t')) return false;
  const at = value.indexOf('@');
  if (at <= 0 || at !== value.lastIndexOf('@')) return false;
  const local = value.slice(0, at);
  const domain = value.slice(at + 1);
  if (!local || !domain) return false;
  const dot = domain.lastIndexOf('.');
  if (dot <= 0 || dot >= domain.length - 1) return false;
  return !domain.includes('@');
}

export function getEmailError(value: string, validateEmail: boolean): string {
  if (!validateEmail || !value) return '';
  return isValidEmailFormat(value) ? '' : 'El correo no tiene un formato válido';
}

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

  const hasValue = value.length > 0;
  const length = value.length;

  const [focused, setFocused] = useState(false);
  const [leftPad, setLeftPad] = useState(16);
  const [emailError, setEmailError] = useState('');

  useEffect(() => {
    const el = document.getElementById(actualId);
    if (!el) return;
    const update = (): void => setLeftPad(getInputLeftPad(el));
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
    setEmailError(getEmailError(value, validateEmail));
  }, [value, validateEmail]);

  const isFloating = focused || hasValue;
  const effectivePlaceholder = isFloating ? placeholder : '';
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
  const labelPositionClass = isFloating ? 'fiscal-input-label-floating' : 'fiscal-input-label-resting';
  const focusedOrDefaultClass = focused ? 'fiscal-input-label-focused' : 'fiscal-input-label-default';
  const labelColorClass = showError ? 'fiscal-input-label-error' : focusedOrDefaultClass;
  const labelClass = `fiscal-input-label ${labelPositionClass} ${labelColorClass} ${requiredMarkClassName}`.trim();

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
