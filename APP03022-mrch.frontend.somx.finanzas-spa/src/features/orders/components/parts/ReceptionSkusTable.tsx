import { useState } from "react";
import { GenericTable } from "@/shared/components/ui";
import type { ReactNode, ReactElement } from "react";
import { Reception, ReceptionSKU } from "../../interfaces";
import { formatDate, formatAmount } from "@/utils/utils";
import ErrorMessage from "@/shared/components/ui/alerts/ErrorMessage";
import "./ReceptionSkusTable.css";

interface ReceptionSkuProps {
  reception: Reception;
}

interface ReceptionAccordionProps {
  skus: ReceptionSKU[];
  receipt: Reception;
}

type Column<T> = {
  header: string;
  render: (row: T) => ReactNode;
};

const columns: Column<ReceptionSKU>[] = [
  { header: "SKU", render: (item) => item.sku },
  { header: "Descripción", render: (item) => item.description },
  { header: "Cantidad", render: (item) => parseInt(item.quantity, 10) },
  { header: "Precio Unitario", render: (item) => formatAmount(parseFloat(item.unitCost)) },
  { header: "Importe", render: (item) => formatAmount(parseFloat(item.totalCost)) },
];

const ReceptionAccordion = ({ skus, receipt }: ReceptionAccordionProps) => {
  const [open] = useState(true);
  const [page, setPage] = useState(1);
  const [perPage, setPerPage] = useState(10);

  const total = skus.length;
  const totalPages = Math.max(1, Math.ceil(total / perPage));
  const safePage = Math.min(page, totalPages);
  const start = (safePage - 1) * perPage;
  const pageRows = skus.slice(start, start + perPage);

  return (
    <div className="rc-accordion">
      <div className="rc-accordion-header">
        <div className="rc-accordion-summary">
          <div>
            <p className="rc-meta-label">Fecha Recepción</p>
            <p className="rc-meta-value">{formatDate(receipt.receptionDate)}</p>
          </div>
          <div>
            <p className="rc-meta-label">Importe</p>
            <p className="rc-meta-value">{formatAmount(receipt.amount)}</p>
          </div>
        </div>
      </div>

      {open && (
        <div className="rc-accordion-content">
          {receipt.comment && <div className="rc-comment">{receipt.comment}</div>}

          <div className="rc-table-wrap">
            <GenericTable
              rows={pageRows}
              columns={columns}
              emptyLabel="Sin artículos"
              perPage={perPage}
              page={safePage}
              totalPages={totalPages}
              totalItems={total}
              onChangePage={setPage}
              onChangePerPage={(n) => {
                setPerPage(n);
                setPage(1);
              }}
            />
          </div>

          
        </div>
      )}
    </div>
  );
};

export default function ReceptionSkusTable({ reception }: ReceptionSkuProps): ReactElement {
  const skus = reception?.receptionSkus || [];

  if (skus.length === 0) {
    return (
      <div className="rc-empty-state">
        <ErrorMessage message="No hay artículos asociados a esta recepción." />
      </div>
    );
  }

  const sumSkus = skus.length;

  return (
    <>
      <div className="rc-section-title rc-section-title-compact">Resumen Artículos</div>

      <div className="rc-skus-panel">
        <div className="rc-stats-inline">
          <span>
            <strong>Solicitados:</strong> {sumSkus}
          </span>
          <span className="rc-stat-sep">|</span>
          <span>
            <strong>Recibidos:</strong> {sumSkus}
          </span>
          <span className="rc-stat-sep">|</span>
          <span>
            <strong>SKUs solicitados:</strong> {sumSkus}
          </span>
          <span className="rc-stat-sep">|</span>
          <span>
            <strong>SKUs recibidos:</strong> {sumSkus}
          </span>
        </div>

        <div className="rc-spacer-sm" />

        <ReceptionAccordion receipt={reception} skus={skus} />
      </div>
    </>
  );
}
