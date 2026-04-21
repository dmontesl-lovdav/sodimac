import { Breadcrumb } from "@/shared/components/ui/navigation";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { ReactElement } from "react";
import { GenericButton } from "@/shared/components/ui/button";
import { useNavigate } from "react-router-dom";
import { GenericMarqueeBar } from "../progress";
import "./Decorator.css";

export interface DecorateOptions {
  className?: string;
}

function GoBackButton(): ReactElement {
  const navigate = useNavigate();
  return (
    <GenericButton variant="link" className="fiscal-decorator-actions-end" onClick={() => navigate(-1)}>
      Volver
    </GenericButton>
  );
}

export function decorate(
  breadcrumItems: BreadcrumbItem[],
  _returnPath: string,
  content: ReactElement,
  actions?: ReactElement,
  options?: DecorateOptions
): ReactElement {
  const containerClass = `fiscal-decorator-container ${options?.className ?? ""}`.trim();
  return (
    <div className={containerClass}>
      <Breadcrumb items={breadcrumItems} />
      <div className="fiscal-decorator-actions">
        {actions}
      </div>
      <div className="fiscal-decorator-card">
        {content}

      </div>
    </div>
  );
}