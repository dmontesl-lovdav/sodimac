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
import "./shippingGuides.css";

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

const deliveryTypeCatalog: Record<
  number,
  { label: string; description: string }
> = {
  1: { label: "Mercancia", description: "Proveedores de mercancía" },
  2: { label: "Transporte", description: "Proveedores de transporte" },
  3: { label: "Indirectos", description: "Proveedores indirectos" },
  4: { label: "Servicios", description: "Proveedores de servicios" },
};

const renderStatus = (status?: number) => {
  const info = status ? statusCatalog[status] : undefined;

  return (
    <span
      className={`sg-status-pill ${
        info ? info.badgeClass : "sg-status-muted"
      }`}
    >
      {info ? info.label : "N/D"}
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

  // IDs seleccionados en el grid
  const [selectedIds, setSelectedIds] = useState<string[]>([]);

  const isRowSelected = (id: string) => selectedIds.includes(id);

  const toggleRow = (id: string, checked: boolean) => {
    setSelectedIds((prev) =>
      checked ? [...prev, id] : prev.filter((x) => x !== id)
    );
  };

  const toggleSelectAll = (
    checked: boolean,
    currentRows: ShippingGuide[]
  ) => {
    setSelectedIds(checked ? currentRows.map((r) => r.shippingGuideId) : []);
  };

  if (!rows)
    return (
      <SimpleLobby message="Haz click en Buscar para iniciar"></SimpleLobby>
    );

  // ✅ ordenar por fecha de registro (menor a mayor)
  const sortedRows: ShippingGuide[] = [...rows].sort(
    (a, b) =>
      new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
  );

  // cuando cambian las filas, limpiamos selección
  useEffect(() => {
    setSelectedIds([]);
  }, [rows]);

  // cada vez que cambian los seleccionados, avisamos al contenedor
  useEffect(() => {
    if (!onSelectionChange || !rows) return;

    const selected = rows.filter((g) =>
      selectedIds.includes(g.shippingGuideId)
    );
    onSelectionChange(selected);
  }, [rows, selectedIds, onSelectionChange]);

  const allSelected =
    sortedRows.length > 0 &&
    sortedRows.every((g) => selectedIds.includes(g.shippingGuideId));

  const columns: Column<ShippingGuide>[] = [
    {
      header: (
        <input
          type="checkbox"
          onChange={(e) => toggleSelectAll(e.target.checked, sortedRows)}
          checked={allSelected}
        />
      ),
      align: "center",
      render: (guide) => (
        <input
          type="checkbox"
          checked={isRowSelected(guide.shippingGuideId)}
          onChange={(e) => toggleRow(guide.shippingGuideId, e.target.checked)}
          onClick={(e) => e.stopPropagation()} // para que no dispare el click de la fila
        />
      ),
    },
    { header: "Número de proveedor", render: (guide) => <>{guide.vendorNumber}</> },
    { header: "Nombre de proveedor", render: () => <>N/D</> },
    { header: "Guia de embarque", render: (guide) => <>{guide.guideNumber}</> },
    { header: "Placa", render: (guide) => <>{guide.truckPlate}</> },
    { header: "Placa remolque", render: (guide) => <>{guide.trailerPlate}</> },
    { header: "Origen", render: (guide) => <>{guide.sourceId}</> },
    {
      header: "Tipo de entrega",
      render: (guide) => {
        const info = deliveryTypeCatalog[Number(guide.deliveryType)];
        return (
          <span title={info?.description || ""}>
            {info ? info.label : guide.deliveryType ?? "N/D"}
          </span>
        );
      },
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
     { header: "Número de orden compra", render: (guide) => <>{guide.deliveryType}</> },
    { header: "Estatus", render: (guide) => renderStatus(guide.status) },
  ];

  const actions: RowAction<ShippingGuide>[] = [
    {
      title: "Ver",
      icon: eyeIcon,
      onClick: (guide) => nav(`/guias/${guide.shippingGuideId}`),
      isDisabled: (guide) => guide.status !== 9,
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
      isDisabled: (guide) => ![1, 2].includes(Number(guide.status)),
    },
    {
      title: "Actualizar estatus",
      icon: editIcon,
      onClick: (guide) =>
        onRequestStatusUpdate
          ? onRequestStatusUpdate(guide)
          : nav(`/guias/${guide.shippingGuideId}/estatus`, { state: { guide } }),
      isDisabled: (guide) => ![1, 4].includes(Number(guide.status)),
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
    />
  );
}
