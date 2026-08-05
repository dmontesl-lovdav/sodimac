import { formatAmount, formatDate } from "@/utils/utils";
import { displayOrDash } from "./publishQuery";
import type { PublishQuery } from "./types";
import "./DiscountInfoGrid.css";

type Props = {
  discount: PublishQuery;
  providers: any[];
};

type Field = {
  label: string;
  value: string;
};

export default function DiscountInfoGrid({ discount, providers }: Props) {
  console.log(providers);
  const supplierRfc = providers.find((provider) => provider.idProveedor == discount.supplierNumber)?.rfc;
  const fields: Field[] = [
    { label: "Referencia", value: displayOrDash(discount.documentReference) },
    { label: "Tipo Rebate", value: displayOrDash(discount.tipoRebate) },
    { label: "Documento SAP", value: displayOrDash(discount.sapDocument) },
    {
      label: "Importe",
      value: discount.amount.trim()
        ? formatAmount(Number(discount.amount))
        : "--",
    },
    { label: "Periodo", value: displayOrDash(discount.periodId) },
    {
      label: "Fecha Aplicación",
      value: discount.postingDate.trim()
        ? formatDate(discount.postingDate)
        : "--",
    },
    {
      label: "Fecha Vencimiento",
      value: discount.dueDate.trim() ? formatDate(discount.dueDate) : "--",
    },
    {
      label: "RFC Proveedor",
      value: displayOrDash(supplierRfc),
    },
    {
      label: "Número de Proveedor",
      value: displayOrDash(discount.supplierNumber),
    },
    { label: "Nombre Proveedor", value: displayOrDash(discount.vendorName) },
    {
      label: "Número de documento",
      value: displayOrDash(discount.documentNumber),
    },
  ];

  return (
    <div className="pcn-discount-header-card">
      <div className="pcn-discount-header-top">
        <span className="pcn-discount-section-title">Datos del descuento</span>
      </div>
      <div className="pcn-discount-summary-grid">
        {fields.map((field) => (
          <div key={field.label} className="pcn-discount-summary-item">
            <div className="pcn-discount-label">{field.label}</div>
            <div>{field.value}</div>
          </div>
        ))}
      </div>
    </div>
  );
}
