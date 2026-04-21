import React from 'react';
import './Modal.css';

export type ModalVariant = 'loading' | 'confirm' | 'alert';
export type ModalSeverity = 'success' | 'error' | 'warning' | 'info';

export interface GenericModalProps {
  visible?: boolean;
  variant?: ModalVariant;
  message?: string;
  title?: string;
  messageConfirm?: string;
  severity?: ModalSeverity;
  buttonText?: string;
  confirmText?: string;
  cancelText?: string;
  onClose?: () => void;
  onConfirm?: () => void;
  onCancel?: () => void;
  className?: string;
}

const SuccessIcon: React.FC<{ className?: string }> = ({ className }) => (
  <svg viewBox="0 0 24 24" fill="currentColor" className={className}>
    <path
      d="M9 12.75 11.25 15 15 9.75"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      fill="none"
    />
    <circle cx="12" cy="12" r="9" fill="none" stroke="currentColor" strokeWidth="2" />
  </svg>
);

const ErrorIcon: React.FC<{ className?: string }> = ({ className }) => (
  <svg viewBox="0 0 24 24" fill="currentColor" className={className}>
    <circle cx="12" cy="12" r="9" fill="none" stroke="currentColor" strokeWidth="2" />
    <path d="M9 9l6 6m0-6l-6 6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
  </svg>
);

const WarnIcon: React.FC<{ className?: string }> = ({ className }) => (
  <svg viewBox="0 0 24 24" fill="currentColor" className={className}>
    <path d="M12 7v4m0 4h.01" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
    <polygon points="12 2 22 20 2 20" fill="none" stroke="currentColor" strokeWidth="2" strokeLinejoin="round" />
  </svg>
);

const InfoIcon: React.FC<{ className?: string }> = ({ className }) => (
  <svg viewBox="0 0 24 24" fill="currentColor" className={className}>
    <circle cx="12" cy="12" r="9" fill="none" stroke="currentColor" strokeWidth="2" />
    <path d="M12 8h.01M11 12h1v4h1" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
  </svg>
);

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
  className = '',
}: GenericModalProps): React.ReactElement | null {
  if (!visible) return null;

  const iconContainerClass = `fiscal-modal-icon-container fiscal-modal-icon-container-${severity}`;
  const iconClass = `fiscal-modal-icon fiscal-modal-icon-${severity}`;

  const overlayClass = `fiscal-modal-overlay ${className}`.trim();

  return (
    <div className={overlayClass}>
      {variant === 'loading' ? (
        <div className="fiscal-modal-loading-container">
          <svg
            className="fiscal-modal-spinner"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth={2}
          >
            <circle cx="12" cy="12" r="10" stroke="currentColor" className="fiscal-modal-spinner-circle" />
            <path d="M22 12a10 10 0 0 1-10 10" stroke="currentColor" strokeLinecap="round" className="fiscal-modal-spinner-path" />
          </svg>
          <p className="fiscal-modal-loading-text">{message}</p>
        </div>
      ) : variant === 'confirm' ? (
        <div className="fiscal-modal-alert-container">
          <div className="fiscal-modal-icon-container fiscal-modal-icon-container-info">
            <InfoIcon className="fiscal-modal-icon fiscal-modal-icon-info" />
          </div>
          {title ? <h3 className="fiscal-modal-title">{title}</h3> : null}
          <p className="fiscal-modal-message">{message || messageConfirm}</p>
          <div className="fiscal-modal-button-row">
            <button type="button" onClick={onCancel} className="fiscal-modal-button fiscal-modal-button-secondary">
              {cancelText}
            </button>
            <button type="button" onClick={onConfirm} className="fiscal-modal-button fiscal-modal-button-primary">
              {confirmText}
            </button>
          </div>
        </div>
      ) : (
        <div className="fiscal-modal-alert-container">
          <div className={iconContainerClass}>
            {severity === 'success' && <SuccessIcon className={iconClass} />}
            {severity === 'error' && <ErrorIcon className={iconClass} />}
            {severity === 'warning' && <WarnIcon className={iconClass} />}
            {severity === 'info' && <InfoIcon className={iconClass} />}
          </div>
          {title ? <h3 className="fiscal-modal-title">{title}</h3> : null}
          <p className="fiscal-modal-message">{message}</p>
          <button
            type="button"
            onClick={onConfirm ?? onClose}
            className="fiscal-modal-button fiscal-modal-button-primary"
          >
            {buttonText}
          </button>
        </div>
      )}
    </div>
  );
}
