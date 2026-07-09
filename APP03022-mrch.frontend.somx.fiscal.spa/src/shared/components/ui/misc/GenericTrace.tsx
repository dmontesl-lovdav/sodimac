import { ReactElement } from "react";

export interface GenericTraceProps {
  traceId: string | null;
  uuid?: string | null;
}

export function GenericTrace({ traceId, uuid }: GenericTraceProps): ReactElement {
  if (!traceId) return <p style={{ color: "red" }}>Sin folio de trazabilidad</p>;

  return (
    <div className="fiscal-header-trace">
      <span className="fiscal-font-medium">Folio:</span>{" "}
      <span className="fiscal-trace-id">{traceId}</span>
    </div>
  );
}
