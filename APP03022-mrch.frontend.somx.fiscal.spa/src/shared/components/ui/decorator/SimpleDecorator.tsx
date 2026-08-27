import { Breadcrumb } from "@/shared/components/ui/navigation";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { ReactElement } from "react";
import { Link } from "react-router-dom";
import { GenericButton } from "@/shared/components/ui/button";
import { GenericMarqueeBar } from "../progress";
import "./Decorator.css";

export interface DecorateOptions {
  className?: string;
  hideBackLink?: boolean;
  actionsAlign?: "start" | "end";
}

function isExternalReturnPath(returnPath: string): boolean {
  return returnPath.startsWith("https://") || returnPath.startsWith("http://");
}

export function decorate(
  breadcrumItems: BreadcrumbItem[],
  returnPath: string,
  content: ReactElement,
  loading?: boolean,
  actions?: ReactElement,
  options?: DecorateOptions
): ReactElement {
  if (loading) {
    return <GenericMarqueeBar />;
  }

  const containerClass = `fiscal-decorator-container ${options?.className ?? ""}`.trim();
  const actionsClass =  "fiscal-decorator-footer-actions fiscal-decorator-footer-actions--end";
  const backLink = isExternalReturnPath(returnPath) ? (
    <a href={returnPath} className="fiscal-decorator-back-link">
      <GenericButton variant="link" type="button">
        Volver
      </GenericButton>
    </a>
  ) : (
    <Link to={returnPath} className="fiscal-decorator-back-link">
      <GenericButton variant="link" type="button">
        Volver
      </GenericButton>
    </Link>
  );

  return (
    <div className={containerClass}>
      <Breadcrumb items={breadcrumItems} />
      <div className="fiscal-decorator-card">
        {content}
        {!options?.hideBackLink && returnPath !== "/" ? (
          <div className={actionsClass}>
            {backLink}
            {actions}
          </div>
        ) : null}
      </div>
    </div>
  );
}