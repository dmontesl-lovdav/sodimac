
import { ReactElement } from "react";
import iconSuccess from "@assets/icons/alert-up.png";

interface SuccessMessageProps {
  message?: string;
  className?: string;
}

export default function SuccessMessage({ message, className }: SuccessMessageProps): ReactElement | null {
  if (!message) return null;

  return (
    <div className={`somx-success-message ${className}`}>
      <img src={iconSuccess} className="somx-success-icon" />
      <span className="somx-success-text">{message}</span>
    </div>
  );
}
