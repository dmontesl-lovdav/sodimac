import { useState, useCallback, useMemo, useRef, useEffect } from "react";
import type { TraceFolioPayload } from "@/services/TraceabilityClient";
import { getUserIdFromStore } from "@/utils/getUserIdFromStore";
import { useLocation } from "react-router-dom";
import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { GenericButton, GenericModal } from "@/shared/components/ui";
import { APP_EVENT, PermissionGate } from "@shared/security";
import { GenericLinearProgress } from "@/shared/components/ui/progress";
import { TraceFolioProvider, useTraceFolio } from "@/hooks/TraceFolioProvider";
import { fetchSystemParameters, formatLocalDateStr, getErrorMessage, buildFiscalSpaUrl, SystemParameter, fetchProvidersAsCatalog } from "@/utils/utils";
import type { Invoice } from "../invoice/interfaces";
import { createCreditNotePublishClient } from "./api/CreditNotePublishClient";
import { BREADCRUMB, MAX_BYTES, MAX_MB } from "./parts/constants";
import { parsePublishQuery, isCommercialDiscountFlow } from "./parts/publishQuery";
import { parseValidatedXml } from "./parts/parseValidatedXml";
import { resolveXmlValidationCommand } from "./utils/resolveXmlValidationCommand";
import { buildPublishFormData } from "./parts/buildPublishFormData";
import { useRelatedInvoice } from "./parts/useRelatedInvoice";
import RelatedInvoiceGrid from "./parts/RelatedInvoiceGrid";
import DiscountInfoGrid from "./parts/DiscountInfoGrid";
import CreditNoteSummary from "./parts/CreditNoteSummary";
import PublishResultNotice from "./parts/PublishResultNotice";
import { buildFinishModal, isPublishSuccessful } from "./parts/publishResult";
import type { CreditNoteXmlData, FinishModalState, PublishQuery } from "./parts/types";
import "./PublishCreditNote.css";

const PARAM_OPTIONAL_PDF_PAYMENT_COMPLEMENT = 12;
const PARAM_OPTIONAL_PDF_CREDIT_NOTE = 11;

export function checkSystemParameterValue(
  systemParameters: SystemParameter[] | null,
  parameterId: number
): { value: string; isEnabled: boolean } {
  const parameter = systemParameters?.find((p) => p.idParameter === parameterId);
  if (!parameter) return { value: "", isEnabled: false };
  return { value: String(parameter.value), isEnabled: parameter.status == "1" };
}

export function getXmlFileError(file: File, maxBytes: number, maxMb: number): string | null {
  if (file.size > maxBytes) return `El archivo no debe exceder ${maxMb} MB.`;
  if (!file.name.trim().toLowerCase().endsWith(".xml")) {
    return "El tipo de archivo no es el correcto, debes subir un xml válido.";
  }
  return null;
}

export function getPdfFileError(file: File, maxBytes: number, maxMb: number): string | null {
  if (file.size > maxBytes) return `El archivo no debe exceder ${maxMb} MB.`;
  if (!file.name.trim().toLowerCase().endsWith(".pdf")) {
    return "El tipo de archivo no es el correcto, debes subir un pdf válido.";
  }
  return null;
}

export function resolveLoadingMessage(isUploading: boolean, isValidating: boolean, loadingInvoice: boolean): string {
  if (isUploading) return "Procesando nota de crédito…";
  if (isValidating) return "Validando nota de crédito…";
  if (loadingInvoice) return "Cargando factura relacionada…";
  return "Cargando información…";
}


export default function PublishCreditNote() {
  const location = useLocation();
  const FBC_URL = process.env.FBC_HOME;
  const query = useMemo(() => parsePublishQuery(location.search), [location.search]);
  const isDiscountFlow = isCommercialDiscountFlow(query);
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
    isDiscountFlow ? `${FBC_URL}finanzas#/finanzas/descuentos-comerciales` : "/fiscal/notas-credito",
    <TraceFolioProvider traceFolioPayload={traceFolioPayload}>
      <PublishCreditNoteContent />
    </TraceFolioProvider>
  );
}

function PublishCreditNoteContent() {
  const location = useLocation();
  const [discountInfo, setDiscountInfo] = useState<PublishQuery | null>(null);
  const publishClient = useMemo(() => createCreditNotePublishClient(), []);
  const query = useMemo(() => parsePublishQuery(location.search), [location.search]);
  const isDiscountFlow = isCommercialDiscountFlow(query);
  useEffect(() => {
    if (isDiscountFlow) {
      setDiscountInfo(query);
    }
  }, [isDiscountFlow, query]);

  const xmlInputRef = useRef<HTMLInputElement>(null);
  const pdfInputRef = useRef<HTMLInputElement>(null);
  const formSessionRef = useRef(0);

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
  const [publishFailed, setPublishFailed] = useState(false);
  const [registeredFiscalUuid, setRegisteredFiscalUuid] = useState("");
  const [finishModal, setFinishModal] = useState<FinishModalState | null>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [alertVisible, setAlertVisible] = useState(false);
  const [alertMessage, setAlertMessage] = useState("");
  const [systemParameters, setSystemParameters] = useState<SystemParameter[] | null>(null);
  const [paramsLoading, setParamsLoading] = useState(true);
  const [optionalPdf, setOptionalPdf] = useState({ value: "0", isEnabled: false });
  const [providers, setProviders] = useState<any>(null);
  const [providersLoading, setProvidersLoading] = useState(true);

  const { traceId, addLog, headerActions, noTraceWarning, traceLoading } = useTraceFolio();
  const hasTraceId = Boolean(traceId);
  const isPageLoading = traceLoading || paramsLoading || providersLoading;
  const uploadsLocked = isFinished || !hasTraceId || isPageLoading;

  useEffect(() => {
    const fetchProvidersList = async () => {
      try {
        const providerCatalog = await fetchProvidersAsCatalog("rfc", true);
        setProviders(providerCatalog);
      } finally {
        setProvidersLoading(false);
      }
    };
    fetchProvidersList();
  }, []);

  useEffect(() => {
    fetchSystemParameters()
      .then((response) => setSystemParameters(response?.data ?? null))
      .finally(() => setParamsLoading(false));
  }, []);

  useEffect(() => {
    const paramId = isDiscountFlow ? PARAM_OPTIONAL_PDF_PAYMENT_COMPLEMENT : PARAM_OPTIONAL_PDF_CREDIT_NOTE;
    setOptionalPdf(checkSystemParameterValue(systemParameters, paramId));
  }, [systemParameters, isDiscountFlow]);

  const supplierMatchesDiscount =
    !isDiscountFlow ||
    (Boolean(creditNoteData) &&
      query.supplierNumber.trim() === (creditNoteData?.numeroProveedor ?? "").trim());

  const canPublish =
    hasTraceId &&
    !isPageLoading &&
    Boolean(xmlFile) &&
    Boolean(creditNoteData) &&
    isValidCreditNote &&
    !isUploading &&
    !isFinished &&
    !publishFailed &&
    supplierMatchesDiscount &&
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
    formSessionRef.current += 1;
    resetFileInputs();
    setXmlFile(null);
    setPdfFile(null);
    setCreditNoteData(null);
    setRelatedInvoiceUuid("");
    setIsValidCreditNote(false);
    setDataMsg("");
    setIsValidating(false);
    setRegisteredFiscalUuid("");
    setIsFinished(false);
    setPublishFailed(false);
    setAlertVisible(false);
    setAlertMessage("");
    setFinishModal(null);
  }, []);

  const handleViewInvoice = useCallback((invoice: Invoice) => {
    const uuid = invoice.fiscalUuid ?? invoice.invoiceUuid;
    if (!uuid) return;
    const end = formatLocalDateStr(new Date(invoice.createdAt ?? ""));
    const start = new Date(invoice.createdAt ?? "");
    const url = buildFiscalSpaUrl(`facturas?uuid=${encodeURIComponent(uuid)}&start=${formatLocalDateStr(start)}&end=${end}`);
    window.open(url, "_blank", "noopener,noreferrer");
  }, []);

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (isFinished) return;

    const session = formSessionRef.current;
    const isActive = () => session === formSessionRef.current;

    setDataMsg("");
    setCreditNoteData(null);
    setRelatedInvoiceUuid("");
    setIsValidating(true);

    const selectedFile = e.target.files?.[0] ?? null;
    if (!selectedFile) {
      if (isActive()) { setXmlFile(null); setIsValidating(false); }
      return;
    }

    const fileError = getXmlFileError(selectedFile, MAX_BYTES, MAX_MB);
    if (fileError) {
      showAlert(fileError);
      resetFileInputs();
      if (isActive()) { setXmlFile(null); setIsValidating(false); }
      return;
    }

    if (isActive()) setXmlFile(selectedFile);

    try {
      const data = await publishClient.validateXml(selectedFile);

      if (!isActive()) return;

      const parsed = parseValidatedXml(data, providers);
      if (!parsed) {
        showAlert("No fue posible leer la información del XML.");
        setIsValidCreditNote(false);
        return;
      }
      setCreditNoteData(parsed);
      const cmd = resolveXmlValidationCommand(data, parsed);
      setIsValidCreditNote(cmd.isValid);
      setDataMsg(cmd.dataMsg);
      if(!isDiscountFlow){
        setRelatedInvoiceUuid(cmd.relatedInvoiceUuid);
      }

      if (
        isDiscountFlow &&
        query.supplierNumber.trim() !== parsed.numeroProveedor.trim()
      ) {
        showAlert(
          "El RFC del proveedor no coincide con el descuento comercial. Por favor, valida el archivo XML."
        );
      } else if (cmd.alert) {
        showAlert(cmd.alert);
      }
    } catch (error: unknown) {
      if (!isActive()) return;
      showAlert(getErrorMessage(error, "No fue posible validar el archivo XML."));
      setIsValidCreditNote(false);
    } finally {
      if (isActive()) setIsValidating(false);
    }
  };

  const handlePublish = useCallback(async () => {
    if (!xmlFile || !traceId || !creditNoteData || isFinished || publishFailed) return;
    if (
      isDiscountFlow &&
      query.supplierNumber.trim() !== creditNoteData.numeroProveedor.trim()
    ) {
      showAlert(
        "El RFC del proveedor no coincide con el descuento comercial. Por favor, valida el archivo XML."
      );
      return;
    }
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
    setPublishFailed(false);

    try {
      const formData = buildPublishFormData({
        xmlFile,
        pdfFile,
        traceId,
        query,
        relatedInvoice,
        
      });

      const response = await publishClient.publishCreditNote(formData);

      if (isPublishSuccessful(response)) {
        setIsFinished(true);
        const fiscalUuid = String(response.fiscalUuid).trim();
        setRegisteredFiscalUuid(fiscalUuid);
        setFinishModal(buildFinishModal(response));
        const logLevel =
          (response.warnings ?? []).some((w) => String(w).trim()) ? "WARN" : "INFO";
        addLog(
          response.message ?? `Transacción ${traceId}`,
          "CREDIT_NOTE",
          "PUBLISH_CREDIT_NOTE",
          logLevel,
          response
        );

        const rebateId = query.rebateId.trim();
        if (isDiscountFlow && rebateId) {
          try {
            await publishClient.updateRebateStatus(rebateId, 2);
          } catch (rebateError: unknown) {
            showAlert(
              getErrorMessage(
                rebateError,
                "La nota de crédito se publicó, pero no se pudo actualizar el estatus del descuento comercial."
              )
            );
          }
        }
        return;
      }

      setFinishModal(buildFinishModal(response));
      setPublishFailed(true);
      addLog(
        response.message ?? `Transacción ${traceId}`,
        "CREDIT_NOTE",
        "PUBLISH_CREDIT_NOTE",
        "ERROR",
        response
      );
    } catch (error: unknown) {
      setPublishFailed(true);
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
    publishFailed,
    isDiscountFlow,
    query,
    relatedInvoice,
    publishClient,
    addLog,
    showAlert,
  ]);

  const uploadLabelClass = (extraDisabled = false) =>
    `pcn-upload-label${uploadsLocked || extraDisabled ? " pcn-upload-label--disabled" : ""}`;

  const showLoadingModal = isPageLoading || isValidating || loadingInvoice || isUploading;
  const loadingMessage = resolveLoadingMessage(isUploading, isValidating, loadingInvoice);

  return (
    <div>
      {showLoadingModal && (
        <GenericModal visible variant="loading" message={loadingMessage} />
      )}

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
        {discountInfo && providers && <DiscountInfoGrid discount={discountInfo} providers={providers}/>}

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
                    const selectedFile = e.target.files?.[0] ?? null;
                    if (!selectedFile) {
                      setPdfFile(null);
                      return;
                    }
                    const fileError = getPdfFileError(selectedFile, MAX_BYTES, MAX_MB);
                    if (fileError) {
                      showAlert(fileError);
                      e.target.value = "";
                      setPdfFile(null);
                      return;
                    }
                    setPdfFile(selectedFile);
                  }}
                  disabled={uploadsLocked}
                />
                <p className="pcn-upload-text">Subir PDF de la nota de crédito ({optionalPdf.isEnabled ? "Opcional" : "Requerido"})</p>
                {pdfFile && <p className="pcn-upload-file">{pdfFile.name}</p>}
              </label>
            </div>

            {registeredFiscalUuid
              ? <PublishResultNotice fiscalUuid={registeredFiscalUuid} />
              : null}
            {!registeredFiscalUuid && dataMsg.trim() !== "" ? (
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
