
import { useState } from "react";
import { buildTable, Column } from "@/shared/components/ui/table/GenericTable";
import downloadIcon from "@assets/download.svg";
import { ReactElement } from "react";
import { Reception, ReceptionSKU } from "../../interfaces";
import { formatDate, formatAmount, exportToCSV } from "@/utils/utils";
import ErrorMessage from "@/shared/components/ui/alerts/ErrorMessage";

interface ReceptionSkuProps {
  reception: Reception;
}
interface ReceptionAccordionProps {
  skus: ReceptionSKU[];
  receipt: Reception;
}

const columns: Column<ReceptionSKU>[] = [
  { header: "SKU", render: (item) => <>{item.sku}</> },
  { header: "Descripción", render: (item) => <>{item.description}</> },
  { header: "Cantidad", render: (item) => <>{parseInt(item.quantity)}</> },
  { header: "Precio Unitario", render: (item) => formatAmount(parseFloat(item.unitCost)) },
  { header: "Importe", render: (item) => formatAmount(parseFloat(item.totalCost)) },
];

const ReceptionAccordion = ({ skus, receipt }: ReceptionAccordionProps) => {
  
  const [open, setOpen] = useState(true);
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
    <div className=" somx-rounded-xl somx-border somx-border-gray-200 somx-m-5">
      <div className="somx-w-full somx-flex somx-justify-between somx-items-center somx-p-4 somx-hover:bg-gray-50">
        <div className="somx-grid somx-grid-cols-2 somx-gap-y-4 somx-mb-3" style={{ justifyItems: "center", gap:"3"}}>
          <div>
            <p className="somx-text-sm somx-text-gray-500">Fecha Recepción</p>
            <p className="somx-font-semibold">{formatDate(receipt.receptionDate)}</p>
          </div>
          <div>
            <p className="somx-text-sm somx-text-gray-500">Cantidad</p>
            <p className="somx-font-semibold">{formatAmount(receipt.amount)}</p>
          </div>
        </div>
      </div>

      {open && (
        <div className="somx-p-4 somx-border-t somx-border-gray-200">
          {receipt.comment && (
            <div className="somx-text-gray-600 somx-italic somx-mb-4">{receipt.comment}</div>
          )}
          <div className="somx-overflow-x-auto">
            {buildTable<ReceptionSKU>(skus, columns, {
              emptyLabel: "Sin artículos",
              perPage: 10,
              page: 1,
              totalPages: 1,
            })}
          </div>
          <div className="somx-flex somx-justify-end somx-mt-4">
            <button
              onClick={handleDownloadCSV}
              className="somx-flex somx-items-center somx-gap-2 somx-text-blue-600 somx-hover:text-blue-800 somx-font-medium"
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
  if(skus.length==0){
    return <div className="mt-2">
      <ErrorMessage message="No hay artículos asociados a esta recepción." />
    </div>;
  }
  const sumSkus =skus.length;
  return (
    <>
      <div className="somx-font-bold somx-pt-6 somx-pb-6">Resumen Artículos</div>
      <div className="somx-w-full somx-rounded-xl">
        <div className="somx-grid somx-grid-cols-2 somx-gap-y-4 somx-mb-3">
          <div className="somx-grid somx-grid-cols-2"><strong>Solicitados:</strong><div>{sumSkus}</div></div>
          <div className="somx-grid somx-grid-cols-2"><strong>Recibidos:</strong><div>{sumSkus}</div></div>
          <div className="somx-grid somx-grid-cols-2"><strong>SKUs Solicitados:</strong><div>{sumSkus}</div></div>
          <div className="somx-grid somx-grid-cols-2"><strong>SKUs Recibidos:</strong><div>{sumSkus}</div></div>
        </div>
        <br />
        <ReceptionAccordion receipt={reception} skus={skus} />
      </div>
    </>
  );
}
