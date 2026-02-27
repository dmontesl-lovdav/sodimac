
import { ReactElement } from "react";


export interface StatusPillParams {
  type?: string;
  large?: boolean;
  children?: any;
}

export function StatusPill({
  type = "info",
  large = false,
  children,
}: StatusPillParams): ReactElement {
  const sizeClass = large ? "somx-status-pill--lg" : "";
  const typeClass = `somx-status-pill--${type}`;

  return (
    <span className={`somx-status-pill ${sizeClass} ${typeClass}`}>
      {children}
    </span>
  );
}
