import { Link } from "react-router-dom";
import { ReactNode } from "react";
import './Modal.css';

const finanzasUrl = process.env.FINANZAS_URL ?? "";

export interface BitacoraErrorModalProps {
  visible: boolean;
  title?: string;
  message?: string;
  traceId: string | null;
  onClose: () => void;
  footer?: ReactNode;
}

export default function BitacoraErrorModal({
  visible,
  title = "Atención",
  message,
  traceId,
  onClose,
  footer,
}: BitacoraErrorModalProps) {
  if (!visible) return null;

  return (
    <div className="fiscal-modal-overlay">
      <div className="fiscal-modal-alert-container">
        <div className="fiscal-modal-icon-container fiscal-modal-icon-container-error">
          <svg viewBox="0 0 24 24" fill="currentColor" className="fiscal-modal-icon fiscal-modal-icon-error">
            <circle cx="12" cy="12" r="9" fill="none" stroke="currentColor" strokeWidth="2" />
            <path d="M9 9l6 6m0-6l-6 6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
          </svg>
        </div>

        {title ? <h3 className="fiscal-modal-title">{title}</h3> : null}
        {message ? <p className="fiscal-modal-message">{message}</p> : null}

        {traceId ? (
          <p className="fiscal-modal-message">
            ID de la bitácora: {traceId}.{" "}
            <Link to={`${finanzasUrl}/auditoria/bitacora-actividades/tren/${traceId}`}>Ver bitácora</Link>
          </p>
        ) : null}

        {footer ?? (
          <button
            type="button"
            onClick={onClose}
            className="fiscal-modal-button fiscal-modal-button-primary"
          >
            Cerrar
          </button>
        )}
      </div>
    </div>
  );
}

