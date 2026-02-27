import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import GenericModal from "@shared/components/ui/modal/GenericModal";
import { ReactElement, useEffect, useMemo, useState } from "react";
import { ShippingGuideClient } from "../api/ShippingGuideClient";
import { ShippingGuide, ShippingGuideFilter } from "../interfaces";
import "./shippingGuides.css";
import ShippingGuideFilterBar from "./ShippingGuideFilterBar";
import { ShippingGuideGrid } from "./ShippingGuideGrid";
import { useNavigate } from "react-router-dom";

const breadcrumb: BreadcrumbItem[] = [
  { label: "Finanzas", to: "/" },
  { label: "Guías de Embarque" },
  { label: "Guías" },
];

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
    localHeader.setUint32(p, 0x04034b50, true); p += 4; // signature
    localHeader.setUint16(p, 20, true); p += 2; // version
    localHeader.setUint16(p, 0, true); p += 2; // flags
    localHeader.setUint16(p, 0, true); p += 2; // method (store)
    localHeader.setUint16(p, 0, true); p += 2; // time
    localHeader.setUint16(p, 0, true); p += 2; // date
    localHeader.setUint32(p, crc, true); p += 4;
    localHeader.setUint32(p, data.length, true); p += 4; // comp size
    localHeader.setUint32(p, data.length, true); p += 4; // uncomp size
    localHeader.setUint16(p, nameBytes.length, true); p += 2; // name len
    localHeader.setUint16(p, 0, true); p += 2; // extra len

    const lf = new Uint8Array(localHeader.byteLength + nameBytes.length + data.length);
    lf.set(new Uint8Array(localHeader.buffer), 0);
    lf.set(nameBytes, localHeader.byteLength);
    lf.set(data, localHeader.byteLength + nameBytes.length);
    localFiles.push(lf);

    // central directory
    const cdh = new DataView(new ArrayBuffer(46));
    p = 0;
    cdh.setUint32(p, 0x02014b50, true); p += 4;
    cdh.setUint16(p, 20, true); p += 2; // version made by
    cdh.setUint16(p, 20, true); p += 2; // version needed
    cdh.setUint16(p, 0, true); p += 2; // flags
    cdh.setUint16(p, 0, true); p += 2; // method store
    cdh.setUint16(p, 0, true); p += 2; // time
    cdh.setUint16(p, 0, true); p += 2; // date
    cdh.setUint32(p, crc, true); p += 4;
    cdh.setUint32(p, data.length, true); p += 4;
    cdh.setUint32(p, data.length, true); p += 4;
    cdh.setUint16(p, nameBytes.length, true); p += 2;
    cdh.setUint16(p, 0, true); p += 2; // extra len
    cdh.setUint16(p, 0, true); p += 2; // comment len
    cdh.setUint16(p, 0, true); p += 2; // disk start
    cdh.setUint16(p, 0, true); p += 2; // int attrs
    cdh.setUint32(p, 0, true); p += 4; // ext attrs
    cdh.setUint32(p, offset, true); p += 4; // relative offset

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
  eocd.setUint32(p, 0x06054b50, true); p += 4;
  eocd.setUint16(p, 0, true); p += 2; // disk number
  eocd.setUint16(p, 0, true); p += 2; // disk start
  eocd.setUint16(p, entries.length, true); p += 2; // entries on disk
  eocd.setUint16(p, entries.length, true); p += 2; // total entries
  eocd.setUint32(p, centralSize, true); p += 4; // size of central dir
  eocd.setUint32(p, centralOffset, true); p += 4; // offset of central dir
  eocd.setUint16(p, 0, true); // comment length

  // ensamblar
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

export default function ShippingGuideContainer(): ReactElement {
  const [loading, setLoading] = useState<boolean>(false);
  const [filters, setFilters] = useState<ShippingGuideFilter>();
  const [rows, setRows] = useState<ShippingGuide[]>();
  const [selectedGuides, setSelectedGuides] = useState<ShippingGuide[]>([]);
  const [showExportMenu, setShowExportMenu] = useState<boolean>(false);
  const [cancelModalOpen, setCancelModalOpen] = useState<boolean>(false);
  const [cancelTargets, setCancelTargets] = useState<ShippingGuide[]>([]);
  const [cancelReason, setCancelReason] = useState<string>("");
  const [cancelComment, setCancelComment] = useState<string>("");
  const [submittingCancel, setSubmittingCancel] = useState<boolean>(false);
  const [alertModal, setAlertModal] = useState<{
    visible: boolean;
    title: string;
    message: string;
    severity: "success" | "error" | "info" | "warning";
  }>({ visible: false, title: "", message: "", severity: "info" });

  const client: ShippingGuideClient = new ShippingGuideClient();
  const nav = useNavigate();

  const fetchData = async (filters: ShippingGuideFilter) => {
    setLoading(true);
    try {
      const response: any = await client.get(filters);
      setRows(response.data.content as ShippingGuide[]);
      setSelectedGuides([]); // limpiar selección cuando cambian los datos
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
      (g as any).vendorNumber ?? (g as any).supplierNumber,
      "N/D",
      g.shippingGuideId,
      (g as any).truckPlate ?? (g as any).plate,
      g.trailerPlate ?? "",
      g.sourceId,
      g.deliveryType,
      toDateString(g.deliveryDate),
      toDateString(g.shippingDate),
      toDateString(g.createdAt),
      g.status,
    ];
    const csv =
      headers.join(separator) +
      "\r\n" +
      row.map((cell) => `"${String(cell ?? "").replace(/"/g, '""')}"`).join(separator);
    return csv;
  };

  const buildXml = (g: ShippingGuide) => {
    const toDateString = (value?: string | null) =>
      value ? new Date(value).toISOString() : "";
    return `<?xml version="1.0" encoding="UTF-8"?>
<CartaPorte>
  <Proveedor>${(g as any).vendorNumber ?? ""}</Proveedor>
  <NombreProveedor>N/D</NombreProveedor>
  <Guia>${g.shippingGuideId}</Guia>
  <Placa>${(g as any).truckPlate ?? ""}</Placa>
  <PlacaRemolque>${g.trailerPlate ?? ""}</PlacaRemolque>
  <Origen>${g.sourceId}</Origen>
  <TipoEntrega>${g.deliveryType}</TipoEntrega>
  <FechaEntrega>${toDateString(g.deliveryDate)}</FechaEntrega>
  <FechaEnvio>${toDateString(g.shippingDate)}</FechaEnvio>
  <FechaRegistro>${toDateString(g.createdAt)}</FechaRegistro>
  <Estatus>${g.status}</Estatus>
</CartaPorte>`;
  };

  // Exportar seleccionados en ZIP (CSV o XML)
  const exportSelected = (format: ExportFormat) => {
    if (selectedGuides.length === 0) {
      alert("Selecciona al menos una guía para exportar.");
      return;
    }

    const entries: ZipEntry[] = selectedGuides.map((g) => {
      const name =
        format === "csv"
          ? `carta_porte_${g.shippingGuideId}.csv`
          : `carta_porte_${g.shippingGuideId}.xml`;
      const content = format === "csv" ? buildCsv(g) : buildXml(g);
      return { name, content };
    });

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
    filters && fetchData(filters);
  }, [filters]);

  // Restaurar filtros desde sessionStorage al montar
  useEffect(() => {
    const saved = sessionStorage.getItem("shippingGuides:lastFilters");
    if (saved) {
      try {
        const parsed = JSON.parse(saved) as ShippingGuideFilter;
        setFilters(parsed);
      } catch {
        // ignore parsing errors
      }
    }
  }, []);

  // wrapper para persistir filtros
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
      allowedCancelStatuses.includes(Number(g.status))
    );
    const disallowed = (targets || []).length - allowed.length;

    if (disallowed > 0) {
      setAlertModal({
        visible: true,
        title: "Estatus no permitido",
        message: "Solo puedes cancelar guías en estatus 1 o 2.",
        severity: "warning",
      });
    }

    if (!allowed || allowed.length === 0) {
      setAlertModal({
        visible: true,
        title: "Sin selección",
        message: "No existe información seleccionada para actualizar, favor de validar",
        severity: "warning",
      });
      return;
    }
    setCancelTargets(allowed);
    setCancelReason("");
    setCancelComment("");
    setCancelModalOpen(true);
  };

  const handleBulkCancelClick = () => {
    openCancelModal(selectedGuides);
  };

  const handleCancelSubmit = async () => {
    if (!cancelReason) {
      setAlertModal({
        visible: true,
        title: "Motivo requerido",
        message: "Selecciona un motivo de cancelación.",
        severity: "warning",
      });
      return;
    }
    if (cancelComment.length > 254) {
      setAlertModal({
        visible: true,
        title: "Comentario demasiado largo",
        message: "El comentario no puede exceder 254 caracteres.",
        severity: "warning",
      });
      return;
    }
    const targetIds = cancelTargets.map((g) => g.shippingGuideId);
    if (targetIds.length === 0) {
      setAlertModal({
        visible: true,
        title: "Sin selección",
        message: "No existe información seleccionada para actualizar, favor de validar",
        severity: "warning",
      });
      setCancelModalOpen(false);
      return;
    }

    setSubmittingCancel(true);
    try {
      await client.cancel({
        shippingGuideIds: targetIds,
        reasonId: Number(cancelReason),
        comment: cancelComment,
      });

      const now = new Date();
      setRows((prev) =>
        prev?.map((g) =>
          targetIds.includes(g.shippingGuideId)
            ? { ...g, status: 9, updatedAt: now.toISOString(), comments: cancelComment }
            : g
        )
      );

      const dateStr = now.toLocaleString("es-MX", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
        hour12: false,
      });

      if (targetIds.length === 1) {
        setAlertModal({
          visible: true,
          title: "Cancelación exitosa",
          message: `La guía de embarque [${targetIds[0]}] ha sido cancelada con éxito.\n\nFecha de Cancelación: ${dateStr}\nMotivo de Cancelación: ${cancelComment || "N/D"}`,
          severity: "success",
        });
      } else {
        setAlertModal({
          visible: true,
          title: "Cancelación exitosa",
          message: `Se han cancelado ${targetIds.length} guías de embarque con éxito.\n\nFecha de Cancelación: ${dateStr}\nMotivo de Cancelación: ${cancelComment || "N/D"}`,
          severity: "success",
        });
      }
      setCancelModalOpen(false);
    } catch (error: any) {
      const code = error?.response?.status ?? "N/D";
      const reason = error?.response?.data?.message ?? "Motivo no disponible";
      setAlertModal({
        visible: true,
        title: "Error al cancelar",
        message: `Error al intentar cancelar las guías de embarque seleccionadas, código de error [${code}] - Motivo [${reason}]`,
        severity: "error",
      });
    } finally {
      setSubmittingCancel(false);
    }
  };

  return decorate(
    breadcrumb,
    "/",
    <>
      <div className="sg-header">
        <div className="sg-title">Guías de Embarque</div>
      </div>

      <div className="sg-divider" />
      <ShippingGuideFilterBar
        filters={filters}
        setFilters={handleSetFilters}
      />

      <div className="sg-divider" />
      <ShippingGuideGrid
        rows={rows}
        loading={loading}
        onSelectionChange={setSelectedGuides}
        onRequestCancel={openCancelModal}
        onRequestStatusUpdate={(guide) => nav(`/guias/${guide.shippingGuideId}/estatus`, { state: { guide } })}
      />
      <div className="sg-actions-bar">
        <button className="sg-cancel-button" onClick={handleBulkCancelClick}>
          Cancelar
        </button>
        <div className="sg-export-wrapper">
          <button
            className="sg-export-button"
            onClick={() => setShowExportMenu((prev) => !prev)}
          >
            <span>Exportar como</span>
            <span style={{ fontSize: "12px" }}>▼</span>
          </button>
          {showExportMenu && (
            <div className="sg-export-menu">
              <button
                className="sg-export-item"
                onClick={() => {
                  setShowExportMenu(false);
                  exportSelected("csv");
                }}
              >
                CSV
              </button>
              <button
                className="sg-export-item"
                onClick={() => {
                  setShowExportMenu(false);
                  exportSelected("xml");
                }}
              >
                XML
              </button>
            </div>
          )}
        </div>
      </div>

      {cancelModalOpen && (
        <div className="sg-modal-backdrop">
          <div className="sg-modal">
            <h3>Cancelar guía{cancelTargets.length > 1 ? "s" : ""}</h3>
            <div className="sg-modal-body">
              <div>
                <label>Motivo de cancelación</label>
                <select
                  value={cancelReason}
                  onChange={(e) => setCancelReason(e.target.value)}
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
                <label>Comentario (máx 254 caracteres)</label>
                <textarea
                  maxLength={254}
                  value={cancelComment}
                  onChange={(e) => setCancelComment(e.target.value)}
                  placeholder="Describe el motivo"
                />
              </div>
            </div>
            <div className="sg-modal-footer">
              <button
                className="sg-btn sg-btn-outline"
                onClick={() => setCancelModalOpen(false)}
                disabled={submittingCancel}
              >
                Cerrar
              </button>
              <button
                className="sg-btn sg-btn-primary"
                onClick={handleCancelSubmit}
                disabled={submittingCancel}
              >
                {submittingCancel ? "Cancelando..." : "Confirmar cancelación"}
              </button>
            </div>
          </div>
        </div>
      )}

      <GenericModal
        variant="alert"
        visible={alertModal.visible}
        title={alertModal.title}
        message={alertModal.message}
        severity={alertModal.severity}
        onClose={() => setAlertModal({ ...alertModal, visible: false })}
      />
    </>,
    loading
  );
}
