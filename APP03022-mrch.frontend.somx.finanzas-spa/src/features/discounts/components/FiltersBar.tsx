import {
  GenericButton,
  GenericInputSearch,
  GenericSelectSearchable,
} from "@shared/components/ui";

import { GenericDateRangePicker } from "@shared/components/ui/date";

import type {
  ChangeEvent,
  ReactElement,
} from "react";

import {
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";

import {
  APP_EVENT,
  PermissionGate,
} from "@shared/security";

import type {
  RebateFilters,
  ProvidersOptions,
} from "../interfaces";

import {
  startOfLocalDay,
  endOfLocalDay,
} from "@/utils/utils";

import type {
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

const FILTERS_KEY =
  FINANCE_LIST_KEYS.discounts.filters;

type DateRange = [
  Date | null,
  Date | null,
];

function initialTodayRange(): DateRange {
  const today = new Date();

  return [
    startOfLocalDay(today),
    endOfLocalDay(today),
  ];
}

interface Props {
  onSearch: (
    filters: RebateFilters
  ) => void;

  onClear?: () => void;

  providers: ProvidersOptions[];

  supplierTypeOptions:
  SelectableOption<string>[];

  rebateTypeOptions:
  SelectableOption<string>[];

  statusOptions:
  SelectableOption<string>[];
}

export default function FiltersBar({
  onSearch,
  onClear,
  providers,
  supplierTypeOptions,
  rebateTypeOptions,
  statusOptions,
}: Props): ReactElement {
  const [dateRange, setDateRange] =
    useState<DateRange>(() =>
      initialTodayRange()
    );

  const [status, setStatus] =
    useState<string>("");

  const [
    supplierNumber,
    setSupplierNumber,
  ] = useState<string>("");

  const [
    supplierType,
    setSupplierType,
  ] = useState<string>("");

  const [
    documentNumber,
    setDocumentNumber,
  ] = useState<string>("");

  const [
    sapDocument,
    setSapDocument,
  ] = useState<string>("");

  const [
    rebateType,
    setRebateType,
  ] = useState<string>("");

  const hasLoadedRef = useRef(false);

  const applyFilterDefaults =
    useCallback(() => {
      setDateRange(
        initialTodayRange()
      );

      setStatus("");
      setSupplierNumber("");
      setSupplierType("");
      setDocumentNumber("");
      setSapDocument("");
      setRebateType("");
    }, []);

  useFinanceListDefaultsOnUrlReset(
    FINANCE_LIST_KEYS.discounts.moduleKey,
    applyFilterDefaults
  );

  useEffect(() => {
    if (hasLoadedRef.current) {
      return;
    }

    const saved =
      readFinanceListFilters<RebateFilters>(
        FILTERS_KEY
      );

    if (saved) {
      const [start, end] =
        parseFinanceListDateRange(
          saved.from,
          saved.to
        );

      setDateRange(
        start && end
          ? [start, end]
          : initialTodayRange()
      );

      setSupplierNumber(
        saved.supplierNumber != null
          ? String(
            saved.supplierNumber
          )
          : ""
      );

      setSupplierType(
        saved.supplierType != null
          ? String(
            saved.supplierType
          )
          : ""
      );

      setStatus(
        saved.status != null
          ? String(saved.status)
          : ""
      );

      setDocumentNumber(
        saved.documentNumber ?? ""
      );

      setSapDocument(
        saved.sapDocument ?? ""
      );

      setRebateType(
        saved.source != null
          ? String(saved.source)
          : ""
      );
    } else {
      applyFilterDefaults();
    }

    hasLoadedRef.current = true;
  }, [applyFilterDefaults]);

  const buildPayload =
    (): RebateFilters => {
      const [start, end] =
        dateRange;

      const dayStart =
        startOfLocalDay(
          start ?? new Date()
        );

      const dayEnd =
        endOfLocalDay(
          end ?? new Date()
        );

      const normalizedSupplier =
        supplierNumber.trim();

      const parsedSupplier =
        normalizedSupplier === ""
          ? Number.NaN
          : Number(
            normalizedSupplier
          );

      const normalizedSupplierType =
        supplierType.trim();

      const parsedSupplierType =
        normalizedSupplierType === ""
          ? Number.NaN
          : Number(
            normalizedSupplierType
          );

      const normalizedRebateType =
        rebateType.trim();

      const parsedRebateType =
        normalizedRebateType === ""
          ? Number.NaN
          : Number(
            normalizedRebateType
          );

      const normalizedStatus =
        status.trim();

      const parsedStatus =
        normalizedStatus === ""
          ? Number.NaN
          : Number(
            normalizedStatus
          );

      const filters: RebateFilters = {
        from:
          dayStart.toISOString(),

        to:
          dayEnd.toISOString(),

        supplierNumber:
          normalizedSupplier !== "" &&
            Number.isFinite(
              parsedSupplier
            )
            ? parsedSupplier
            : undefined,

        supplierType:
          normalizedSupplierType !== "" &&
            Number.isFinite(
              parsedSupplierType
            )
            ? parsedSupplierType
            : undefined,

        documentNumber:
          documentNumber.trim() ||
          undefined,

        sapDocument:
          sapDocument.trim() ||
          undefined,

        source:
          normalizedRebateType !== "" &&
            normalizedRebateType !== "0" &&
            Number.isFinite(
              parsedRebateType
            )
            ? parsedRebateType
            : undefined,

        status:
          normalizedStatus !== "" &&
            normalizedStatus !== "0" &&
            Number.isFinite(
              parsedStatus
            )
            ? parsedStatus
            : undefined,

        pageNumber: 1,
        pageSize: 10,
      };

      return filters;
    };

  const handleSubmit = (): void => {
    const filterData =
      buildPayload();

    saveFinanceListFilters(
      FILTERS_KEY,
      filterData
    );

    onSearch(filterData);
  };

  const handleClear = (): void => {
    applyFilterDefaults();
    onClear?.();
  };

  return (
    <div className="dc-filters">
      <div className="dc-row finz-filter-row">
        <div className="dc-filter-provider">
          <GenericSelectSearchable
            value={supplierNumber}
            onChange={(
              event: {
                target: {
                  value: string;
                };
              }
            ) =>
              setSupplierNumber(
                event.target.value
              )
            }
            options={providers}
            placeholder="Nombre Proveedor"
            widthClass="gs-width-provider"
          />
        </div>

        <div className="dc-filter-supplier-type">
          <GenericSelectSearchable
            value={supplierType}
            onChange={(
              event: {
                target: {
                  value: string;
                };
              }
            ) =>
              setSupplierType(
                event.target.value
              )
            }
            options={
              supplierTypeOptions
            }
            placeholder="Tipo de Proveedor"
            widthClass="gs-width-default"
          />
        </div>

        <div className="dc-filter-document">
          <GenericInputSearch
            value={documentNumber}
            onChange={(
              event:
                ChangeEvent<HTMLInputElement>
            ) =>
              setDocumentNumber(
                event.target.value
              )
            }
            placeholder="Documento"
            className="generic-input dc-filter-input"
          />
        </div>

        <div className="dc-filter-sap">
          <GenericInputSearch
            value={sapDocument}
            onChange={(
              event:
                ChangeEvent<HTMLInputElement>
            ) =>
              setSapDocument(
                event.target.value
              )
            }
            placeholder="Documento SAP"
            className="generic-input dc-filter-input"
          />
        </div>

        <div className="dc-filter-rebate-type">
          <GenericSelectSearchable
            value={rebateType}
            onChange={(
              event: {
                target: {
                  value: string;
                };
              }
            ) =>
              setRebateType(
                event.target.value
              )
            }
            options={
              rebateTypeOptions
            }
            placeholder="Tipo Rebate"
            widthClass="gs-width-default"
          />
        </div>

        <div className="dc-filter-status-wrap">
          <GenericSelectSearchable
            value={status}
            onChange={(
              event: {
                target: {
                  value: string;
                };
              }
            ) =>
              setStatus(
                event.target.value
              )
            }
            placeholder="Estatus"
            options={statusOptions}
            widthClass="gs-width-default"
          />
        </div>

        <div className="dc-field-dates">
          <GenericDateRangePicker
            value={dateRange}
            onChange={(dates) =>
              setDateRange(dates)
            }
            placeholder="Fecha descuento"
            size="md"
          />
        </div>

        <div className="finz-filter-actions">
          <PermissionGate
            appEvent={
              APP_EVENT.DISCOUNTS
                .SEARCH
            }
          >
            <GenericButton
              variant="outlineFill"
              onClick={handleSubmit}
            >
              Buscar
            </GenericButton>
          </PermissionGate>

          <PermissionGate
            appEvent={
              APP_EVENT.DISCOUNTS
                .CLEAR_FILTERS
            }
          >
            <GenericButton
              variant="outline"
              onClick={handleClear}
            >
              Limpiar
            </GenericButton>
          </PermissionGate>
        </div>
      </div>
    </div>
  );
}