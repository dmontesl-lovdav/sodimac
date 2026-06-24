
import { Breadcrumb } from "@/shared/components/ui/navigation";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { ReactElement } from "react";

import { Link } from "react-router-dom";
import { GenericMarqueeBar } from "../progress";
import "../button/styles/GenericButton.css";
import {
  useFinanceListReturnFromDetail,
  type FinanceListSessionKeys,
} from "@/shared/hooks";

import "./SimpleDecorator.css";

function FinanceListReturnMarker({
  session,
}: {
  session: FinanceListSessionKeys;
}): null {
  useFinanceListReturnFromDetail(session.moduleKey, session.listPath);
  return null;
}

export function decorate(
  breadcrumItems: BreadcrumbItem[],
  returnPath: string,
  content: ReactElement,
  loading?: boolean,
  actions?: ReactElement,
  options?: {
    actionsAlign?: "start" | "end";
    hideBackLink?: boolean;
    /** Al volver al listado, restaurar filtros guardados y refetch del grid. */
    financeListSession?: FinanceListSessionKeys;
  }
): ReactElement {
  if (loading) {
    return <GenericMarqueeBar />;
  }

  return (
    <div className="somx-decorate-container">
      {options?.financeListSession ? (
        <FinanceListReturnMarker session={options.financeListSession} />
      ) : null}
      <Breadcrumb items={breadcrumItems} />
      <div className="somx-decorate-card">
        {content}
        <div
          className={
            options?.actionsAlign === "end"
              ? "somx-decorate-actions somx-decorate-actions--end"
              : "somx-decorate-actions"
          }
        >
          <div className="finz-page-actions">
            {!options?.hideBackLink && (
              <Link className="generic-btn btn-back" to={returnPath}>
                Volver
              </Link>
            )}
            {actions}
          </div>
        </div>
      </div>
    </div>
  );
}