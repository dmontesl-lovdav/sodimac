import './styles/GenericSelect.css';

export default function GenericSelect({
    value,
    onChange,
    options = [],
    placeholder = 'Select…',

    // DEFAULT WIDTH:
    widthClass = 'gs-width-default',

    selectClassName = '',
    containerClassName = '',
    ...props
}) {
    return (
        <div className={`generic-select-container ${containerClassName}`}>
            <select
                value={value}
                onChange={onChange}
                className={`generic-select ${widthClass} ${selectClassName}`}
                {...props}
            >
                {placeholder && <option value="">{placeholder}</option>}

                {options.map(({ value: v, label }) => (
                    <option key={v} value={v}>
                        {label}
                    </option>
                ))}
            </select>

            <span className="generic-select-caret">▾</span>
        </div>
    );
}
