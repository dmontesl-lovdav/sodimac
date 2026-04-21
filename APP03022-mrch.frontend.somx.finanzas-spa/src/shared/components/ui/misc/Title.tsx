
import { ReactElement } from "react";
import { Divider } from "./Divider";

interface TitleProps {
  title?: string;
  description?: string;
}
export function Title({ title, description }: TitleProps): ReactElement {
  return (
   <div>
      <h3>{title}</h3>
      {description && <p className="somx-mt-3 somx-mb-3">{description}</p>}
      <Divider />
   </div>
  );
}

