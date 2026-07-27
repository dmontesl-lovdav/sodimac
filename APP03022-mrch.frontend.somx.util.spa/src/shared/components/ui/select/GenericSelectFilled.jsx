import PropTypes from 'prop-types';

export default function GenericSelectFilled({
    value,
    onChange,
    options = [],
    placeholder = 'Selecciona…',
    fullWidth = true,
    className = '',
    ...props
}) {
    return (
        <div className={`relative ${fullWidth ? 'w-full' : 'w-60'} ${className}`}>
            <select
                value={value}
                onChange={onChange}
                className={`
          block w-full appearance-none
          bg-gray-50 border border-gray-300
          rounded-md h-11 pl-3 pr-8 text-sm
          focus:ring-2 focus:ring-sky-500 focus:border-sky-500
        `}
                {...props}
            >
                {placeholder && <option value="">{placeholder}</option>}
                {options.map(({ value: v, label }) => (
                    <option key={v} value={v}>
                        {label}
                    </option>
                ))}
            </select>

            <span className="absolute right-2 top-1/2 -translate-y-1/2 text-gray-600 pointer-events-none">
                ▾
            </span>
        </div>
    );
}

GenericSelectFilled.propTypes = {
    value: PropTypes.any,
    onChange: PropTypes.func,
    options: PropTypes.array,
    placeholder: PropTypes.string,
    fullWidth: PropTypes.bool,
    className: PropTypes.string,
};
