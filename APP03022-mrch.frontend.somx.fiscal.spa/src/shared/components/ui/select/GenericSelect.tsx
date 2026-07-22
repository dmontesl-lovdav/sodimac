import React from 'react';
import './Select.css';

export interface GenericSelectOption {
  value: string | number;
  label: string;
}

export interface GenericSelectProps extends Omit<React.SelectHTMLAttributes<HTMLSelectElement>, 'value' | 'onChange'> {
  value?: string | number | null;
  onChange?: (e: React.ChangeEvent<HTMLSelectElement>) => void;
  options?: GenericSelectOption[];
  placeholder?: string;
  widthClass?: string;
  selectClassName?: string;
  containerClassName?: string;
  className?: string;
}

export default function GenericSelect({
  value,
  onChange,
  options = [],
  placeholder = 'Select…',
  widthClass = '',
  selectClassName = '',
  containerClassName = '',
  className = '',
  ...props
}: GenericSelectProps): React.ReactElement {
  const rootClass = `fiscal-select-default-root ${containerClassName} ${className}`.trim();
  const width = widthClass ?? '15rem';
  return (
    <div className={rootClass} style={{ ['--fiscal-select-width' as string]: width } as React.CSSProperties}>
      <select
        value={value ?? ''}
        onChange={onChange ?? undefined}
        className={`fiscal-select-default-field ${selectClassName}`.trim()}
        {...props}
      >
        {placeholder ? <option value="">{placeholder}</option> : null}
        {options.map(({ value: v, label }) => (
          <option key={String(v)} value={v}>
            {label}
          </option>
        ))}
      </select>
      <span className="fiscal-select-default-caret">▾</span>
    </div>
  );
}
