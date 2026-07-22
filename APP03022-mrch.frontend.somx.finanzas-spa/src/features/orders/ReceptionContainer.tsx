import { ReactElement, useEffect, useState } from "react";
import { Breadcrumb, GenericButton, GenericModal } from "@shared/components/ui";
import { withFinanceBreadcrumb } from "@shared/components/ui/navigation/financeBreadcrumb";
import { useFinanceAlertModal } from "@/shared/hooks/useFinanceAlertModal";
import { OrderClient } from "./api/OrderClient";
import { Title, Divider } from "@/shared/components/ui/misc";
import { toNumber, exportToCSV, formatDate, formatFilenameTimestamp, capitalizeWord } from "@/utils/utils";
import { APP_EVENT, EVENT_KEYS, PermissionGate } from "@shared/security";

import FiltersBar from "./components/parts/FiltersBar";
import ReceptionGridTable from "./components/ReceptionGridTable";

import type { Order, Reception, ReceptionAxios, ReceptionSKU, OrdersFilters } from "./interfaces";
import { resolveReceptionStatusDisplay } from "./receptionStatusDisplay";
import {
    FINANCE_LIST_KEYS,
    useFinanceListScreenSession,
    useFinanceListRefetchOnReturn,
} from "@/shared/hooks";

import downloadIconUrl from "@assets/download.svg";

import "./styles/ReceptionContainer.css";

const FETCH_ORDERS_PAGE_SIZE = 500;

function mergeShippingNumbers(shippings: any[]): string {
  if (!shippings || shippings.length === 0) return "--";
  return shippings
    .map((s) => s?.shippingGuide?.guideNumber ?? s?.guideNumber ?? "")
    .filter(Boolean)
    .join(", ");
}



function getAdendumInvoice(re: Reception) {
  return re.listAddendum?.[0]?.invoice;
}

function filterByReceptionQuery(receptions: Reception[], q?: string): Reception[] {
  const t = q?.trim().toLowerCase();
  if (!t) return receptions;
  return receptions.filter(
    (r) =>
      (r.receptionNumber ?? "").toLowerCase().includes(t) ||
      String(r.receptionId ?? "")
        .toLowerCase()
        .includes(t)
  );
}

function filterByProviderType(
  receptions: Reception[],
  providerType?: string | number
): Reception[] {
  if (providerType == null || String(providerType).trim() === "") {
    return receptions;
  }
  const q = String(providerType).trim().toLowerCase();
  return receptions.filter((r) => r.supplier?.supplierType?.id === Number(q));
}

function resolveReceptionInvoiceUuid(rec: {
  invoiceUuid?: string;
  listAddendum?: { invoice?: { fiscalUuid?: string; fiscal_uuid?: string; invoiceUuid?: string; invoice_uuid?: string } }[];
}): string {
  const inv = rec.listAddendum?.[0]?.invoice;
  return (
    [
      rec.invoiceUuid?.trim(),
      inv?.fiscalUuid?.trim(),
      inv?.fiscal_uuid?.trim(),
      inv?.invoiceUuid?.trim(),
      inv?.invoice_uuid?.trim(),
    ].find((v) => Boolean(v)) ?? ""
  );
}

export default function ReceptionContainer(): ReactElement {
  const financeAlert = useFinanceAlertModal();

  const [loading, setLoading] = useState(false);
  const [rows, setRows] = useState<Reception[]>([]);
  const [allFiltered, setAllFiltered] = useState<Reception[]>([]);

  const [page, setPage] = useState<number>(1);
  const [perPage, setPerPage] = useState<number>(10);
  const [totalPages, setTotalPages] = useState<number>(1);
  const [totalItems, setTotalItems] = useState<number>(0);

  const returningFromDetail = useFinanceListScreenSession(
    FINANCE_LIST_KEYS.receptions
  );

  const buildReceptionsFromApi = (orders: any[]): Reception[] => {
    const receptions: Reception[] = [];

    orders.forEach((item: any) => {
      const receptionList = item.receptions ?? [];

      receptionList.forEach((rec: any) => {
        const skus: ReceptionSKU[] = rec.receptionSkus ?? [];
        const invoiceUuid = resolveReceptionInvoiceUuid(rec);

        const order: Order = {
          invoiceUuid,
          purchaseOrderId: item.purchaseOrderId,
          orderNumber: item.orderNumber,
          shippingGuideNumber: mergeShippingNumbers(item.shippingGuidePurchaseOrders),
          vendorName: item?.supplier?.businessName ?? "",
          originId: toNumber(item.originId),
          amount: toNumber(item.amount),
          status: item.status,
          purchaseOrderDate: item.purchaseOrderDate,
          supplierNumber: item.supplierNumber,
          receptions: item.receptions,
          supplier: item.supplier,
        };

        const reception: Reception = {
          invoiceUuid,
          receptionDate: rec.receptionDate,
          orderNumber: item.orderNumber,
          receptionSkus: skus,
          order,
          originId: String(rec.originId ?? item.originId ?? "0"),
          originName:
            typeof rec.originName === "string" ? rec.originName.trim() : "",
          purchaseOrderDate: item.purchaseOrderDate,
          supplierNumber: String(item.supplierNumber ?? ""),
          vendorName:
            [
              rec.vendorName,
              item?.supplier?.businessName,
              item?.vendorName,
            ].find((v) => Boolean(v)) ?? "",

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
          supplier: rec.supplier ?? item.supplier,
          listAddendum: rec.listAddendum ?? [],
        };

        receptions.push(reception);
      });
    });

    return receptions;
  };

  useEffect(() => {
    const n = allFiltered.length;
    if (n === 0) {
      setRows([]);
      setTotalItems(0);
      setTotalPages(1);
      return;
    }
    const tp = Math.max(1, Math.ceil(n / perPage));
    const safePage = Math.min(Math.max(1, page), tp);
    if (safePage !== page) {
      setPage(safePage);
      return;
    }
    setTotalPages(tp);
    setTotalItems(n);
    const start = (safePage - 1) * perPage;
    setRows(allFiltered.slice(start, start + perPage));
  }, [allFiltered, page, perPage]);

  const runSearch = async (criteria: OrdersFilters, _nextPage?: number, nextPerPage?: number) => {
    const s = nextPerPage ?? perPage;

    try {
      setLoading(true);

      const { receptionNumber: receptionQ, providerType: providerTypeQ, ...apiCriteria } = criteria;

      const finalCriteria: OrdersFilters = {
        ...apiCriteria,
        pageNumber: 1,
        pageSize: FETCH_ORDERS_PAGE_SIZE,
      };

      const res: ReceptionAxios = await OrderClient.get(finalCriteria);
      const orders = res?.data?.content ?? [];
      let receptions = buildReceptionsFromApi(orders);
      receptions = receptions.filter((r) => r.status !== 8);
      if (apiCriteria.status != null && !Number.isNaN(Number(apiCriteria.status))) {
        receptions = receptions.filter(
          (r) => Number(r.status) === Number(apiCriteria.status)
        );
      }
      receptions = filterByReceptionQuery(receptions, receptionQ);
      receptions = filterByProviderType(receptions, providerTypeQ);

      setAllFiltered(receptions);
      setPerPage(s);

      if (receptions.length === 0) {
        financeAlert.showWarning(
          "Sin registros",
          "No se encontraron recepciones con los criterios indicados."
        );
      }
    } catch (err) {
      financeAlert.showErrorFrom(
        "Error",
        err,
        "No fue posible obtener las recepciones. Intenta nuevamente."
      );
      setAllFiltered([]);
      setRows([]);
      setTotalItems(0);
      setTotalPages(1);
    } finally {
      setLoading(false);
    }
  };

  useFinanceListRefetchOnReturn<OrdersFilters>(
    FINANCE_LIST_KEYS.receptions,
    returningFromDetail,
    (criteria) => {
      setPage(1);
      runSearch(criteria, 1, perPage);
    }
  );

  const handleClearList = () => {
    setAllFiltered([]);
    setRows([]);
    setPage(1);
    setTotalItems(0);
    setTotalPages(1);
  };

  const handleExportCsv = () => {
    if (allFiltered.length === 0) return;
    const headers = [
      "Recepción",
      "Orden Compra",
      "Guía",
      "Tipo Proveedor",
      "Documento",
      "Importe",
      "Serie",
      "Folio",
      "UUID",
      "Número Proveedor",
      "Nombre Proveedor",
      "Fecha Recepción",
      "Fecha Registro",
      "Estatus",
    ];
    const body = allFiltered.map((r) => {
      const inv = getAdendumInvoice(r);
      const doc = inv
        ? inv.document_type ?? inv.documentType ?? "--"
        : "--";
      const serie = inv?.series ?? "--";
      const folio = inv?.folio ?? "--";
      const uuid = resolveReceptionInvoiceUuid(r) ?? "--";
      const statusLabel = resolveReceptionStatusDisplay(r.status).label;
      return [
        r.receptionNumber ?? r.receptionId ?? "",
        r.order?.orderNumber ?? r.orderNumber ?? "",
        mergeShippingNumbers(r.shippingGuidePurchaseOrders),
        capitalizeWord(r.supplier?.supplierType?.code ?? ""),
        String(doc),
        String(r.amount ?? ""),
        String(serie),
        String(folio),
        String(uuid),
        String(r.order?.supplierNumber ?? r.supplierNumber ?? ""),
        r.supplier?.businessName ?? r.vendorName ?? "",
        r.receptionDate ? formatDate(String(r.receptionDate)) : "",
        r.createdAt ? formatDate(String(r.createdAt), true) : "",
        statusLabel,
      ];
    });
    exportToCSV(headers, body, `recepcion_${formatFilenameTimestamp()}`);
  };

  return (
    <div className="rc-layout">
      <Breadcrumb
        items={withFinanceBreadcrumb([{ label: "Recepciones" }])}
      />

      <div className="rc-box">
        <div className="rc-header">
          <div>
            <Title title={'Recepción'}></Title>
            <p className="rc-description">
              Consulta y gestión de recepciones asociadas a órdenes de compra.
            </p>
          </div>
          <div className="rc-header-actions">
            <PermissionGate appEvent={APP_EVENT.RECEPTIONS.DOWNLOAD_CSV}>
              <GenericButton
                onClick={handleExportCsv}
                disabled={loading || allFiltered.length === 0}
              >
                <span
                  style={{
                    display: "flex",
                    alignItems: "center",
                    gap: 6,
                  }}
                >
                  <span
                    className="rc-download-ico"
                    aria-hidden="true"
                    style={{
                      WebkitMaskImage: `url(${downloadIconUrl})`,
                      maskImage: `url(${downloadIconUrl})`,
                    }}
                  />
                  Exportar CSV
                </span>
              </GenericButton>
            </PermissionGate>
          </div>
        </div>

        <div className="rc-filters-section">
          <FiltersBar
            onSearch={(criteria) => {
              setPage(1);
              runSearch({ ...criteria }, 1, perPage);
            }}
            onClear={handleClearList}
          />
        </div>

        <Divider />

        <div className="rc-grid-section rc-grid-compact">
          <ReceptionGridTable
            rows={rows}
            page={page}
            perPage={perPage}
            totalPages={totalPages}
            totalItems={totalItems}
            loading={loading}
            onChangePage={(newPage) => {
              setPage(newPage);
            }}
            onChangePerPage={(newSize) => {
              setPerPage(newSize);
              setPage(1);
            }}
          />
        </div>

        {loading && (
          <GenericModal visible={true} variant="loading" message={'Cargando…'}></GenericModal>
        )}

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
