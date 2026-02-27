
import { ReactElement } from "react";
import { Reception } from "../../interfaces";
import { formatDate, formatAmount } from "@/utils/utils";
import { StatusPill } from "@/shared/components/ui/statusPill/StatusPill";

interface ReceptionHeaderProps {
  reception: Reception;
}

export default function ReceptionHeader({ reception }: ReceptionHeaderProps): ReactElement {
  return (
    <div>
      <div className="somx-font-bold">Resumen recepción</div>
      <div className="somx-pl-1">
        <StatusPill large>{reception.receptionNumber}</StatusPill>
      </div>
      <div className="somx-mt-5 somx-p-3 somx-border-1 somx-border-gray-200 somx-grid somx-grid-cols-6">
        <div>
          <div className="somx-font-bold">Orden de compra</div>
          <div>
            <StatusPill large>{reception.order.orderNumber}</StatusPill>
          </div>
        </div>
        <div>
          <div className="somx-font-bold">Fecha Recepción</div>
          <div>{formatDate(reception.receptionDate) || "Sin Información"}</div>
        </div>
        <div>
          <div className="somx-font-bold">Fecha Registro Documento</div>
          <div>{formatDate(reception.receptionDate, true) || "Sin Información"}</div>
        </div>
        <div>
          <div className="somx-font-bold">Monto</div>
          <div>{formatAmount(reception.amount)}</div>
        </div>
        <div>
          <div className="somx-font-bold">Id Proveedor</div>
          <div>{reception.order.supplierNumber}</div>
        </div>
        <div>
          <div className="somx-font-bold">Nombre Proveedor</div>
          <div>{reception.order.supplier?.businessName}</div>
        </div>
      </div>
    </div>
  );
}
