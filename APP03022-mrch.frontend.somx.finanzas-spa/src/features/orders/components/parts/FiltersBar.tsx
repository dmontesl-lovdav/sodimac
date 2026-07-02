import {
  GenericSelectSearchable,
  GenericDateRangePicker,
  GenericButton,
  GenericModal,
  GenericInputSearch,
} from "@shared/components/ui";
import { APP_EVENT, PermissionGate } from "@shared/security";
import type { ReactElement } from "react";
import { useCallback, useEffect, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { ProvidersOptions, OrdersFilters } from "../../interfaces";
import {
  fetchProvidersAsCatalog,
  endOfLocalDay,
  startOfLocalDay,
  fetchCatalog,
  mapCatalogResponseToFilterOptions,
  fetchCatalogDetails,
  fetchCatalogAsSelectableOptions,
} from "@/utils/utils";
import {
  clearFinanceListSession,
  FINANCE_LIST_KEYS,
  isFinanceListUrlReset,
  readFinanceListFilters,
  saveFinanceListFilters,
  financeListTodayDateRange,
  parseFinanceListDateRange,
  formatFinanceListLocalDate,
  useFinanceListDefaultsOnUrlReset,
} from "@/shared/hooks";

import "./FiltersBar.css";

const FILTERS_KEY = FINANCE_LIST_KEYS.receptions.filters;

interface Props {
  onSearch: (filters: OrdersFilters) => void;
  onClear?: () => void;
}

function initialDefaultRange(): [Date | null, Date | null] {
  return financeListTodayDateRange();
}

/** Rango amplio (máx. 6 meses) para búsqueda por `?orderNumber=` en URL. */
function orderNumberDeepLinkRange(): [Date, Date] {
  const end = endOfLocalDay(new Date());
  const start = startOfLocalDay(new Date());
  start.setMonth(start.getMonth() - 6);
  return [start, end];
}

function buildOrdersFilterPayload(input: {
  dateRange: [Date | null, Date | null];
  provider: string;
  orderNumber: string;
  receptionNumber: string;
  status: string;
  providerType: string;
}): OrdersFilters {
  const [start, end] = input.dateRange;
  const dayStart = startOfLocalDay(start ?? new Date());
  const dayEnd = endOfLocalDay(end ?? new Date());

  const sn = input.provider.trim();
  const parsedSupplier = sn === "" ? NaN : Number(sn);
  return {
    purchaseOrderDateAtInitial: formatFinanceListLocalDate(dayStart)+"T00:00:00.000Z",
    purchaseOrderDateAtEnd: formatFinanceListLocalDate(dayEnd)+"T23:59:59.999Z",
    supplierNumber:
      sn !== "" && Number.isFinite(parsedSupplier) ? parsedSupplier : undefined,
    providerType: input.providerType.trim() || undefined,
    orderNumber: input.orderNumber.trim() || undefined,
    receptionNumber: input.receptionNumber.trim() || undefined,
    status: input.status.trim() !== "" ? Number(input.status) : undefined,
    pageNumber: 1,
    pageSize: 10,
  };
}

export default function FiltersBar({ onSearch, onClear }: Props): ReactElement {
  const [searchParams] = useSearchParams();
  const [providers, setProviders] = useState<ProvidersOptions[]>([]);
  const [providerTypeCatalog, setProviderTypeCatalog] = useState<
    ProvidersOptions[]
  >([]);
  const [statusCatalog, setStatusCatalog] = useState<ProvidersOptions[]>([]);
  const [providerType, setProviderType] = useState<string>("");
  const [provider, setProvider] = useState<string>("");
  const [orderNumber, setOrderNumber] = useState("");
  const [receptionNumber, setReceptionNumber] = useState("");
  const [status, setStatus] = useState<string>("");
  const [dateRange, setDateRange] = useState<[Date | null, Date | null]>(() =>
    initialDefaultRange()
  );

  const [rangeErrorModal, setRangeErrorModal] = useState<boolean>(false);
  const hasLoadedRef = useRef(false);
  const lastUrlOrderNumberRef = useRef<string | null>(null);
  const onSearchRef = useRef(onSearch);
  onSearchRef.current = onSearch;


  useEffect(() => {
    const loadCatalogs = async () => {
      const [providersRes, tipoProveedorRes, tipoRecepcionRes] =
        await Promise.all([
          fetchProvidersAsCatalog("supplierNumber"),
          fetchCatalogDetails("CatTipoProveedor"),
          fetchCatalogDetails("CatEstatusRecepcion"),
        ]);

      if (providersRes) setProviders(providersRes);

      if (tipoProveedorRes) {
        setProviderTypeCatalog(fetchCatalogAsSelectableOptions(tipoProveedorRes, "Todos los tipos", "internalStatus"));
      }

      if (tipoRecepcionRes) {
        const mappedStatus = fetchCatalogAsSelectableOptions(tipoRecepcionRes, "Todos los estatus");
        setStatusCatalog(mappedStatus.filter((item: any) => item.value !== "8"));
      }
    };

    loadCatalogs();
  }, []);

  const applyFilterDefaults = useCallback(() => {
    setDateRange(initialDefaultRange());
    setProvider("");
    setProviderType("");
    setOrderNumber("");
    setReceptionNumber("");
    setStatus("");
  }, []);

  useFinanceListDefaultsOnUrlReset(
    FINANCE_LIST_KEYS.receptions.moduleKey,
    applyFilterDefaults
  );

  useEffect(() => {
    if (providers.length === 0) return;

    if (isFinanceListUrlReset(searchParams)) {
      if (!hasLoadedRef.current) {
        applyFilterDefaults();
        hasLoadedRef.current = true;
      }
      return;
    }

    const urlOrderNumber = searchParams.get("orderNumber")?.trim() ?? "";

    if (!urlOrderNumber) {
      lastUrlOrderNumberRef.current = null;
      if (hasLoadedRef.current) return;

      const saved = readFinanceListFilters<OrdersFilters>(FILTERS_KEY);

      if (saved) {
        setDateRange(
          parseFinanceListDateRange(
            saved.purchaseOrderDateAtInitial,
            saved.purchaseOrderDateAtEnd
          )
        );
        setProvider(saved.supplierNumber ? String(saved.supplierNumber) : "");
        setProviderType(
          saved.providerType != null && String(saved.providerType).trim() !== ""
            ? String(saved.providerType)
            : ""
        );
        setOrderNumber(saved.orderNumber?.trim() ? String(saved.orderNumber) : "");
        setReceptionNumber(
          saved.receptionNumber?.trim() ? String(saved.receptionNumber) : ""
        );
        setStatus(saved.status != null ? String(saved.status) : "");
      } else {
        setDateRange(initialDefaultRange());
      }

      hasLoadedRef.current = true;
      return;
    }

    if (lastUrlOrderNumberRef.current === urlOrderNumber) return;
    lastUrlOrderNumberRef.current = urlOrderNumber;

    clearFinanceListSession(FINANCE_LIST_KEYS.receptions);

    const linkRange = orderNumberDeepLinkRange();
    setProvider("");
    setProviderType("");
    setReceptionNumber("");
    setStatus("");
    setOrderNumber(urlOrderNumber);
    setDateRange(linkRange);

    const filterData = buildOrdersFilterPayload({
      dateRange: linkRange,
      provider: "",
      providerType: "",
      orderNumber: urlOrderNumber,
      receptionNumber: "",
      status: "",
    });

    saveFinanceListFilters(FILTERS_KEY, filterData);
    onSearchRef.current(filterData);
    hasLoadedRef.current = true;
  }, [providers, searchParams]);

  const validateRange = (): boolean => {
    const [d1, d2] = dateRange;
    if (!d1 || !d2) return true;

    const diffMonths =
      (d2.getFullYear() - d1.getFullYear()) * 12 +
      (d2.getMonth() - d1.getMonth());

    return diffMonths <= 6;
  };

  const buildFilterPayload = (): OrdersFilters =>
    buildOrdersFilterPayload({
      dateRange,
      provider,
      providerType,
      orderNumber,
      receptionNumber,
      status,
    });

  const handleSearch = (): void => {
    if (!validateRange()) {
      setRangeErrorModal(true);
      return;
    }

    const filterData = buildFilterPayload();
    saveFinanceListFilters(FILTERS_KEY, filterData);
    onSearch(filterData);
  };

  const handleClear = (): void => {
    lastUrlOrderNumberRef.current = null;
    setProvider("");
    setProviderType("");
    setOrderNumber("");
    setReceptionNumber("");
    setStatus("");
    setDateRange(initialDefaultRange());
    onClear?.();
  };

  return (
    <>
      <div className="rc-filters">
        <div className="rc-row finz-filter-row">
          <GenericSelectSearchable
            value={provider}
            onChange={(e: { target: { value: string } }) =>
              setProvider(e.target.value)
            }
            options={providers}
            placeholder="Nombre Proveedor"
            widthClass="gs-width-provider"
          />

          <div className="rc-filter-provider-type">
            <GenericSelectSearchable
              value={providerType}
              onChange={(e: { target: { value: string } }) =>
                setProviderType(e.target.value)
              }
              options={providerTypeCatalog}
              placeholder="Tipo Proveedor"
              widthClass="gs-width-md"
            />
          </div>
          <div className="rc-filter-order">
            <GenericInputSearch
              value={orderNumber}
              onChange={(e) => setOrderNumber(e.target.value)}
              placeholder="Orden Compra"
              className="generic-input rc-filter-input"
            />
          </div>

          <GenericInputSearch
            value={receptionNumber}
            onChange={(e) => setReceptionNumber(e.target.value)}
            placeholder="Recepción"
            className="generic-input rc-filter-input"
          />

          <div className="rc-filter-status-wrap">
            <GenericSelectSearchable
              value={status}
              onChange={(e: { target: { value: string } }) =>
                setStatus(e.target.value)
              }
              options={statusCatalog}
              placeholder="Estatus"
            />
          </div>
          <div className="rc-field-dates">
          <GenericDateRangePicker
            value={dateRange}
            onChange={(dates) => setDateRange(dates)}
            placeholder="Fecha de recepción"
          />
          </div>
          <div className="finz-filter-actions">
            <PermissionGate appEvent={APP_EVENT.RECEPTIONS.SEARCH}>
              <GenericButton variant="outlineFill" onClick={handleSearch}>
                Buscar
              </GenericButton>
            </PermissionGate>
            <PermissionGate appEvent={APP_EVENT.RECEPTIONS.CLEAR_FILTERS}>
              <GenericButton variant="outlineFill" onClick={handleClear}>
                Limpiar
              </GenericButton>
            </PermissionGate>
          </div>
        </div>
      </div>

      <GenericModal
        visible={rangeErrorModal}
        variant="alert"
        severity="warning"
        title="Rango inválido"
        message="El rango máximo permitido es 6 meses."
        buttonText="Aceptar"
        onClose={() => setRangeErrorModal(false)}
      />
    </>
  );
}
