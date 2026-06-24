import { useState, useCallback, useMemo, useEffect } from "react";
import type { TraceFolioPayload } from "@/services/TraceabilityClient";
import { getUserIdFromStore } from "@/utils/getUserIdFromStore";
import { useLocation } from "react-router-dom";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { Divider, Title } from "@/shared/components/ui/misc";
import { GenericDropzone, GenericButton, GenericModal } from "@/shared/components/ui";
import { createComplementPaymentClient } from "./api/ComplementPaymentClient";
import { TraceFolioProvider, useTraceFolio } from "@/hooks/TraceFolioProvider";
import { EMPTY_HEADER, PaymentHeaderData, ProviderCatalogItem, QueryPaymentData, XmlComplementPreview } from "./interfaces";
import { parseComplementXml } from "./utils/helpers";
import { fetchProvidersAsCatalog, formatBytes, getErrorMessage, isValidPdf, isValidXml, toCurrency } from "@/utils/utils";
import { ModalMsg } from "@/shared/components/ui/modal/ModalMsg";
import BitacoraErrorModal from "@/shared/components/ui/modal/BitacoraErrorModal";

//TODO: Obtener de api de configuración o constantes globales
const XML_ACCEPT = ".xml,application/xml,text/xml";
const PDF_ACCEPT = ".pdf,application/pdf";
const MAX_MB = 10;

const PAYMENT_LABELS: Array<{ key: keyof PaymentHeaderData; label: string; formatter?: (value: string) => string }> = [
  { key: "idProveedor", label: "Id Proveedor" },
  { key: "nombreProveedor", label: "Nombre Proveedor" },
  { key: "referenciaPago", label: "Referencia de pago" },
  { key: "anioPagos", label: "Año Pagos" },
  { key: "moneda", label: "Moneda" },
  { key: "monto", label: "Monto", formatter: toCurrency },
  { key: "estatus", label: "Estatus" },
  { key: "fechaRegistro", label: "Fecha de registro" },
];

const XML_PREVIEW_LABELS: Array<{ key: keyof XmlComplementPreview; label: string }> = [
  { key: "uuid", label: "UUID" },
  { key: "rfcEmisor", label: "RFC Emisor" },
  { key: "nombreEmisor", label: "Nombre Emisor" },
  { key: "monto", label: "Monto" },
  { key: "fechaTimbrado", label: "Fecha de timbrado" },
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
  };

  return Object.values(data).some(Boolean) ? data : null;
}



const BREADCRUMB: BreadcrumbItem[] = [
  { label: "Fiscal", to: "/" },
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
    "/",
    <TraceFolioProvider traceFolioPayload={traceFolioPayload}>
      <AddComplementContent />
    </TraceFolioProvider>
  );
}

function AddComplementContent() {
  const location = useLocation();
  const [providers, setProviders] = useState<ProviderCatalogItem[]>([]);

  useEffect(() => {
    async function loadProviders() {
      const providerOptions = (await fetchProvidersAsCatalog("supplierNumber", true)) as ProviderCatalogItem[] | null;
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
      nombreProveedor: provider?.businessName || EMPTY_HEADER.nombreProveedor,
      referenciaPago: queryData.referenciaPago || EMPTY_HEADER.referenciaPago,
      idProveedor: queryData.idProveedor || EMPTY_HEADER.idProveedor,
      moneda: queryData.moneda || EMPTY_HEADER.moneda,
      monto: queryData.monto || EMPTY_HEADER.monto,
      fechaRegistro: queryData.fechaRegistro || EMPTY_HEADER.fechaRegistro,
      anioPagos: queryData.anioPagos || EMPTY_HEADER.anioPagos,
    };
  }, [location.search, providers]);

  const payment = (location.state as { payment?: PaymentHeaderData } | null)?.payment ?? null;
  const header: PaymentHeaderData = payment ?? paymentFromQuery ?? EMPTY_HEADER;
  const client = useMemo(() => createComplementPaymentClient(), []);

  const [xmlFile, setXmlFile] = useState<File | null>(null);
  const [pdfFile, setPdfFile] = useState<File | null>(null);
  const [xmlPreview, setXmlPreview] = useState<XmlComplementPreview | null>(null);
  const [published, setPublished] = useState(false);
  const [resultMessage, setResultMessage] = useState<string>("");
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [isUploading, setIsUploading] = useState(false);

  const selectedProvider = useMemo(
    () => providers.find((provider) => provider.idProveedor === header.idProveedor),
    [providers, header.idProveedor],
  );

  const handleXmlSelect = useCallback((file: File | null) => {
    if (file && !isValidXml(file)) {
      setErrorMsg("El archivo debe ser XML (extensión .xml y tipo MIME application/xml o text/xml).");
      return;
    }
    setXmlFile(file);
    setXmlPreview(null);
    setErrorMsg(null);
    if (file) {
      const reader = new FileReader();
      reader.onload = () => {
        const text = String(reader.result ?? "");
        const parsed = parseComplementXml(text);
        setXmlPreview(parsed);
      };
      reader.readAsText(file);
    }
  }, []);

  const handlePdfSelect = useCallback((file: File | null) => {
    if (file && !isValidPdf(file)) {
      setErrorMsg("El archivo debe ser PDF (extensión .pdf y tipo MIME application/pdf).");
      return;
    }
    setPdfFile(file);
    setErrorMsg(null);
  }, []);

  const handleRemoveXml = useCallback(() => {
    setXmlFile(null);
    setXmlPreview(null);
    setErrorMsg(null);
    setPublished(false);
  }, []);

  const handleRemovePdf = useCallback(() => {
    setPdfFile(null);
    setErrorMsg(null);
    setPublished(false);
  }, []);

  const { traceId, addLog, headerActions, noTraceWarning, traceFooter } = useTraceFolio();
  const hasTraceId = Boolean(traceId);
  const canPublish = hasTraceId && Boolean(xmlFile) && !isUploading && !published;
  const canRelate = hasTraceId && published && !isUploading;

  const handleXmlSelectWithTrace = useCallback(
    (file: File | null) => {
      handleXmlSelect(file);
      if (file && traceId) {
        const userId = client.getUser() ?? "unknown";
        addLog(
          `Usuario ${userId} sube el archivo ${file.name} de tipo XML en la pantalla complemento de pago`,
          "COMPLEMENT",
          "UPLOAD_XML",
          "INFO",
          { userId, fileName: file.name, fileType: file.type, fileSize: file.size }
        );
      }
    },
    [traceId, addLog, client, handleXmlSelect]
  );

  const handlePublish = useCallback(async () => {
    if (!xmlFile || !traceId) return;
    setIsUploading(true);
    setErrorMsg(null);
    try {
      const formData = new FormData();
      formData.append("idProveedor", header.idProveedor !== "--" ? header.idProveedor : "");
      formData.append("tipoProveedor", selectedProvider?.id || "");
      formData.append("idUsuario", client.getUser() + "");
      formData.append("tipoAddenda", "5");
      formData.append("idTransaccion", traceId);
      formData.append("xmlFile", xmlFile);
      if (pdfFile) formData.append("pdf", pdfFile);
      const response = await client.publishComplement(formData);
      const msg = (response as { message?: string })?.message ?? "Complemento de pago publicado correctamente.";
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
  }, [xmlFile, pdfFile, client, selectedProvider, traceId, addLog, header.idProveedor]);

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

  return (
    <div>
      <BitacoraErrorModal
        visible={!!errorMsg}
        traceId={traceId}
        message={errorMsg || ""}
        onClose={() => setErrorMsg(null)}
      />
      <ModalMsg severity="success" visible={!!resultMessage} msg={resultMessage || ""} onClose={() => setResultMessage("")} />
      {isUploading && (
        <GenericModal
          visible
          variant="loading"
          message="Procesando complemento de pago..."
        />
      )}
      <Title
        title="Nuevo complemento de pago"
        description="Relaciona un pago con el complemento de pago."
        actions={headerActions}
      />
      
      <section className="fiscal-mb-4">
        <h5 className="fiscal-mb-2">Datos del pago</h5>
        <div className="fiscal-row fiscal-gap fiscal-mb-4">
          {PAYMENT_LABELS.map(({ key, label, formatter }) => (
            <div key={key} className="fiscal-col-6">
              <span className="fiscal-font-medium">{label}:</span> {formatter ? formatter(header[key]) : header[key]}
            </div>
          ))}
        </div>
      </section>
      <Divider />
      {noTraceWarning}
      {
        hasTraceId ? (
          <div>
            <section className="fiscal-mb-4">
        <h5 className="fiscal-mb-2">Carga de archivos</h5>
        <div className="fiscal-row fiscal-gap">
          <div className="fiscal-col-6">
            <p className="fiscal-block fiscal-font-medium fiscal-mb-2" id="complement-xml-label">
              Complemento XML (obligatorio)
            </p>
            {!xmlFile ? (
              <GenericDropzone
                file={xmlFile}
                onFileSelect={handleXmlSelectWithTrace}
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
                <GenericButton
                  type="button"
                  onClick={handleRemoveXml}
                  variant="outline"
                >
                  Cambiar o eliminar archivo
                </GenericButton>
                {xmlPreview && (
                  <div className="fiscal-mt-4">
                    <div className="fiscal-row fiscal-gap">
                      {XML_PREVIEW_LABELS.map(({ key, label }) => (
                        <div key={key} className="fiscal-col-6">
                          <span className="fiscal-font-medium">{label}:</span> {xmlPreview[key] || "--"}
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
          <div className="fiscal-col-6">
            <p className="fiscal-block fiscal-font-medium fiscal-mb-2" id="complement-pdf-label">
              PDF (opcional)
            </p>
            {!pdfFile ? (
              <GenericDropzone
                file={pdfFile}
                onFileSelect={handlePdfSelect}
                accept={PDF_ACCEPT}
                maxSizeMb={MAX_MB}
                fileInfoPosition="below"
              />
            ) : (
              <div className="fiscal-p-4 fiscal-bg-gray fiscal-rounded">
                <h6 className="fiscal-font-medium fiscal-mb-2">Archivo PDF seleccionado</h6>
                <p className="fiscal-mb-2">
                  <span className="fiscal-font-medium">Nombre:</span> {pdfFile.name}
                </p>
                <p className="fiscal-mb-2">
                  <span className="fiscal-font-medium">Tamaño:</span> {formatBytes(pdfFile.size)}
                </p>
                <GenericButton type="button" onClick={handleRemovePdf} variant="outline">
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
          <GenericButton onClick={() => { handlePublish(); }} disabled={!canPublish}>
            {isUploading && !published ? "Publicando…" : "Publicar complemento"}
          </GenericButton>
          <GenericButton variant="outline" onClick={() => { handleRelate(); }} disabled={!canRelate}>
            Relacionar complemento
          </GenericButton>
        </div>
      </section>
          </div>
        ): traceFooter
      }
    </div>
  );
}
