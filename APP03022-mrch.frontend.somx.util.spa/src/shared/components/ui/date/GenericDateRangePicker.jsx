import { forwardRef, useRef, useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import DatePicker from 'react-datepicker';
import es from 'date-fns/locale/es';
import 'react-datepicker/dist/react-datepicker.css';
import './GenericDateRangePicker.css';

const SIZE_CLASSES = {
    sm: 'dr-sm',
    md: 'dr-md',
    lg: 'dr-lg',
};

const RangeInput = forwardRef(({ value, onChange: _onChange, onClick, placeholder, sizeClass, inputClassName, inputProps, hasValue, onClear, onOpen }, ref) => (
    <div className="dr-container">
        <input
            ref={ref}
            value={value ?? ''}
            onChange={_onChange}
            onClick={onClick}
            placeholder={placeholder}
            className={`dr-input ${sizeClass} ${inputClassName}`}
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
));

RangeInput.displayName = 'RangeInput';

RangeInput.propTypes = {
    value: PropTypes.any,
    onChange: PropTypes.func,
    onClick: PropTypes.func,
    placeholder: PropTypes.string,
    sizeClass: PropTypes.string,
    inputClassName: PropTypes.string,
    inputProps: PropTypes.object,
    hasValue: PropTypes.bool,
    onClear: PropTypes.func,
    onOpen: PropTypes.func,
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
    const datePickerRef = useRef(null);
    const [internalRange, setInternalRange] = useState(value);

    const incomingStartTs = value?.[0] instanceof Date ? value[0].getTime() : null;
    const incomingEndTs = value?.[1] instanceof Date ? value[1].getTime() : null;

    useEffect(() => {
        setInternalRange((prev) => {
            const prevStart = prev?.[0] instanceof Date ? prev[0].getTime() : null;
            const prevEnd = prev?.[1] instanceof Date ? prev[1].getTime() : null;
            if (prevStart === incomingStartTs && prevEnd === incomingEndTs) {
                return prev;
            }
            return [
                incomingStartTs ? new Date(incomingStartTs) : null,
                incomingEndTs ? new Date(incomingEndTs) : null,
            ];
        });
    }, [incomingStartTs, incomingEndTs]);

    const handleChange = (dates) => {
        const [start, end] = dates;

        if (start && !end) {
            setInternalRange([start, null]);
            return;
        }

        if (start && end) {
            setInternalRange([start, end]);
            onChange?.([start, end]);
        }
    };

    const clearRange = (e) => {
        e.preventDefault();
        e.stopPropagation();

        setInternalRange([null, null]);
        onChange?.([null, null]);

        datePickerRef.current?.setOpen(false);
    };

    const openCalendar = (e) => {
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
                onChange={(dates) => handleChange(dates)}
                dateFormat="dd/MM/yyyy"
                isClearable={false}
                showPopperArrow={false}
                popperPlacement="bottom-start"
                popperClassName={`dr-popper ${popperClassName ?? ''}`}
                shouldCloseOnSelect={false}
                disabledKeyboardNavigation={false}
                customInput={
                    <RangeInput
                        placeholder={placeholder}
                        sizeClass={SIZE_CLASSES[size]}
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
                    const years = [];

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

GenericDateRangePicker.propTypes = {
    value: PropTypes.any,
    onChange: PropTypes.func,
    placeholder: PropTypes.any,
    className: PropTypes.string,
    size: PropTypes.string,
    inputClassName: PropTypes.string,
    popperClassName: PropTypes.string,
    inputProps: PropTypes.object,
};
