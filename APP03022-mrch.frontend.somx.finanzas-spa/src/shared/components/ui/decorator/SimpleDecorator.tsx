
import { Breadcrumb } from "@/shared/components/ui/navigation";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { ReactElement } from "react";

import { GenericButton } from "@/shared/components/ui/button";
import { Link } from "react-router-dom";
import { GenericMarqueeBar } from "../progress";

export function decorate(
  breadcrumItems: BreadcrumbItem[],
  returnPath: string,
  content: ReactElement,
  loading?: boolean,
  actions?: ReactElement
): ReactElement {
  if (loading) {
    return <GenericMarqueeBar />;
  }

  return (
    <div className="somx-decorate-container">
      <Breadcrumb items={breadcrumItems} />
      <div className="somx-decorate-card">
        {content}
        <div className="somx-decorate-actions">
          {actions}
          <GenericButton variant="link" className="somx-self-end">
            <Link to={returnPath}>Volver</Link>
          </GenericButton>
        </div>
      </div>
    </div>
  );
}