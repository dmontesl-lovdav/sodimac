
import { forwardRef } from 'react';
import DatePicker from 'react-datepicker';
import es from 'date-fns/locale/es';
import 'react-datepicker/dist/react-datepicker.css';

const SIZE_CLASSES = {
  sm: 'somx-size-sm',
  md: 'somx-size-md',
  lg: 'somx-size-lg',
};

export default function GenericDateRangePicker({
  value = [null, null],
  onChange,
  placeholder = 'Fecha desde – hasta',
  className = '',
  size = 'md',
  inputClassName = '',
  popperClassName = '',
  inputProps = {},
}) {
  const RangeInput = forwardRef(({ value, onClick, onKeyDown }, ref) => (
    <div className="somx-date-range-input-container">
      <input
        ref={ref}
        readOnly
        value={value}
        onClick={onClick}
        onKeyDown={onKeyDown}
        placeholder={placeholder}
        className={`somx-date-range-input ${SIZE_CLASSES[size]} ${inputClassName}`}
        {...inputProps}
      />
      <span className="somx-date-range-icon">▾</span>
    </div>
  ));
  RangeInput.displayName = 'RangeInput';

  return (
    <DatePicker
      selectsRange
      locale={es}
      startDate={value?.[0] ?? null}
      endDate={value?.[1] ?? null}
      onChange={onChange}
      dateFormat="dd/MM/yyyy"
      isClearable
      placeholderText={placeholder}
      showPopperArrow={false}
      wrapperClassName={`somx-date-range-wrapper ${className}`}
      popperClassName={popperClassName}
      popperPlacement="bottom-start"
      disabledKeyboardNavigation
      withPortal
      customInput={<RangeInput />}
    />
  );
}
