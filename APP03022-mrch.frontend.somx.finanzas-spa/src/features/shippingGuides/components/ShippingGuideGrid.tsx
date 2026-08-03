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
import {
  formatDate,
} from "@/utils/utils";
import { ShippingGuide } from "../interfaces";
import {
  getRegisteredShippingGuideStatusLabels,
  resolveShippingGuideStatusDescription,
} from "../shippingGuideStatusCatalog";
import { getShippingGuideStatusCode } from "../utils/shippingGuideStatus";
import { APP_EVENT, useSecurityContext } from "@shared/security";
import "../styles/shippingGuides.css";

const statusBadgeByCode: Record<number, string> = {
  1: "sg-status-yellow",
  2: "sg-status-yellow",
  3: "sg-status-yellow",
  4: "sg-status-yellow",
  5: "sg-status-yellow",
  6: "sg-status-red",
  7: "sg-status-green",
  8: "sg-status-red",
  9: "sg-status-red",
};

const getStatusLabel = (guide: ShippingGuide) => {
  const code = getShippingGuideStatusCode(guide);
  return resolveShippingGuideStatusDescription(
    code,
    // @ts-ignore
    guide.status,
    getRegisteredShippingGuideStatusLabels() ?? undefined
  );
};

const getStatusBadgeClass = (guide: ShippingGuide) => {
  const code = getShippingGuideStatusCode(guide);
  return statusBadgeByCode[code] ?? "sg-status-muted";
};

const getCatalogDisplay = (item?: { description?: string; value?: string; key?: string; internalStatus?: number } | null) => {
  if (!item) return "N/D";
  return item.description ?? item.value ?? item.key ?? (item.internalStatus != null ? String(item.internalStatus) : "N/D");
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
  onDownloadCsvRow?: (guide: ShippingGuide) => void;
  onDownloadXmlRow?: (guide: ShippingGuide) => void;
}

export function ShippingGuideGrid({
  rows,
  loading,
  onSelectionChange,
  onRequestCancel,
  onRequestStatusUpdate,
  onDownloadCsvRow,
  onDownloadXmlRow,
}: ShippingGuideGridProps): ReactElement {
  const nav = useNavigate();
  const { can } = useSecurityContext();
  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  const [page, setPage] = useState(1);
  const [perPage, setPerPage] = useState(10);


  const toggleRow = (id: string, checked: boolean) => {
    setSelectedIds((prev) => {
      if (checked) return prev.includes(id) ? prev : [...prev, id];
      return prev.filter((x) => x !== id);
    });
  };

  useEffect(() => {
    setSelectedIds([]);
    setPage(1);
  }, [rows]);

  useEffect(() => {
    if (!onSelectionChange || !rows) return;

    const selected = rows.filter((g) =>
      selectedIds.includes(g.shippingGuideId)
    );
    onSelectionChange(selected);
  }, [rows, selectedIds, onSelectionChange]);

  if (!rows || !Array.isArray(rows)) {
    return (
      <SimpleLobby message="Haz click en Buscar para iniciar"></SimpleLobby>
    );
  }

  const sortedRows = [...rows]
    .sort((a, b) => new Date(b.shippingDate).getTime() - new Date(a.shippingDate).getTime())
    .map(row => ({ ...row, id: row.shippingGuideId }));

  const totalItems = sortedRows.length;
  const totalPages = Math.max(1, Math.ceil(totalItems / perPage));

  const columns: Column<ShippingGuide>[] = [
    {
      header: "Guía Embarque",
      render: (guide) => <>{guide.guideNumber}</>,
    },
    {
      header: "Placa",
      render: (guide) => <>{guide.truckPlate ?? "N/D"}</>,
    },
    {
      header: "Placa Remolque",
      render: (guide) => <>{guide.trailerPlate ?? "N/D"}</>,
    },
    {
      header: "Origen",
      render: (guide) => (
        <>{guide.originId}</>
      ),
    },
    {
      header: "Tipo Entrega",
      render: (guide) => (
        <span title={guide.deliveryType?.key ?? ""}>
          {getCatalogDisplay(guide.deliveryType)}
        </span>
      ),
    },
    {
      header: "Orden Compra",
      render: (guide) => {
        const order = guide?.orderNumber == "undefined" ? "N/D" : guide?.orderNumber;
        return <>{order}</>;
      },
    },
    {
      header: "Número Proveedor",
      render: (guide) => <>{guide.vendorNumber}</>,
    },
    {
      header: "Nombre Proveedor",
      render: (guide) => <>{guide.supplier?.businessName ?? "N/D"}</>,
    },
    {
      header: "Fecha Entrega",
      render: (guide) => (
        <>
          {guide.deliveryDate
            ? formatDate(guide.deliveryDate)
            : "N/D"}
        </>
      ),
    },
    {
      header: "Fecha Envió",
      render: (guide) => (
        <>
          {guide.shippingDate
            ? formatDate(guide.shippingDate)
            : "N/D"}
        </>
      ),
    },
    {
      header: "Fecha Registro",
      render: (guide) => (
        <>
          {guide.createdAt
            ? formatDate(guide.createdAt)
            : "N/D"}
        </>
      ),
    },
    {
      header: "Estatus",
      render: (guide) => renderStatus(guide),
    },
  ];

  const allActions: { gate: { app: string; event: string }; action: RowAction<ShippingGuide> }[] = [
    {
      gate: APP_EVENT.CARTA_PORTE.VIEW_DETAIL,
      action: {
        title: "Ver",
        icon: eyeIcon,
        onClick: (guide) => nav(`/finanzas/guias/${guide.shippingGuideId}`),
      },
    },
    {
      gate: APP_EVENT.CARTA_PORTE.DOWNLOAD_CSV,
      action: {
        title: "Exportar CSV",
        icon: csvIcon,
        onClick: (guide) => onDownloadCsvRow?.(guide),
        isDisabled: (guide) => getShippingGuideStatusCode(guide) === 9,
      },
    },
    {
      gate: APP_EVENT.CARTA_PORTE.DOWNLOAD_XML,
      action: {
        title: "Exportar XML",
        icon: xmlIcon,
        onClick: (guide) => onDownloadXmlRow?.(guide),
        isDisabled: (guide) => getShippingGuideStatusCode(guide) === 9,
      },
    },
    {
      gate: APP_EVENT.CARTA_PORTE.CANCEL,
      action: {
        title: "Cancelar",
        icon: deleteIcon,
        onClick: (guide) => onRequestCancel?.([guide]),
        isDisabled: (guide) => ![1, 2].includes(getShippingGuideStatusCode(guide)),
      },
    },
    {
      gate: APP_EVENT.CARTA_PORTE.UPDATE_STATUS,
      action: {
        title: "Actualizar estatus",
        icon: editIcon,
        onClick: (guide) =>
          onRequestStatusUpdate
            ? onRequestStatusUpdate(guide)
            : nav(`/finanzas/guias/${guide.shippingGuideId}/estatus`, {
                state: { guide },
              }),
        isDisabled: (guide) => ![1, 4].includes(getShippingGuideStatusCode(guide)),
      },
    },
  ];
  const actions: RowAction<ShippingGuide>[] = allActions
    .filter(({ gate }) => can(gate))
    .map(({ action }) => action);

  return (
    <GenericTable<ShippingGuide>
      rows={sortedRows}
      actions={actions}
      columns={columns}
      emptyLabel={loading ? "Cargando..." : "Sin resultados"}
      perPage={perPage}
      page={page}
      totalPages={totalPages}
      totalItems={totalItems}
      onChangePage={setPage}
      onChangePerPage={(size) => {
        setPerPage(size);
        setPage(1);
      }}
      enableSelection={true}
      selectedIds={selectedIds}
      onSelectRow={toggleRow}
    />
  );
}