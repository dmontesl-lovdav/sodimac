import { formatAmount, formatDate } from "@/utils/utils";
import type { CreditNoteXmlData } from "./types";

type Props = {
  data: CreditNoteXmlData;
};

const SUMMARY_ROWS: Array<{
  label: string;
  getValue: (data: CreditNoteXmlData) => string;
}> = [
  { label: "RFC Emisor:", getValue: (d) => d.rfcEmisor },
  { label: "Número Proveedor:", getValue: (d) => d.numeroProveedor },
  { label: "Nombre Proveedor:", getValue: (d) => d.nombreProveedor },
  { label: "UUID:", getValue: (d) => d.uuid },
  { label: "UUID Factura:", getValue: (d) => d.uuidRelacionado },
  { label: "Serie:", getValue: (d) => d.serie },
  { label: "Folio:", getValue: (d) => d.folio },
  {
    label: "Importe:",
    getValue: (d) => formatAmount(parseFloat(d.monto)),
  },
  {
    label: "Fecha Timbrado:",
    getValue: (d) => formatDate(d.fechaTimbrado, true),
  },
  { label: "Uso CFDI:", getValue: (d) => d.usoCfdi },
  { label: "Tipo Comprobante:", getValue: (d) => d.tipoDeComprobante },
  { label: "Forma de Pago:", getValue: (d) => d.formaPago },
];

export default function CreditNoteSummary({ data }: Props) {
  return (
    <div className="pcn-summary-wrap">
      <div className="pcn-summary">
        <table className="pcn-summary-table">
          <tbody>
            {SUMMARY_ROWS.map((row) => (
              <tr key={row.label}>
                <th scope="row" className="pcn-cell pcn-cell-label">
                  {row.label}
                </th>
                <td className="pcn-cell">{row.getValue(data)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
