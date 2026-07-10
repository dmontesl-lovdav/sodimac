import {
  ReactElement,
  useEffect,
  useRef,
  useState,
} from "react";

import {
  Breadcrumb,
  GenericModal,
  GenericButton,
} from "@shared/components/ui";

import {
  withFinanceBreadcrumb,
} from "@/shared/components/ui/navigation/financeBreadcrumb";

import {
  useFinanceAlertModal,
} from "@/shared/hooks/useFinanceAlertModal";

import {
  DiscountsClient,
} from "./api/DiscountsClient";

import type {
  ProvidersOptions,
  Rebate,
  RebateFilters,
} from "./interfaces";

import {
  RebateStatusOptions,
} from "./interfaces";

import FiltersBar from "./components/FiltersBar";

import {
  Title,
  Divider,
} from "@/shared/components/ui/misc";

import DiscountsGridTable from "./components/DiscountsGridTable";

import {
  APP_EVENT,
  PermissionGate,
} from "@shared/security";

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
  fetchSupplierTypesAsCatalog,
} from "@/utils/utils";

import {
  FINANCE_LIST_KEYS,
  useFinanceListScreenSession,
  useFinanceListRefetchOnReturn,
} from "@/shared/hooks";

/* ---------------------- Status Renderer ---------------------- */

const renderStatus = (
  status: number
) => {
  const selected =
    RebateStatusOptions.filter(
      (item) =>
        item.value === status
    );

  if (selected.length === 1) {
    return selected[0];
  }

  return {
    value: -1,
    type: "error",
    label: "Desconocido",
  };
};

function parseFilterDateBound(
  value?: string,
  asEndOfDay = false
): number | null {
  if (!value?.trim()) {
    return null;
  }

  const parsed =
    parseDisplayDate(
      value.trim()
    );

  if (!parsed) {
    return null;
  }

  return (
    asEndOfDay
      ? endOfLocalDay(parsed)
      : startOfLocalDay(parsed)
  ).getTime();
}

function isPostingDateInFilterRange(
  postingDate:
    | string
    | null
    | undefined,
  from?: string,
  to?: string
): boolean {
  if (!postingDate?.trim()) {
    return false;
  }

  const parsed =
    parseDisplayDate(
      postingDate
    );

  if (!parsed) {
    return false;
  }

  const timestamp =
    parsed.getTime();

  const fromTimestamp =
    parseFilterDateBound(
      from,
      false
    );

  const toTimestamp =
    parseFilterDateBound(
      to,
      true
    );

  if (
    fromTimestamp != null &&
    timestamp < fromTimestamp
  ) {
    return false;
  }

  if (
    toTimestamp != null &&
    timestamp > toTimestamp
  ) {
    return false;
  }

  return true;
}

export default function DiscountsContainer():
  ReactElement {
  const financeAlert =
    useFinanceAlertModal();

  const [loading, setLoading] =
    useState(false);

  const [rows, setRows] =
    useState<Rebate[]>([]);

  const [page, setPage] =
    useState<number>(1);

  const [perPage, setPerPage] =
    useState<number>(10);

  const [
    totalPages,
    setTotalPages,
  ] = useState<number>(1);

  const [
    totalItems,
    setTotalItems,
  ] = useState<number>(0);

  const [
    providers,
    setProviders,
  ] = useState<
    ProvidersOptions[]
  >([]);

  const [
    supplierTypeOptions,
    setSupplierTypeOptions,
  ] = useState<
    ProvidersOptions[]
  >([]);

  const [
    rebateTypeOptions,
    setRebateTypeOptions,
  ] = useState<
    ProvidersOptions[]
  >([]);

  const [
    statusOptions,
    setStatusOptions,
  ] = useState<
    ProvidersOptions[]
  >([]);

  const [filters, setFilters] =
    useState<RebateFilters | null>(
      null
    );

  const warnIfEmptyRef =
    useRef(false);

  useEffect(() => {
    let active = true;

    const fetchCatalogs =
      async (): Promise<void> => {
        const [
          providerList,
          supplierTypeList,
          rebateTypeCatalog,
          statusCatalog,
        ] = await Promise.all([
          fetchProvidersAsCatalog(
            "supplierNumber"
          ),
          fetchSupplierTypesAsCatalog(),
          fetchCatalogDetails(
            "CATTIPOREBATE"
          ),
          fetchCatalogDetails(
            "CEDC"
          ),
        ]);

        if (!active) {
          return;
        }

        setProviders(
          providerList ?? []
        );

        setSupplierTypeOptions(
          supplierTypeList ?? []
        );

        if (rebateTypeCatalog) {
          setRebateTypeOptions(
            fetchCatalogAsSelectableOptions(
              rebateTypeCatalog,
              "Todos los tipos"
            )
          );
        }

        if (statusCatalog) {
          setStatusOptions(
            fetchCatalogAsSelectableOptions(
              statusCatalog,
              "Todos los tipos"
            )
          );
        }
      };

    void fetchCatalogs();

    return () => {
      active = false;
    };
  }, []);

  const returningFromDetail =
    useFinanceListScreenSession(
      FINANCE_LIST_KEYS.discounts
    );

  const runSearch = async (
    criteria: RebateFilters,
    nextPage?: number,
    nextPerPage?: number
  ): Promise<void> => {
    try {
      setLoading(true);

      const currentPage =
        nextPage ?? page;

      const currentPageSize =
        nextPerPage ?? perPage;

      const finalCriteria:
        RebateFilters = {
        ...criteria,
        pageNumber:
          currentPage,
        pageSize:
          currentPageSize,
      };

      setFilters(
        finalCriteria
      );

      let data =
        (
          await DiscountsClient.get(
            finalCriteria
          )
        ) ?? [];

      /*
       * Se conservan estas validaciones
       * para mantener el comportamiento
       * actual de la pantalla.
       */
      if (
        criteria.documentNumber
          ?.trim()
      ) {
        const query =
          criteria.documentNumber
            .trim()
            .toLowerCase();

        data = data.filter(
          (row) =>
            (
              row.documentNumber ||
              ""
            )
              .toLowerCase()
              .includes(query)
        );
      }

      if (
        criteria.sapDocument
          ?.trim()
      ) {
        const query =
          criteria.sapDocument
            .trim()
            .toLowerCase();

        data = data.filter(
          (row) =>
            (
              row.sapDocument ||
              ""
            )
              .toLowerCase()
              .includes(query)
        );
      }

      if (
        criteria.from ||
        criteria.to
      ) {
        data = data.filter(
          (row) =>
            isPostingDateInFilterRange(
              row.postingDate,
              criteria.from,
              criteria.to
            )
        );
      }

      setRows(data);
      setPage(currentPage);
      setPerPage(
        currentPageSize
      );

      const hasClientFilter =
        Boolean(
          criteria.documentNumber
            ?.trim()
        ) ||
        Boolean(
          criteria.sapDocument
            ?.trim()
        ) ||
        Boolean(
          criteria.from ||
          criteria.to
        );

      if (hasClientFilter) {
        setTotalItems(
          data.length
        );

        setTotalPages(
          Math.max(
            1,
            Math.ceil(
              data.length /
              currentPageSize
            )
          )
        );
      } else {
        const knownMinimum =
          (
            currentPage -
            1
          ) *
          currentPageSize +
          data.length;

        const hasMore =
          data.length >=
          currentPageSize;

        setTotalItems(
          hasMore
            ? knownMinimum + 1
            : knownMinimum
        );

        setTotalPages(
          hasMore
            ? Math.max(
              currentPage + 1,
              currentPage
            )
            : currentPage
        );
      }

      if (
        data.length === 0 &&
        warnIfEmptyRef.current
      ) {
        financeAlert.showWarning(
          "Sin registros",
          "No se encontraron descuentos comerciales con los criterios indicados."
        );
      }

      warnIfEmptyRef.current =
        false;
    } catch (error) {
      warnIfEmptyRef.current =
        false;

      financeAlert.showErrorFrom(
        "Error",
        error,
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
      warnIfEmptyRef.current =
        true;

      setPage(1);

      void runSearch(
        criteria,
        1,
        perPage
      );
    }
  );

  const handleClearGrid =
    (): void => {
      warnIfEmptyRef.current =
        false;

      setRows([]);
      setFilters(null);
      setPage(1);
      setTotalItems(0);
      setTotalPages(1);
    };

  const handleExportCsv =
    (): void => {
      if (!rows.length) {
        return;
      }

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
        ? rows.filter((row) =>
          isPostingDateInFilterRange(
            row.postingDate,
            filters.from,
            filters.to
          )
        )
        : rows.filter(
          (row) =>
            row.postingDate
        );

      if (!exportRows.length) {
        return;
      }

      const body =
        exportRows.map(
          (row) => {
            const statusLabel =
              renderStatus(
                row.status
              ).label;

            return [
              row.documentNumber ??
              "",

              row.documentReference ??
              "",

              rebateTypeOptions.find(
                (item) =>
                  item.value ===
                  String(
                    row.source
                  )
              )?.label ??
              "--",

              row.sapDocument ??
              "",

              formatAmount(
                row.amount
              ),

              String(
                row.periodId ??
                ""
              ),

              String(
                row.vendorNumber ??
                ""
              ),

              providers
                .find(
                  (item) =>
                    item.value ===
                    String(
                      row.vendorNumber
                    )
                )
                ?.label
                .split("(")[0]
                .trim() ??
              "--",

              row.postingDate
                ? formatDate(
                  String(
                    row.postingDate
                  )
                )
                : "",

              row.dueDate
                ? formatDate(
                  String(
                    row.dueDate
                  )
                )
                : "",

              statusLabel,
            ];
          }
        );

      exportToCSV(
        headers,
        body,
        `descuentos_comerciales_${formatFilenameTimestamp()}`
      );
    };

  return (
    <div className="dc-layout">
      <Breadcrumb
        items={withFinanceBreadcrumb([
          {
            label:
              "Descuentos Comerciales",
          },
        ])}
      />

      <div className="dc-box">
        <div className="dc-header">
          <div>
            <Title
              title="Listado de Descuentos Comerciales"
            />

            <p className="dc-description">
              Consulta y seguimiento
              de rebates/documentos
              comerciales.
            </p>
          </div>

          <PermissionGate
            appEvent={
              APP_EVENT
                .DISCOUNTS
                .DOWNLOAD_CSV
            }
          >
            <GenericButton
              variant="primary"
              onClick={
                handleExportCsv
              }
              disabled={
                loading ||
                rows.length ===
                0
              }
              type="button"
            >
              Exportar CSV
            </GenericButton>
          </PermissionGate>
        </div>

        <div className="dc-filters-section">
          <FiltersBar
            providers={
              providers
            }
            supplierTypeOptions={
              supplierTypeOptions
            }
            rebateTypeOptions={
              rebateTypeOptions
            }
            statusOptions={
              statusOptions
            }
            onSearch={(
              criteria
            ) => {
              warnIfEmptyRef.current =
                true;

              setPage(1);

              void runSearch(
                {
                  ...criteria,
                },
                1,
                perPage
              );
            }}
            onClear={
              handleClearGrid
            }
          />
        </div>

        <Divider />

        <div className="dc-grid-section">
          <DiscountsGridTable
            providers={
              providers
            }
            rebateTypeOptions={
              rebateTypeOptions
            }
            rows={rows}
            page={page}
            perPage={perPage}
            totalPages={
              totalPages
            }
            totalItems={
              totalItems
            }
            loading={loading}
            onChangePage={(
              newPage
            ) => {
              setPage(
                newPage
              );

              if (filters) {
                void runSearch(
                  filters,
                  newPage,
                  perPage
                );
              }
            }}
            onChangePerPage={(
              newPageSize
            ) => {
              setPerPage(
                newPageSize
              );

              setPage(1);

              if (filters) {
                void runSearch(
                  filters,
                  1,
                  newPageSize
                );
              }
            }}
            renderStatus={
              renderStatus
            }
          />
        </div>

        {loading && (
          <GenericModal
            visible
            variant="loading"
            message="Cargando…"
          />
        )}

        <GenericModal
          visible={
            financeAlert
              .alertVisible
          }
          variant="alert"
          severity={
            financeAlert
              .alertSeverity
          }
          title={
            financeAlert
              .alertTitle
          }
          message={
            financeAlert
              .alertMessage
          }
          buttonText="Aceptar"
          onClose={
            financeAlert
              .closeAlert
          }
        />
      </div>
    </div>
  );
}