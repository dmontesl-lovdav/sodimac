import { forwardRef } from 'react';
import DatePicker from 'react-datepicker';
import es from 'date-fns/locale/es';
import 'react-datepicker/dist/react-datepicker.css';

const SIZE_PRESETS = {
    sm: { height: '2.5rem', fontSize: '0.875rem' },
    md: { height: '2.75rem', fontSize: '0.875rem' },
    lg: { height: '3rem', fontSize: '1rem' },
};

const styles = {
    inputWrapper: {
        position: 'relative',
        width: '100%',
    },
    input: {
        width: '100%',
        borderRadius: '0.375rem',
        border: '1px solid #d1d5db',
        paddingLeft: '1rem',
        paddingRight: '2.5rem',
        backgroundColor: '#ffffff',
        outline: 'none',
        transition: 'border-color 150ms, box-shadow 150ms',
    },
    caret: {
        position: 'absolute',
        right: '0.75rem',
        top: '50%',
        transform: 'translateY(-50%)',
        color: '#002d4c',
        pointerEvents: 'none',
    },
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
    const RangeInput = forwardRef(({ value, onClick, onKeyDown }, ref) => {
        const handleFocus = (e) => {
            e.currentTarget.style.borderColor = '#0ea5e9';
            e.currentTarget.style.boxShadow = '0 0 0 2px rgba(14, 165, 233, 0.3)';
        };

        const handleBlur = (e) => {
            e.currentTarget.style.borderColor = '#d1d5db';
            e.currentTarget.style.boxShadow = 'none';
        };

        return (
            <div style={styles.inputWrapper}>
                <input
                    ref={ref}
                    readOnly
                    value={value}
                    onClick={onClick}
                    onKeyDown={onKeyDown}
                    onFocus={handleFocus}
                    onBlur={handleBlur}
                    placeholder={placeholder}
                    style={{
                        ...styles.input,
                        ...SIZE_PRESETS[size],
                    }}
                    {...inputProps}
                />
                <span style={styles.caret}>▾</span>
            </div>
        );
    });
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
            wrapperClassName={className}
            popperClassName={popperClassName}
            popperPlacement="bottom-start"
            disabledKeyboardNavigation
            withPortal
            customInput={<RangeInput />}
        />
    );
}
