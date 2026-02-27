
import React from 'react';

export default function GenericModal({
  visible = false,
  variant = 'loading',
  message = 'Procesando…',
  title = '',
  severity = 'info',
  buttonText = 'Aceptar',
  onClose,
}) {
  if (!visible) return null;

  const palette = {
    success: { icon: SuccessIcon, bgCls: 'somx-sev-success-bg', colorCls: 'somx-sev-success-color' },
    error: { icon: ErrorIcon, bgCls: 'somx-sev-error-bg', colorCls: 'somx-sev-error-color' },
    warning: { icon: WarnIcon, bgCls: 'somx-sev-warning-bg', colorCls: 'somx-sev-warning-color' },
    info: { icon: InfoIcon, bgCls: 'somx-sev-info-bg', colorCls: 'somx-sev-info-color' },
  };

  return (
    <div className="somx-modal-overlay">
      {variant === 'loading' ? (
        <div className="somx-modal-loading">
          <Spinner className="somx-spinner" />
          <p className="somx-modal-loading-text">{message}</p>
        </div>
      ) : (
        <div className="somx-modal-alert">
          <div className={`somx-modal-alert-icon ${palette[severity].bgCls}`}>
            {React.createElement(palette[severity].icon, {
              className: `h-8 w-8 ${palette[severity].colorCls}`,
            })}
          </div>

          {title && <h3 className="somx-modal-title">{title}</h3>}
          <p className="somx-modal-message">{message}</p>

          <button onClick={onClose} className="somx-modal-button">
            {buttonText}
          </button>
        </div>
      )}
    </div>
  );
}

const Spinner = (props) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2} {...props}>
    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" />
    <path className="opacity-75" d="M22 12a10 10 0 0 1-10 10" stroke="currentColor" strokeLinecap="round" />
  </svg>
);

const SuccessIcon = (props) => (
  <svg viewBox="0 0 24 24" fill="currentColor" {...props}>
    <path d="M9 12.75 11.25 15 15 9.75" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" fill="none" />
    <circle cx="12" cy="12" r="9" fill="none" stroke="currentColor" strokeWidth="2" />
  </svg>
);

const ErrorIcon = (props) => (
  <svg viewBox="0 0 24 24" fill="currentColor" {...props}>
    <circle cx="12" cy="12" r="9" fill="none" stroke="currentColor" strokeWidth="2" />
    <path d="M9 9l6 6m0-6l-6 6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
  </svg>
);

const WarnIcon = (props) => (
  <svg viewBox="0 0 24 24" fill="currentColor" {...props}>
    <path d="M12 7v4m0 4h.01" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
    <polygon points="12 2 22 20 2 20" fill="none" stroke="currentColor" strokeWidth="2" strokeLinejoin="round" />
  </svg>
);
