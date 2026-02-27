
import lobbyIcon from "@assets/lobby.svg";
import { ReactElement } from "react";

export default function SimpleLobby({ message }: { message?: string }): ReactElement {
  return (
    <div className="somx-lobby-container">
      <div className="somx-lobby-icon-wrapper">
        <img src={lobbyIcon} />
      </div>
      <div className="somx-lobby-message-wrapper">
        <div className="somx-lobby-message">{message}</div>
      </div>
    </div>
  );
}
