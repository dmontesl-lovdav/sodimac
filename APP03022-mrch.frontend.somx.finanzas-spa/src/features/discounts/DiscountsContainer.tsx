import { ReactElement, useCallback, useEffect, useRef, useState } from "react";
import { Breadcrumb, GenericModal, GenericButton } from "@shared/components/ui";
import { withFinanceBreadcrumb } from "@/shared/components/ui/navigation/financeBreadcrumb";
import { useFinanceAlertModal } from "@/shared/hooks/useFinanceAlertModal";
import { Title, Divider } from "@/shared/components/ui/misc";
import { APP_EVENT, PermissionGate } from "@shared/security";
import {
  FINANCE_LIST_KEYS,
  useFinanceListScreenSession,
  useFinanceListRefetchOnReturn,
} from "@/shared/hooks";
import {
  exportToCSV,
  formatAmount,
  formatDate,
  formatFilenameTimestamp,
  parseDisplayDate,
  startOfLocalDay,
  endOfLocalDay,
  capitalizeWord,
  fetchCatalogDetails,
  fetchCatalogAsSelectableOptions,
  fetchSupplierTypesAsCatalog,
  fetchProviders,
} from "@/utils/utils";

import { DiscountsClient } from "./api/DiscountsClient";
import type { ProvidersOptions, Rebate, RebateFilters } from "./interfaces";
import { RebateStatusOptions } from "./interfaces";
import FiltersBar from "./components/FiltersBar";
import DiscountsGridTable from "./components/DiscountsGridTable";
import "./styles/DiscountsContainer.css";

type ProviderRow = {
  supplierNumber?: string | number;
  businessName?: string;
  rfc?: string;
  supplierType?: { id?: number; code?: string };
};

const CSV_HEADERS = [
  "Documento",
  "Referencia",
  "Tipo Rebate",
  "Documento Sap",
  "Importe",
  "Período",
  "Número Proveedor",
  "Nombre Proveedor",
  "Tipo Proveedor",
  "Fecha Aplicación",
  "Fecha Vencimiento",
  "Estatus",
];

const renderStatus = (status: number) => {
  const selected = RebateStatusOptions.find((item) => item.value === status);
  return selected ?? { value: -1, type: "error", label: "Desconocido" };
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

  const timestamp = parsed.getTime();
  const fromTs = parseFilterDateBound(from, false);
  const toTs = parseFilterDateBound(to, true);

  if (fromTs != null && timestamp < fromTs) return false;
  if (toTs != null && timestamp > toTs) return false;
  return true;
}

function includesIgnoreCase(value: string | undefined, query: string): boolean {
  return (value ?? "").toLowerCase().includes(query);
}

function applyClientFilters(
  rows: Rebate[],
  criteria: RebateFilters,
  providers: ProviderRow[]
): Rebate[] {
  let data = rows;

  if (criteria.supplierType && criteria.supplierType > 0) {
    const vendorNumbers = new Set(
      providers
        .filter((item) => item.supplierType?.id == criteria.supplierType)
        .map((item) => String(item.supplierNumber))
    );
    data = data.filter((row) =>
      vendorNumbers.has(String(row.vendorNumber ?? row.supplierNumber ?? ""))
    );
  }

  const documentQuery = criteria.documentNumber?.trim().toLowerCase();
  if (documentQuery) {
    data = data.filter((row) => includesIgnoreCase(row.documentNumber, documentQuery));
  }

  const sapQuery = criteria.sapDocument?.trim().toLowerCase();
  if (sapQuery) {
    data = data.filter((row) => includesIgnoreCase(row.sapDocument, sapQuery));
  }

  if (criteria.from || criteria.to) {
    data = data.filter((row) =>
      isPostingDateInFilterRange(row.postingDate, criteria.from, criteria.to)
    );
  }

  return data;
}

function hasClientSideFilter(criteria: RebateFilters): boolean {
  return Boolean(
    (criteria.supplierType && criteria.supplierType > 0) ||
      criteria.documentNumber?.trim() ||
      criteria.sapDocument?.trim() ||
      criteria.from ||
      criteria.to
  );
}

function resolvePagination(
  dataLength: number,
  page: number,
  pageSize: number,
  clientFiltered: boolean
): { totalItems: number; totalPages: number } {
  if (clientFiltered) {
    return {
      totalItems: dataLength,
      totalPages: Math.max(1, Math.ceil(dataLength / pageSize)),
    };
  }

  const knownMinimum = (page - 1) * pageSize + dataLength;
  const hasMore = dataLength >= pageSize;
  return {
    totalItems: hasMore ? knownMinimum + 1 : knownMinimum,
    totalPages: hasMore ? page + 1 : page,
  };
}

function findProvider(
  providers: ProviderRow[],
  vendorNumber: string | number | undefined
): ProviderRow | undefined {
  if (vendorNumber == null || vendorNumber === "") return undefined;
  return providers.find((item) => String(item.supplierNumber) === String(vendorNumber));
}

function toProviderCatalog(providers: ProviderRow[]): ProvidersOptions[] {
  return [
    { label: "Todos los proveedores", value: "" },
    ...providers.map((provider) => ({
      label: `${provider.businessName ?? ""} (${provider.rfc ?? ""})`,
      value: String(provider.supplierNumber ?? ""),
    })),
  ];
}

export default function DiscountsContainer(): ReactElement {
  const financeAlert = useFinanceAlertModal();
  const warnIfEmptyRef = useRef(false);

  const [loading, setLoading] = useState(false);
  const [rows, setRows] = useState<Rebate[]>([]);
  const [page, setPage] = useState(1);
  const [perPage, setPerPage] = useState(10);
  const [totalPages, setTotalPages] = useState(1);
  const [totalItems, setTotalItems] = useState(0);
  const [providers, setProviders] = useState<ProviderRow[]>([]);
  const [providerCatalog, setProviderCatalog] = useState<ProvidersOptions[]>([]);
  const [supplierTypeOptions, setSupplierTypeOptions] = useState<ProvidersOptions[]>([]);
  const [rebateTypeOptions, setRebateTypeOptions] = useState<ProvidersOptions[]>([]);
  const [statusOptions, setStatusOptions] = useState<ProvidersOptions[]>([]);
  const [filters, setFilters] = useState<RebateFilters | null>(null);

  useEffect(() => {
    let active = true;

    const loadCatalogs = async () => {
      const [providerList, supplierTypeList, rebateTypeCatalog, statusCatalog] =
        await Promise.all([
          fetchProviders(),
          fetchSupplierTypesAsCatalog(),
          fetchCatalogDetails("CATTIPOREBATE"),
          fetchCatalogDetails("CEDC"),
        ]);

      if (!active) return;

      const list = providerList ?? [];
      setProviders(list);
      setProviderCatalog(toProviderCatalog(list));
      setSupplierTypeOptions(supplierTypeList ?? []);

      if (rebateTypeCatalog) {
        setRebateTypeOptions(
          fetchCatalogAsSelectableOptions(rebateTypeCatalog, "Todos los tipos")
        );
      }
      if (statusCatalog) {
        setStatusOptions(
          fetchCatalogAsSelectableOptions(statusCatalog, "Todos los tipos")
        );
      }
    };

    loadCatalogs();
    return () => {
      active = false;
    };
  }, []);

  const returningFromDetail = useFinanceListScreenSession(
    FINANCE_LIST_KEYS.discounts
  );

  const runSearch = useCallback(
    async (
      criteria: RebateFilters,
      nextPage?: number,
      nextPerPage?: number
    ): Promise<void> => {
      try {
        setLoading(true);

        const currentPage = nextPage ?? page;
        const currentPageSize = nextPerPage ?? perPage;
        const finalCriteria: RebateFilters = {
          ...criteria,
          pageNumber: currentPage,
          pageSize: currentPageSize,
        };

        setFilters(finalCriteria);

        const raw = (await DiscountsClient.get(finalCriteria)) ?? [];
        const data = applyClientFilters(raw, criteria, providers);

        setRows(data);
        setPage(currentPage);
        setPerPage(currentPageSize);

        const pagination = resolvePagination(
          data.length,
          currentPage,
          currentPageSize,
          hasClientSideFilter(criteria)
        );
        setTotalItems(pagination.totalItems);
        setTotalPages(pagination.totalPages);

        if (data.length === 0 && warnIfEmptyRef.current) {
          financeAlert.showWarning(
            "Sin registros",
            "No se encontraron descuentos comerciales con los criterios indicados."
          );
        }
      } catch (error) {
        financeAlert.showErrorFrom(
          "Error",
          error,
          "No fue posible obtener los descuentos comerciales. Intenta nuevamente."
        );
        setRows([]);
        setTotalItems(0);
        setTotalPages(1);
      } finally {
        warnIfEmptyRef.current = false;
        setLoading(false);
      }
    },
    [page, perPage, providers, financeAlert]
  );

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

  const handleSearch = (criteria: RebateFilters) => {
    warnIfEmptyRef.current = true;
    setPage(1);
    runSearch(criteria, 1, perPage);
  };

  const handleExportCsv = () => {
    if (!rows.length) return;

    const exportRows = rows.filter((row) =>
      filters
        ? isPostingDateInFilterRange(row.postingDate, filters.from, filters.to)
        : Boolean(row.postingDate)
    );
    if (!exportRows.length) return;

    const body = exportRows.map((row) => {
      const provider = findProvider(providers, row.vendorNumber);
      const rebateType =
        rebateTypeOptions.find((item) => item.value === String(row.source))
          ?.label ?? "--";
      const supplierTypeCode = provider?.supplierType?.code;
      const supplierType = supplierTypeCode
        ? capitalizeWord(supplierTypeCode)
        : "--";

      return [
        row.documentNumber ?? "",
        row.documentReference ?? "",
        rebateType,
        row.sapDocument ?? "",
        formatAmount(row.amount),
        String(row.periodId ?? ""),
        String(row.vendorNumber ?? ""),
        provider?.businessName ?? "--",
        supplierType,
        row.postingDate ? formatDate(String(row.postingDate)) : "",
        row.dueDate ? formatDate(String(row.dueDate)) : "",
        renderStatus(row.status).label,
      ];
    });

    exportToCSV(
      CSV_HEADERS,
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

          <PermissionGate appEvent={APP_EVENT.DISCOUNTS.DOWNLOAD_CSV}>
            <GenericButton
              variant="primary"
              onClick={handleExportCsv}
              disabled={loading || rows.length === 0}
              type="button"
            >
              Exportar CSV
            </GenericButton>
          </PermissionGate>
        </div>

        <div className="dc-filters-section">
          <FiltersBar
            providers={providerCatalog}
            supplierTypeOptions={supplierTypeOptions}
            rebateTypeOptions={rebateTypeOptions}
            statusOptions={statusOptions}
            onSearch={handleSearch}
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
            onChangePage={(newPage) => {
              setPage(newPage);
              if (filters) runSearch(filters, newPage, perPage);
            }}
            onChangePerPage={(newPageSize) => {
              setPerPage(newPageSize);
              setPage(1);
              if (filters) runSearch(filters, 1, newPageSize);
            }}
            renderStatus={renderStatus}
          />
        </div>

        {loading && (
          <GenericModal visible variant="loading" message="Cargando…" />
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
