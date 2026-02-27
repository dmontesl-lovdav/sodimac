
import { ReactElement } from "react";
import iconWarning from "@assets/icons/warning.png";

interface ErrorMessageProps {
  message?: string;
  className?: string;
}

export default function ErrorMessage({ message, className }: ErrorMessageProps): ReactElement | null {
  if (!message) return null;

  return (
    <div className={`somx-error-message ${className}`}>
      <img src={iconWarning} className="somx-error-icon" />
      <span className="somx-error-text">{message}</span>
    </div>
  );
}
