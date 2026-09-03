import viewIcon from "@assets/eye-show.svg";
import { GenericLinearProgress } from "@/shared/components/ui/progress";
import { WarningMessage } from "@/shared/components/ui";
import { formatAmount, formatDate } from "@/utils/utils";
import type { Invoice } from "../../invoice/interfaces";

type Props = {
  invoice: Invoice | null;
  loading: boolean;
  onViewInvoice: (invoice: Invoice) => void;
};

export default function RelatedInvoiceGrid({ invoice, loading, onViewInvoice }: Props) {
  if (loading) return <GenericLinearProgress />;

  if (!invoice) {
    return (
      <WarningMessage
        className="pcn-invoice-grid-empty"
        message="No se encontró la factura relacionada. Verifique el archivo XML."
      />
    );
  }

  const ncCount = Array.isArray(invoice.notasCreditoRelacionadas)
    ? invoice.notasCreditoRelacionadas.length
    : 0;

  return (
    <div className="pcn-invoice-grid-wrap">
      <table className="pcn-invoice-grid">
        <thead>
          <tr>
            <th>Serie</th>
            <th>Folio</th>
            <th>Subtotal</th>
            <th>UUID</th>
            <th>NC Relacionadas</th>
            <th>Tipo Proveedor</th>
            <th>Número Proveedor</th>
            <th>RFC</th>
            <th>Acción</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>{invoice.series ?? "--"}</td>
            <td>{invoice.folio ?? "--"}</td>
            <td>{invoice.subtotal != null ? formatAmount(invoice.subtotal) : "--"}</td>
            <td className="pcn-cell-wrap">{invoice.fiscalUuid ?? invoice.invoiceUuid ?? "--"}</td>
            <td>{ncCount}</td>
            <td>{invoice.tipoProveedorDescripcion ?? "--"}</td>
            <td>{invoice.numeroProveedor ?? "--"}</td>
            <td>{invoice.emisorRfc ?? "--"}</td>
             <td>
              <button
                type="button"
                className="pcn-action-btn"
                title="Ver factura"
                onClick={() => onViewInvoice(invoice)}
              >
                <img src={viewIcon} alt="Ver factura" className="pcn-action-icon" />
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  );
}
