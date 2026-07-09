import {
  forwardRef,
  useEffect,
  useState,
  type ChangeEventHandler,
  type InputHTMLAttributes,
  type MouseEvent,
  type MouseEventHandler,
} from 'react';
import DatePicker from 'react-datepicker';
import { es } from 'date-fns/locale/es';

import 'react-datepicker/dist/react-datepicker.css';
import './styles/GenericDateRangePicker.css';

export type DateRange = [Date | null, Date | null];

export type Size = 'sm' | 'md' | 'lg';
export type DatePickerSize = Size;

export type GenericDateRangePickerProps = {
  value?: DateRange;
  onChange: (dates: DateRange) => void;
  placeholder?: string;
  className?: string;
  size?: Size;
  inputClassName?: string;
  popperClassName?: string;
  inputProps?: InputHTMLAttributes<HTMLInputElement>;
};

type DateInputProps = Omit<InputHTMLAttributes<HTMLInputElement>, 'size'> & {
  label: string;
  hasValue: boolean;
  onClearDate: (e: MouseEvent<HTMLButtonElement>) => void;
  onClick?: MouseEventHandler<HTMLInputElement>;
  onChange?: ChangeEventHandler<HTMLInputElement>;
};

const SIZE_CLASSES: Record<Size, string> = {
  sm: 'dr-sm',
  md: 'dr-md',
  lg: 'dr-lg',
};

const DateInput = forwardRef<HTMLInputElement, DateInputProps>(
  (
    {
      label,
      hasValue,
      onClearDate,
      className = '',
      value,
      onClick,
      onChange,
      placeholder,
      ...props
    },
    ref
  ) => {
    return (
      <div className="dr-input-shell">
        <span className="dr-field-label">{label}</span>

        <input
          ref={ref}
          value={value ?? ''}
          onClick={onClick}
          onChange={onChange}
          placeholder={placeholder}
          className={`${className} ${hasValue ? 'dr-input-with-clear' : ''}`}
          autoComplete="off"
          {...props}
        />

        {hasValue && (
          <button
            type="button"
            className="dr-field-clear-btn"
            onMouseDown={(e) => {
              e.preventDefault();
              e.stopPropagation();
            }}
            onClick={(e) => {
              e.preventDefault();
              e.stopPropagation();
              onClearDate(e);
            }}
            aria-label={`Limpiar fecha ${label.toLowerCase()}`}
            title={`Limpiar fecha ${label.toLowerCase()}`}
          >
            ×
          </button>
        )}
      </div>
    );
  }
);

DateInput.displayName = 'DateInput';

export default function GenericDateRangePicker({
  value = [null, null],
  onChange,
  placeholder = 'Selecciona',
  className = '',
  size = 'sm',
  inputClassName = '',
  popperClassName = '',
  inputProps = {},
}: GenericDateRangePickerProps) {
  const [internalRange, setInternalRange] = useState<DateRange>(value);

  useEffect(() => {
    setInternalRange(value);
  }, [value]);

  const today = new Date();

  const updateRange = (nextRange: DateRange) => {
    setInternalRange(nextRange);
    onChange(nextRange);
  };

  const handleStartChange = (date: Date | null) => {
    const [, currentEnd] = internalRange;
    const nextEnd = date && currentEnd && currentEnd < date ? null : currentEnd;

    updateRange([date, nextEnd]);
  };

  const handleEndChange = (date: Date | null) => {
    const [currentStart] = internalRange;

    updateRange([currentStart, date]);
  };

  const clearStartDate = (e: MouseEvent<HTMLButtonElement>) => {
    e.preventDefault();
    e.stopPropagation();

    const [, currentEnd] = internalRange;
    updateRange([null, currentEnd]);
  };

  const clearEndDate = (e: MouseEvent<HTMLButtonElement>) => {
    e.preventDefault();
    e.stopPropagation();

    const [currentStart] = internalRange;
    updateRange([currentStart, null]);
  };

  const [startDate, endDate] = internalRange;

  const baseInputClassName = `dr-input ${SIZE_CLASSES[size]} ${inputClassName}`;

  return (
    <div className={`dr-wrapper ${className}`}>
      <div className="dr-range-fields">
        <div className="dr-field">
          <DatePicker
            selected={startDate}
            onChange={(date) => handleStartChange(date as Date | null)}
            maxDate={endDate ?? today}
            locale={es}
            dateFormat="dd/MM/yyyy"
            placeholderText={placeholder}
            showMonthDropdown
            showYearDropdown
            dropdownMode="scroll"
            showPopperArrow={false}
            popperPlacement="bottom-start"
            wrapperClassName="dr-datepicker-wrapper"
            popperClassName={`dr-popper ${popperClassName}`}
            customInput={
              <DateInput
                {...inputProps}
                label="Desde"
                hasValue={Boolean(startDate)}
                onClearDate={clearStartDate}
                className={baseInputClassName}
                aria-label="Fecha desde"
                readOnly
              />
            }
          />
        </div>

        <div className="dr-field">
          <DatePicker
            selected={endDate}
            onChange={(date) => handleEndChange(date as Date | null)}
            minDate={startDate ?? undefined}
            maxDate={today}
            locale={es}
            dateFormat="dd/MM/yyyy"
            placeholderText={placeholder}
            showMonthDropdown
            showYearDropdown
            dropdownMode="scroll"
            showPopperArrow={false}
            popperPlacement="bottom-start"
            wrapperClassName="dr-datepicker-wrapper"
            popperClassName={`dr-popper ${popperClassName}`}
            customInput={
              <DateInput
                {...inputProps}
                label="Hasta"
                hasValue={Boolean(endDate)}
                onClearDate={clearEndDate}
                className={baseInputClassName}
                aria-label="Fecha hasta"
                readOnly
              />
            }
          />
        </div>
      </div>
    </div>
  );
}