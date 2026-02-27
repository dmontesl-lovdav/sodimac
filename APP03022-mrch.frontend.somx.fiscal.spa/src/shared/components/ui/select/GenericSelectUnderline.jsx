import { useMemo, useState } from 'react';

const styles = {
    wrapper: {
        position: 'relative',
    },
    innerWrapper: {
        position: 'relative',
        paddingTop: '0.75rem',
    },
    label: {
        position: 'absolute',
        left: 0,
        top: 0,
        color: '#6b7280',
        pointerEvents: 'none',
        transition: 'all 150ms ease-out',
    },
    labelFloating: {
        transform: 'translateY(-0.5rem)',
        fontSize: '0.75rem',
        color: '#0284c7',
    },
    labelResting: {
        transform: 'translateY(0.25rem)',
        fontSize: '0.875rem',
    },
    select: {
        display: 'block',
        width: '100%',
        appearance: 'none',
        backgroundColor: 'transparent',
        color: '#111827',
        fontSize: '0.875rem',
        padding: '0',
        paddingRight: '1.5rem',
        border: 'none',
        borderBottom: '2px solid #d1d5db',
        height: '2.75rem',
        outline: 'none',
        transition: 'border-color 150ms',
    },
    selectFocused: {
        borderBottomColor: '#0284c7',
    },
    caret: {
        position: 'absolute',
        right: '0.25rem',
        top: '50%',
        transform: 'translateY(-50%)',
        color: '#4b5563',
        pointerEvents: 'none',
    },
};

export default function GenericSelectUnderline({
    label,
    value,
    onChange,
    options = [],
    placeholder = 'Selecciona…',
    includePlaceholder = true,
    fullWidth = true,
    className = '',
    ...props
}) {
    const [focused, setFocused] = useState(false);
    const normValue = value == null ? '' : String(value);

    const hasEmpty = useMemo(
        () => options.some(o => String(o?.value ?? '') === ''),
        [options]
    );
    const showPlaceholder = includePlaceholder && !hasEmpty && Boolean(placeholder);

    const filled = focused || normValue !== '';

    const getLabelStyle = () => ({
        ...styles.label,
        ...(filled ? styles.labelFloating : styles.labelResting),
    });

    const getSelectStyle = () => ({
        ...styles.select,
        ...(focused ? styles.selectFocused : {}),
    });

    return (
        <div style={{ ...styles.wrapper, width: fullWidth ? '100%' : '15rem' }} className={className}>
            <div style={styles.innerWrapper}>
                {label && (
                    <span style={getLabelStyle()}>
                        {label}
                    </span>
                )}

                <select
                    value={normValue}
                    onChange={(e) => onChange?.(e)}
                    onFocus={() => setFocused(true)}
                    onBlur={() => setFocused(false)}
                    style={getSelectStyle()}
                    {...props}
                >
                    {showPlaceholder && (
                        <option value="" disabled>
                            {placeholder}
                        </option>
                    )}
                    {options.map(({ value: v, label: lbl }) => {
                        const val = String(v ?? '');
                        return (
                            <option key={`opt-${val}`} value={val}>
                                {lbl}
                            </option>
                        );
                    })}
                </select>

                <span style={styles.caret}>▾</span>
            </div>
        </div>
    );
}
