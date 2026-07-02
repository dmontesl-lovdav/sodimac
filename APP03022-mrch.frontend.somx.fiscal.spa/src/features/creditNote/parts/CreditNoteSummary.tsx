import { formatAmount, formatDate } from "@/utils/utils";
import type { CreditNoteXmlData } from "./types";

type Props = {
  data: CreditNoteXmlData;
};

export default function CreditNoteSummary({ data }: Props) {
  return (
    <div className="pcn-summary-wrap">
      <div className="pcn-summary">
        <table className="pcn-summary-table">
          <tbody>
            <tr>
              <td className="pcn-cell pcn-cell-label">RFC Emisor:</td>
              <td className="pcn-cell">{data.rfcEmisor}</td>
            </tr>
            <tr>
              <td className="pcn-cell pcn-cell-label">Nombre Proveedor:</td>
              <td className="pcn-cell">{data.nombreProveedor}</td>
            </tr>
            <tr>
              <td className="pcn-cell pcn-cell-label">UUID Factura:</td>
              <td className="pcn-cell">{data.uuidRelacionado}</td>
            </tr>
            <tr>
              <td className="pcn-cell pcn-cell-label">Serie:</td>
              <td className="pcn-cell">{data.serie}</td>
            </tr>
            <tr>
              <td className="pcn-cell pcn-cell-label">Folio:</td>
              <td className="pcn-cell">{data.folio}</td>
            </tr>
            <tr>
              <td className="pcn-cell pcn-cell-label">Importe:</td>
              <td className="pcn-cell">{formatAmount(parseFloat(data.monto))}</td>
            </tr>
            <tr>
              <td className="pcn-cell pcn-cell-label">Fecha Timbrado:</td>
              <td className="pcn-cell">{formatDate(data.fechaTimbrado, true)}</td>
            </tr>
            <tr>
              <td className="pcn-cell pcn-cell-label">Uso CFDI:</td>
              <td className="pcn-cell">{data.usoCfdi}</td>
            </tr>
            <tr>
              <td className="pcn-cell pcn-cell-label">Tipo Comprobante:</td>
              <td className="pcn-cell">{data.tipoDeComprobante}</td>
            </tr>
            <tr>
              <td className="pcn-cell pcn-cell-label">Forma de Pago:</td>
              <td className="pcn-cell">{data.formaPago}</td>
            </tr>
            
          </tbody>
        </table>
      </div>
    </div>
  );
}
