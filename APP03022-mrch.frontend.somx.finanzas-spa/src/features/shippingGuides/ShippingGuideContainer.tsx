import { Breadcrumb, GenericModal } from "@shared/components/ui";
import { withFinanceBreadcrumb } from "@shared/components/ui/navigation/financeBreadcrumb";
import { useFinanceAlertModal } from "@/shared/hooks/useFinanceAlertModal";
import {
  FINANCE_LIST_KEYS,
  saveFinanceListFilters,
  useFinanceListScreenSession,
  useFinanceListRefetchOnReturn,
} from "@/shared/hooks";
import { getErrorMessage } from "@/utils/errorMessage";
import {
  exportToCSV,
  exportToExcelSpreadsheet,
  formatDate,
  formatFilenameTimestamp,
  parseDisplayDate,
  startOfLocalDay,
  endOfLocalDay,
} from "@/utils/utils";
import type { ReactElement } from "react";
import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

import { shippingGuideService } from "./api/ShippingGuideClient";
import type { ShippingGuide, ShippingGuideFilter } from "./interfaces";

import ShippingGuideFilterBar from "./components/ShippingGuideFilterBar";
import { ShippingGuideGrid } from "./components/ShippingGuideGrid";
import ShippingGuideToolbar from "./components/ShippingGuideToolbar";

import {
  SHIPPING_GUIDE_STATUS_BORRADO,
  getRegisteredShippingGuideStatusLabels,
  resolveShippingGuideStatusDescription,
} from "./shippingGuideStatusCatalog";
import { getShippingGuideStatusCode } from "./utils/shippingGuideStatus";

type RowExportFormat = "csv" | "xml";

const safeGuideFileTag = (g: ShippingGuide) => {
  const raw = g.guideNumber ?? g.shippingGuideId ?? "guia";
  return String(raw).replace(/[^\w-]+/g, "_").slice(0, 80);
};

const getCatalogDisplay = (item?: { description?: string; value?: string; key?: string; internalStatus?: number } | null) => {
  if (!item) return "N/D";
  return item.description ?? item.value ?? item.key ?? (item.internalStatus != null ? String(item.internalStatus) : "N/D");
};

function parseFilterDateBound(value?: string, asEndOfDay = false): number | null {
  if (!value?.trim()) return null;
  const trimmed = value.trim();
  const ymd = /^(\d{4})-(\d{2})-(\d{2})/.exec(trimmed);
  let date: Date | null = null;
  if (ymd) {
    date = new Date(Number(ymd[1]), Number(ymd[2]) - 1, Number(ymd[3]));
  } else {
    date = parseDisplayDate(trimmed);
  }
  if (!date || Number.isNaN(date.getTime())) return null;
  return (asEndOfDay ? endOfLocalDay(date) : startOfLocalDay(date)).getTime();
}

/** Fecha de registro (`createdAt`) dentro del rango del filtro, sin incluir el día siguiente. */
function isRegistrationDateInFilterRange(
  createdAt: string | undefined | null,
  from?: string,
  to?: string
): boolean {
  if (!createdAt?.trim()) return false;
  const created = parseDisplayDate(createdAt);
  if (!created) return false;

  const createdTs = created.getTime();
  const fromTs = parseFilterDateBound(from, false);
  const toTs = parseFilterDateBound(to, true);

  if (fromTs != null && createdTs < fromTs) return false;
  if (toTs != null && createdTs > toTs) return false;
  return true;
}

export default function ShippingGuideContainer(): ReactElement {
  const financeAlert = useFinanceAlertModal();

  const [loading, setLoading] = useState<boolean>(false);
  const [rows, setRows] = useState<ShippingGuide[]>([]);
  const [selectedGuides, setSelectedGuides] = useState<ShippingGuide[]>([]);

  const [cancelModalOpen, setCancelModalOpen] = useState<boolean>(false);
  const [cancelTargets, setCancelTargets] = useState<ShippingGuide[]>([]);
  const [cancelReason, setCancelReason] = useState<string>("");
  const [cancelComment, setCancelComment] = useState<string>("");
  const [submittingCancel, setSubmittingCancel] = useState<boolean>(false);

  const nav = useNavigate();
  const hasData = rows.length > 0;

  const returningFromDetail = useFinanceListScreenSession(
    FINANCE_LIST_KEYS.shippingGuides
  );

  const fetchData = async (f: ShippingGuideFilter) => {
    setLoading(true);
    try {
      const response: any = await shippingGuideService.get(f);

      if (response && response.success === false) {
        setRows([]);
        financeAlert.showError(
          "Error",
          response.message ?? "Ocurrió un error en el backend al obtener las guías."
        );
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

      if (f.orderNumber?.trim()) {
        const q = f.orderNumber.trim().toLowerCase();
        content = content.filter((g) =>
          (g.orderNumber ?? "").toLowerCase().includes(q)
        );
      }

      content = content.filter((g) => getShippingGuideStatusCode(g) !== SHIPPING_GUIDE_STATUS_BORRADO);

      if (f.from || f.to) {
        content = content.filter((g) =>
          isRegistrationDateInFilterRange(g.createdAt, f.from, f.to)
        );
      }

      content = content.sort(
        (a, b) =>
          new Date(b.shippingDate).getTime() - new Date(a.shippingDate).getTime()
      );

      setRows(content);
      setSelectedGuides([]);
      if (content.length === 0) {
        financeAlert.showWarning(
          "Sin registros",
          "No se encontraron guías de embarque con los filtros aplicados."
        );
      }
    } catch (error: unknown) {
      setRows([]);
      financeAlert.showErrorFrom(
        "Error",
        error,
        "Ocurrió un problema al conectar con el servidor para obtener las guías."
      );
    } finally {
      setLoading(false);
    }
  };

  useFinanceListRefetchOnReturn(
    FINANCE_LIST_KEYS.shippingGuides,
    returningFromDetail,
    (f) => {
      saveFinanceListFilters(FINANCE_LIST_KEYS.shippingGuides.filters, f);
      fetchData(f as ShippingGuideFilter);
    }
  );


  /** Reporte del grid: mismas columnas visibles en `ShippingGuideGrid`. */
  const exportGridReport = (format: "csv" | "xlsx") => {
    const targets = selectedGuides.length > 0 ? selectedGuides : rows;
    if (!targets.length) {
      financeAlert.showWarning(
        "Atención",
        "No hay guías para exportar. Ejecuta una búsqueda con resultados."
      );
      return;
    }

    const headers = [
      "Guía Embarque",
      "Placa",
      "Placa Remolque",
      "Origen",
      "Tipo Entrega",
      "Orden Compra",
      "Número Proveedor",
      "Nombre Proveedor",
      "Fecha Entrega",
      "Fecha Envió",
      "Fecha Registro",
      "Estatus",
    ];

    const body = targets.map((guide) => [
      guide.guideNumber,
      guide.truckPlate ?? "N/D",
      guide.trailerPlate ?? "N/D",
      guide.originId,
      getCatalogDisplay(guide.deliveryType),
      guide?.orderNumber == "undefined" ? "N/D" : guide?.orderNumber,
      guide.vendorNumber,
      guide.supplier?.businessName ?? "N/D",
      guide.deliveryDate ? formatDate(guide.deliveryDate) : "N/D",
      guide.shippingDate ? formatDate(guide.shippingDate) : "N/D",
      guide.createdAt ? formatDate(guide.createdAt) : "N/D",
      resolveShippingGuideStatusDescription(
        getShippingGuideStatusCode(guide),
        guide.status,
        getRegisteredShippingGuideStatusLabels() ?? undefined
      ),
    ]);

    const baseName = `guias_embarque_${formatFilenameTimestamp()}`;

    if (format === "csv") {
      exportToCSV(headers, body, baseName);
    } else {
      exportToExcelSpreadsheet(headers, body, baseName);
    }
  };
  const downloadSingleGuide = (g: ShippingGuide, format: number) => {
    const doc = g.shippingGuideDocuments.find((d) => d.fileType == format);
    const baseUrl = process.env.API_BASE_URL+"/shipping-guide/downloadFile?fileName="+doc?.fileName;

    fetch(baseUrl).then((response) => {
      if (response.status === 404) {
        financeAlert.showError("Error", "No se encontró el archivo solicitado.");
        return;
      }
      window.open(baseUrl, "_blank");
    }).catch(() => {
      window.open(baseUrl, "_blank");
    });
  };

  const handleSearch = (f: ShippingGuideFilter) => {
    saveFinanceListFilters(FINANCE_LIST_KEYS.shippingGuides.filters, f);
    fetchData(f);
  };

  const handleClearFilters = () => {
    setRows([]);
    setSelectedGuides([]);
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
    const list = targets ?? [];
    if (list.length === 0) {
      financeAlert.showWarning(
        "Atención",
        "Selecciona al menos una guía para cancelar."
      );
      return;
    }

    const allowed = list.filter((g) =>
      allowedCancelStatuses.includes(getShippingGuideStatusCode(g))
    );

    if (allowed.length === 0) {
      financeAlert.showWarning(
        "Atención",
        "Solo puedes cancelar guías en estatus pendiente."
      );
      return;
    }

    setCancelTargets(allowed);
    setCancelReason("");
    setCancelComment("");
    setCancelModalOpen(true);
  };

  const handleCancelSubmit = async () => {
    if (!cancelReason) {
      financeAlert.showWarning("Atención", "Selecciona un motivo de cancelación.");
      return;
    }
    if (cancelComment.length > 254) {
      financeAlert.showWarning(
        "Atención",
        "El comentario no puede exceder 254 caracteres."
      );
      return;
    }

    const targetIds = cancelTargets.map((g) => g.shippingGuideId);
    if (targetIds.length === 0) {
      financeAlert.showWarning(
        "Atención",
        "No existe información seleccionada para actualizar."
      );
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
                ...(g.status ?? {
                  key: "ECF009",
                  value: 9,
                  color: "",
                  externalKey: "ECF009",
                  internalStatus: 9,
                  description: "",
                } as any),
                internalStatus: 9,
                description:
                  "Guía de embarque cancelada en el portal de proveedores FBC",
                value: "Guía de embarque cancelada en el portal de proveedores FBC",
                key: "ECF009",
              },
              updatedAt: now,
              comments: cancelComment,
            }
            : g
        )
      );
      const guidenumbers = cancelTargets.map((g) => g.guideNumber);

      financeAlert.showSuccess(
        "Operación exitosa",
        targetIds.length === 1
          ? `La guía de embarque ${guidenumbers[0]} ha sido cancelada con éxito.`
          : `Se han cancelado ${guidenumbers.length} guías de embarque con éxito.`,
      );

      setSelectedGuides([]);
      setCancelModalOpen(false);
    } catch (error: unknown) {
      financeAlert.showError(
        "Error al cancelar",
        getErrorMessage(
          error,
          "No fue posible cancelar las guías seleccionadas."
        )
      );
    } finally {
      setSubmittingCancel(false);
    }
  };

  const cancelDisabled = useMemo(() => {
    if (!hasData) return true;
    if (selectedGuides.length === 0) return true;
    return !selectedGuides.some((g) => allowedCancelStatuses.includes(getShippingGuideStatusCode(g)));
  }, [hasData, selectedGuides, allowedCancelStatuses]);

  const exportToolbarDisabled = !hasData;

  return (
    <div className="twm-layout">
      <Breadcrumb
        items={withFinanceBreadcrumb([{ label: "Guías de Embarque" }])}
      />

      <div className="twm-box">
        <div className="twm-header">
          <div>
            <h3 className="twm-title">Guías de Embarque</h3>
            <p className="twm-description">
              Listado de viajes asociados a la carta porte para su validación y consulta de detalle
            </p>
          </div>

          <div className="twm-toolbar">
            <ShippingGuideToolbar
              onExportCsv={() => exportGridReport("csv")}
              onExportXlsx={() => exportGridReport("xlsx")}
              onCancelSelection={() => openCancelModal(selectedGuides)}
              cancelDisabled={cancelDisabled}
              disabled={exportToolbarDisabled}
            />
          </div>
        </div>

        <div className="twm-filters-section">
          <ShippingGuideFilterBar
            onSearch={handleSearch}
            onClear={handleClearFilters}
          />
        </div>

        <div className="twm-grid-section sg-grid-compact-font">
          <ShippingGuideGrid
            rows={rows}
            loading={loading}
            onSelectionChange={setSelectedGuides}
            onRequestCancel={openCancelModal}
            onRequestStatusUpdate={(guide) =>
              nav(`/finanzas/guias/${guide.shippingGuideId}/estatus`, {
                state: { guide },
              })
            }
            onDownloadCsvRow={(g) => downloadSingleGuide(g, 2)}
            onDownloadXmlRow={(g) => downloadSingleGuide(g, 1)}
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
                  <label
                    htmlFor="sg-cancel-reason"
                    style={{ display: "block", marginBottom: "0.5rem", fontWeight: "bold" }}
                  >
                    Motivo de cancelación
                  </label>
                  <select
                    id="sg-cancel-reason"
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
                  <label
                    htmlFor="sg-cancel-comment"
                    style={{ display: "block", marginBottom: "0.5rem", fontWeight: "bold" }}
                  >
                    Comentario (máx 254 caracteres)
                  </label>
                  <textarea
                    id="sg-cancel-comment"
                    style={{ width: "100%", padding: "0.5rem", minHeight: "80px", resize: "none" }}
                    maxLength={254}
                    value={cancelComment}
                    onChange={(e) => setCancelComment(e.target.value)}
                    placeholder="Describe el motivo"
                    disabled={submittingCancel}
                  />
                </div>
              </div>
            ) 
          }
          confirmText={submittingCancel ? "Cancelando..." : "Confirmar cancelación"}
          cancelText="Cerrar"
          onConfirm={handleCancelSubmit}
          onCancel={() => {
            if (!submittingCancel) setCancelModalOpen(false);
          }}
        />

        <GenericModal
          visible={financeAlert.alertVisible}
          variant="alert"
          severity={financeAlert.alertSeverity}
          title={financeAlert.alertTitle}
          message={financeAlert.alertMessage}
          buttonText="Aceptar"
          onClose={financeAlert.closeAlert}
        />
      </div>
    </div>
  );
}