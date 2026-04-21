import SimpleLobby from "@/shared/components/ui/lobby/Lobby";
import GenericTable, {
  Column,
  RowAction,
} from "@/shared/components/ui/table/GenericTable";
import csvIcon from "@assets/csv.svg";
import eyeIcon from "@assets/eye-show.svg";
import xmlIcon from "@assets/xml.svg";
import deleteIcon from "@assets/delete.svg";
import editIcon from "@assets/edit.svg";
import { ReactElement, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ShippingGuide } from "../interfaces";
import "../styles/shippingGuides.css";

const statusCatalog: Record<number, { label: string; badgeClass: string }> = {
  1: { label: "Pendiente de OC", badgeClass: "sg-status-yellow" },
  2: { label: "Pendiente de Facturar", badgeClass: "sg-status-yellow" },
  3: { label: "Pendiente de Pago", badgeClass: "sg-status-yellow" },
  4: { label: "En proceso de contabilización", badgeClass: "sg-status-yellow" },
  5: { label: "Contabilizada", badgeClass: "sg-status-yellow" },
  6: { label: "Rechazo contable", badgeClass: "sg-status-red" },
  7: { label: "Pagada", badgeClass: "sg-status-green" },
  8: { label: "Rechazo de guía", badgeClass: "sg-status-red" },
  9: { label: "Cancelada", badgeClass: "sg-status-red" },
};

const getStatusCode = (guide: ShippingGuide) => Number(guide.status?.internalStatus ?? 0);

const getStatusLabel = (guide: ShippingGuide) => {
  const code = getStatusCode(guide);
  return (
    guide.status?.description ||
    guide.status?.value ||
    statusCatalog[code]?.label ||
    guide.status?.key ||
    "N/D"
  );
};

const getStatusBadgeClass = (guide: ShippingGuide) => {
  const code = getStatusCode(guide);
  return statusCatalog[code]?.badgeClass || "sg-status-muted";
};

const getCatalogDisplay = (item?: { description?: string; value?: string; key?: string; internalStatus?: number } | null) => {
  if (!item) return "N/D";
  return item.description || item.value || item.key || (item.internalStatus != null ? String(item.internalStatus) : "N/D");
};

const renderStatus = (guide: ShippingGuide) => {
  return (
    <span className={`sg-status-pill ${getStatusBadgeClass(guide)}`}>
      {getStatusLabel(guide)}
    </span>
  );
};

interface ShippingGuideGridProps {
  rows?: ShippingGuide[];
  loading: boolean;
  onSelectionChange?: (selected: ShippingGuide[]) => void;
  onRequestCancel?: (guides: ShippingGuide[]) => void;
  onRequestStatusUpdate?: (guide: ShippingGuide) => void;
}

export function ShippingGuideGrid({
  rows,
  loading,
  onSelectionChange,
  onRequestCancel,
  onRequestStatusUpdate,
}: ShippingGuideGridProps): ReactElement {
  const nav = useNavigate();
  const [selectedIds, setSelectedIds] = useState<string[]>([]);

  const toggleRow = (id: string, checked: boolean) => {
    setSelectedIds((prev) => {
      if (checked) return prev.includes(id) ? prev : [...prev, id];
      return prev.filter((x) => x !== id);
    });
  };

  if (!rows || !Array.isArray(rows)) {
    return (
      <SimpleLobby message="Haz click en Buscar para iniciar"></SimpleLobby>
    );
  }

  const sortedRows = [...rows]
    .sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime())
    .map(row => ({ ...row, id: row.shippingGuideId }));

  useEffect(() => {
    setSelectedIds([]);
  }, [rows]);

  useEffect(() => {
    if (!onSelectionChange || !rows) return;

    const selected = rows.filter((g) =>
      selectedIds.includes(g.shippingGuideId)
    );
    onSelectionChange(selected);
  }, [rows, selectedIds, onSelectionChange]);

  const columns: Column<ShippingGuide>[] = [
    {
      header: "Número de proveedor",
      render: (guide) => <>{guide.vendorNumber}</>,
    },
    {
      header: "Nombre de proveedor",
      render: (guide) => <>{guide.supplier?.businessName || "N/D"}</>,
    },
    {
      header: "Guia de embarque",
      render: (guide) => <>{guide.guideNumber}</>,
    },
    {
      header: "Placa",
      render: (guide) => <>{guide.truckPlate || "N/D"}</>,
    },
    {
      header: "Placa remolque",
      render: (guide) => <>{guide.trailerPlate || "N/D"}</>,
    },
    {
      header: "Origen",
      render: (guide) => <>{getCatalogDisplay(guide.OrigenCartaPorte)}</>,
    },
    {
      header: "Tipo de entrega",
      render: (guide) => (
        <span title={guide.deliveryType?.key || ""}>
          {getCatalogDisplay(guide.deliveryType)}
        </span>
      ),
    },
    {
      header: "Fecha de entrega",
      render: (guide) => (
        <>
          {guide.deliveryDate
            ? new Date(guide.deliveryDate).toLocaleDateString()
            : "N/D"}
        </>
      ),
    },
    {
      header: "Fecha de envió",
      render: (guide) => (
        <>
          {guide.shippingDate
            ? new Date(guide.shippingDate).toLocaleDateString()
            : "N/D"}
        </>
      ),
    },
    {
      header: "Fecha de registro",
      render: (guide) => (
        <>
          {guide.createdAt
            ? new Date(guide.createdAt).toLocaleDateString()
            : "N/D"}
        </>
      ),
    },
    {
      header: "Número de orden compra",
      render: (guide) => <>{guide.orderNumber || "N/D"}</>,
    },
    {
      header: "Estatus",
      render: (guide) => renderStatus(guide),
    },
  ];

  const actions: RowAction<ShippingGuide>[] = [
    {
      title: "Ver",
      icon: eyeIcon,
      onClick: (guide) => nav(`/guias/${guide.shippingGuideId}`),
      isDisabled: (guide) => getStatusCode(guide) !== 9,
    },
    {
      title: "Descargar CSV",
      icon: csvIcon,
      onClick: (guide) => console.log(guide.shippingGuideId),
    },
    {
      title: "Dercargar XML",
      icon: xmlIcon,
      onClick: (guide) => console.log(guide.shippingGuideId),
    },
    {
      title: "Cancelar",
      icon: deleteIcon,
      onClick: (guide) => onRequestCancel?.([guide]),
      isDisabled: (guide) => ![1, 2].includes(getStatusCode(guide)),
    },
    {
      title: "Actualizar estatus",
      icon: editIcon,
      onClick: (guide) =>
        onRequestStatusUpdate
          ? onRequestStatusUpdate(guide)
          : nav(`/guias/${guide.shippingGuideId}/estatus`, { state: { guide } }),
      isDisabled: (guide) => ![1, 4].includes(getStatusCode(guide)),
    },
  ];

  return (
    <GenericTable<ShippingGuide>
      rows={sortedRows}
      actions={actions}
      columns={columns}
      emptyLabel={loading ? "Cargando..." : "Sin resultados"}
      perPage={10}
      page={1}
      totalPages={1}
      enableSelection={true}
      selectedIds={selectedIds}
      onSelectRow={toggleRow}
    />
  );
}