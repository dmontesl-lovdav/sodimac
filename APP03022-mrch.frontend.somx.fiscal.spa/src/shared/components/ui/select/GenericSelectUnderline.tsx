import { useMemo, useState } from 'react';
import './Select.css';

export interface SelectOption<T = string> {
  value: T;
  label: string;
}

export interface GenericSelectUnderlineProps<T = string> extends Omit<React.SelectHTMLAttributes<HTMLSelectElement>, 'value' | 'onChange'> {
  label?: string;
  value?: T | null;
  onChange?: (e: React.ChangeEvent<HTMLSelectElement>) => void;
  options?: SelectOption<T>[];
  placeholder?: string;
  includePlaceholder?: boolean;
  fullWidth?: boolean;
  className?: string;
}

export default function GenericSelectUnderline<T = string>({
  label,
  value,
  onChange,
  options = [],
  placeholder = 'Selecciona…',
  includePlaceholder = true,
  fullWidth = true,
  className = '',
  ...props
}: GenericSelectUnderlineProps<T>): React.ReactElement {
  const [focused, setFocused] = useState(false);
  const normValue = value == null ? '' : String(value);

  const hasEmpty = useMemo(
    () => options.some((o) => String((o as SelectOption)?.value ?? '') === ''),
    [options]
  );
  const showPlaceholder = includePlaceholder && !hasEmpty && Boolean(placeholder);
  const filled = focused || normValue !== '';

  const rootClass = `fiscal-select-root ${fullWidth ? 'fiscal-select-root-fullWidth' : 'fiscal-select-root-fixed'} ${className}`.trim();
  const labelClass = `fiscal-select-label ${filled ? 'fiscal-select-label-floating' : 'fiscal-select-label-resting'}`;

  return (
    <div className={rootClass}>
      <div className="fiscal-select-inner">
        {label ? <span className={labelClass}>{label}</span> : null}
        <select
          value={normValue}
          onChange={onChange ?? undefined}
          onFocus={() => setFocused(true)}
          onBlur={() => setFocused(false)}
          className="fiscal-select-field"
          {...(props as React.SelectHTMLAttributes<HTMLSelectElement>)}
        >
          {showPlaceholder ? (
            <option value="" disabled>
              {placeholder}
            </option>
          ) : null}
          {options.map(({ value: v, label: lbl }) => {
            const val = String((v as unknown) ?? '');
            return (
              <option key={`opt-${val}`} value={val}>
                {lbl}
              </option>
            );
          })}
        </select>
        <span className="fiscal-select-caret">▾</span>
      </div>
    </div>
  );
}
