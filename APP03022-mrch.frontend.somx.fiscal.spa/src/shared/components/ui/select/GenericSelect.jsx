const styles = {
    wrapper: {
        position: 'relative',
    },
    select: {
        height: '2.75rem',
        borderRadius: '0.375rem',
        border: '1px solid #d1d5db',
        paddingLeft: '1rem',
        paddingRight: '2.5rem',
        fontSize: '0.875rem',
        backgroundColor: '#ffffff',
        appearance: 'none',
        outline: 'none',
        transition: 'border-color 150ms, box-shadow 150ms',
    },
    caret: {
        position: 'absolute',
        right: '0.5rem',
        top: '50%',
        transform: 'translateY(-50%)',
        color: '#002d4c',
        pointerEvents: 'none',
    },
};

export default function GenericSelect({
    value,
    onChange,
    options = [],
    placeholder = 'Select…',
    widthClass = '',
    selectClassName = '',
    containerClassName = '',
    ...props
}) {
    const handleFocus = (e) => {
        e.currentTarget.style.borderColor = '#0ea5e9';
        e.currentTarget.style.boxShadow = '0 0 0 2px rgba(14, 165, 233, 0.3)';
    };

    const handleBlur = (e) => {
        e.currentTarget.style.borderColor = '#d1d5db';
        e.currentTarget.style.boxShadow = 'none';
    };

    return (
        <div style={styles.wrapper}>
            <select
                value={value}
                onChange={onChange}
                style={{
                    ...styles.select,
                    width: widthClass || '15rem',
                }}
                onFocus={handleFocus}
                onBlur={handleBlur}
                {...props}
            >
                {placeholder && <option value="">{placeholder}</option>}
                {options.map(({ value: v, label }) => (
                    <option key={v} value={v}>
                        {label}
                    </option>
                ))}
            </select>

            <span style={styles.caret}>▾</span>
        </div>
    );
}
