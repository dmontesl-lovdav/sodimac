import { forwardRef } from 'react';
import DatePicker from 'react-datepicker';
import es from 'date-fns/locale/es';
import 'react-datepicker/dist/react-datepicker.css';
import './GenericDateRangePicker.css';

const SIZE_PRESETS = {
    sm: 'gdrp-size-sm',
    md: 'gdrp-size-md',
    lg: 'gdrp-size-lg',
};

// Fix universal de timezone
function normalizeDate(d) {
    if (!d) return null;
    if (typeof d === 'string') d = new Date(d);
    return new Date(d.getFullYear(), d.getMonth(), d.getDate());
}

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
        <div className="gdrp-wrapper">
            <input
                ref={ref}
                readOnly
                value={value}
                onClick={onClick}
                onKeyDown={onKeyDown}
                placeholder={placeholder}
                className={[
                    'gdrp-input',
                    SIZE_PRESETS[size],
                    inputClassName
                ].join(' ')}
                {...inputProps}
            />

            <span className="gdrp-caret">▾</span>
        </div>
    ));
    RangeInput.displayName = 'RangeInput';

    return (
        <DatePicker
            selectsRange
            locale={es}
            startDate={normalizeDate(value?.[0])}
            endDate={normalizeDate(value?.[1])}
            onChange={onChange}
            dateFormat="dd/MM/yyyy"
            isClearable
            placeholderText={placeholder}
            showPopperArrow={false}
            wrapperClassName={`gdrp-full ${className}`}
            popperClassName={popperClassName}
            popperPlacement="bottom-start"
            disabledKeyboardNavigation
            withPortal
            customInput={<RangeInput />}
        />
    );
}
