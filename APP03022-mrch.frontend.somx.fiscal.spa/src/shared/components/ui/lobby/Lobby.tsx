import { ReactElement } from "react";
import "./Lobby.css";

export interface SimpleLobbyProps {
  message?: string;
  className?: string;
  error?: boolean;
}

export default function SimpleLobby({ message, className = "", error = false }: SimpleLobbyProps): ReactElement {
  const rootClass = `fiscal-lobby-container ${className}`.trim();
  const messageClass = `fiscal-lobby-message ${error ? "fiscal-lobby-message-error" : ""}`.trim();
  return (
    <div className={rootClass}>
      <div className="fiscal-lobby-message-wrapper">
        <div className={messageClass}>{message}</div>
      </div>
    </div>
  );
}
