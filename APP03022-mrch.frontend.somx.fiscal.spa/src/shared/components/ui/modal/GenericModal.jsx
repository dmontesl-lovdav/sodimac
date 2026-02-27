import React from 'react';

const styles = {
    overlay: {
        position: 'fixed',
        inset: 0,
        zIndex: 50,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: 'rgba(0, 0, 0, 0.4)',
        backdropFilter: 'blur(4px)',
    },
    loadingContainer: {
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: '1rem',
        borderRadius: '0.5rem',
        backgroundColor: '#ffffff',
        padding: '2rem 2.5rem',
        boxShadow: '0 20px 25px -5px rgb(0 0 0 / 0.1)',
    },
    spinner: {
        height: '2.5rem',
        width: '2.5rem',
        color: '#0284c7',
        animation: 'spin 1s linear infinite',
    },
    loadingText: {
        fontSize: '0.875rem',
        fontWeight: '500',
        color: '#1e293b',
    },
    alertContainer: {
        width: '90%',
        maxWidth: '24rem',
        borderRadius: '0.5rem',
        backgroundColor: '#ffffff',
        padding: '2rem',
        boxShadow: '0 20px 25px -5px rgb(0 0 0 / 0.1)',
        textAlign: 'center',
    },
    iconContainer: {
        margin: '0 auto',
        display: 'flex',
        height: '4rem',
        width: '4rem',
        alignItems: 'center',
        justifyContent: 'center',
        borderRadius: '9999px',
        marginBottom: '1.5rem',
    },
    icon: {
        height: '2rem',
        width: '2rem',
    },
    title: {
        fontSize: '1.125rem',
        fontWeight: '600',
        color: '#1e293b',
        marginBottom: '0.5rem',
    },
    message: {
        fontSize: '0.875rem',
        color: '#334155',
        whiteSpace: 'pre-line',
        marginBottom: '1.5rem',
    },
    button: {
        borderRadius: '0.375rem',
        padding: '0.5rem 1.5rem',
        fontSize: '0.875rem',
        fontWeight: '500',
        cursor: 'pointer',
        transition: 'background-color 150ms ease',
    },
    primaryButton: {
        backgroundColor: '#0284c7',
        color: '#ffffff',
        border: 'none',
    },
    secondaryButton: {
        backgroundColor: '#ffffff',
        color: '#334155',
        border: '1px solid #d1d5db',
    },
    buttonRow: {
        display: 'flex',
        justifyContent: 'center',
        gap: '0.75rem',
        marginTop: '1rem',
    },
};

const palette = {
    success: { bg: '#d1fae5', color: '#059669' },
    error: { bg: '#ffe4e6', color: '#e11d48' },
    warning: { bg: '#fef3c7', color: '#d97706' },
    info: { bg: '#e0f2fe', color: '#0284c7' },
};

export default function GenericModal({
    visible = false,
    variant = 'loading',
    message = 'Procesando…',
    title = '',
    messageConfirm = '',
    severity = 'info',
    buttonText = 'Aceptar',
    confirmText = 'Aceptar',
    cancelText = 'Cancelar',
    onClose,
    onConfirm,
    onCancel,
}) {
    if (!visible) return null;

    const pal = palette[severity] || palette.info;

    return (
        <>
            <style>{`
                @keyframes spin {
                    from { transform: rotate(0deg); }
                    to { transform: rotate(360deg); }
                }
            `}</style>
            <div style={styles.overlay}>
                {variant === 'loading' ? (
                    <div style={styles.loadingContainer}>
                        <svg
                            style={styles.spinner}
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="currentColor"
                            strokeWidth={2}
                        >
                            <circle
                                style={{ opacity: 0.25 }}
                                cx="12"
                                cy="12"
                                r="10"
                                stroke="currentColor"
                            />
                            <path
                                style={{ opacity: 0.75 }}
                                d="M22 12a10 10 0 0 1-10 10"
                                stroke="currentColor"
                                strokeLinecap="round"
                            />
                        </svg>
                        <p style={styles.loadingText}>{message}</p>
                    </div>
                ) : variant === 'confirm' ? (
                    <div style={styles.alertContainer}>
                        <div style={{ ...styles.iconContainer, backgroundColor: palette.info.bg }}>
                            <InfoIcon style={{ ...styles.icon, color: palette.info.color }} />
                        </div>

                        {title && <h3 style={styles.title}>{title}</h3>}
                        <p style={styles.message}>{message || messageConfirm}</p>

                        <div style={styles.buttonRow}>
                            <button
                                onClick={onCancel}
                                style={{ ...styles.button, ...styles.secondaryButton }}
                                onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f3f4f6'}
                                onMouseLeave={(e) => e.currentTarget.style.backgroundColor = '#ffffff'}
                            >
                                {cancelText}
                            </button>
                            <button
                                onClick={onConfirm}
                                style={{ ...styles.button, ...styles.primaryButton }}
                                onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#0369a1'}
                                onMouseLeave={(e) => e.currentTarget.style.backgroundColor = '#0284c7'}
                            >
                                {confirmText}
                            </button>
                        </div>
                    </div>
                ) : (
                    <div style={styles.alertContainer}>
                        <div style={{ ...styles.iconContainer, backgroundColor: pal.bg }}>
                            {severity === 'success' && <SuccessIcon style={{ ...styles.icon, color: pal.color }} />}
                            {severity === 'error' && <ErrorIcon style={{ ...styles.icon, color: pal.color }} />}
                            {severity === 'warning' && <WarnIcon style={{ ...styles.icon, color: pal.color }} />}
                            {severity === 'info' && <InfoIcon style={{ ...styles.icon, color: pal.color }} />}
                        </div>

                        {title && <h3 style={styles.title}>{title}</h3>}
                        <p style={styles.message}>{message}</p>

                        <button
                            onClick={onClose}
                            style={{ ...styles.button, ...styles.primaryButton }}
                            onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#0369a1'}
                            onMouseLeave={(e) => e.currentTarget.style.backgroundColor = '#0284c7'}
                        >
                            {buttonText}
                        </button>
                    </div>
                )}
            </div>
        </>
    );
}

const SuccessIcon = (props) => (
    <svg viewBox="0 0 24 24" fill="currentColor" {...props}>
        <path
            d="M9 12.75 11.25 15 15 9.75"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
            fill="none"
        />
        <circle
            cx="12"
            cy="12"
            r="9"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
        />
    </svg>
);

const ErrorIcon = (props) => (
    <svg viewBox="0 0 24 24" fill="currentColor" {...props}>
        <circle
            cx="12"
            cy="12"
            r="9"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
        />
        <path
            d="M9 9l6 6m0-6l-6 6"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
        />
    </svg>
);

const WarnIcon = (props) => (
    <svg viewBox="0 0 24 24" fill="currentColor" {...props}>
        <path
            d="M12 7v4m0 4h.01"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
        />
        <polygon
            points="12 2 22 20 2 20"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinejoin="round"
        />
    </svg>
);

const InfoIcon = (props) => (
    <svg viewBox="0 0 24 24" fill="currentColor" {...props}>
        <circle
            cx="12"
            cy="12"
            r="9"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
        />
        <path
            d="M12 8h.01M11 12h1v4h1"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
        />
    </svg>
);
