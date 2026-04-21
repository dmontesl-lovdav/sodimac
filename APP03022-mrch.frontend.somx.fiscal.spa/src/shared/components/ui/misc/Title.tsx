
import { ReactElement, type ReactNode } from "react";
import "./Title.css";

interface TitleProps {
  title?: string;
  description?: string;
  actions?: ReactNode;
}

export function Title({ title, description, actions }: TitleProps): ReactElement {
  return (
    <div className="fiscal-header">
      <div>
        <h4 className="fiscal-header-title">{title}</h4>
        {description && <p>{description}</p>}
      </div>
      <div className="fiscal-header-actions">
        {actions}
      </div>
    </div>
  );
}

