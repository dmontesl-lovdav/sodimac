import React from 'react';
import './GenericModal.css';

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
    items,
    onClose,
    onConfirm,
    onCancel,
}) {
    if (!visible) return null;

    const palette = {
        success: { bg: 'sev-bg-success', icon: SuccessIcon, color: 'sev-color-success' },
        error: { bg: 'sev-bg-error', icon: ErrorIcon, color: 'sev-color-error' },
        warning: { bg: 'sev-bg-warning', icon: WarnIcon, color: 'sev-color-warning' },
        info: { bg: 'sev-bg-info', icon: InfoIcon, color: 'sev-color-info' },
    };

    const sev = palette[severity] ?? palette.info;

    const renderContent = () => {
        if (variant === 'loading') {
            return (
                <div className="gm-box gm-loading">
                    <svg className="gm-spinner" viewBox="0 0 24 24">
                        <circle className="gm-spinner-track" cx="12" cy="12" r="10" />
                        <path className="gm-spinner-head" d="M22 12a10 10 0 0 1-10 10" />
                    </svg>
                    <p className="gm-msg">{message}</p>
                </div>
            );
        }
        if (variant === 'confirm') {
            return (
                <div className="gm-box gm-content">
                    <div className={`gm-icon-circle ${sev.bg}`}>
                        {React.createElement(sev.icon, { className: `gm-icon ${sev.color}` })}
                    </div>
                    {title && <h3 className="gm-title">{title}</h3>}
                    <p className="gm-text">{message || messageConfirm}</p>
                    <div className="gm-actions">
                        <button onClick={onCancel} className="gm-btn gm-btn-cancel">
                            {cancelText}
                        </button>
                        <button onClick={onConfirm} className="gm-btn gm-btn-confirm">
                            {confirmText}
                        </button>
                    </div>
                </div>
            );
        }
        return (
            <div className={`gm-box gm-content ${Array.isArray(items) && items.length > 0 ? 'gm-content-wide' : ''}`}>
                <div className={`gm-icon-circle ${palette[severity].bg}`}>
                    {React.createElement(palette[severity].icon, {
                        className: `gm-icon ${palette[severity].color}`,
                    })}
                </div>
                {title && <h3 className="gm-title">{title}</h3>}
                {message && <p className="gm-text">{message}</p>}
                {Array.isArray(items) && items.length > 0 && (
                    <ul className="gm-items">
                        {items.map((it) => (
                            <li key={typeof it === 'string' ? it : JSON.stringify(it)} className="gm-items-item">
                                {it}
                            </li>
                        ))}
                    </ul>
                )}
                <button onClick={onClose} className="gm-btn gm-btn-confirm gm-btn-full">
                    {buttonText}
                </button>
            </div>
        );
    };

    return (
        <div className="gm-overlay">
            {renderContent()}
        </div>
    );
}

const SuccessIcon = (props) => (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" {...props}>
        <circle cx="12" cy="12" r="9" strokeWidth="2" />
        <path d="M9 12.75 11.25 15 15 9.75" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
);

const ErrorIcon = (props) => (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" {...props}>
        <circle cx="12" cy="12" r="9" strokeWidth="2" />
        <path d="M9 9l6 6m0-6l-6 6" strokeWidth="2" strokeLinecap="round" />
    </svg>
);

const WarnIcon = (props) => (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" {...props}>
        <polygon points="12 2 22 20 2 20" strokeWidth="2" strokeLinejoin="round" />
        <path d="M12 7v4m0 4h.01" strokeWidth="2" strokeLinecap="round" />
    </svg>
);

const InfoIcon = (props) => (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" {...props}>
        <circle cx="12" cy="12" r="9" strokeWidth="2" />
        <path d="M12 8h.01M11 12h1v4h1" strokeWidth="2" strokeLinecap="round" />
    </svg>
);
