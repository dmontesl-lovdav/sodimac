import { useState, useCallback, useMemo, useEffect, useRef } from "react";
import type { TraceFolioPayload } from "@/services/TraceabilityClient";
import { getUserIdFromStore } from "@/utils/getUserIdFromStore";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import {
  GenericButton,
  GenericModal,
} from "@/shared/components/ui";
import { GenericLinearProgress } from "@/shared/components/ui/progress";
import { TraceFolioProvider, useTraceFolio } from "@/hooks/TraceFolioProvider";
import {
  formatAmount,
  formatDate,
  formatLocalDateStr,
  getErrorMessage,
  startOfLocalDay,
} from "@/utils/utils";
import "@/shared/components/ui/alerts/Alerts.css";
import iconSuccess from "@assets/icons/alert-up.png";
import iconWarning from "@assets/icons/warning.png";
import viewIcon from "@assets/eye-show.svg";
import { createCreditNotePublishClient } from "./api/CreditNotePublishClient";
import { createInvoicesClient } from "../invoice/api/InvoiceClient";
import {
  EMPTY_INVOICE,
  type Invoice,
} from "../invoice/interfaces";
import BitacoraErrorModal from "@/shared/components/ui/modal/BitacoraErrorModal";
import {
  normalizePublishCreditNoteResponse,
  resolvePublishCreditNoteOutcome,
} from "./utils/publishCreditNoteResponse";
import "./PublishCreditNote.css";

const MAX_MB = 10;
const MAX_BYTES = MAX_MB * 1024 * 1024;

type CreditNoteXmlData = {
  timbreFiscalDigital: {
    uuid: string;
    fechaTimbrado: string;
  };
  rfcEmisor: string;
  nombreProveedor: string;
  serie: string;
  folio: string;
  monto: string;
  fechaTimbrado: string;
  usoCfdi: string;
  tipoDeComprobante: string;
  uuid: string;
};

type PublishQuery = {
  supplierNumber: string;
  documentNumber: string;
  currency: string;
  amount: string;
  postingDate: string;
  relatedInvoiceUuid: string;
};

const BREADCRUMB: BreadcrumbItem[] = [
  { label: "Fiscal", to: "/" },
  { label: "Notas de Crédito", to: "/fiscal/notas-credito" },
  { label: "Publicar Nota de Crédito" },
];

function parsePublishQuery(search: string): PublishQuery {
  const params = new URLSearchParams(search);
  return {
    supplierNumber: params.get("supplierNumber") ?? "",
    documentNumber: params.get("documentNumber") ?? "",
    currency: params.get("currency") ?? "MXN",
    amount: params.get("amount") ?? "",
    postingDate: params.get("postingDate") ?? "",
    relatedInvoiceUuid: params.get("uuid") ?? "",
  };
}

function parseValidatedXml(data: unknown): CreditNoteXmlData | null {
  if (!data || typeof data !== "object") return null;
  const root = data as Record<string, unknown>;
  const emisor = root.emisor as Record<string, unknown> | undefined;
  const comprobante = root.comprobante as Record<string, unknown> | undefined;
  const receptor = root.receptor as Record<string, unknown> | undefined;
  const timbre = root.timbreFiscalDigital as Record<string, unknown> | undefined;
  if (!emisor || !comprobante || !timbre) return null;

  return {
    timbreFiscalDigital: {
      uuid: String(timbre.uuid ?? ""),
      fechaTimbrado: String(timbre.fechaTimbrado ?? ""),
    },
    rfcEmisor: String(emisor.rfc ?? ""),
    nombreProveedor: String(emisor.nombre ?? ""),
    serie: String(comprobante.serie ?? ""),
    folio: String(comprobante.folio ?? ""),
    monto: String(comprobante.subTotal ?? comprobante.total ?? ""),
    fechaTimbrado: String(comprobante.fecha ?? ""),
    usoCfdi: String(receptor?.usoCFDI ?? ""),
    tipoDeComprobante: String(comprobante.tipoDeComprobante ?? ""),
    uuid: String(timbre.uuid ?? ""),
  };
}

export default function PublishCreditNote() {
  const traceFolioPayload = useMemo<TraceFolioPayload>(
    () => ({
      idAplicativo: "fiscal-front",
      idModulo: "NOTA_CREDITO",
      paso: "INIT_PUBLISH_CREDIT_NOTE",
      detalle: "Inicio de trazabilidad en pantalla Publicar Nota de Crédito.",
      fechaHora: new Date().toISOString(),
      tipoEvento: "INFO",
      idUsuario: getUserIdFromStore() ?? "1",
    }),
    []
  );

  return decorate(
    BREADCRUMB,
    "/fiscal/notas-credito",
    <TraceFolioProvider traceFolioPayload={traceFolioPayload}>
      <PublishCreditNoteContent />
    </TraceFolioProvider>
  );
}

type OperationSeverity = "success" | "warning" | "error";
type OperationResult = { severity: OperationSeverity; backendText?: string };

function RelatedInvoiceGrid({
  invoice,
  loading,
  onViewInvoice,
}: {
  invoice: Invoice | null;
  loading: boolean;
  onViewInvoice: (row: Invoice) => void;
}) {
  if (loading) {
    return <GenericLinearProgress />;
  }

  if (!invoice ) {
    return (
      <p className="pcn-invoice-grid-empty">
        No se encontró la factura relacionada. Verifique los parámetros de la URL.
      </p>
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
            <th>Total</th>
            <th>UUID</th>
            <th>NC Relacionadas</th>
            <th>Tipo Proveedor</th>
            <th>Número Proveedor</th>
            <th>RFC</th>
            <th>Nombre Proveedor</th>
            <th>Fecha Emisión</th>
            <th>Fecha Recepción</th>
            <th>Estado</th>
            <th>Acción</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>{invoice.series ?? "--"}</td>
            <td>{invoice.folio ?? "--"}</td>
            <td>
              {invoice.subtotal != null ? formatAmount(invoice.subtotal) : "--"}
            </td>
            <td>{invoice.total != null ? formatAmount(invoice.total) : "--"}</td>
            <td className="pcn-cell-wrap">
              {invoice.fiscalUuid ?? invoice.invoiceUuid ?? "--"}
            </td>
            <td>{ncCount}</td>
            <td>{invoice.tipoProveedor ?? "--"}</td>
            <td>{invoice.numeroProveedor ?? "--"}</td>
            <td>{invoice.emisorRfc ?? "--"}</td>
            <td>{invoice.supplierName ?? invoice.emisorName ?? "--"}</td>
            <td>
              {invoice.issueDate ? formatDate(invoice.issueDate) : "N/D"}
            </td>
            <td>
              {invoice.certificationDate
                ? formatDate(invoice.certificationDate)
                : "N/D"}
            </td>
            <td>{invoice.statusName ?? "--"}</td>
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

function PublishCreditNoteContent() {
  const location = useLocation();
  const navigate = useNavigate();
  const publishClient = useMemo(() => createCreditNotePublishClient(), []);
  const invoiceClient = useMemo(
    () =>
      createInvoicesClient<{
        content: Invoice[];
        totalElements: number;
      }>(),
    []
  );

  const query = useMemo(
    () => parsePublishQuery(location.search),
    [location.search]
  );

  const xmlInputRef = useRef<HTMLInputElement>(null);
  const pdfInputRef = useRef<HTMLInputElement>(null);

  const [relatedInvoice, setRelatedInvoice] = useState<Invoice | null>(null);
  const [loadingInvoice, setLoadingInvoice] = useState(false);
  const [invoiceLoaded, setInvoiceLoaded] = useState(false);
  const [xmlFile, setXmlFile] = useState<File | null>(null);
  const [pdfFile, setPdfFile] = useState<File | null>(null);
  const [creditNoteData, setCreditNoteData] = useState<CreditNoteXmlData | null>(
    null
  );
  const [isValidating, setIsValidating] = useState(false);
  const [isValidCreditNote, setIsValidCreditNote] = useState(false);
  const [dataMsg, setDataMsg] = useState("");
  const [operationResult, setOperationResult] = useState<OperationResult | null>(
    null
  );
  const [invoice, setInvoice] = useState("");
  const [published, setPublished] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [alertVisible, setAlertVisible] = useState(false);
  const [alertMessage, setAlertMessage] = useState("");

  const { traceId, addLog, headerActions, noTraceWarning } = useTraceFolio();
  const hasTraceId = Boolean(traceId);
  const canPublish =
    hasTraceId &&
    Boolean(xmlFile) &&
    Boolean(creditNoteData) &&
    isValidCreditNote &&
    !isUploading &&
    relatedInvoice &&
    !published;

  const FINANZAS_URL = process.env.FINANZAS_URL ?? "";
  const traceIdLink = traceId ? (
    <Link to={`${FINANZAS_URL}/#/auditoria/bitacora-actividades/tren/${traceId}`}>
      {traceId}
    </Link>
  ) : null;

  useEffect(() => {
    const uuid = invoice.trim();
    if (!uuid) {
      setRelatedInvoice(null);
      return;
    }

    let cancelled = false;
    (async () => {
      setLoadingInvoice(true);
      try {
        const end = startOfLocalDay(new Date());
        const start = new Date(end);
        start.setMonth(start.getMonth() - 6);

        const result = await invoiceClient.getInvoices({
          ...EMPTY_INVOICE,
          uuid,
          fechaInicioRecepcion: formatLocalDateStr(start),
          fechaFinalRecepcion: formatLocalDateStr(end),
          page: 0,
          size: 1,
        });

        if (cancelled) return;
        setRelatedInvoice(result?.content?.[0] ?? null);
        setInvoiceLoaded(true);
        if(result?.content?.length ==0) {
          showAlert("No se encontró la factura relacionada, verifique el archivo de la nota de crédito.");
        }
      } catch {
        if (!cancelled) setRelatedInvoice(null);
      } finally {
        if (!cancelled) setLoadingInvoice(false);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [invoice, invoiceLoaded]);

  const showAlert = useCallback((message: string) => {
    setAlertMessage(message);
    setAlertVisible(true);
  }, []);

  const resetFileInputs = () => {
    if (xmlInputRef.current) xmlInputRef.current.value = "";
    if (pdfInputRef.current) pdfInputRef.current.value = "";
  };

  const handleClearForm = useCallback(() => {
    resetFileInputs();
    setXmlFile(null);
    setPdfFile(null);
    setCreditNoteData(null);
    setIsValidCreditNote(false);
    setDataMsg("");
    setIsValidating(false);
    setOperationResult(null);
    setPublished(false);
  }, []);

  const handleViewInvoice = useCallback(
    (invoice: Invoice) => {
      const uuid = invoice.fiscalUuid ?? invoice.invoiceUuid;
      if (!uuid) return;
      const end = formatLocalDateStr(new Date());
      const start = new Date();
      start.setMonth(start.getMonth() - 6);
      navigate(
        `/fiscal/facturas?uuid=${encodeURIComponent(uuid)}&start=${formatLocalDateStr(start)}&end=${end}`
      );
    },
    [navigate]
  );

  const handleFilePDFChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setPdfFile(e.target.files?.[0] ?? null);
  };

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    setDataMsg("");
    setCreditNoteData(null);
    setIsValidating(true);
    setOperationResult(null);
    setPublished(false);

    const selectedFile = e.target.files?.[0] ?? null;
    if (!selectedFile) {
      setXmlFile(null);
      setIsValidating(false);
      return;
    }

    if (selectedFile.size > MAX_BYTES) {
      showAlert(`El archivo no debe exceder ${MAX_MB} MB.`);
      resetFileInputs();
      setXmlFile(null);
      setIsValidating(false);
      return;
    }

    if (!selectedFile.name.toLowerCase().endsWith(".xml")) {
      showAlert("El archivo XML es requerido y debe tener extensión .xml.");
      resetFileInputs();
      setXmlFile(null);
      setIsValidating(false);
      return;
    }

    setXmlFile(selectedFile);

    try {
      const data = await publishClient.validateXml(selectedFile);
      const parsed = parseValidatedXml(data);
      if (!parsed) {
        showAlert("No fue posible leer la información del XML.");
        setIsValidCreditNote(false);
        return;
      }

      setCreditNoteData(parsed);

      if (parsed.tipoDeComprobante !== "E") {
        setIsValidCreditNote(false);
        setDataMsg("");
        showAlert(
          "El archivo XML no corresponde a una nota de crédito válida. Por favor, valida el documento antes de continuar."
        );
        return;
      }

      const root = data as Record<string, unknown>;
      const metadatos = root.metadatos as Record<string, unknown> | undefined;
      const xmlValidationOk = metadatos?.estado === "SUCCESS";
      setIsValidCreditNote(xmlValidationOk);

      if (xmlValidationOk) {
        setDataMsg(String(metadatos?.mensaje ?? "XML validado correctamente."));
        //setInvoice("49b6551d-59e0-4112-ac64-314752bac1db")
        setInvoice(parsed.timbreFiscalDigital.uuid);
      } else {
        setDataMsg("");
        showAlert(
          String(metadatos?.mensaje ?? "El archivo XML no es válido.")
        );
      }
    } catch (error: unknown) {
      showAlert(getErrorMessage(error, "No fue posible validar el archivo XML."));
      setIsValidCreditNote(false);
    } finally {
      setIsValidating(false);
    }
  };

  const handlePublish = useCallback(async () => {
    if (!xmlFile || !traceId) return;

    if (!creditNoteData?.monto) {
      showAlert(
        "Primero selecciona y valida el XML; cuando aparezca el resumen podrás guardar."
      );
      return;
    }

    if (creditNoteData.tipoDeComprobante !== "E") {
      showAlert(
        "El archivo XML no corresponde a una nota de crédito válida. Por favor, valida el documento antes de continuar."
      );
      return;
    }

    setIsUploading(true);
    setOperationResult(null);

    try {
      const formData = new FormData();
      formData.append("file", xmlFile);
      if (pdfFile) formData.append("pdfFile", pdfFile);
      formData.append("idTransaccion", traceId);
      formData.append("documentNumber", query.documentNumber);
      formData.append("supplierNumber", query.supplierNumber);
      formData.append("type", "2");

      const response = await publishClient.publishCreditNote(formData);
      const normalized = normalizePublishCreditNoteResponse(response);
      const outcome = resolvePublishCreditNoteOutcome(normalized);
      const logText = normalized.displayText || `Transacción ${traceId}`;

      if (outcome === "success") {
        setOperationResult({ severity: "success" });
        addLog(logText, "CREDIT_NOTE", "PUBLISH_CREDIT_NOTE", "INFO", response);
        if (normalized.creditNoteUuid) {
          localStorage.setItem("lastCreditNoteUuid", normalized.creditNoteUuid);
        }
        setPublished(true);
        setDataMsg("Tu nota de crédito se procesó correctamente");
        return;
      }

      if (outcome === "warning") {
        setOperationResult({ severity: "warning" });
        addLog(logText, "CREDIT_NOTE", "PUBLISH_CREDIT_NOTE", "WARN", response);
        setPublished(false);
        return;
      }

      setOperationResult({
        severity: "error",
        backendText:
          normalized.displayText || "Error al publicar la nota de crédito",
      });
      setPublished(false);
      addLog(logText, "CREDIT_NOTE", "PUBLISH_CREDIT_NOTE", "ERROR", response);
    } catch (error: unknown) {
      const backendText = getErrorMessage(
        error,
        "Error al publicar la nota de crédito"
      );
      setOperationResult({ severity: "error", backendText });
      setPublished(false);
      addLog(backendText, "CREDIT_NOTE", "PUBLISH_CREDIT_NOTE", "ERROR", error);
    } finally {
      setIsUploading(false);
    }
  }, [
    xmlFile,
    pdfFile,
    traceId,
    creditNoteData,
    publishClient,
    addLog,
    showAlert,
  ]);

  return (
    <div>
      <BitacoraErrorModal
        visible={operationResult?.severity === "error" && !!traceId}
        traceId={traceId}
        message={
          operationResult?.backendText || "Error al publicar la nota de crédito"
        }
        onClose={() => setOperationResult(null)}
      />

      {isUploading && (
        <GenericModal visible variant="loading" message="Procesando…" />
      )}

      <div className="pcn-page-header">
        <div className="pcn-page-header-main">
          <h1>Publicar Nota de Crédito</h1>
          <p>Carga y publica la nota de crédito al sistema</p>
        </div>
        <div className="pcn-page-header-actions">
          {headerActions}
          <GenericButton
            variant="primary"
            disabled={!canPublish}
            onClick={() => handlePublish()}
          >
            Guardar
          </GenericButton>
          <GenericButton
            variant="outlineFill"
            disabled={isUploading}
            onClick={handleClearForm}
          >
            Limpiar
          </GenericButton>
        </div>
      </div>

      {noTraceWarning}

      <div className="pcn-control">
       

        {isUploading ? <GenericLinearProgress /> : null}

        <div className="pcn-layout">
          <div className="pcn-form">
            <h2 className="pcn-section-title">Subir Nota de Crédito</h2>

            <div className="pcn-upload-grid">
              {isValidating ? (
                <GenericLinearProgress />
              ) : (
                <label className="pcn-upload-label">
                  <input
                    ref={xmlInputRef}
                    type="file"
                    accept=".xml"
                    className="pcn-file-input"
                    onChange={(event) => { handleFileChange(event); }}
                    disabled={!hasTraceId || published}
                  />
                  <p className="pcn-upload-text">
                    Subir XML de la nota de crédito (Requerido)
                  </p>
                  {xmlFile && (
                    <p className="pcn-upload-file">{xmlFile.name}</p>
                  )}
                </label>
              )}

              <label className="pcn-upload-label">
                <input
                  ref={pdfInputRef}
                  type="file"
                  accept=".pdf"
                  className="pcn-file-input"
                  onChange={handleFilePDFChange}
                  disabled={!hasTraceId || published}
                />
                <p className="pcn-upload-text">
                  Subir PDF de la nota de crédito (opcional)
                </p>
                {pdfFile && <p className="pcn-upload-file">{pdfFile.name}</p>}
              </label>
            </div>

            {dataMsg.trim() !== "" ? (
              <p
                className={`pcn-notice pcn-notice--${isValidCreditNote ? "success" : "error"}`}
                role="status"
                aria-live="polite"
              >
                {dataMsg}
              </p>
            ) : null}
          </div>

          {creditNoteData && (
            <div className="pcn-summary-wrap">
              <div className="pcn-summary">
                <table className="pcn-summary-table">
                  <tbody>
                    <tr>
                      <td className="pcn-cell pcn-cell-label">RFC Emisor:</td>
                      <td className="pcn-cell">{creditNoteData.rfcEmisor}</td>
                    </tr>
                    <tr>
                      <td className="pcn-cell pcn-cell-label">
                        Nombre Proveedor:
                      </td>
                      <td className="pcn-cell">{creditNoteData.nombreProveedor}</td>
                    </tr>
                    <tr>
                      <td className="pcn-cell pcn-cell-label">Serie:</td>
                      <td className="pcn-cell">{creditNoteData.serie}</td>
                    </tr>
                    <tr>
                      <td className="pcn-cell pcn-cell-label">Folio:</td>
                      <td className="pcn-cell">{creditNoteData.folio}</td>
                    </tr>
                    <tr>
                      <td className="pcn-cell pcn-cell-label">Importe:</td>
                      <td className="pcn-cell">
                        {formatAmount(parseFloat(creditNoteData.monto))}
                      </td>
                    </tr>
                    <tr>
                      <td className="pcn-cell pcn-cell-label">
                        Fecha Timbrado:
                      </td>
                      <td className="pcn-cell">
                        {formatDate(creditNoteData.fechaTimbrado, true)}
                      </td>
                    </tr>
                    <tr>
                      <td className="pcn-cell pcn-cell-label">Uso CFDI:</td>
                      <td className="pcn-cell">{creditNoteData.usoCfdi}</td>
                    </tr>
                    <tr>
                      <td className="pcn-cell pcn-cell-label">
                        Tipo Comprobante:
                      </td>
                      <td className="pcn-cell">
                        {creditNoteData.tipoDeComprobante}
                      </td>
                    </tr>
                    <tr>
                      <td className="pcn-cell pcn-cell-label">UUID Factura:</td>
                      <td className="pcn-cell">{creditNoteData.timbreFiscalDigital.uuid}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
       {
        relatedInvoice && (
          <div>
            <h2 className="pcn-section-title">Datos de la factura relacionada</h2>
            <RelatedInvoiceGrid
              invoice={relatedInvoice}
              loading={loadingInvoice}
              onViewInvoice={handleViewInvoice}
            />
          </div>
        )
       }
      </div>

      {operationResult && traceId && operationResult.severity !== "error" && (
        <div className="fiscal-mt-4">
          {operationResult.severity === "success" && (
            <div className="fiscal-alert-success">
              <img src={iconSuccess} className="fiscal-alert-success-icon" alt="" />
              <span className="fiscal-alert-success-text">
                La nota de crédito se registró correctamente. ID de transacción:{" "}
                {traceIdLink}.
              </span>
            </div>
          )}

          {operationResult.severity === "warning" && (
            <div className="fiscal-alert-warning">
              <img src={iconWarning} className="fiscal-alert-warning-icon" alt="" />
              <span className="fiscal-alert-warning-text">
                No fue posible publicar la nota de crédito, revisar el detalle en
                la siguiente transacción {traceIdLink}.
              </span>
            </div>
          )}
        </div>
      )}

      <GenericModal
        visible={alertVisible}
        variant="alert"
        severity="warning"
        title="Atención"
        message={alertMessage}
        buttonText="Aceptar"
        onClose={() => setAlertVisible(false)}
      />
    </div>
  );
}
