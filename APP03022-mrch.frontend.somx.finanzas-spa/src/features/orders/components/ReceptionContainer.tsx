
import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { ReactElement, useState } from "react";
import { OrderClient } from "../api/OrderClient";
import { Order, Reception, ReceptionAxios, ReceptionSKU, OrdersFilters, ReceptionStatusOptions } from "../interfaces";
import FiltersBar from "./parts/FiltersBar";
import { DataGrid } from "@/shared/components/ui/datagrid";
import { Title, Divider } from "@/shared/components/ui/misc";
import { toNumber, formatDate, formatAmount } from "@/utils/utils";
import eyeIconUrl from "@assets/eye-show.svg"; 
import editIconUrl from "@assets/edit.svg"; 
import invoiceIconUrl from "@assets/document.svg"; 
import creditsIconUrl from "@assets/xml.svg"; 
import { DataGridColumn } from "@/shared/components/ui/datagrid/DataGrid";
import { useNavigate } from "react-router-dom";
import { StatusPill } from "@/shared/components/ui/statusPill/StatusPill";

/* ---------------------- Migas ---------------------- */
const breadcrumb: BreadcrumbItem[] = [
  { label: "Finanzas", to: "/" },
  { label: "Recepciones" },
];

/* ---------------------- Columnas de Recepciones ---------------------- */

const renderStatus = (status:number) => {
  const selected = ReceptionStatusOptions.filter(item=>item.value == status);
  
  if(selected.length==1){
    return selected[0];
  }
  return {
    "value": "-1",
    "type": "error",
    "label": "Desconocido"
  }
}

const mergeShippingNumbers = (shippings: any[]): string => {
  if (!shippings || shippings.length === 0) {
    return "--";
  }
  return shippings.map(s => s.shippingGuide.guideNumber).join(", ");
}

const getAdendumReferences = (reception: Reception) => {
  if (!reception.listAddendum || reception.listAddendum.length === 0) {
    return null;
  }
  return reception.listAddendum[0].invoice;
}

export default function ReceptionContainer(): ReactElement {
  const [loading, setLoading] = useState(false);
  const [rows, setRows] = useState<Reception[]>([]);
  const client = new OrderClient();

  const nav = useNavigate();

  const columns: DataGridColumn<Reception>[] = [
  { header: "Recepción", accessor: r => r.receptionId, exportAccessor: r => r.receptionId },
  { header: "Orden", accessor: r => r.order?.orderNumber ?? r.orderNumber, exportAccessor: r => r.order?.orderNumber ?? r.orderNumber },
  { header: "Guía", accessor: r => mergeShippingNumbers(r.shippingGuidePurchaseOrders) ?? "", exportAccessor: r => mergeShippingNumbers(r.shippingGuidePurchaseOrders) ?? "" },
  { header: "Origen", accessor: r => r.originId, exportAccessor: r => r.originId },
  { header: "Fecha Recepción", accessor: r => r.receptionDate ? formatDate(r.receptionDate) : "N/D", exportAccessor: r => r.receptionDate },
  { header: "Fecha Registro Documento", accessor: r => r.createdAt ? formatDate(r.createdAt) : "--", exportAccessor: r => r.createdAt },
  { header: "Importe", accessor: r => formatAmount(r.amount), exportHeader: "Monto", exportAccessor: r => r.amount },
  // Si tienes opciones de estatus con label (ReceptionStatusOptions), puedes mapearlo aquí:
  { header: "Estatus", accessor: r => <StatusPill type={renderStatus(r.status).type}>{renderStatus(r.status).label}</StatusPill>, exportAccessor: r => renderStatus(r.status).label },
  
  { header: "Id Proveedor", accessor: r => r.order?.supplierNumber ?? r.supplierNumber, exportAccessor: r => r.order?.supplierNumber ?? r.supplierNumber },
  { header: "Nombre Proveedor", accessor: r => r.supplier?.businessName || "--", exportAccessor: r => r.supplier?.businessName || "" },
  // Placeholders si aún no existen en tu modelo:
  { header: "Serie", accessor: r => getAdendumReferences(r)?.series ?? "--", exportAccessor: r => getAdendumReferences(r)?.series ?? "" },
  { header: "Folio", accessor: r => getAdendumReferences(r)?.folio ?? "--", exportAccessor: r => getAdendumReferences(r)?.folio ?? "" },
  { header: "UUID", accessor: r => getAdendumReferences(r)?.invoiceUuid ?? "--", exportAccessor: r => getAdendumReferences(r)?.invoiceUuid ?? "" },

  // Columna de acción visual (sin usar props actions):
  {
    header: "Acción",
    align: "center",
    render: (r) => {
      const addendum = getAdendumReferences(r);
      return (
        <div style={{width:"150px"}}>
        <button
        title="Ver Detalles"
        onClick={() => nav(`/recepciones/${r.receptionId}`)}
        className="gt-action-btn"
      >
        <img src={eyeIconUrl} alt="Ver" className="gt-action-icon" />
      </button>
      <button
        title="Editar Recepción"
        onClick={() => nav(`/recepciones/${r.receptionId}/editar`)}
        disabled={r.status!=0}
        className="gt-action-btn"
      >
        <img src={editIconUrl} alt="Ver" className="gt-action-icon" />
      </button>
      <button
        title="Facturación"
        onClick={() => nav(`/recepciones/${r.receptionId}/factura`)}
        className="gt-action-btn"
        disabled={r.status!=0 || addendum}
      >
        <img src={invoiceIconUrl} alt="Facturación" className="gt-action-icon" />
      </button>

      <button
        title="Ver notas de crédito"
        onClick={() => nav(`/recepciones/${r.receptionId}/notas-credito`)}
        className="gt-action-btn"
        disabled={!addendum}
      >
        <img src={creditsIconUrl} alt="Ver notas de crédito" className="gt-action-icon" />
      </button>

      </div>
      );
      
      
    },
  },
];

  const onFilter = async (criteria: OrdersFilters) => {
    try {
      setLoading(true);
      const res: ReceptionAxios = await client.get(criteria);
      const orders = res?.data?.content ?? [];
      const receptions: Reception[] = [];
      orders.forEach((item: any) => {
        const receptionList = item.receptions ?? [];

        receptionList.forEach((rec: any) => {
          const skus: ReceptionSKU[] = rec.receptionSkus ?? [];

          const order: Order = {
            purchaseOrderId: item.purchaseOrderId,
            orderNumber: item.orderNumber,
            vendorName: item.supplier.businessName || "",
            originId: toNumber(item.originId),
            amount: toNumber(item.amount),
            status: item.status,
            purchaseOrderDate: item.purchaseOrderDate,
            supplierNumber: item.supplierNumber,
          };

          const reception: Reception = {
            receptionDate: rec.receptionDate,
            orderNumber: item.orderNumber,
            receptionSkus: skus,
            order,
            originId: rec.originId ?? item.originId,
            purchaseOrderDate: item.purchaseOrderDate,
            supplierNumber: item.supplierNumber,
            vendorName: item.supplier.businessName || "",

            // Opcionales
            receptionId: rec.receptionId,
            receptionNumber: rec.receptionNumber,
            destinationId: rec.destinationId,
            purchaseOrderId: item.purchaseOrderId,
            amount: toNumber(rec.amount ?? item.amount),
            status: toNumber(rec.status ?? item.status),
            comment: rec.comment,
            receivedAt: rec.receivedAt,
            createdBy: rec.createdBy ?? item.createdBy,
            createdAt: rec.createdAt ?? item.createdAt,
            updatedBy: rec.updatedBy ?? item.updatedBy,
            updatedAt: rec.updatedAt ?? item.updatedAt,
            shippingGuidePurchaseOrders: item.shippingGuidePurchaseOrders,
            supplier: item.supplier,
            listAddendum: rec.listAddendum ?? [],
          };

          receptions.push(reception);
        });
      });

      setRows(receptions);
    } catch (err) {
      console.error("Error al obtener recepciones:", err);
      setRows([]);
    } finally {
      setLoading(false);
    }
  };

  return decorate(
    breadcrumb,
    "/",
    <>
      <Title title="Listado de Recepciones" />
      <FiltersBar onSearch={(criteria) => onFilter(criteria)} />
      <Divider />
      <DataGrid<Reception>
        rows={rows}
        loading={loading}
        columns={columns}
        getRowId={r => r.receptionId}
        perPage={25}
        page={1}
        totalPages={1}
        selectable
        enableCsv
        csvFilename="Mis Recepciones"
      />
    </>,
    loading
  );
}
