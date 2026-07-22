import {
  GenericButton,
  GenericInputSearch,
  GenericSelectSearchable,
  GenericModal,
} from "@shared/components/ui";
import { GenericDateRangePicker } from "@shared/components/ui/date";
import type { ChangeEvent, ReactElement } from "react";
import { useCallback, useEffect, useState } from "react";
import { ShippingGuideFilter } from "../interfaces";
import { APP_EVENT, PermissionGate } from "@shared/security";
import {
  fetchProvidersAsCatalog,
  endOfLocalDay,
  startOfLocalDay,
} from "@/utils/utils";
import {
  loadShippingGuideStatusFilterOptions,
  registerShippingGuideStatusLabels,
} from "../shippingGuideStatusCatalog";
import {
  FINANCE_LIST_KEYS,
  readFinanceListFilters,
  removeFinanceListFilters,
  saveFinanceListFilters,
  parseFinanceListDateRange,
  useFinanceListDefaultsOnUrlReset,
} from "@/shared/hooks";
import "../styles/shippingGuideFilterBar.css";

type ShippingGuideFilterBarProps = {
  onSearch: (filters: ShippingGuideFilter) => void;
  onClear?: () => void;
  isAdmin?: boolean;
};

const FILTERS_KEY = FINANCE_LIST_KEYS.shippingGuides.filters;

function todayRange(): [Date, Date] {
  const t = new Date();
  return [startOfLocalDay(t), endOfLocalDay(t)];
}

function buildDefaultPayload(): ShippingGuideFilter {
  const [start, end] = todayRange();
  const ymd = (d: Date) =>
    `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
  return {
    from: ymd(start),
    to: ymd(end),
  };
}

function applySavedFilters(saved: ShippingGuideFilter): {
  guideNumber: string;
  orderNumber: string;
  vendorNumber: string;
  status: string;
  range: [Date | null, Date | null];
} {
  return {
    guideNumber: saved.guideNumber ?? "",
    orderNumber: saved.orderNumber ?? "",
    vendorNumber: saved.vendorNumber ?? "",
    status: saved.status != null ? String(saved.status) : "",
    range:
      saved.from && saved.to
        ? parseFinanceListDateRange(saved.from, saved.to)
        : todayRange(),
  };
}

export default function ShippingGuideFilterBar({
  onSearch,
  onClear,
  isAdmin = true,
}: ShippingGuideFilterBarProps): ReactElement {
  const [providers, setProviders] = useState<
    { label: string; value: string }[]
  >([]);
  const [statusOptions, setStatusOptions] = useState<
    { label: string; value: string }[]
  >([{ label: "Todos los estatus", value: "" }]);

  const [guideNumber, setGuideNumber] = useState<string>("");
  const [orderNumber, setOrderNumber] = useState<string>("");
  const [vendorNumber, setVendorNumber] = useState<string>("");
  const [status, setStatus] = useState<string>("");
  const [alertModal, setAlertModal] = useState({ visible: false, message: "" });

  const [range, setRange] = useState<[Date | null, Date | null]>(() =>
    todayRange()
  );

  const applyFilterDefaults = useCallback(() => {
    setGuideNumber("");
    setOrderNumber("");
    setVendorNumber("");
    setStatus("");
    setRange(todayRange());
  }, []);

  useFinanceListDefaultsOnUrlReset(
    FINANCE_LIST_KEYS.shippingGuides.moduleKey,
    applyFilterDefaults
  );

  useEffect(() => {
    (async () => {
      const list = await fetchProvidersAsCatalog("supplierNumber");
      if (list) setProviders(list);
    })();
    (async () => {
      const opts = await loadShippingGuideStatusFilterOptions();
      setStatusOptions(opts);
      registerShippingGuideStatusLabels(opts);
    })();
  }, []);

  useEffect(() => {
    const saved = readFinanceListFilters<ShippingGuideFilter>(FILTERS_KEY);
    if (!saved) {
      applyFilterDefaults();
      return;
    }
    const applied = applySavedFilters(saved);
    setGuideNumber(applied.guideNumber);
    setOrderNumber(applied.orderNumber);
    setVendorNumber(applied.vendorNumber);
    setStatus(applied.status);
    setRange(applied.range);
  }, [applyFilterDefaults]);

  const buildSearchPayload = (): ShippingGuideFilter | null => {
    if (!range?.[0] || !range?.[1]) {
      setAlertModal({
        visible: true,
        message: "Selecciona las fechas de inicio y final (obligatorias).",
      });
      return null;
    }
    if (range[0] > range[1]) {
      setAlertModal({
        visible: true,
        message: "La fecha de inicio no puede ser mayor a la final.",
      });
      return null;
    }

    const from = startOfLocalDay(range[0]!);
    const to = endOfLocalDay(range[1]!);
    const ymdLocal = (d: Date) =>
      `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;

    const payload: ShippingGuideFilter = {
      vendorNumber: (() => { const v = vendorNumber.trim(); return v === "" ? undefined : v; })(),
      from: ymdLocal(from),
      to: ymdLocal(to),
    };

    const g = guideNumber.trim();
    if (g) payload.guideNumber = g;

    const oc = orderNumber.trim();
    if (oc) payload.orderNumber = oc;

    if (status.trim() !== "") {
      const parsed = Number(status);
      if (Number.isFinite(parsed)) payload.status = parsed;
    }

    return payload;
  };

  const handleClear = () => {
    applyFilterDefaults();
    removeFinanceListFilters(FILTERS_KEY);
    onClear?.();
  };

  const handleSearchClick = () => {
    const payload = buildSearchPayload();
    if (!payload) return;
    saveFinanceListFilters(FILTERS_KEY, payload);
    onSearch(payload);
  };

  return (
    <>
      <div className="sg-filter-bar sg-filter-bar-horizontal finz-filter-row">
        {isAdmin && (
          <div className="sg-field">
            <GenericSelectSearchable
              value={vendorNumber}
              onChange={(e: { target: { value: string } }) =>
                setVendorNumber(e.target.value)
              }
              options={providers}
              placeholder="Nombre Proveedor"
              widthClass="gs-width-provider"
            />
          </div>
        )}

        <div className="sg-field">
          <GenericInputSearch
            value={guideNumber}
            onChange={(event: ChangeEvent<HTMLInputElement>) =>
              setGuideNumber(event.target.value)
            }
            placeholder="Guía Embarque"
            className="generic-input sg-filter-input"
          />
        </div>

        <div className="sg-field">
          <GenericInputSearch
            value={orderNumber}
            onChange={(event: ChangeEvent<HTMLInputElement>) =>
              setOrderNumber(event.target.value)
            }
            placeholder="Orden Compra"
            className="generic-input sg-filter-input"
          />
        </div>

        <div className="sg-field ">
          <GenericSelectSearchable
            value={status}
            onChange={(e: { target: { value: string } }) =>
              setStatus(e.target.value)
            }
            options={statusOptions}
            placeholder="Estatus"
          />
        </div>

        <div className="sg-field sg-field-dates">
          <GenericDateRangePicker
            value={range}
            onChange={(dates: [Date | null, Date | null]) => setRange(dates)}
            placeholder="Fecha de Consulta"
            size="md"
          />
        </div>

        <div className="finz-filter-actions sg-filter-actions">
          <PermissionGate appEvent={APP_EVENT.CARTA_PORTE.SEARCH}>
            <GenericButton variant="outlineFill" onClick={handleSearchClick}>
              Buscar
            </GenericButton>
          </PermissionGate>
          <PermissionGate appEvent={APP_EVENT.CARTA_PORTE.CLEAR_FILTERS}>
            <GenericButton variant="outlineFill" onClick={handleClear}>
              Limpiar
            </GenericButton>
          </PermissionGate>
        </div>
      </div>

      <GenericModal
        visible={alertModal.visible}
        variant="alert"
        severity="warning"
        title="Fechas requeridas"
        message={alertModal.message}
        buttonText="Aceptar"
        onClose={() => setAlertModal({ visible: false, message: "" })}
      />
    </>
  );
}
