import { useState } from "react";
import { GenericTable } from "@/shared/components/ui";
import type { ReactNode, ReactElement } from "react";
import downloadIcon from "@assets/download.svg";
import { Reception, ReceptionSKU } from "../../interfaces";
import { formatDate, formatAmount, exportToCSV } from "@/utils/utils";
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
  { header: "Cantidad", render: (item) => parseInt(item.quantity) },
  { header: "Precio Unitario", render: (item) => formatAmount(parseFloat(item.unitCost)) },
  { header: "Importe", render: (item) => formatAmount(parseFloat(item.totalCost)) },
];

const ReceptionAccordion = ({ skus, receipt }: ReceptionAccordionProps) => {

  const [open] = useState(true);
  const [isExporting, setIsExporting] = useState(false);

  const handleDownloadCSV = () => {
    if (!skus || skus.length === 0) return;

    setIsExporting(true);

    const headers = ['SKU', 'Descripción', 'Cantidad', 'Precio Unitario', 'Importe'];

    const rows = skus.map(item => [
      item.sku,
      item.description,
      parseInt(item.quantity).toString(),
      parseFloat(item.unitCost).toFixed(2),
      parseFloat(item.totalCost).toFixed(2)
    ]);

    exportToCSV(headers, rows, "recepcion_" + receipt.receptionId);

    setIsExporting(false);
  };

  return (
    <div className="rc-accordion">
      <div className="rc-accordion-header">
        <div className="rc-accordion-summary">
          <div>
            <p className="rc-meta-label">Fecha Recepción</p>
            <p className="rc-meta-value">{formatDate(receipt.receptionDate)}</p>
          </div>
          <div>
            <p className="rc-meta-label">Cantidad</p>
            <p className="rc-meta-value">{formatAmount(receipt.amount)}</p>
          </div>
        </div>
      </div>

      {open && (
        <div className="rc-accordion-content">

          {receipt.comment && (
            <div className="rc-comment">
              {receipt.comment}
            </div>
          )}

          <div className="rc-table-wrap">
            <GenericTable
              rows={skus}
              columns={columns}
              emptyLabel="Sin artículos"
              perPage={10}
              page={1}
              totalPages={1}
            />
          </div>

          <div className="rc-download-row">
            <button
              onClick={handleDownloadCSV}
              className="rc-download-btn"
              disabled={isExporting}
            >
              <img src={downloadIcon} alt="Descargar" width={20} />
              Exportar CSV
            </button>
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
      <div className="rc-section-title">
        Resumen Artículos
      </div>

      <div className="rc-skus-panel">
        <div className="rc-stats-grid">
          <div className="rc-stat-row">
            <strong>Solicitados:</strong>
            <div>{sumSkus}</div>
          </div>
          <div className="rc-stat-row">
            <strong>Recibidos:</strong>
            <div>{sumSkus}</div>
          </div>
          <div className="rc-stat-row">
            <strong>SKUs Solicitados:</strong>
            <div>{sumSkus}</div>
          </div>
          <div className="rc-stat-row">
            <strong>SKUs Recibidos:</strong>
            <div>{sumSkus}</div>
          </div>
        </div>

        <div className="rc-spacer" />

        <ReceptionAccordion receipt={reception} skus={skus} />

      </div>
    </>
  );
}
