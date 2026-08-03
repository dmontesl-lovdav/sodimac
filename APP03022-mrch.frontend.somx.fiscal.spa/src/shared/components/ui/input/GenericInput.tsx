import { useEffect, useId, useState } from 'react';
import './Input.css';

export function getInputLeftPad(el: HTMLElement): number {
  const cs = window.getComputedStyle(el);
  const pl = parseFloat(cs.paddingLeft ?? '16');
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

/** Lee el padding-left del input y mantiene el label flotante alineado con el texto. */
function useLeftPad(actualId: string): number {
  const [leftPad, setLeftPad] = useState(16);

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

  return leftPad;
}

function getLabelClass(
  isFloating: boolean,
  focused: boolean,
  showError: boolean,
  requiredMarkClassName: string,
): string {
  const positionClass = isFloating ? 'fiscal-input-label-floating' : 'fiscal-input-label-resting';
  const focusedOrDefaultClass = focused ? 'fiscal-input-label-focused' : 'fiscal-input-label-default';
  const colorClass = showError ? 'fiscal-input-label-error' : focusedOrDefaultClass;
  return `fiscal-input-label ${positionClass} ${colorClass} ${requiredMarkClassName}`.trim();
}

function buildDescribedBy(
  hasMessage: boolean,
  hasCounter: boolean,
  helperId: string,
  counterId: string,
): string | undefined {
  const ids = [hasMessage ? helperId : null, hasCounter ? counterId : null].filter(Boolean);
  return ids.length > 0 ? ids.join(' ') : undefined;
}

interface InputLabelProps {
  actualId: string;
  label: string;
  required: boolean;
  labelClass: string;
}

function InputLabel({ actualId, label, required, labelClass }: InputLabelProps): React.ReactElement | null {
  if (!label) return null;
  return (
    <label htmlFor={actualId} className={labelClass} title={label}>
      {label}
      {required ? <span className="fiscal-input-required" aria-hidden="true">*</span> : null}
    </label>
  );
}

interface InputHelperRowProps {
  message: string;
  maxLength?: number;
  length: number;
  showError: boolean;
  helperId: string;
  counterId: string;
}

function InputHelperRow({
  message,
  maxLength,
  length,
  showError,
  helperId,
  counterId,
}: InputHelperRowProps): React.ReactElement | null {
  const hasCounter = maxLength !== undefined;
  if (!message && !hasCounter) return null;

  const helperClass = showError ? 'fiscal-input-helper-error' : 'fiscal-input-helper-default';
  return (
    <div className="fiscal-input-helper-row">
      <div id={helperId} className={helperClass}>
        {message}
      </div>
      {hasCounter ? (
        <div id={counterId} className={helperClass}>
          {length} / {maxLength}
        </div>
      ) : null}
    </div>
  );
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
  const [emailError, setEmailError] = useState('');
  const leftPad = useLeftPad(actualId);

  useEffect(() => {
    setEmailError(getEmailError(value, validateEmail));
  }, [value, validateEmail]);

  const isFloating = focused || hasValue;
  const effectivePlaceholder = isFloating ? placeholder : '';
  const showError = !!error || !!emailError;
  const message = emailError || helperText;
  const hasCounter = maxLength !== undefined;

  const describedBy = buildDescribedBy(Boolean(message), hasCounter, helperId, counterId);
  const rootClass = `fiscal-input-root ${className}`.trim();
  const inputClass = `fiscal-input-field ${showError ? 'fiscal-input-field-error' : ''}`.trim();
  const labelClass = getLabelClass(isFloating, focused, showError, requiredMarkClassName);

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
        aria-describedby={describedBy}
        className={inputClass}
        {...props}
      />

      <InputLabel actualId={actualId} label={label} required={required} labelClass={labelClass} />

      <InputHelperRow
        message={message}
        maxLength={maxLength}
        length={length}
        showError={showError}
        helperId={helperId}
        counterId={counterId}
      />
    </div>
  );
}
