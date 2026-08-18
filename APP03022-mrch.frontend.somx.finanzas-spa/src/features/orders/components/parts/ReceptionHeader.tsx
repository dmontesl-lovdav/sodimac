
import { ReactElement, ReactNode } from "react";
import { Reception } from "../../interfaces";
import type { ReceptionSupplierInfo } from "../../receptionSupplierInfo";
import { formatDate, formatAmount } from "@/utils/utils";
import { StatusPill } from "@/shared/components/ui/statusPill/StatusPill";
import "./ReceptionHeader.css";

interface ReceptionHeaderProps {
  reception: Reception;
  supplierInfo: ReceptionSupplierInfo;
  headerActions?: ReactNode;
}


const getAdendumReferences = (reception: Reception) => {
  if (!reception.listAddendum || reception.listAddendum.length === 0) return null;
  return reception.listAddendum[0].invoice;
};

const getInvoiceDocumentKind = (reception: Reception) => {
  const inv = getAdendumReferences(reception);
  if (!inv) return "--";
  return inv.document_type ?? inv.documentType ?? "--";
};

export default function ReceptionHeader({
  reception,
  supplierInfo,
  headerActions,
}: ReceptionHeaderProps): ReactElement {
  return (
    <div className="rc-header-card">
      <div className="rc-header-top">
        <div className="rc-title-inline">
          <span className="rc-section-title">Resumen recepción</span>
          <StatusPill>{reception.receptionNumber ?? reception.receptionId ?? "—"}</StatusPill>
        </div>
        {headerActions ? (
          <div className="rc-header-actions">{headerActions}</div>
        ) : null}
      </div>
      <div className="rc-summary-grid">
        <div className="rc-summary-item">
          <div className="rc-label">Orden Compra</div>
          <div>
            <span className="rc-ord-val">
              {reception.order?.orderNumber ?? reception.orderNumber ?? "—"}
            </span>
          </div>
        </div>
        <div className="rc-summary-item">
          <div className="rc-label">Fecha Recepción</div>
          <div>{formatDate(reception.receptionDate) ?? "Sin Información"}</div>
        </div>
        <div className="rc-summary-item">
          <div className="rc-label">Fecha Registro</div>
          <div>{formatDate(reception.createdAt, true) ?? "Sin Información"}</div>
        </div>
        <div className="rc-summary-item">
          <div className="rc-label">Importe</div>
          <div>{formatAmount(reception.amount)}</div>
        </div>
        <div className="rc-summary-item">
          <div className="rc-label">RFC</div>
          <div>{supplierInfo.rfc}</div>
        </div>
        <div className="rc-summary-item">
          <div className="rc-label">Número Proveedor</div>
          <div>{supplierInfo.number}</div>
        </div>
        <div className="rc-summary-item">
          <div className="rc-label">Nombre Proveedor</div>
          <div>{supplierInfo.name}</div>
        </div>
        <div className="rc-summary-item">
          <div className="rc-label">Guía</div>
          <div>{reception.order?.shippingGuideNumber ?? "—"}</div>
        </div>
        <div className="rc-summary-item">
          <div className="rc-label">Sucursal</div>
          <div>{reception.destinationId ?? "—"}</div>
        </div>
        <div className="rc-summary-item">
          <div className="rc-label">Documento</div>
          <div>{getInvoiceDocumentKind(reception)}</div>
        </div>
      </div>
    </div>
  );
}
