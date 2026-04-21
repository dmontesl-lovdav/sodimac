import { ReactElement } from "react";
import iconWarning from "@assets/icons/warning.png";
import "./Alerts.css";

export interface WarningMessageProps {
  message?: string;
  className?: string;
}

export default function WarningMessage({ message, className = "" }: WarningMessageProps): ReactElement | null {
  if (!message) return null;

  return (
    <div className={`fiscal-alert-warning ${className}`.trim()}>
      <img src={iconWarning} className="fiscal-alert-warning-icon" alt="" />
      <span className="fiscal-alert-warning-text">{message}</span>
    </div>
  );
}

