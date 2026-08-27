import { useState, useCallback, useMemo, useEffect, useRef } from "react";
import type { TraceFolioPayload } from "@/services/TraceabilityClient";
import { getUserIdFromStore } from "@/utils/getUserIdFromStore";
import { useLocation } from "react-router-dom";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { GenericButton, GenericModal } from "@/shared/components/ui";
import { APP_EVENT, PermissionGate } from "@shared/security";
import { GenericLinearProgress } from "@/shared/components/ui/progress";
import { createComplementPaymentClient } from "./api/ComplementPaymentClient";
import { TraceFolioProvider, useTraceFolio } from "@/hooks/TraceFolioProvider";
import {
  EMPTY_HEADER,
  PaymentHeaderData,
  ProviderCatalogItem,
  QueryPaymentData,
  XmlComplementPreview,
} from "./interfaces";
import { parseComplementXml } from "./utils/helpers";
import { fetchProvidersAsCatalog, getErrorMessage, toCurrency } from "@/utils/utils";
import { ModalMsg } from "@/shared/components/ui/modal/ModalMsg";
import BitacoraErrorModal from "@/shared/components/ui/modal/BitacoraErrorModal";
import "../creditNote/PublishCreditNote.css";
import "../creditNote/parts/DiscountInfoGrid.css";

const MAX_MB = 10;
const MAX_BYTES = MAX_MB * 1024 * 1024;

const PAYMENT_FIELDS: Array<{
  key: keyof PaymentHeaderData;
  label: string;
  formatter?: (value: string) => string;
}> = [
  { key: "idProveedor", label: "Número Proveedor" },
  { key: "nombreProveedor", label: "Nombre Proveedor" },
  { key: "rfcProveedor", label: "RFC Proveedor" },
  { key: "anioPagos", label: "Año Pago" },
  { key: "moneda", label: "Moneda" },
  { key: "monto", label: "Monto", formatter: toCurrency },
  { key: "status", label: "Estatus" },
  { key: "fechaRegistro", label: "Fecha Registro" },
];

const XML_SUMMARY_ROWS: Array<{ key: keyof XmlComplementPreview; label: string }> = [
  
  { key: "rfcEmisor", label: "RFC Emisor:" },
  { key: "nombreEmisor", label: "Nombre Emisor:" },
  { key: "serie", label: "Serie:" },
  { key: "folio", label: "Folio:" },
  { key: "uuid", label: "UUID:" },
  { key: "monto", label: "Monto:" },
  { key: "tipoComprobante", label: "Tipo Comprobante:" },
  { key: "formaDePagoP", label: "Forma de Pago:" },
  { key: "fechaPago", label: "Fecha Pago:" },
  { key: "fechaTimbrado", label: "Fecha Timbrado:" }
];

function parsePaymentQuery(search: string): QueryPaymentData | null {
  if (!search) return null;

  const params = new URLSearchParams(search);
  const data: QueryPaymentData = {
    referenciaPago: params.get("ref") ?? "",
    idProveedor: params.get("provider") ?? "",
    moneda: params.get("currency") ?? "",
    monto: params.get("amount") ?? "",
    fechaRegistro: params.get("paymentDate") ?? "",
    anioPagos: params.get("year") ?? "",
    uuid: params.get("uuid") ?? "",
    status: params.get("status") ?? "",
  };

  return Object.values(data).some(Boolean) ? data : null;
}

function getXmlFileError(file: File): string | null {
  if (file.size > MAX_BYTES) return `El archivo no debe exceder ${MAX_MB} MB.`;
  if (!file.name.trim().toLowerCase().endsWith(".xml")) {
    return "El tipo de archivo no es el correcto, debes subir un xml válido.";
  }
  return null;
}

function getPdfFileError(file: File): string | null {
  if (file.size > MAX_BYTES) return `El archivo no debe exceder ${MAX_MB} MB.`;
  if (!file.name.trim().toLowerCase().endsWith(".pdf")) {
    return "El tipo de archivo no es el correcto, debes subir un pdf válido.";
  }
  return null;
}

const BREADCRUMB: BreadcrumbItem[] = [
  { label: "Fiscal", to: "/" },
  { label: "Consulta complemento pago", to: "/fiscal/consulta-complemento-pago" },
  { label: "Añadir complemento de pago" },
];

export default function AddComplement() {
  const traceFolioPayload = useMemo<TraceFolioPayload>(
    () => ({
      idAplicativo: "fiscal-front",
      idModulo: "COMPLEMENT",
      paso: "INIT_UPLOAD_XML",
      detalle: "Inicio de trazabilidad en pantalla Añadir complemento de pago (fiscal-front).",
      fechaHora: new Date().toISOString(),
      tipoEvento: "INFO",
      idUsuario: getUserIdFromStore() ?? "1",
    }),
    []
  );
  return decorate(
    BREADCRUMB,
    "/fiscal/consulta-complemento-pago",
    <TraceFolioProvider traceFolioPayload={traceFolioPayload}>
      <AddComplementContent />
    </TraceFolioProvider>
  );
}

function AddComplementContent() {
  const location = useLocation();
  const xmlInputRef = useRef<HTMLInputElement>(null);
  const pdfInputRef = useRef<HTMLInputElement>(null);
  const [providers, setProviders] = useState<ProviderCatalogItem[]>([]);

  useEffect(() => {
    async function loadProviders() {
      const providerOptions = (await fetchProvidersAsCatalog(
        "supplierNumber",
        true
      )) as ProviderCatalogItem[] | null;
      if (providerOptions) {
        setProviders(providerOptions);
      }
    }
    loadProviders();
  }, []);

  const paymentFromQuery = useMemo<PaymentHeaderData | null>(() => {
    const queryData = parsePaymentQuery(location.search);
    if (!queryData) return null;
    const provider = providers.find((p) => p.idProveedor === queryData.idProveedor);
    return {
      ...EMPTY_HEADER,
      nombreProveedor: provider?.businessName ?? EMPTY_HEADER.nombreProveedor,
      rfcProveedor: provider?.rfc ?? EMPTY_HEADER.rfcProveedor,
      referenciaPago: queryData.referenciaPago ?? EMPTY_HEADER.referenciaPago,
      idProveedor: queryData.idProveedor ?? EMPTY_HEADER.idProveedor,
      moneda: queryData.moneda ?? EMPTY_HEADER.moneda,
      monto: queryData.monto ?? EMPTY_HEADER.monto,
      fechaRegistro: queryData.fechaRegistro ?? EMPTY_HEADER.fechaRegistro,
      anioPagos: queryData.anioPagos ?? EMPTY_HEADER.anioPagos,
      uuid: queryData.uuid ?? EMPTY_HEADER.uuid,
      status: queryData.status ?? EMPTY_HEADER.status,
    };
  }, [location.search, providers]);

  const payment = (location.state as { payment?: PaymentHeaderData } | null)?.payment ?? null;
  const header: PaymentHeaderData = payment ?? paymentFromQuery ?? EMPTY_HEADER;
  const client = useMemo(() => createComplementPaymentClient(), []);

  const [xmlFile, setXmlFile] = useState<File | null>(null);
  const [pdfFile, setPdfFile] = useState<File | null>(null);
  const [xmlPreview, setXmlPreview] = useState<XmlComplementPreview | null>(null);
  const [isValidComplement, setIsValidComplement] = useState(false);
  const [published, setPublished] = useState(false);
  const [resultMessage, setResultMessage] = useState("");
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [alertVisible, setAlertVisible] = useState(false);
  const [alertMessage, setAlertMessage] = useState("");

  const selectedProvider = useMemo(
    () => providers.find((provider) => provider.idProveedor === header.idProveedor),
    [providers, header.idProveedor]
  );

  const selectedRfc = useMemo(() => {
    const fromHeader = header.rfcProveedor?.trim();
    if (fromHeader && fromHeader !== "--") return fromHeader.toUpperCase();
    const fromProvider = selectedProvider?.rfc?.trim();
    return fromProvider ? fromProvider.toUpperCase() : "";
  }, [header.rfcProveedor, selectedProvider]);

  const { traceId, addLog, headerActions, noTraceWarning, traceFooter, traceLoading } =
    useTraceFolio();
  const hasTraceId = Boolean(traceId);
  const uploadsLocked = published || !hasTraceId || traceLoading || isUploading;
  const canPublish =
    hasTraceId && Boolean(xmlFile) && isValidComplement && !isUploading && !published;
  const canRelate = hasTraceId && published && !isUploading;

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
    setXmlPreview(null);
    setIsValidComplement(false);
    setPublished(false);
    setResultMessage("");
    setErrorMsg(null);
    setAlertVisible(false);
    setAlertMessage("");
  }, []);

  const validateComplementXml = useCallback(
    (parsed: XmlComplementPreview | null): boolean => {
      if (!parsed) {
        showAlert("No fue posible leer la información del XML.");
        return false;
      }

      if (parsed.tipoComprobante.trim().toUpperCase() !== "P") {
        showAlert(
          "El archivo XML no corresponde a un complemento de pago válido. Por favor, valida el documento antes de continuar."
        );
        return false;
      }

      const xmlRfc = parsed.rfcEmisor.trim().toUpperCase();
      if (selectedRfc && xmlRfc && xmlRfc !== selectedRfc) {
        showAlert(
          "El RFC del proveedor no coincide con el complemento de pago. Por favor, valida el archivo XML."
        );
        return false;
      }

      return true;
    },
    [selectedRfc, showAlert]
  );

  const handleXmlChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      if (published) return;
      const selectedFile = e.target.files?.[0] ?? null;
      if (!selectedFile) {
        setXmlFile(null);
        setXmlPreview(null);
        setIsValidComplement(false);
        return;
      }

      const fileError = getXmlFileError(selectedFile);
      if (fileError) {
        showAlert(fileError);
        e.target.value = "";
        resetFileInputs();
        setXmlFile(null);
        setXmlPreview(null);
        setIsValidComplement(false);
        return;
      }

      setXmlFile(selectedFile);
      setXmlPreview(null);
      setIsValidComplement(false);
      setErrorMsg(null);
      setPublished(false);


      const reader = new FileReader();
      reader.onload = () => {
        const text = String(reader.result ?? "");
        const parsed = parseComplementXml(text);
        setXmlPreview(parsed);
        setIsValidComplement(validateComplementXml(parsed));
      };
      reader.readAsText(selectedFile);

      if (traceId) {
        const userId = client.getUser() ?? "unknown";
        addLog(
          `Usuario ${userId} sube el archivo ${selectedFile.name} de tipo XML en la pantalla complemento de pago`,
          "COMPLEMENT",
          "UPLOAD_XML",
          "INFO",
          {
            userId,
            fileName: selectedFile.name,
            fileType: selectedFile.type,
            fileSize: selectedFile.size,
          }
        );
      }
    },
    [published, showAlert, traceId, client, addLog, validateComplementXml]
  );

  const handlePdfChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      if (published) return;
      const selectedFile = e.target.files?.[0] ?? null;
      if (!selectedFile) {
        setPdfFile(null);
        return;
      }

      const fileError = getPdfFileError(selectedFile);
      if (fileError) {
        showAlert(fileError);
        e.target.value = "";
        setPdfFile(null);
        return;
      }

      setPdfFile(selectedFile);
      setErrorMsg(null);
      setPublished(false);
    },
    [published, showAlert]
  );

  const handlePublish = useCallback(async () => {
    if (!xmlFile || !traceId || !xmlPreview || published) return;

    if (xmlPreview.tipoComprobante.trim().toUpperCase() !== "P") {
      showAlert(
        "El archivo XML no corresponde a un complemento de pago válido. Por favor, valida el documento antes de continuar."
      );
      return;
    }

    const xmlRfc = xmlPreview.rfcEmisor.trim().toUpperCase();
    if (selectedRfc && xmlRfc && xmlRfc !== selectedRfc) {
      showAlert(
        "El RFC del proveedor no coincide con el complemento de pago. Por favor, valida el archivo XML."
      );
      return;
    }

    setIsUploading(true);
    setErrorMsg(null);
    try {
      const formData = new FormData();
      formData.append("idProveedor", header.idProveedor !== "--" ? header.idProveedor : "");
      formData.append("tipoProveedor", selectedProvider?.id ?? "");
      formData.append("idUsuario", client.getUser() + "");
      formData.append("tipoAddenda", "5");
      formData.append("idTransaccion", traceId);
      formData.append("xmlFile", xmlFile);
      if (pdfFile) formData.append("pdf", pdfFile);
      const response = await client.publishComplement(formData);
      const msg =
        (response as { message?: string })?.message ??
        "Complemento de pago publicado correctamente.";
      setResultMessage(msg);
      addLog(msg, "COMPLEMENT", "PUBLISH_COMPLEMENT", "INFO", response);
      setPublished(true);
    } catch (error: unknown) {
      const detail = getErrorMessage(error, "Error al subir el complemento de pago");
      setErrorMsg(detail);
      addLog(detail, "COMPLEMENT", "PUBLISH_COMPLEMENT", "ERROR", error);
    } finally {
      setIsUploading(false);
    }
  }, [
    xmlFile,
    pdfFile,
    xmlPreview,
    published,
    client,
    selectedProvider,
    selectedRfc,
    traceId,
    addLog,
    header.idProveedor,
    showAlert,
  ]);

  const handleRelate = useCallback(async () => {
    const idPago = payment?.referenciaPago ?? header.referenciaPago;
    if (idPago === "--" || !idPago) {
      setErrorMsg("No hay pago seleccionado para relacionar.");
      return;
    }
    if (!traceId) return;
    setIsUploading(true);
    setErrorMsg(null);
    try {
      const relationDetail = `Se relaciona la nota de crédito ${traceId} con el pago ${idPago}`;
      const relationResponse = await client.relateComplement(traceId, idPago);
      addLog(relationDetail, "COMPLEMENT", "RELATE_PAYMENT", "INFO", relationResponse);
      setResultMessage("Complemento relacionado con el pago correctamente.");
    } catch (error: unknown) {
      const detail = getErrorMessage(error, "Error al subir el complemento de pago");
      setErrorMsg(detail);
      addLog(detail, "COMPLEMENT", "RELATE_PAYMENT", "ERROR", error);
    } finally {
      setIsUploading(false);
    }
  }, [payment, header.referenciaPago, client, traceId, addLog]);

  const uploadLabelClass = () =>
    `pcn-upload-label${uploadsLocked ? " pcn-upload-label--disabled" : ""}`;

  const displayOrDash = (value: string) => {
    const trimmed = value?.trim();
    return trimmed && trimmed !== "--" ? trimmed : "--";
  };

  return (
    <div>
      <BitacoraErrorModal
        visible={!!errorMsg}
        traceId={traceId}
        message={errorMsg ?? ""}
        onClose={() => setErrorMsg(null)}
      />
      <ModalMsg
        severity="success"
        visible={!!resultMessage}
        msg={resultMessage ?? ""}
        onClose={() => setResultMessage("")}
      />
      {(isUploading || traceLoading) && (
        <GenericModal
          visible
          variant="loading"
          message={isUploading ? "Procesando complemento de pago…" : "Cargando información…"}
        />
      )}
      <GenericModal
        visible={alertVisible && !published}
        variant="alert"
        severity="warning"
        title="Atención"
        message={alertMessage}
        buttonText="Aceptar"
        onClose={() => setAlertVisible(false)}
      />

      <div className="pcn-page-header">
        <div className="pcn-page-header-main">
          <h1>Publicar Complemento de Pago</h1>
          <p>Carga y publica el complemento de pago al sistema</p>
        </div>
        <div className="pcn-page-header-actions">
          {headerActions}
          <PermissionGate appEvent={APP_EVENT.PAYMENT_COMPLEMENTS.PUBLISH}>
            <GenericButton
              variant="primary"
              disabled={!canPublish}
              onClick={() => {
                handlePublish();
              }}
            >
              {isUploading && !published ? "Publicando…" : "Publicar"}
            </GenericButton>
          </PermissionGate>
         {
          /*
           <PermissionGate appEvent={APP_EVENT.PAYMENT_COMPLEMENTS.PUBLISH}>
            <GenericButton
              variant="outlineFill"
              disabled={!canRelate}
              onClick={() => {
                handleRelate();
              }}
            >
              Relacionar
            </GenericButton>
          </PermissionGate>
          */
         }
          <GenericButton
            variant="outlineFill"
            disabled={isUploading || published}
            onClick={handleClearForm}
          >
            Limpiar
          </GenericButton>
        </div>
      </div>

      {noTraceWarning}

      {hasTraceId ? (
        <div className="pcn-control">
          <div className="pcn-discount-header-card">
            <div className="pcn-discount-header-top">
              <span className="pcn-discount-section-title">Datos del pago</span>
            </div>
            <div className="pcn-discount-summary-grid">
              {PAYMENT_FIELDS.map(({ key, label, formatter }) => (
                <div key={key} className="pcn-discount-summary-item">
                  <div className="pcn-discount-label">{label}</div>
                  <div>
                    {formatter
                      ? formatter(header[key])
                      : displayOrDash(header[key])}
                  </div>
                </div>
              ))}
            </div>
          </div>

          {isUploading ? <GenericLinearProgress /> : null}

          <div className="pcn-layout">
            <div className="pcn-form">
              <h2 className="pcn-section-title">Subir Complemento de Pago</h2>

              <div className="pcn-upload-grid">
                <label className={uploadLabelClass()}>
                  <input
                    ref={xmlInputRef}
                    type="file"
                    accept=".xml"
                    className="pcn-file-input"
                    onChange={handleXmlChange}
                    disabled={uploadsLocked}
                  />
                  <p className="pcn-upload-text">
                    Subir XML del complemento de pago (Requerido)
                  </p>
                  {xmlFile && <p className="pcn-upload-file">{xmlFile.name}</p>}
                </label>

                <label className={uploadLabelClass()}>
                  <input
                    ref={pdfInputRef}
                    type="file"
                    accept=".pdf"
                    className="pcn-file-input"
                    onChange={handlePdfChange}
                    disabled={uploadsLocked}
                  />
                  <p className="pcn-upload-text">
                    Subir PDF del complemento de pago (Opcional)
                  </p>
                  {pdfFile && <p className="pcn-upload-file">{pdfFile.name}</p>}
                </label>
              </div>

              {published && resultMessage.trim() !== "" ? (
                <p className="pcn-notice pcn-notice--success" role="status" aria-live="polite">
                  {resultMessage}
                </p>
              ) : null}
            </div>

            {xmlPreview ? (
              <div className="pcn-summary-wrap">
                <div className="pcn-summary">
                  <table className="pcn-summary-table">
                    <tbody>
                      {XML_SUMMARY_ROWS.map((row) => (
                        <tr key={row.key}>
                          <th scope="row" className="pcn-cell pcn-cell-label">
                            {row.label}
                          </th>
                          <td className="pcn-cell">{xmlPreview[row.key] ?? "--"}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            ) : null}
          </div>
        </div>
      ) : (
        traceFooter
      )}
    </div>
  );
}
