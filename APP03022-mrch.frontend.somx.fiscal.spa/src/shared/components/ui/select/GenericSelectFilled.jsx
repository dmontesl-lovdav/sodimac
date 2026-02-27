const styles = {
    wrapper: {
        position: 'relative',
    },
    select: {
        display: 'block',
        width: '100%',
        appearance: 'none',
        backgroundColor: '#f9fafb',
        border: '1px solid #d1d5db',
        borderRadius: '0.375rem',
        height: '2.75rem',
        paddingLeft: '0.75rem',
        paddingRight: '2rem',
        fontSize: '0.875rem',
        outline: 'none',
        transition: 'border-color 150ms, box-shadow 150ms',
    },
    caret: {
        position: 'absolute',
        right: '0.5rem',
        top: '50%',
        transform: 'translateY(-50%)',
        color: '#4b5563',
        pointerEvents: 'none',
    },
};

export default function GenericSelectFilled({
    value,
    onChange,
    options = [],
    placeholder = 'Selecciona…',
    fullWidth = true,
    className = '',
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
        <div style={{ ...styles.wrapper, width: fullWidth ? '100%' : '15rem' }} className={className}>
            <select
                value={value}
                onChange={onChange}
                style={styles.select}
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
