import { useEffect, useId, useState } from 'react';

import './styles/GenericInput.css';

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

  const wrapperMods = [
    'generic-textfield-wrapper',
    maxLength !== undefined ? 'generic-textfield-wrapper--with-counter' : '',
    className,
  ]
    .filter(Boolean)
    .join(' ');

  const toneClass = (() => {
    if (error) return 'generic-textfield-label--tone-error';
    if (focused) return 'generic-textfield-label--tone-focus';
    return 'generic-textfield-label--tone-idle';
  })();

  const inputMods = [
    'generic-textfield-input',
    label ? 'generic-textfield-input--labeled' : '',
    disabled ? 'generic-textfield-input--disabled' : '',
    error ? 'generic-textfield-input--error' : '',
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <div className={wrapperMods}>
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
        className={inputMods}
        {...props}
      />

      {label && (
        <label
          htmlFor={id}
          className={[
            'generic-textfield-label',
            focused || hasValue ? 'generic-textfield-label--floating' : '',
            toneClass,
          ]
            .filter(Boolean)
            .join(' ')}
          style={{ left: `${leftPad}px` }}
        >
          {label}
          {required && <span aria-hidden="true">*</span>}
        </label>
      )}

      {maxLength !== undefined && (
        <div
          id={counterId}
          className={[
            'generic-textfield-counter',
            error ? 'generic-textfield-counter--error' : '',
          ]
            .filter(Boolean)
            .join(' ')}
        >
          {length} / {maxLength}
        </div>
      )}
    </div>
  );
}
