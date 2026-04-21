import { Breadcrumb, GenericModal } from "@shared/components/ui";
import type { ReactElement } from "react";
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

import { shippingGuideService } from "./api/ShippingGuideClient";
import type { ShippingGuide, ShippingGuideFilter } from "./interfaces";

import ShippingGuideFilterBar from "./components/ShippingGuideFilterBar";
import { ShippingGuideGrid } from "./components/ShippingGuideGrid";
import ShippingGuideToolbar from "./components/ShippingGuideToolbar";

import "./styles/shippingGuideContainer.css";

type ExportFormat = "csv" | "xml";

// --- util: CRC32 para ZIP sin dependencias ---
const makeCrc32Table = () => {
  let c;
  const table = [];
  for (let n = 0; n < 256; n++) {
    c = n;
    for (let k = 0; k < 8; k++) {
      c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
    }
    table[n] = c >>> 0;
  }
  return table;
};
const CRC32_TABLE = makeCrc32Table();
const crc32 = (str: string): number => {
  let crc = 0 ^ -1;
  for (let i = 0; i < str.length; i++) {
    crc = (crc >>> 8) ^ CRC32_TABLE[(crc ^ str.charCodeAt(i)) & 0xff];
  }
  return (crc ^ -1) >>> 0;
};

// --- util: generar ZIP en cliente (método STORE, sin compresión) ---
type ZipEntry = { name: string; content: string };
const buildZip = (entries: ZipEntry[]): Blob => {
  const encoder = new TextEncoder();
  const localFiles: Uint8Array[] = [];
  const centralDir: Uint8Array[] = [];
  let offset = 0;

  const pushLocalFile = (name: string, content: string) => {
    const data = encoder.encode(content);
    const nameBytes = encoder.encode(name);
    const crc = crc32(content);
    const localHeader = new DataView(new ArrayBuffer(30));
    let p = 0;
    localHeader.setUint32(p, 0x04034b50, true);
    p += 4;
    localHeader.setUint16(p, 20, true);
    p += 2;
    localHeader.setUint16(p, 0, true);
    p += 2;
    localHeader.setUint16(p, 0, true);
    p += 2;
    localHeader.setUint16(p, 0, true);
    p += 2;
    localHeader.setUint16(p, 0, true);
    p += 2;
    localHeader.setUint32(p, crc, true);
    p += 4;
    localHeader.setUint32(p, data.length, true);
    p += 4;
    localHeader.setUint32(p, data.length, true);
    p += 4;
    localHeader.setUint16(p, nameBytes.length, true);
    p += 2;
    localHeader.setUint16(p, 0, true);
    p += 2;

    const lf = new Uint8Array(localHeader.byteLength + nameBytes.length + data.length);
    lf.set(new Uint8Array(localHeader.buffer), 0);
    lf.set(nameBytes, localHeader.byteLength);
    lf.set(data, localHeader.byteLength + nameBytes.length);
    localFiles.push(lf);

    const cdh = new DataView(new ArrayBuffer(46));
    p = 0;
    cdh.setUint32(p, 0x02014b50, true);
    p += 4;
    cdh.setUint16(p, 20, true);
    p += 2;
    cdh.setUint16(p, 20, true);
    p += 2;
    cdh.setUint16(p, 0, true);
    p += 2;
    cdh.setUint16(p, 0, true);
    p += 2;
    cdh.setUint16(p, 0, true);
    p += 2;
    cdh.setUint16(p, 0, true);
    p += 2;
    cdh.setUint32(p, crc, true);
    p += 4;
    cdh.setUint32(p, data.length, true);
    p += 4;
    cdh.setUint32(p, data.length, true);
    p += 4;
    cdh.setUint16(p, nameBytes.length, true);
    p += 2;
    cdh.setUint16(p, 0, true);
    p += 2;
    cdh.setUint16(p, 0, true);
    p += 2;
    cdh.setUint16(p, 0, true);
    p += 2;
    cdh.setUint16(p, 0, true);
    p += 2;
    cdh.setUint32(p, 0, true);
    p += 4;
    cdh.setUint32(p, offset, true);
    p += 4;

    const cdf = new Uint8Array(cdh.byteLength + nameBytes.length);
    cdf.set(new Uint8Array(cdh.buffer), 0);
    cdf.set(nameBytes, cdh.byteLength);
    centralDir.push(cdf);

    offset += lf.byteLength;
  };

  entries.forEach((e) => pushLocalFile(e.name, e.content));

  const centralSize = centralDir.reduce((sum, b) => sum + b.byteLength, 0);
  const centralOffset = offset;
  const eocd = new DataView(new ArrayBuffer(22));
  let p = 0;
  eocd.setUint32(p, 0x06054b50, true);
  p += 4;
  eocd.setUint16(p, 0, true);
  p += 2;
  eocd.setUint16(p, 0, true);
  p += 2;
  eocd.setUint16(p, entries.length, true);
  p += 2;
  eocd.setUint16(p, entries.length, true);
  p += 2;
  eocd.setUint32(p, centralSize, true);
  p += 4;
  eocd.setUint32(p, centralOffset, true);
  p += 4;
  eocd.setUint16(p, 0, true);

  const size =
    localFiles.reduce((sum, b) => sum + b.byteLength, 0) +
    centralDir.reduce((sum, b) => sum + b.byteLength, 0) +
    eocd.byteLength;

  const out = new Uint8Array(size);
  let cursor = 0;
  localFiles.forEach((b) => {
    out.set(b, cursor);
    cursor += b.byteLength;
  });
  centralDir.forEach((b) => {
    out.set(b, cursor);
    cursor += b.byteLength;
  });
  out.set(new Uint8Array(eocd.buffer), cursor);

  return new Blob([out], { type: "application/zip" });
};

const formatZipName = (format: ExportFormat) => {
  const now = new Date();
  const pad = (n: number) => n.toString().padStart(2, "0");
  const yyyy = now.getFullYear();
  const MM = pad(now.getMonth() + 1);
  const dd = pad(now.getDate());
  const HH = pad(now.getHours());
  const mi = pad(now.getMinutes());
  return `Carta_Porte_${format.toUpperCase()}_${yyyy}${MM}${dd}_${HH}${mi}.zip`;
};

const getCatalogDisplay = (item?: { description?: string; value?: string; key?: string; internalStatus?: number } | null) => {
  if (!item) return "N/D";
  return item.description || item.value || item.key || (item.internalStatus != null ? String(item.internalStatus) : "N/D");
};

const getStatusCode = (guide: ShippingGuide) => Number(guide.status?.internalStatus ?? 0);

export default function ShippingGuideContainer(): ReactElement {
  const [loading, setLoading] = useState<boolean>(false);
  const [filters, setFilters] = useState<ShippingGuideFilter>();
  const [rows, setRows] = useState<ShippingGuide[]>([]);
  const [selectedGuides, setSelectedGuides] = useState<ShippingGuide[]>([]);

  const [errorModal, setErrorModal] = useState({
    visible: false,
    message: "",
  });

  const [cancelModalOpen, setCancelModalOpen] = useState<boolean>(false);
  const [cancelTargets, setCancelTargets] = useState<ShippingGuide[]>([]);
  const [cancelReason, setCancelReason] = useState<string>("");
  const [cancelComment, setCancelComment] = useState<string>("");
  const [submittingCancel, setSubmittingCancel] = useState<boolean>(false);

  const nav = useNavigate();
  const hasData = rows.length > 0;

  const fetchData = async (f: ShippingGuideFilter) => {
    setLoading(true);
    try {
      const response: any = await shippingGuideService.get(f);

      if (response && response.success === false) {
        setRows([]);
        setErrorModal({
          visible: true,
          message: response.message || "Ocurrió un error en el backend al obtener las guías.",
        });
        return;
      }

      let content: ShippingGuide[] = [];
      if (Array.isArray(response)) {
        content = response;
      } else if (response?.data?.content && Array.isArray(response.data.content)) {
        content = response.data.content;
      } else if (response?.content && Array.isArray(response.content)) {
        content = response.content;
      }

      setRows(content);
      setSelectedGuides([]);
    } catch (error: any) {
      console.error("Error obteniendo guías:", error);
      setRows([]);
      setErrorModal({
        visible: true,
        message: "Ocurrió un problema al conectar con el servidor para obtener las guías.",
      });
    } finally {
      setLoading(false);
    }
  };

  const buildCsv = (g: ShippingGuide) => {
    const headers = [
      "Número de proveedor",
      "Nombre de proveedor",
      "Guía de embarque",
      "Placa",
      "Placa remolque",
      "Origen",
      "Tipo de entrega",
      "Fecha de entrega",
      "Fecha de envío",
      "Fecha de registro",
      "Estatus",
    ];
    const separator = ";";
    const toDateString = (value?: string | null) =>
      value ? new Date(value).toLocaleDateString("es-MX") : "";

    const row = [
      g.vendorNumber ?? g.supplier?.supplierNumber ?? "",
      g.supplier?.businessName ?? "N/D",
      g.guideNumber ?? g.shippingGuideId,
      g.truckPlate ?? "",
      g.trailerPlate ?? "",
      getCatalogDisplay(g.OrigenCartaPorte),
      getCatalogDisplay(g.deliveryType),
      toDateString(g.deliveryDate),
      toDateString(g.shippingDate),
      toDateString(g.createdAt),
      getCatalogDisplay(g.status),
    ];

    return (
      headers.join(separator) +
      "\r\n" +
      row.map((cell) => `"${String(cell ?? "").replace(/"/g, '""')}"`).join(separator)
    );
  };

  const buildXml = (g: ShippingGuide) => {
    const toDateString = (value?: string | null) =>
      value ? new Date(value).toISOString() : "";

    return `<?xml version="1.0" encoding="UTF-8"?>
<CartaPorte>
  <Proveedor>${g.vendorNumber ?? ""}</Proveedor>
  <NombreProveedor>${g.supplier?.businessName ?? "N/D"}</NombreProveedor>
  <Guia>${g.guideNumber ?? g.shippingGuideId}</Guia>
  <Placa>${g.truckPlate ?? ""}</Placa>
  <PlacaRemolque>${g.trailerPlate ?? ""}</PlacaRemolque>
  <Origen>${getCatalogDisplay(g.OrigenCartaPorte)}</Origen>
  <TipoEntrega>${getCatalogDisplay(g.deliveryType)}</TipoEntrega>
  <FechaEntrega>${toDateString(g.deliveryDate)}</FechaEntrega>
  <FechaEnvio>${toDateString(g.shippingDate)}</FechaEnvio>
  <FechaRegistro>${toDateString(g.createdAt)}</FechaRegistro>
  <Estatus>${getCatalogDisplay(g.status)}</Estatus>
</CartaPorte>`;
  };

  const exportSelected = (format: ExportFormat) => {
    if (selectedGuides.length === 0) {
      setErrorModal({ visible: true, message: "Selecciona al menos una guía para exportar." });
      return;
    }

    const entries: ZipEntry[] = selectedGuides.map((g) => ({
      name:
        format === "csv"
          ? `carta_porte_${g.shippingGuideId}.csv`
          : `carta_porte_${g.shippingGuideId}.xml`,
      content: format === "csv" ? buildCsv(g) : buildXml(g),
    }));

    const zipBlob = buildZip(entries);
    const url = window.URL.createObjectURL(zipBlob);
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", formatZipName(format));
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  };

  useEffect(() => {
    if (filters) fetchData(filters);
  }, [filters]);

  useEffect(() => {
    const saved = sessionStorage.getItem("shippingGuides:lastFilters");
    if (saved) {
      try {
        setFilters(JSON.parse(saved) as ShippingGuideFilter);
      } catch { }
    }
  }, []);

  const handleSetFilters = (f: ShippingGuideFilter) => {
    setFilters(f);
    sessionStorage.setItem("shippingGuides:lastFilters", JSON.stringify(f));
  };

  const allowedCancelStatuses = useMemo(() => [1, 2], []);
  const cancellationReasons = useMemo(
    () => [
      { value: 1, label: "Error en datos de la guía" },
      { value: 2, label: "Guía duplicada" },
      { value: 3, label: "Corrección de mercancía" },
      { value: 4, label: "Otro" },
    ],
    []
  );

  const openCancelModal = (targets: ShippingGuide[]) => {
    const allowed = (targets || []).filter((g) =>
      allowedCancelStatuses.includes(getStatusCode(g))
    );
    const disallowed = (targets || []).length - allowed.length;

    if (disallowed > 0) {
      setErrorModal({
        visible: true,
        message: "Solo puedes cancelar guías en estatus 1 o 2.",
      });
    }

    if (!allowed || allowed.length === 0) {
      setErrorModal({
        visible: true,
        message: "No existe información seleccionada para actualizar, favor de validar",
      });
      return;
    }

    setCancelTargets(allowed);
    setCancelReason("");
    setCancelComment("");
    setCancelModalOpen(true);
  };

  const handleCancelSubmit = async () => {
    if (!cancelReason) {
      setErrorModal({ visible: true, message: "Selecciona un motivo de cancelación." });
      return;
    }
    if (cancelComment.length > 254) {
      setErrorModal({ visible: true, message: "El comentario no puede exceder 254 caracteres." });
      return;
    }

    const targetIds = cancelTargets.map((g) => g.shippingGuideId);
    if (targetIds.length === 0) {
      setErrorModal({ visible: true, message: "No existe información seleccionada para actualizar." });
      setCancelModalOpen(false);
      return;
    }

    setSubmittingCancel(true);
    try {
      await shippingGuideService.cancel({
        shippingGuideIds: targetIds,
        reasonId: Number(cancelReason),
        comment: cancelComment,
      });

      const now = new Date().toISOString();
      setRows((prev) =>
        prev?.map((g) =>
          targetIds.includes(g.shippingGuideId)
            ? {
              ...g,
              status: {
                ...(g.status || {
                  key: "",
                  value: "",
                  color: "",
                  externalKey: "",
                  internalStatus: 0,
                  description: "",
                }),
                internalStatus: 9,
                description: "Cancelada",
                value: "Cancelada",
              },
              updatedAt: now,
              comments: cancelComment,
            }
            : g
        )
      );

      setErrorModal({
        visible: true,
        message:
          targetIds.length === 1
            ? `La guía de embarque [${targetIds[0]}] ha sido cancelada con éxito.`
            : `Se han cancelado ${targetIds.length} guías de embarque con éxito.`,
      });

      setCancelModalOpen(false);
    } catch (error: any) {
      const code = error?.response?.status ?? "N/D";
      const reason = error?.response?.data?.message ?? "Motivo no disponible";
      setErrorModal({
        visible: true,
        message: `Error al intentar cancelar las guías seleccionadas, código de error [${code}] - Motivo [${reason}]`,
      });
    } finally {
      setSubmittingCancel(false);
    }
  };

  const cancelDisabled = useMemo(() => {
    if (!hasData) return true;
    if (selectedGuides.length === 0) return true;
    return !selectedGuides.some((g) => allowedCancelStatuses.includes(getStatusCode(g)));
  }, [hasData, selectedGuides, allowedCancelStatuses]);

  return (
    <div className="twm-layout">
      <Breadcrumb
        items={[
          { label: "Finanzas", to: "/" },
          { label: "Guías de Embarque" },
        ]}
      />

      <div className="twm-box">
        <div className="twm-header">
          <div>
            <h3 className="twm-title">Guías de Embarque</h3>
            <p className="twm-description">Consulta y validación de documentos financieros.</p>
          </div>

          <div className="twm-toolbar">
            <ShippingGuideToolbar
              onExportCsv={() => exportSelected("csv")}
              onExportXlsx={() => exportSelected("xml")}
              disabled={cancelDisabled}
            />
          </div>
        </div>

        <div className="twm-filters-section">
          <ShippingGuideFilterBar
            filters={filters}
            disabled={!hasData}
            setFilters={handleSetFilters}
            onCancel={() => openCancelModal(selectedGuides)}
          />
        </div>

        <div className="twm-grid-section">
          <ShippingGuideGrid
            rows={rows}
            loading={loading}
            onSelectionChange={setSelectedGuides}
            onRequestCancel={openCancelModal}
            onRequestStatusUpdate={(guide) =>
              nav(`/guias/${guide.shippingGuideId}/estatus`, { state: { guide } })
            }
          />
        </div>

        {loading && <GenericModal visible variant="loading" message="Cargando guías..." />}

        <GenericModal
          visible={cancelModalOpen}
          variant="confirm"
          severity="warning"
          title={`Cancelar guía${cancelTargets.length > 1 ? "s" : ""}`}
          messageConfirm={
            (
              <div style={{ textAlign: "left", marginTop: "1rem" }}>
                <div style={{ marginBottom: "1rem" }}>
                  <label style={{ display: "block", marginBottom: "0.5rem", fontWeight: "bold" }}>
                    Motivo de cancelación
                  </label>
                  <select
                    style={{ width: "100%", padding: "0.5rem" }}
                    value={cancelReason}
                    onChange={(e) => setCancelReason(e.target.value)}
                    disabled={submittingCancel}
                  >
                    <option value="">Selecciona un motivo</option>
                    {cancellationReasons.map((r) => (
                      <option key={r.value} value={r.value}>
                        {r.label}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label style={{ display: "block", marginBottom: "0.5rem", fontWeight: "bold" }}>
                    Comentario (máx 254 caracteres)
                  </label>
                  <textarea
                    style={{ width: "100%", padding: "0.5rem", minHeight: "80px", resize: "none" }}
                    maxLength={254}
                    value={cancelComment}
                    onChange={(e) => setCancelComment(e.target.value)}
                    placeholder="Describe el motivo"
                    disabled={submittingCancel}
                  />
                </div>
              </div>
            ) as any
          }
          confirmText={submittingCancel ? "Cancelando..." : "Confirmar cancelación"}
          cancelText="Cerrar"
          onConfirm={handleCancelSubmit}
          onCancel={() => {
            if (!submittingCancel) setCancelModalOpen(false);
          }}
        />

        <GenericModal
          visible={errorModal.visible}
          variant="alert"
          severity="error"
          title="Error"
          message={errorModal.message}
          buttonText="Aceptar"
          onClose={() => setErrorModal({ visible: false, message: "" })}
        />
      </div>
    </div>
  );
}