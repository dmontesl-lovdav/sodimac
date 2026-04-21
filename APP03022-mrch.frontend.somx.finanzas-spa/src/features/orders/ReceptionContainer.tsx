import { ReactElement, useState } from "react";
import { Breadcrumb, GenericModal } from "@shared/components/ui";
import { OrderClient } from "./api/OrderClient";
import { Title, Divider } from "@/shared/components/ui/misc";
import { toNumber } from "@/utils/utils";

import FiltersBar from "./components/parts/FiltersBar";
import ReceptionGridTable from "./components/ReceptionGridTable";

import type { Order, Reception, ReceptionAxios, ReceptionSKU, OrdersFilters } from "./interfaces";

import "./styles/ReceptionContainer.css";

export default function ReceptionContainer(): ReactElement {
  const [loading, setLoading] = useState(false);
  const [rows, setRows] = useState<Reception[]>([]);

  const [page, setPage] = useState<number>(1);
  const [perPage, setPerPage] = useState<number>(10);
  const [totalPages, setTotalPages] = useState<number>(1);
  const [totalItems, setTotalItems] = useState<number>(0);

  const [filters, setFilters] = useState<OrdersFilters | null>(null);

  const buildReceptionsFromApi = (orders: any[]): Reception[] => {
    const receptions: Reception[] = [];

    orders.forEach((item: any) => {
      const receptionList = item.receptions ?? [];

      receptionList.forEach((rec: any) => {
        const skus: ReceptionSKU[] = rec.receptionSkus ?? [];

        const order: Order = {
          purchaseOrderId: item.purchaseOrderId,
          orderNumber: item.orderNumber,
          vendorName: item?.supplier?.businessName || "",
          originId: toNumber(item.originId),
          amount: toNumber(item.amount),
          status: item.status,
          purchaseOrderDate: item.purchaseOrderDate,
          supplierNumber: item.supplierNumber,
          receptions: item.receptions,
          supplier: item.supplier,
        };

        const reception: Reception = {
          receptionDate: rec.receptionDate,
          orderNumber: item.orderNumber,
          receptionSkus: skus,
          order,
          originId: String(rec.originId ?? item.originId ?? "0"),
          purchaseOrderDate: item.purchaseOrderDate,
          supplierNumber: String(item.supplierNumber ?? ""),
          vendorName: item?.supplier?.businessName || "",

          receptionId: String(rec.receptionId ?? ""),
          receptionNumber: rec.receptionNumber,
          destinationId: rec.destinationId,
          purchaseOrderId: item.purchaseOrderId,
          amount: toNumber(rec.amount ?? item.amount),
          status: toNumber(rec.status ?? item.status),
          comment: rec.comment ?? "",
          receivedAt: rec.receivedAt,
          createdBy: rec.createdBy ?? item.createdBy,
          createdAt: rec.createdAt ?? item.createdAt,
          updatedBy: rec.updatedBy ?? item.updatedBy,
          updatedAt: rec.updatedAt ?? item.updatedAt,
          shippingGuidePurchaseOrders: item.shippingGuidePurchaseOrders ?? [],
          supplier: item.supplier,
          listAddendum: rec.listAddendum ?? [],
        };

        receptions.push(reception);
      });
    });

    return receptions;
  };

  const runSearch = async (
    criteria: OrdersFilters,
    nextPage?: number,
    nextPerPage?: number
  ) => {
    try {
      setLoading(true);

      const p = nextPage ?? page;
      const s = nextPerPage ?? perPage;

      const finalCriteria: OrdersFilters = {
        ...criteria,
        pageNumber: p,
        pageSize: s,
      };

      setFilters(finalCriteria);

      const res: ReceptionAxios = await OrderClient.get(finalCriteria);
      const orders = res?.data?.content ?? [];
      const receptions = buildReceptionsFromApi(orders);

      setRows(receptions);

      setTotalItems(receptions.length);
      setTotalPages(1);
      setPage(p);
      setPerPage(s);
    } catch (err) {
      console.error("Error al obtener recepciones:", err);
      setRows([]);
      setTotalItems(0);
      setTotalPages(1);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="rc-layout">
      <Breadcrumb
        items={[
          { label: "Finanzas", to: "/finanzas" },
          { label: "Recepciones" },
        ]}
      />

      <div className="rc-box">
        <div className="rc-header">
          <div>
            <Title title="Listado de Recepciones" />
            <p className="rc-description">
              Consulta y gestión de recepciones asociadas a órdenes de compra.
            </p>
          </div>
        </div>

        <div className="rc-filters-section">
          <FiltersBar
            onSearch={(criteria) => {
              setPage(1);
              runSearch({ ...criteria, pageNumber: 1, pageSize: perPage }, 1, perPage);
            }}
          />
        </div>

        <Divider />

        <div className="rc-grid-section">
          <ReceptionGridTable
            rows={rows}
            page={page}
            perPage={perPage}
            totalPages={totalPages}
            totalItems={totalItems}
            loading={loading}
            onChangePage={(p) => {
              setPage(p);
              if (filters) runSearch(filters, p, perPage);
            }}
            onChangePerPage={(s) => {
              setPerPage(s);
              setPage(1);
              if (filters) runSearch(filters, 1, s);
            }}
          />
        </div>

        {loading && <GenericModal visible variant="loading" message="Cargando…" />}
      </div>
    </div>
  );
}