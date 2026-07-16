import { forwardRef, useRef, useState, useEffect } from 'react';
import DatePicker from 'react-datepicker';
import { es } from 'date-fns/locale/es';
import './styles/GenericDateRangePicker.css';
import 'react-datepicker/dist/react-datepicker.css';

type DateRange = [Date | null, Date | null];
type Size = 'sm' | 'md' | 'lg';

type Props = {
  value?: DateRange;
  onChange: (dates: DateRange) => void;
  placeholder?: string;
  className?: string;
  size?: Size;
  inputClassName?: string;
  popperClassName?: string;
  inputProps?: React.InputHTMLAttributes<HTMLInputElement>;
};

const SIZE_CLASSES: Record<Size, string> = {
  sm: 'dr-sm',
  md: 'dr-md',
  lg: 'dr-lg',
};

type RangeInputProps = {
  value?: string;
  onChange?: React.ChangeEventHandler<HTMLInputElement>;
  onClick?: React.MouseEventHandler<HTMLInputElement>;
  placeholder: string;
  size: Size;
  inputClassName: string;
  inputProps: React.InputHTMLAttributes<HTMLInputElement>;
  hasValue: boolean;
  onClear: (e: React.MouseEvent<HTMLButtonElement>) => void;
  onOpen: (e: React.MouseEvent<HTMLButtonElement>) => void;
};

const RangeInput = forwardRef<HTMLInputElement, RangeInputProps>(
  (
    {
      value,
      onChange: _onChange,
      onClick,
      placeholder,
      size,
      inputClassName,
      inputProps,
      hasValue,
      onClear,
      onOpen,
    },
    ref
  ) => (
    <div className="dr-container">
      <input
        ref={ref}
        value={value ?? ''}
        onChange={_onChange}
        onClick={onClick}
        placeholder={placeholder}
        className={`dr-input ${SIZE_CLASSES[size]} ${inputClassName}`}
        {...inputProps}
      />

      {hasValue && (
        <button
          type="button"
          className="dr-clear-btn"
          onClick={onClear}
          aria-label="Limpiar rango"
          title="Limpiar"
        >
          ✕
        </button>
      )}

      {!hasValue && (
        <button
          type="button"
          className="dr-btn"
          onClick={onOpen}
          aria-label="Abrir calendario"
          title="Abrir calendario"
        >
          📅
        </button>
      )}
    </div>
  )
);

RangeInput.displayName = 'RangeInput';

export default function GenericDateRangePicker({
  value = [null, null],
  onChange,
  placeholder = 'Fecha desde – hasta',
  className = '',
  size = 'md',
  inputClassName = '',
  popperClassName = '',
  inputProps = {},
}: Props) {
  const datePickerRef = useRef<DatePicker | null>(null);
  const [internalRange, setInternalRange] = useState<DateRange>(value);

  useEffect(() => {
    setInternalRange(value);
  }, [value]);

  const handleChange = (dates: DateRange) => {
    const [start, end] = dates;

    if (start && !end) {
      setInternalRange([start, null]);
      return;
    }

    if (start && end) {
      setInternalRange([start, end]);
      onChange([start, end]);
    }
  };

  const clearRange = (e: React.MouseEvent<HTMLButtonElement>) => {
    e.preventDefault();
    e.stopPropagation();

    setInternalRange([null, null]);
    onChange([null, null]);

    // opcional: cerrar calendario si está abierto
    datePickerRef.current?.setOpen(false);
  };

  const openCalendar = (e: React.MouseEvent<HTMLButtonElement>) => {
    e.preventDefault();
    e.stopPropagation();
    datePickerRef.current?.setOpen(true);
  };

  const hasValue = Boolean(internalRange?.[0] || internalRange?.[1]);

  return (
    <div className={`dr-wrapper ${className}`}>
      <DatePicker
        ref={datePickerRef}
        selectsRange
        locale={es}
        startDate={internalRange?.[0]}
        endDate={internalRange?.[1]}
        maxDate={new Date()}
        onChange={(dates) => handleChange(dates as DateRange)}
        dateFormat="dd/MM/yyyy"
        isClearable={false}  // 🔥 apagamos la X rota del lib
        showPopperArrow={false}
        popperPlacement="bottom-start"
        popperClassName={`dr-popper ${popperClassName ?? ''}`}
        shouldCloseOnSelect={false}
        disabledKeyboardNavigation={false}
        customInput={
          <RangeInput
            placeholder={placeholder}
            size={size}
            inputClassName={inputClassName}
            inputProps={inputProps}
            hasValue={hasValue}
            onClear={clearRange}
            onOpen={openCalendar}
          />
        }
        renderCustomHeader={({
          date,
          decreaseMonth,
          increaseMonth,
          changeYear,
        }) => {
          const currentYear = new Date().getFullYear();
          const years: number[] = [];

          for (let i = currentYear - 20; i <= currentYear + 5; i++) {
            years.push(i);
          }

          return (
            <div className="dr-header">
              <button
                type="button"
                onClick={decreaseMonth}
                className="dr-nav-btn"
              >
                ‹
              </button>

              <div className="dr-header-center">
                <span className="dr-month">
                  {date.toLocaleString('es', { month: 'long' })}
                </span>

                <select
                  value={date.getFullYear()}
                  onChange={(e) => changeYear(Number(e.target.value))}
                  className="dr-year-select"
                >
                  {years.map((y) => (
                    <option key={y} value={y}>
                      {y}
                    </option>
                  ))}
                </select>
              </div>

              <button
                type="button"
                onClick={increaseMonth}
                className="dr-nav-btn"
              >
                ›
              </button>
            </div>
          );
        }}
      />
    </div>
  );
}
