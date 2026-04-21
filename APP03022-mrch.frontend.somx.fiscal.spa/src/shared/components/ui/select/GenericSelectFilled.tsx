import React from 'react';
import './Select.css';

export interface GenericSelectFilledOption {
  value: string | number;
  label: string;
}

export interface GenericSelectFilledProps extends Omit<React.SelectHTMLAttributes<HTMLSelectElement>, 'value' | 'onChange'> {
  value?: string | number | null;
  onChange?: (e: React.ChangeEvent<HTMLSelectElement>) => void;
  options?: GenericSelectFilledOption[];
  placeholder?: string;
  fullWidth?: boolean;
  className?: string;
}

export default function GenericSelectFilled({
  value,
  onChange,
  options = [],
  placeholder = 'Selecciona…',
  fullWidth = true,
  className = '',
  ...props
}: GenericSelectFilledProps): React.ReactElement {
  const rootClass = `fiscal-select-filled-root ${fullWidth ? 'fiscal-select-filled-root-fullWidth' : 'fiscal-select-filled-root-fixed'} ${className}`.trim();
  return (
    <div className={rootClass}>
      <select
        value={value ?? ''}
        onChange={onChange ?? undefined}
        className="fiscal-select-filled-field"
        {...props}
      >
        {placeholder ? <option value="">{placeholder}</option> : null}
        {options.map(({ value: v, label }) => (
          <option key={String(v)} value={v}>
            {label}
          </option>
        ))}
      </select>
      <span className="fiscal-select-caret">▾</span>
    </div>
  );
}
