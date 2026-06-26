import {
  GenericButton,
  GenericInputSearch,
  GenericSelect,
  GenericSelectSearchable,
} from "@shared/components/ui";
import { GenericDateRangePicker } from "@shared/components/ui/date";
import type { ChangeEvent, ReactElement } from "react";
import { useCallback, useEffect, useRef, useState } from "react";
import { APP_EVENT, PermissionGate } from "@shared/security";
import {
  RebateStatusOptions,
  RebateFilters,
  ProvidersOptions,
} from "../interfaces";
import {
  fetchProvidersAsCatalog,
  fetchCatTipoRebateCatalog,
  startOfLocalDay,
  endOfLocalDay,
  fetchCatalog,
  mapCatalogResponseToFilterOptions,
  fetchCatalogDetails,
  fetchCatalogAsSelectableOptions,
  SelectableOption,
} from "@/utils/utils";
import {
  FINANCE_LIST_KEYS,
  readFinanceListFilters,
  saveFinanceListFilters,
  parseFinanceListDateRange,
  useFinanceListDefaultsOnUrlReset,
} from "@/shared/hooks";

import "../styles/DiscountsFiltersBar.css";

const FILTERS_KEY = FINANCE_LIST_KEYS.discounts.filters;

type DateRange = [Date | null, Date | null];

function initialTodayRange(): DateRange {
  const t = new Date();
  return [startOfLocalDay(t), endOfLocalDay(t)];
}

interface Props {
  onSearch: (filters: RebateFilters) => void;
  onClear?: () => void;
  providers: ProvidersOptions[];
  rebateTypeOptions: SelectableOption<string>[];
  statusOptions: SelectableOption<string>[];
}

export default function FiltersBar({ onSearch, onClear, providers, rebateTypeOptions, statusOptions }: Props): ReactElement {
  const [dateRange, setDateRange] = useState<DateRange>(() =>
    initialTodayRange()
  );
  const [status, setStatus] = useState<string>("");
  const [supplierNumber, setSupplierNumber] = useState<string>("");
  const [documentNumber, setDocumentNumber] = useState("");
  const [sapDocument, setSapDocument] = useState("");
  const [rebateType, setRebateType] = useState<string>("");
  const hasLoadedRef = useRef(false);

  const applyFilterDefaults = useCallback(() => {
    setDateRange(initialTodayRange());
    setStatus("");
    setSupplierNumber("");
    setDocumentNumber("");
    setSapDocument("");
    setRebateType("");
  }, []);

  useFinanceListDefaultsOnUrlReset(
    FINANCE_LIST_KEYS.discounts.moduleKey,
    applyFilterDefaults
  );



  useEffect(() => {
    if (!hasLoadedRef.current) {
      const saved = readFinanceListFilters<RebateFilters>(FILTERS_KEY);

      if (saved) {
        const [start, end] = parseFinanceListDateRange(saved.from, saved.to);
        setDateRange(
          start && end ? [start, end] : initialTodayRange()
        );
        setSupplierNumber(
          saved.supplierNumber != null ? String(saved.supplierNumber) : ""
        );
        setStatus(saved.status != null ? String(saved.status) : "");
        setDocumentNumber(saved.documentNumber ?? "");
        setSapDocument(saved.sapDocument ?? "");
        setRebateType(
          saved.source != null ? String(saved.source) : ""
        );
      } else {
        setDateRange(initialTodayRange());
        setStatus("");
        setSupplierNumber("");
        setDocumentNumber("");
        setSapDocument("");
        setRebateType("");
      }

      hasLoadedRef.current = true;
    }
  }, []);

  const buildPayload = (): RebateFilters => {
    const [start, end] = dateRange;
    const dayStart = startOfLocalDay(start ?? new Date());
    const dayEnd = endOfLocalDay(end ?? new Date());

    const sn = supplierNumber.trim();
    const parsedSupplier = sn === "" ? NaN : Number(sn);

    let filters: RebateFilters = {
      from: dayStart.toISOString(),
      to: dayEnd.toISOString(),
      supplierNumber:
        sn !== "" && Number.isFinite(parsedSupplier) ? parsedSupplier : undefined,
      documentNumber: documentNumber.trim() || undefined,
      sapDocument: sapDocument.trim() || undefined,
      pageNumber: 1,
      pageSize: 10,
    };


    if (rebateType!="0" && rebateType != "" && rebateType != " ") {
      filters = {
        ...filters,
        source: Number(rebateType),
      };
    }
    if (status != "" && status != "0" && status!=" ") {
      filters = {
        ...filters,
        status: Number(status),
      };
    }
    return filters;
  };

  const handleSubmit = () => {
    const filterData = buildPayload();
    saveFinanceListFilters(FILTERS_KEY, filterData);
    onSearch(filterData);
  };

  const handleClear = () => {
    setDateRange(initialTodayRange());
    setStatus("");
    setSupplierNumber("");
    setDocumentNumber("");
    setSapDocument("");
    setRebateType("");
    onClear?.();
  };

  return (
    <div className="dc-filters">
      <div className="dc-row finz-filter-row">
        <GenericSelectSearchable
          value={supplierNumber}
          onChange={(e: { target: { value: string } }) =>
            setSupplierNumber(e.target.value)
          }
          options={providers}
          placeholder="Nombre Proveedor"
          widthClass="gs-width-provider"
        />

        <div className="dc-filter-document">
          <GenericInputSearch
            value={documentNumber}
            onChange={(e: ChangeEvent<HTMLInputElement>) =>
              setDocumentNumber(e.target.value)
            }
            placeholder="Documento"
            className="generic-input dc-filter-input"
          />
        </div>

        <div className="dc-filter-sap">
          <GenericInputSearch
            value={sapDocument}
            onChange={(e: ChangeEvent<HTMLInputElement>) =>
              setSapDocument(e.target.value)
            }
            placeholder="Documento SAP"
            className="generic-input dc-filter-input"
          />
        </div>

        <div className="dc-filter-rebate-type">
          <GenericSelectSearchable
            value={rebateType}
            onChange={(e: { target: { value: string } }) =>
              setRebateType(e.target.value)
            }
            options={rebateTypeOptions}
            placeholder="Tipo Rebate"
            widthClass="gs-width-default"
          />
        </div>

        <div className="dc-filter-status-wrap">
          <GenericSelectSearchable  
            value={status ?? ""}
            onChange={(e: { target: { value: string } }) =>
              setStatus(e.target.value)
            }
            placeholder="Estatus"
            options={statusOptions}
            widthClass="gs-width-default"
          />
        </div>

        <div className="dc-field-dates">
          <GenericDateRangePicker
            value={dateRange}
            onChange={(dates) => setDateRange(dates)}
            placeholder="Fecha descuento"
            size="md"
          />
        </div>

        <div className="finz-filter-actions">
          <PermissionGate appEvent={APP_EVENT.DISCOUNTS.SEARCH}>
            <GenericButton variant="outlineFill" onClick={handleSubmit}>
              Buscar
            </GenericButton>
          </PermissionGate>
          <PermissionGate appEvent={APP_EVENT.DISCOUNTS.CLEAR_FILTERS}>
            <GenericButton variant="outline" onClick={handleClear}>
              Limpiar
            </GenericButton>
          </PermissionGate>
        </div>
      </div>
    </div>
  );
}
