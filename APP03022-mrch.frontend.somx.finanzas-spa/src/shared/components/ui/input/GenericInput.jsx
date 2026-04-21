import { useEffect, useId, useState } from 'react';

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
  ...props
}) {
  const id = useId();
  const counterId = `${id}-counter`;
  const hasValue = typeof value === 'string' ? value.length > 0 : !!value;
  const length = typeof value === 'string' ? value.length : 0;

  const [focused, setFocused] = useState(false);
  const [leftPad, setLeftPad] = useState(16);

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

  return (
    <div className={`somx-input-wrapper ${className}`}>
      <input
        id={id}
        name={name}
        type={type}
        value={value}
        onChange={onChange}
        onFocus={() => setFocused(true)}
        onBlur={() => setFocused(false)}
        placeholder={placeholder}
        disabled={disabled}
        readOnly={readOnly}
        maxLength={maxLength}
        aria-invalid={!!error}
        aria-describedby={maxLength ? counterId : undefined}
        className={`somx-input ${disabled ? 'somx-input-disabled' : ''} ${error ? 'somx-input-error' : ''
          }`}
        {...props}
      />

      {label && (
        <label
          htmlFor={id}
          className={`somx-label ${focused || hasValue ? 'somx-label-focused' : 'somx-label-default'
            } ${error
              ? 'somx-label-error'
              : focused
                ? 'somx-label-focused-color'
                : 'somx-label-default-color'
            }`}
          style={{ left: `${leftPad}px` }}
        >
          {label}
          {required && <span aria-hidden="true">*</span>}
        </label>
      )}

      {maxLength !== undefined && (
        <div
          id={counterId}
          className={`somx-counter ${error ? 'somx-counter-error' : ''}`}
        >
          {length} / {maxLength}
        </div>
      )}
    </div>
  );
}
