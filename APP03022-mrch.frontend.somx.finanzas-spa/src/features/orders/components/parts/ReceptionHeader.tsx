
import { ReactElement } from "react";
import { Reception } from "../../interfaces";
import { formatDate, formatAmount } from "@/utils/utils";
import { StatusPill } from "@/shared/components/ui/statusPill/StatusPill";
import "./ReceptionHeader.css";

interface ReceptionHeaderProps {
  reception: Reception;
}

export default function ReceptionHeader({ reception }: ReceptionHeaderProps): ReactElement {
  return (
    <div className="rc-header-card">
      <div className="rc-section-title">Resumen recepción</div>
      <div className="rc-main-pill">
        <StatusPill large>{reception.receptionNumber}</StatusPill>
      </div>
      <div className="rc-summary-grid">
        <div className="rc-summary-item">
          <div className="rc-label">Orden de compra</div>
          <div>
            <StatusPill large>{reception.order.orderNumber}</StatusPill>
          </div>
        </div>
        <div className="rc-summary-item">
          <div className="rc-label">Fecha Recepción</div>
          <div>{formatDate(reception.receptionDate) || "Sin Información"}</div>
        </div>
        <div className="rc-summary-item">
          <div className="rc-label">Fecha Registro Documento</div>
          <div>{formatDate(reception.receptionDate, true) || "Sin Información"}</div>
        </div>
        <div className="rc-summary-item">
          <div className="rc-label">Monto</div>
          <div>{formatAmount(reception.amount)}</div>
        </div>
        <div className="rc-summary-item">
          <div className="rc-label">Id Proveedor</div>
          <div>{reception.order.supplierNumber}</div>
        </div>
        <div className="rc-summary-item">
          <div className="rc-label">Nombre Proveedor</div>
          <div>{reception.order.supplier?.businessName}</div>
        </div>
      </div>
    </div>
  );
}
