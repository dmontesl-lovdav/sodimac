import { ReactElement } from "react";
import iconSuccess from "@assets/icons/alert-up.png";
import "./Alerts.css";

export interface SuccessMessageProps {
  message?: string;
  className?: string;
}

export default function SuccessMessage({ message, className = "" }: SuccessMessageProps): ReactElement | null {
  if (!message) return null;

  return (
    <div className={`fiscal-alert-success ${className}`.trim()}>
      <img src={iconSuccess} className="fiscal-alert-success-icon" alt="" />
      <span className="fiscal-alert-success-text">{message}</span>
    </div>
  );
}
