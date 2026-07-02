import { useState, useCallback, useMemo, useRef, useEffect } from "react";
import type { TraceFolioPayload } from "@/services/TraceabilityClient";
import { getUserIdFromStore } from "@/utils/getUserIdFromStore";
import { useLocation, useNavigate } from "react-router-dom";
import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { GenericButton, GenericModal } from "@/shared/components/ui";
import { APP_EVENT, PermissionGate } from "@shared/security";
import { GenericLinearProgress } from "@/shared/components/ui/progress";
import { TraceFolioProvider, useTraceFolio } from "@/hooks/TraceFolioProvider";
import { fetchSystemParameters, formatLocalDateStr, getErrorMessage, SystemParameter } from "@/utils/utils";
import "@/shared/components/ui/alerts/Alerts.css";
import type { Invoice } from "../invoice/interfaces";
import { createCreditNotePublishClient } from "./api/CreditNotePublishClient";
import { BREADCRUMB, MAX_BYTES, MAX_MB } from "./parts/constants";
import { parsePublishQuery, isCommercialDiscountFlow } from "./parts/publishQuery";
import { parseValidatedXml, getXmlValidationMessage } from "./parts/parseValidatedXml";
import { buildPublishFormData } from "./parts/buildPublishFormData";
import { useRelatedInvoice } from "./parts/useRelatedInvoice";
import RelatedInvoiceGrid from "./parts/RelatedInvoiceGrid";
import CreditNoteSummary from "./parts/CreditNoteSummary";
import PublishResultNotice from "./parts/PublishResultNotice";
import { buildFinishModal, isPublishSuccessful } from "./parts/publishResult";
import type { CreditNoteXmlData, FinishModalState } from "./parts/types";
import "./PublishCreditNote.css";

const PARAM_OPTIONAL_PDF_PAYMENT_COMPLEMENT = 12;
const PARAM_OPTIONAL_PDF_CREDIT_NOTE = 11;

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

function PublishCreditNoteContent() {
  const location = useLocation();
  const navigate = useNavigate();
  const publishClient = useMemo(() => createCreditNotePublishClient(), []);
  const query = useMemo(() => parsePublishQuery(location.search), [location.search]);
  const isDiscountFlow = isCommercialDiscountFlow(query);

  const xmlInputRef = useRef<HTMLInputElement>(null);
  const pdfInputRef = useRef<HTMLInputElement>(null);

  const [relatedInvoiceUuid, setRelatedInvoiceUuid] = useState("");
  const { invoice: relatedInvoice, loading: loadingInvoice } =
    useRelatedInvoice(relatedInvoiceUuid);

  const [xmlFile, setXmlFile] = useState<File | null>(null);
  const [pdfFile, setPdfFile] = useState<File | null>(null);
  const [creditNoteData, setCreditNoteData] = useState<CreditNoteXmlData | null>(null);
  const [isValidating, setIsValidating] = useState(false);
  const [isValidCreditNote, setIsValidCreditNote] = useState(false);
  const [dataMsg, setDataMsg] = useState("");
  const [isFinished, setIsFinished] = useState(false);
  const [registeredFiscalUuid, setRegisteredFiscalUuid] = useState("");
  const [finishModal, setFinishModal] = useState<FinishModalState | null>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [alertVisible, setAlertVisible] = useState(false);
  const [alertMessage, setAlertMessage] = useState("");

  const { traceId, addLog, headerActions, noTraceWarning } = useTraceFolio();
  const hasTraceId = Boolean(traceId);
  const uploadsLocked = isFinished || !hasTraceId;

  const [systemParameters, setSystemParameters] = useState<SystemParameter[] | null>(null);
  const [optionalPdf, setOptionalPdf] = useState({ value: "0", isEnabled: false });


  const checkSystemParameterValue = (parameterId: number): { value: string; isEnabled: boolean } => {
    const parameter = systemParameters?.find((p) => p.idParameter === parameterId);
    if (!parameter) {
        return { value: "", isEnabled: false };
    }

    return {
        value: String(parameter.value),
        isEnabled: parameter.status == "1",
    };
};

  useEffect(() => {
    const fetchSystemParametersData = async () => {
      const response = await fetchSystemParameters();
      setSystemParameters(response?.data ?? null);
    };
    fetchSystemParametersData();
  }, []);

  useEffect(() => {
    setOptionalPdf(checkSystemParameterValue((isDiscountFlow ? PARAM_OPTIONAL_PDF_PAYMENT_COMPLEMENT : PARAM_OPTIONAL_PDF_CREDIT_NOTE)));
  }, [systemParameters, isDiscountFlow]);

  const canPublish =
    hasTraceId &&
    Boolean(xmlFile) &&
    Boolean(creditNoteData) &&
    isValidCreditNote &&
    !isUploading &&
    !isFinished &&
    (isDiscountFlow ? Boolean(query.supplierNumber.trim()) : Boolean(relatedInvoice));

  const showAlert = useCallback((message: string) => {
    setAlertMessage(message);
    setAlertVisible(true);
  }, []);

  const resetFileInputs = () => {
    if (xmlInputRef.current) xmlInputRef.current.value = "";
    if (pdfInputRef.current) pdfInputRef.current.value = "";
  };

  const handleClearForm = useCallback(() => {
    if (isFinished) return;
    resetFileInputs();
    setXmlFile(null);
    setPdfFile(null);
    setCreditNoteData(null);
    setRelatedInvoiceUuid("");
    setIsValidCreditNote(false);
    setDataMsg("");
    setIsValidating(false);
  }, [isFinished]);

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

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (isFinished) return;

    setDataMsg("");
    setCreditNoteData(null);
    setRelatedInvoiceUuid("");
    setIsValidating(true);

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

      const validation = getXmlValidationMessage(data);
      setIsValidCreditNote(validation.ok);

      if (validation.ok) {
        setDataMsg(validation.message);
        setRelatedInvoiceUuid(parsed.uuidRelacionado);
        if (!parsed.uuidRelacionado.trim()) {
          showAlert("No se encontró la factura relacionada, verifique el archivo de la nota de crédito.");
        }
      } else {
        setDataMsg("");
        showAlert(validation.message);
      }
    } catch (error: unknown) {
      showAlert(getErrorMessage(error, "No fue posible validar el archivo XML."));
      setIsValidCreditNote(false);
    } finally {
      setIsValidating(false);
    }
  };

  const handlePublish = useCallback(async () => {
    if (!xmlFile || !traceId || !creditNoteData || isFinished) return;
    if (!optionalPdf.isEnabled) {
      showAlert("El archivo PDF es requerido para publicar la nota de crédito.");
      return;
    }

    if (creditNoteData.tipoDeComprobante !== "E") {
      showAlert(
        "El archivo XML no corresponde a una nota de crédito válida. Por favor, valida el documento antes de continuar."
      );
      return;
    }

    setIsUploading(true);

    try {
      const formData = buildPublishFormData({
        xmlFile,
        pdfFile,
        traceId,
        query,
        relatedInvoice,
      });

      const response = await publishClient.publishCreditNote(formData);
      setIsFinished(true);

      if (isPublishSuccessful(response)) {
        const fiscalUuid = String(response.fiscalUuid).trim();
        setRegisteredFiscalUuid(fiscalUuid);
        setFinishModal(buildFinishModal(response));
        const logLevel =
          (response.warnings ?? []).some((w) => String(w).trim()) ? "WARN" : "INFO";
        addLog(
          response.message || `Transacción ${traceId}`,
          "CREDIT_NOTE",
          "PUBLISH_CREDIT_NOTE",
          logLevel,
          response
        );
        return;
      }

      setFinishModal(buildFinishModal(response));
      addLog(
        response.message || `Transacción ${traceId}`,
        "CREDIT_NOTE",
        "PUBLISH_CREDIT_NOTE",
        "ERROR",
        response
      );
    } catch (error: unknown) {
      setIsFinished(true);
      const message = getErrorMessage(error, "Error al publicar la nota de crédito");
      setFinishModal({ severity: "error", title: "Error", message });
      addLog(message, "CREDIT_NOTE", "PUBLISH_CREDIT_NOTE", "ERROR", error);
    } finally {
      setIsUploading(false);
    }
  }, [
    xmlFile,
    pdfFile,
    traceId,
    creditNoteData,
    isFinished,
    query,
    relatedInvoice,
    publishClient,
    addLog,
    showAlert,
  ]);

  const uploadLabelClass = (extraDisabled = false) =>
    `pcn-upload-label${uploadsLocked || extraDisabled ? " pcn-upload-label--disabled" : ""}`;

  return (
    <div>
      {isUploading && <GenericModal visible variant="loading" message="Procesando…" />}

      <GenericModal
        visible={!!finishModal}
        variant="alert"
        severity={finishModal?.severity ?? "info"}
        title={finishModal?.title ?? ""}
        message={finishModal?.message ?? ""}
        buttonText="Aceptar"
        onClose={() => setFinishModal(null)}
        onConfirm={() => setFinishModal(null)}
      />

      <div className="pcn-page-header">
        <div className="pcn-page-header-main">
          <h1>Publicar Nota de Crédito</h1>
          <p>Carga y publica la nota de crédito al sistema</p>
        </div>
        <div className="pcn-page-header-actions">
          {headerActions}
          <PermissionGate appEvent={APP_EVENT.CREDIT_NOTES.PUBLISH}>
            <GenericButton
              variant="primary"
              disabled={!canPublish}
              onClick={() => { handlePublish(); }}
            >
              Guardar
            </GenericButton>
          </PermissionGate>
          <PermissionGate appEvent={APP_EVENT.CREDIT_NOTES.CLEAR_FILTERS}>
            <GenericButton variant="outlineFill" disabled={isUploading || isFinished} onClick={handleClearForm}>
              Limpiar
            </GenericButton>
          </PermissionGate>
        </div>
      </div>

      {noTraceWarning}

      <div className="pcn-control">
        {relatedInvoiceUuid && (
          <div>
            <h2 className="pcn-section-title">Datos de la factura relacionada</h2>
            <RelatedInvoiceGrid
              invoice={relatedInvoice}
              loading={loadingInvoice}
              onViewInvoice={handleViewInvoice}
            />
          </div>
        )}

        {isUploading ? <GenericLinearProgress /> : null}

        <div className="pcn-layout">
          <div className="pcn-form">
            <h2 className="pcn-section-title">Subir Nota de Crédito</h2>

            <div className="pcn-upload-grid">
              {isValidating ? (
                <GenericLinearProgress />
              ) : (
                <label className={uploadLabelClass()}>
                  <input
                    ref={xmlInputRef}
                    type="file"
                    accept=".xml"
                    className="pcn-file-input"
                    onChange={(event) => { handleFileChange(event); }}
                    disabled={uploadsLocked}
                  />
                  <p className="pcn-upload-text">Subir XML de la nota de crédito (Requerido)</p>
                  {xmlFile && <p className="pcn-upload-file">{xmlFile.name}</p>}
                </label>
              )}

              <label className={uploadLabelClass()}>
                <input
                  ref={pdfInputRef}
                  type="file"
                  accept=".pdf"
                  className="pcn-file-input"
                  onChange={(e) => {
                    if (isFinished) return;
                    setPdfFile(e.target.files?.[0] ?? null);
                  }}
                  disabled={uploadsLocked}
                />
                <p className="pcn-upload-text">Subir PDF de la nota de crédito ({optionalPdf.isEnabled ? "Opcional" : "Requerido"})</p>
                {pdfFile && <p className="pcn-upload-file">{pdfFile.name}</p>}
              </label>
            </div>

            {registeredFiscalUuid ? (
              <PublishResultNotice fiscalUuid={registeredFiscalUuid} />
            ) : dataMsg.trim() !== "" ? (
              <p
                className={`pcn-notice pcn-notice--${isValidCreditNote ? "success" : "error"}`}
                role="status"
                aria-live="polite"
              >
                {dataMsg}
              </p>
            ) : null}
          </div>

          {creditNoteData && <CreditNoteSummary data={creditNoteData} />}
        </div>
      </div>

      <GenericModal
        visible={alertVisible && !isFinished}
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
