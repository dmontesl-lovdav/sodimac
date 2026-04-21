import { ReactElement } from "react";
import iconWarning from "@assets/icons/warning.png";
import "./Alerts.css";

export interface ErrorMessageProps {
  message?: string;
  className?: string;
}

export default function ErrorMessage({ message, className = "" }: ErrorMessageProps): ReactElement | null {
  if (!message) return null;

  return (
    <div className={`fiscal-alert-error ${className}`.trim()}>
      <img src={iconWarning} className="fiscal-alert-error-icon" alt="" />
      <span className="fiscal-alert-error-text">{message}</span>
    </div>
  );
}
