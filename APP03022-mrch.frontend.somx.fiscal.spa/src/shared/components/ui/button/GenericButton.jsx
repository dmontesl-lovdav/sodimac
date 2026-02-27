const styles = {
    base: {
        height: '2.75rem',
        padding: '0 1.5rem',
        borderRadius: '0.375rem',
        fontSize: '0.875rem',
        fontWeight: '500',
        border: 'none',
        transition: 'all 150ms ease',
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
    },
    primary: {
        color: '#ffffff',
        backgroundColor: '#002d4c',
    },
    secondary: {
        color: '#ffffff',
        backgroundColor: '#005b96',
    },
    outline: {
        border: '1px solid #9ca3af',
        backgroundColor: '#ffffff',
        color: '#374151',
    },
    link: {
        height: 'auto',
        padding: '0',
        backgroundColor: 'transparent',
        color: '#003865',
        textDecoration: 'underline',
        textUnderlineOffset: '2px',
    },
    outlineFill: {
        border: '1px solid #002d4c',
        color: '#002d4c',
        backgroundColor: 'transparent',
    },
    disabled: {
        opacity: 0.6,
        cursor: 'not-allowed',
    },
    enabled: {
        cursor: 'pointer',
    },
};

const hoverStyles = {
    primary: { backgroundColor: '#003865' },
    secondary: { backgroundColor: '#004b7a' },
    outline: { backgroundColor: '#f9fafb' },
    link: { color: '#002d4c' },
    outlineFill: { backgroundColor: '#002d4c', color: '#ffffff' },
};

export default function GenericButton({
    children,
    variant = 'primary',
    className = '',
    style = {},
    disabled = false,
    ...props
}) {
    const handleMouseEnter = (e) => {
        if (!disabled && hoverStyles[variant]) {
            Object.assign(e.currentTarget.style, hoverStyles[variant]);
        }
    };

    const handleMouseLeave = (e) => {
        if (!disabled) {
            Object.assign(e.currentTarget.style, styles[variant]);
        }
    };

    const combinedStyle = {
        ...styles.base,
        ...styles[variant],
        ...(disabled ? styles.disabled : styles.enabled),
        ...style,
    };

    return (
        <button
            {...props}
            disabled={disabled}
            style={combinedStyle}
            className={className}
            onMouseEnter={handleMouseEnter}
            onMouseLeave={handleMouseLeave}
        >
            {children}
        </button>
    );
}
