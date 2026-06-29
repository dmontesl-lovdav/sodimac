import { Link } from "react-router-dom";
import { buildCreditNotesListPath } from "./publishResult";

type Props = {
  fiscalUuid: string;
};

export default function PublishResultNotice({ fiscalUuid }: Props) {
  return (
    <p className="pcn-notice pcn-notice--success" role="status" aria-live="polite">
      Consulta la nota de crédito en el listado:{" "}
      <Link to={buildCreditNotesListPath(fiscalUuid)} className="pcn-result-link">
        Ver nota de crédito ({fiscalUuid})
      </Link>
    </p>
  );
}
