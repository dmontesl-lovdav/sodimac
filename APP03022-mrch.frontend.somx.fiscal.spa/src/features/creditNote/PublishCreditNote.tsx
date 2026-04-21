import { useState, useCallback, useMemo, useEffect } from "react";
import { Link, useLocation } from "react-router-dom";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { Divider, Title } from "@/shared/components/ui/misc";
import { GenericDropzone, GenericButton, GenericModal } from "@/shared/components/ui";
import { TraceFolioProvider, useTraceFolio } from "@/hooks/TraceFolioProvider";
import { fetchProvidersAsCatalog, formatAmount, formatBytes, formatDate, getErrorMessage, isValidXml } from "@/utils/utils";
import "@/shared/components/ui/alerts/Alerts.css";
import iconSuccess from "@assets/icons/alert-up.png";
import iconWarning from "@assets/icons/warning.png";
import { createCreditNotePublishClient } from "./api/CreditNotePublishClient";
import BitacoraErrorModal from "@/shared/components/ui/modal/BitacoraErrorModal";
import {
  normalizePublishCreditNoteResponse,
  resolvePublishCreditNoteOutcome,
} from "./utils/publishCreditNoteResponse";

const XML_ACCEPT = ".xml,application/xml,text/xml";
const MAX_MB = 10;
const MAX_BYTES = MAX_MB * 1024 * 1024;

type CreditNoteHeaderData = {
  idProveedor: string;
  nombreProveedor: string;
  numeroDocumento: string;
  moneda: string;
  monto: string;
  fechaRegistro: string;
};

const EMPTY_HEADER: CreditNoteHeaderData = {
  idProveedor: "--",
  nombreProveedor: "--",
  numeroDocumento: "--",
  moneda: "--",
  monto: "--",
  fechaRegistro: "--",
};

const BREADCRUMB: BreadcrumbItem[] = [
  { label: "Home", to: "/" },
  { label: "Fiscal", to: "/" },
  { label: "Publicar Nota de Crédito" },
];

function parseCreditNoteQuery(search: string): Partial<CreditNoteHeaderData> | null {
  if (!search) return null;



  const params = new URLSearchParams(search);
  const data: Partial<CreditNoteHeaderData> = {
    idProveedor: params.get("supplierNumber") ?? "",
    numeroDocumento: params.get("documentNumber") ?? "",
    moneda: params.get("currency") ?? "MXN",
    monto: params.get("amount") ?? "",
    fechaRegistro: params.get("postingDate") ?? "",
  };

  return Object.values(data).some(Boolean) ? data : null;
}

export default function PublishCreditNote() {
  const traceFolioPayload = {
    codigoModulo: "FIS",
    pantallaOrigen: "Publicar Nota de Crédito",
    caso: "POST",
    metadatos: {},
    idUsuario: "1",
    origen: "fiscal",
  };
  return decorate(
    BREADCRUMB,
    "/",
    <TraceFolioProvider traceFolioPayload={traceFolioPayload}>
      <PublishCreditNoteContent />
    </TraceFolioProvider>,
  );
}

type OperationSeverity = "success" | "warning" | "error";
type OperationResult = { severity: OperationSeverity; backendText?: string };

function PublishCreditNoteContent() {
  const location = useLocation();
  const client = createCreditNotePublishClient();
  const hasQueryStrings = new URLSearchParams(location.search).toString().length > 0;

  const [providers, setProviders] = useState<any[]>([]);
  useEffect(() => {
    const loadProviders = async () => {
      const providerOptions = (await fetchProvidersAsCatalog("supplierNumber", true)) as any[] | null;
      if (providerOptions) setProviders(providerOptions);
    };
    loadProviders();
  }, []);

  const headerFromQuery = useMemo<CreditNoteHeaderData | null>(() => {
    const queryData = parseCreditNoteQuery(location.search);
    if (!queryData) return null;
    const provider = providers.find((p) => p.idProveedor === queryData.idProveedor);
    return {
      ...EMPTY_HEADER,
      idProveedor: queryData.idProveedor || EMPTY_HEADER.idProveedor,
      nombreProveedor: provider?.businessName || EMPTY_HEADER.nombreProveedor,
      numeroDocumento: queryData.numeroDocumento || EMPTY_HEADER.numeroDocumento,
      moneda: queryData.moneda || EMPTY_HEADER.moneda,
      monto: queryData.monto || EMPTY_HEADER.monto,
      fechaRegistro: queryData.fechaRegistro || EMPTY_HEADER.fechaRegistro,
    };
  }, [location.search, providers]);

  const headerFromState = (location.state as { creditNoteHeader?: CreditNoteHeaderData } | null)?.creditNoteHeader ?? null;
  const header: CreditNoteHeaderData = headerFromState ?? headerFromQuery ?? EMPTY_HEADER;
  const hasHeaderData = Object.values(header).some((value) => {
    const normalized = String(value ?? "").trim();
    return normalized !== "" && normalized !== "--";
  });
  const showBasicData = hasQueryStrings && hasHeaderData;

  const hasProvider = header.nombreProveedor.trim() !== "" && header.nombreProveedor !== "--";
  const hasDocument = header.numeroDocumento.trim() !== "" && header.numeroDocumento !== "--";
  const hasCurrency = header.moneda.trim() !== "" && header.moneda !== "--";
  const numericAmount = Number(header.monto);
  const hasAmount = header.monto.trim() !== "" && header.monto !== "--" && Number.isFinite(numericAmount);
  const hasDate =
    header.fechaRegistro.trim() !== "" &&
    header.fechaRegistro !== "--" &&
    !Number.isNaN(Date.parse(header.fechaRegistro));

  const selectedProvider = useMemo(() => {
    return providers.find((provider) => provider.idProveedor === header.idProveedor);
  }, [providers, header.idProveedor]);

  const [xmlFile, setXmlFile] = useState<File | null>(null);
  const [operationResult, setOperationResult] = useState<OperationResult | null>(null);
  const [published, setPublished] = useState(false);
  const [isUploading, setIsUploading] = useState(false);

  const { traceId, addLog, headerActions, traceFooter } = useTraceFolio();
  const hasTraceId = Boolean(traceId);
  const canPublish = hasTraceId && Boolean(xmlFile) && !isUploading && !published;

  const FINANZAS_URL = process.env.FINANZAS_URL ?? "";
  const traceIdLink = traceId ? <Link to={`${FINANZAS_URL}/#/auditoria/bitacora-actividades/tren/${traceId}`}>{traceId}</Link> : null;

  const handleXmlSelect = useCallback((file: File | null) => {
    setOperationResult(null);

    if (!file) {
      setXmlFile(null);
      setPublished(false);
      return;
    }

    if (file.size > MAX_BYTES) {
      setOperationResult({
        severity: "error",
        backendText: `El archivo no debe exceder ${MAX_MB} MB.`,
      });
      setXmlFile(null);
      return;
    }

    if (!isValidXml(file)) {
      setOperationResult({
        severity: "error",
        backendText: "El archivo debe ser XML (extensión .xml y tipo MIME application/xml o text/xml).",
      });
      setXmlFile(null);
      return;
    }

    setXmlFile(file);
    setPublished(false);
  }, []);

  const handleRemoveXml = useCallback(() => {
    setXmlFile(null);
    setOperationResult(null);
    setPublished(false);
  }, []);

  const handlePublish = useCallback(async () => {
    if (!xmlFile) return;
    if (!traceId) return;

    setIsUploading(true);
    setOperationResult(null);

    try {
      const formData = new FormData();

      //if (selectedProvider?.tipoProveedor?.id) formData.append("idProveedor", selectedProvider.tipoProveedor.id);
      //if (selectedProvider?.id) formData.append("tipoProveedor", selectedProvider.id);
      //formData.append("idUsuario", client.getUser() + "");
      formData.append("idTransaccion", traceId);
      /*if (header.numeroDocumento && header.numeroDocumento !== "--") {
        formData.append("numeroDocumento", header.numeroDocumento);
      }*/
      formData.append("xmlFile", xmlFile);

      const response = await client.publishCreditNote(formData);
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
        backendText: normalized.displayText || "Error al publicar la nota de crédito",
      });
      setPublished(false);
      addLog(logText, "CREDIT_NOTE", "PUBLISH_CREDIT_NOTE", "ERROR", response);
    } catch (error: unknown) {
      const backendText = getErrorMessage(error, "Error al publicar la nota de crédito");
      setOperationResult({
        severity: "error",
        backendText,
      });
      setPublished(false);
      addLog(backendText, "CREDIT_NOTE", "PUBLISH_CREDIT_NOTE", "ERROR", error);
    } finally {
      setIsUploading(false);
    }
  }, [xmlFile, traceId, selectedProvider, client, header.numeroDocumento, addLog]);

  return (
    <div>
      <BitacoraErrorModal
        visible={operationResult?.severity === "error" && !!traceId}
        traceId={traceId}
        message={operationResult?.backendText || "Error al publicar la nota de crédito"}
        onClose={() => setOperationResult(null)}
      />

      {isUploading && (
        <GenericModal visible variant="loading" message="Procesando…" />
      )}



      <Title
        title="Publicar Nota de Crédito"
        description="Carga y publica la nota de crédito al sistema"
        actions={headerActions}
      />

      {showBasicData && (
        <>
          <section className="fiscal-mb-4">
            <h5 className="fiscal-mb-2">Datos básicos</h5>
            <div className="fiscal-row fiscal-gap fiscal-mb-4">
              {hasProvider && (
                <div className="fiscal-col-6">
                  <span className="fiscal-font-medium">Proveedor:</span> {header.nombreProveedor}
                </div>
              )}
              {hasDocument && (
                <div className="fiscal-col-6">
                  <span className="fiscal-font-medium">Número documento:</span> {header.numeroDocumento}
                </div>
              )}
              {hasCurrency && (
                <div className="fiscal-col-6">
                  <span className="fiscal-font-medium">Moneda:</span> {header.moneda}
                </div>
              )}
              {hasAmount && (
                <div className="fiscal-col-6">
                  <span className="fiscal-font-medium">Importe:</span> {formatAmount(numericAmount)}
                </div>
              )}
              {hasDate && (
                <div className="fiscal-col-6">
                  <span className="fiscal-font-medium">Fecha de registro:</span> {formatDate(header.fechaRegistro)}
                </div>
              )}
            </div>
          </section>

          <Divider />
        </>
      )}

      {hasTraceId && (
        <div>
          <section className="fiscal-mb-4">
            <h5 className="fiscal-mb-2">Carga de archivos</h5>
            <div className="fiscal-row fiscal-gap">
              <div className="fiscal-col-6">
                <label className="fiscal-block fiscal-font-medium fiscal-mb-2">Nota de crédito XML (obligatorio)</label>

                {!xmlFile ? (
                  <GenericDropzone
                    file={xmlFile}
                    onFileSelect={handleXmlSelect}
                    accept={XML_ACCEPT}
                    maxSizeMb={MAX_MB}
                    fileInfoPosition="below"
                  />
                ) : (
                  <div className="fiscal-p-4 fiscal-bg-gray fiscal-rounded">
                    <h6 className="fiscal-font-medium fiscal-mb-2">Archivo XML seleccionado</h6>
                    <p className="fiscal-mb-2">
                      <span className="fiscal-font-medium">Nombre:</span> {xmlFile.name}
                    </p>
                    <p className="fiscal-mb-2">
                      <span className="fiscal-font-medium">Tamaño:</span> {formatBytes(xmlFile.size)}
                    </p>
                    <GenericButton type="button" onClick={handleRemoveXml} variant="outline">
                      Cambiar o eliminar archivo
                    </GenericButton>
                  </div>
                )}
              </div>
            </div>
          </section>

          <Divider />

          <section className="fiscal-mb-4">
            <div className="fiscal-flex fiscal-gap-2 fiscal-flex-wrap">
              <GenericButton onClick={handlePublish} disabled={!canPublish}>
                {isUploading ? "Procesando…" : "Subir Nota de Crédito"}
              </GenericButton>
            </div>
          </section>
        </div>
      )}

      {traceFooter}

      {operationResult && traceId && operationResult.severity !== "error" && (
        <div className="fiscal-mt-4">
          {operationResult.severity === "success" && (
            <div className="fiscal-alert-success">
              <img src={iconSuccess} className="fiscal-alert-success-icon" alt="" />
              <span className="fiscal-alert-success-text">
                La nota de crédito se registró correctamente. ID de transacción: {traceIdLink}.
              </span>
            </div>
          )}

          {operationResult.severity === "warning" && (
            <div className="fiscal-alert-warning">
              <img src={iconWarning} className="fiscal-alert-warning-icon" alt="" />
              <span className="fiscal-alert-warning-text">
                No fue posible publicar la nota de crédito, revisar el detalle en la siguiente transacción {traceIdLink}.
              </span>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

