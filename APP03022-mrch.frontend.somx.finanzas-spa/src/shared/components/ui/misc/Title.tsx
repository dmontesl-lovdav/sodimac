
import { ReactElement } from "react";
import { Divider } from "./Divider";

export function Title({ title }: { title?: string }): ReactElement {
  return (
   <div>
      <p className="font-bold">{title}</p>
      <Divider />
   </div>
  );
}

