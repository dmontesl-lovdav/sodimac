import { formatAmount, formatDate } from "@/utils/utils";
import type { ComplementPayment } from "../interfaces";
import "../../creditNote/parts/DiscountInfoGrid.css";

type Props = {
  complement: ComplementPayment;
};

type Field = {
  label: string;
  value: string;
};

const dash = (v: unknown): string => {
  if (v == null || v === "") return "--";
  return String(v);
};

export default function ComplementHeaderInfo({ complement }: Props) {
  const fields: Field[] = [
    { label: "Serie", value: dash(complement.series) },
    { label: "Folio", value: dash(complement.folio) },
    { label: "UUID", value: dash(complement.fiscalUuid) },
    {
      label: "SubTotal",
      value:
        complement.subtotalAmount != null
          ? formatAmount(Number(complement.subtotalAmount))
          : "--",
    },
    {
      label: "Total",
      value:
        complement.totalAmount != null
          ? formatAmount(Number(complement.totalAmount))
          : "--",
    },
    {
      label: "Fecha de Emisión",
      value: complement.paymentDate ? formatDate(complement.paymentDate) : "--",
    },
    { label: "RFC Emisor", value: dash(complement.issuerRfc) },
    { label: "Nombre Emisor", value: dash(complement.issuerName) },
    {
      label: "Fecha de Pago",
      value: complement.paymentDate ? formatDate(complement.paymentDate) : "--",
    },
    {
      label: "Fecha Registro",
      value: complement.createdAt ? formatDate(complement.createdAt) : "--",
    },
    {
      label: "Facturas Relacionadas",
      value: dash(complement.relatedDocumentsCount),
    },
    { label: "Estatus", value: dash(complement.statusDescription) },
  ];

  return (
    <div className="pcn-discount-header-card">
      <div className="pcn-discount-header-top">
        <span className="pcn-discount-section-title">Datos del complemento</span>
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
