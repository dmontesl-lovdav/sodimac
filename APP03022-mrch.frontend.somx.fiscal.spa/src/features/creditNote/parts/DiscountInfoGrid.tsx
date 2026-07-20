import { Fragment } from "react";
import { formatAmount, formatDate } from "@/utils/utils";
import { displayOrDash } from "./publishQuery";
import type { PublishQuery } from "./types";

type Props = {
  discount: PublishQuery;
};

type Field = {
  label: string;
  value: string;
};

function chunkFields(fields: Field[], size: number): Field[][] {
  const rows: Field[][] = [];
  for (let i = 0; i < fields.length; i += size) {
    rows.push(fields.slice(i, i + size));
  }
  return rows;
}

export default function DiscountInfoGrid({ discount }: Props) {
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
      label: "Número de Proveedor",
      value: displayOrDash(discount.supplierNumber),
    },
    { label: "Nombre Proveedor", value: displayOrDash(discount.vendorName) },
    {
      label: "Número de documento",
      value: displayOrDash(discount.documentNumber),
    },
  ];

  const rows = chunkFields(fields, 5);

  return (
    <div className="pcn-invoice-grid-wrap">
      <table className="pcn-invoice-grid pcn-discount-grid">
        <tbody>
          {rows.map((row) => {
            const rowKey = row.map((f) => f.label).join("|");
            return (
              <Fragment key={rowKey}>
                <tr>
                  {row.map((field) => (
                    <th key={`th-${field.label}`} scope="col">
                      {field.label}
                    </th>
                  ))}
                  {Array.from({ length: 5 - row.length }).map((_, idx) => (
                    <th key={`th-empty-${idx}`} scope="col" />
                  ))}
                </tr>
                <tr>
                  {row.map((field) => (
                    <td key={`td-${field.label}`} className="pcn-cell-wrap">
                      {field.value}
                    </td>
                  ))}
                  {Array.from({ length: 5 - row.length }).map((_, idx) => (
                    <td key={`td-empty-${idx}`} />
                  ))}
                </tr>
              </Fragment>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
