import { forwardRef } from 'react';
import DatePicker from 'react-datepicker';
import { es } from 'date-fns/locale/es';
import 'react-datepicker/dist/react-datepicker.css';
import './DatePicker.css';

export type DateRange = [Date | null, Date | null];
export type DatePickerSize = 'sm' | 'md' | 'lg';

export interface GenericDateRangePickerProps {
  value?: DateRange;
  onChange?: (dates: DateRange) => void;
  placeholder?: string;
  className?: string;
  size?: DatePickerSize;
  inputClassName?: string;
  popperClassName?: string;
  inputProps?: React.InputHTMLAttributes<HTMLInputElement>;
}

interface RangeInputProps extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'size'> {
  size?: DatePickerSize;
  inputClassName?: string;
}

const RangeInput = forwardRef<HTMLInputElement, RangeInputProps>(
  ({ value, onClick, onKeyDown, placeholder, size = 'md', inputClassName = '', ...inputProps }, ref) => (
    <div className="fiscal-date-wrapper">
      <input
        ref={ref}
        readOnly
        value={value ?? ''}
        onClick={onClick}
        onKeyDown={onKeyDown}
        placeholder={placeholder}
        className={`fiscal-date-input fiscal-date-input-${size} ${inputClassName}`.trim()}
        {...inputProps}
      />
      <span className="fiscal-date-caret">▾</span>
    </div>
  )
);
RangeInput.displayName = 'RangeInput';

export default function GenericDateRangePicker({
  value = [null, null],
  onChange,
  placeholder = 'Fecha desde – hasta',
  className = '',
  size = 'lg',
  inputClassName = '',
  popperClassName = '',
  inputProps = {},
}: GenericDateRangePickerProps): React.ReactElement {
  return (
    <DatePicker
      selectsRange
      locale={es}
      startDate={value?.[0] ?? null}
      endDate={value?.[1] ?? null}
      onChange={(dates) => {
        const [start, end] = Array.isArray(dates) ? dates : [dates, null];
        onChange?.([start ?? null, end ?? null]);
      }}
      dateFormat="dd/MM/yyyy"
      isClearable
      placeholderText={placeholder}
      showPopperArrow
      wrapperClassName={className}
      popperClassName={popperClassName}
      popperPlacement="top-start"
      disabledKeyboardNavigation={false}
      
      customInput={<RangeInput {...inputProps} placeholder={placeholder} inputClassName={inputClassName} size={size} />}
    />
  );
}
