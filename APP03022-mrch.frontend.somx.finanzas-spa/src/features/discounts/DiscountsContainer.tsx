import { ReactElement, useEffect, useRef, useState } from "react";
import { Breadcrumb, GenericModal, GenericButton } from "@shared/components/ui";
import { withFinanceBreadcrumb } from "@shared/components/ui/navigation/financeBreadcrumb";
import { useFinanceAlertModal } from "@/shared/hooks/useFinanceAlertModal";
import { DiscountsClient } from "./api/DiscountsClient";
import type { ProvidersOptions, Rebate, RebateFilters } from "./interfaces";
import {
    RebateStatusOptions,
} from "./interfaces";
import FiltersBar from "./components/FiltersBar";
import { Title, Divider } from "@/shared/components/ui/misc";

import DiscountsGridTable from "./components/DiscountsGridTable";

import "./styles/DiscountsContainer.css";
import {
  exportToCSV,
  formatAmount,
  formatDate,
  formatFilenameTimestamp,
  parseDisplayDate,
  startOfLocalDay,
  endOfLocalDay,
  fetchProvidersAsCatalog,
  fetchCatalogDetails,
  fetchCatalogAsSelectableOptions,
} from "@/utils/utils";
import {
  FINANCE_LIST_KEYS,
  useFinanceListScreenSession,
  useFinanceListRefetchOnReturn,
} from "@/shared/hooks";

/* ---------------------- Status Renderer ---------------------- */
const renderStatus = (status: number) => {
  const selected = RebateStatusOptions.filter((item) => item.value == status);

  if (selected.length === 1) return selected[0];

  return {
    value: "-1",
    type: "error",
    label: "Desconocido",
  };
};

function parseFilterDateBound(value?: string, asEndOfDay = false): number | null {
  if (!value?.trim()) return null;
  const parsed = parseDisplayDate(value.trim());
  if (!parsed) return null;
  return (asEndOfDay ? endOfLocalDay(parsed) : startOfLocalDay(parsed)).getTime();
}

function isPostingDateInFilterRange(
  postingDate: string | null | undefined,
  from?: string,
  to?: string
): boolean {
  if (!postingDate?.trim()) return false;
  const parsed = parseDisplayDate(postingDate);
  if (!parsed) return false;

  const ts = parsed.getTime();
  const fromTs = parseFilterDateBound(from, false);
  const toTs = parseFilterDateBound(to, true);

  if (fromTs != null && ts < fromTs) return false;
  if (toTs != null && ts > toTs) return false;
  return true;
}

export default function DiscountsContainer(): ReactElement {
  const financeAlert = useFinanceAlertModal();
  const [loading, setLoading] = useState(false);
  const [rows, setRows] = useState<Rebate[]>([]);

  const [page, setPage] = useState<number>(1);
  const [perPage, setPerPage] = useState<number>(10);
  const [totalPages, setTotalPages] = useState<number>(1);
  const [totalItems, setTotalItems] = useState<number>(0);

  const [providers, setProviders] = useState<ProvidersOptions[]>([]);
  const [rebateTypeOptions, setRebateTypeOptions] = useState<ProvidersOptions[]>([]);
  const [statusOptions, setStatusOptions] = useState<ProvidersOptions[]>([]);

  const [filters, setFilters] = useState<RebateFilters | null>(null);
  const warnIfEmptyRef = useRef(false);

  useEffect(() => {
    const fetCatalogs = async () => {
      const list = await fetchProvidersAsCatalog("supplierNumber");
      if (list) setProviders(list);
      const catTipo = await fetchCatalogDetails("CATTIPOREBATE");
      if (catTipo) {
        setRebateTypeOptions(fetchCatalogAsSelectableOptions(catTipo, "Todos los tipos"));
      }
      const catEstatus = await fetchCatalogDetails("CEDC");
      if (catEstatus) {
        setStatusOptions(fetchCatalogAsSelectableOptions(catEstatus, "Todos los tipos"));
      }
    }
    fetCatalogs();
  }, []);

 

  const returningFromDetail = useFinanceListScreenSession(
    FINANCE_LIST_KEYS.discounts
  );

  const runSearch = async (
    criteria: RebateFilters,
    nextPage?: number,
    nextPerPage?: number
  ) => {
    try {
      setLoading(true);

      const p = nextPage ?? page;
      const s = nextPerPage ?? perPage;

      const finalCriteria: RebateFilters = {
        ...criteria,
        pageNumber: p,
        pageSize: s,
      };

      setFilters(finalCriteria);

      let data = (await DiscountsClient.get(finalCriteria)) || [];
      if (criteria.documentNumber?.trim()) {
        const q = criteria.documentNumber.trim().toLowerCase();
        data = data.filter((r) =>
          (r.documentNumber || "").toLowerCase().includes(q)
        );
      }
      if (criteria.sapDocument?.trim()) {
        const q = criteria.sapDocument.trim().toLowerCase();
        data = data.filter((r) =>
          (r.sapDocument || "").toLowerCase().includes(q)
        );
      }
      if (criteria.from || criteria.to) {
        data = data.filter((r) =>
          isPostingDateInFilterRange(r.postingDate, criteria.from, criteria.to)
        );
      }
      setRows(data);
      setPage(p);
      setPerPage(s);

      const hasClientFilter =
        !!criteria.documentNumber?.trim() ||
        !!criteria.sapDocument?.trim() ||
        !!(criteria.from || criteria.to);

      if (hasClientFilter) {
        setTotalItems(data.length);
        setTotalPages(Math.max(1, Math.ceil(data.length / s)));
      } else {
        const knownMin = (p - 1) * s + data.length;
        const hasMore = data.length >= s;
        setTotalItems(hasMore ? knownMin + 1 : knownMin);
        setTotalPages(hasMore ? Math.max(p + 1, p) : p);
      }

      if (data.length === 0 && warnIfEmptyRef.current) {
        financeAlert.showWarning(
          "Sin registros",
          "No se encontraron descuentos comerciales con los criterios indicados."
        );
      }
      warnIfEmptyRef.current = false;
    } catch (err) {
      warnIfEmptyRef.current = false;
      financeAlert.showErrorFrom(
        "Error",
        err,
        "No fue posible obtener los descuentos comerciales. Intenta nuevamente."
      );
      setRows([]);
      setTotalItems(0);
      setTotalPages(1);
    } finally {
      setLoading(false);
    }
  };

  useFinanceListRefetchOnReturn<RebateFilters>(
    FINANCE_LIST_KEYS.discounts,
    returningFromDetail,
    (criteria) => {
      warnIfEmptyRef.current = true;
      setPage(1);
      runSearch(criteria, 1, perPage);
    }
  );

  const handleClearGrid = () => {
    warnIfEmptyRef.current = false;
    setRows([]);
    setFilters(null);
    setPage(1);
    setTotalItems(0);
    setTotalPages(1);
  };


  const handleExportCsv = () => {
    if (!rows.length) return;
    const headers = [
      "Documento",
      "Referencia",
      "Tipo Rebate",
      "Documento Sap",
      "Importe",
      "Período",
      "Número Proveedor",
      "Nombre Proveedor",
      "Fecha Aplicación",
      "Fecha Vencimiento",
      "Estatus",
    ];
    const exportRows = filters
      ? rows.filter((r) =>
          isPostingDateInFilterRange(r.postingDate, filters.from, filters.to)
        )
      : rows.filter((r) => r.postingDate);

    if (!exportRows.length) return;

    const body = exportRows.map((r) => {
      const st = renderStatus(r.status).label;
      return [
        r.documentNumber ?? "",
        r.documentReference ?? "",
        rebateTypeOptions.find((item) => item.value === String(r.source))?.label ?? "--",
        r.sapDocument ?? "",
        formatAmount(r.amount),
        String(r.periodId ?? ""),
        String(r.vendorNumber ?? ""),
        providers.find((item) => item.value === String(r.vendorNumber))?.label.split("(")[0] ?? "--",
        r.postingDate ? formatDate(String(r.postingDate)) : "",
        r.dueDate ? formatDate(String(r.dueDate)) : "",
        st,
      ];
    });
    exportToCSV(
      headers,
      body,
      `descuentos_comerciales_${formatFilenameTimestamp()}`
    );
  };

  return (
    <div className="dc-layout">
      <Breadcrumb
        items={withFinanceBreadcrumb([{ label: "Descuentos Comerciales" }])}
      />

      <div className="dc-box">
        <div className="dc-header">
          <div>
            <Title title="Listado de Descuentos Comerciales" />
            <p className="dc-description">
              Consulta y seguimiento de rebates/documentos comerciales.
            </p>
          </div>
          <GenericButton
            variant="primary"
            onClick={handleExportCsv}
            disabled={loading || rows.length === 0}
            type="button"
          >
            Exportar CSV
          </GenericButton>
        </div>

        <div className="dc-filters-section">
          <FiltersBar
            providers={providers}
            rebateTypeOptions={rebateTypeOptions}
            statusOptions={statusOptions}
            onSearch={(criteria) => {
              warnIfEmptyRef.current = true;
              setPage(1);
              runSearch({ ...criteria }, 1, perPage);
            }}
            onClear={handleClearGrid}
          />
        </div>

        <Divider />

        <div className="dc-grid-section">
          <DiscountsGridTable
            providers={providers}
            rebateTypeOptions={rebateTypeOptions}
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
            renderStatus={renderStatus}
          />
        </div>

        {loading && <GenericModal visible variant="loading" message="Cargando…" />}

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